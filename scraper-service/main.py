"""
AI Job Agent — Scraper Service
FastAPI application that scrapes job listings from Indeed, LinkedIn, and Naukri
and publishes them to RabbitMQ for processing by the Spring Boot backend.
"""

import json
import logging
import os
from datetime import datetime, timezone
from typing import Optional

import pika
from dotenv import load_dotenv
from fastapi import BackgroundTasks, FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, HttpUrl

load_dotenv()

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(name)s - %(message)s")
logger = logging.getLogger(__name__)

app = FastAPI(
    title="AI Job Agent — Scraper Service",
    description="Scrapes job listings from Indeed, LinkedIn, and Naukri and publishes to RabbitMQ",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# ── RabbitMQ Configuration ────────────────────────────────────────────────────

RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USERNAME = os.getenv("RABBITMQ_USERNAME", "guest")
RABBITMQ_PASSWORD = os.getenv("RABBITMQ_PASSWORD", "guest")
JOBS_RAW_QUEUE = "jobs.raw"
JOBS_RAW_EXCHANGE = "jobs"


def get_rabbitmq_connection() -> pika.BlockingConnection:
    """Create and return a RabbitMQ blocking connection."""
    credentials = pika.PlainCredentials(RABBITMQ_USERNAME, RABBITMQ_PASSWORD)
    parameters = pika.ConnectionParameters(
        host=RABBITMQ_HOST,
        port=RABBITMQ_PORT,
        credentials=credentials,
        heartbeat=600,
        blocked_connection_timeout=300,
    )
    return pika.BlockingConnection(parameters)


def publish_job(job_data: dict) -> None:
    """Publish a single job payload to the jobs.raw RabbitMQ queue."""
    connection = get_rabbitmq_connection()
    try:
        channel = connection.channel()
        channel.queue_declare(queue=JOBS_RAW_QUEUE, durable=True)
        channel.basic_publish(
            exchange="",
            routing_key=JOBS_RAW_QUEUE,
            body=json.dumps(job_data, default=str),
            properties=pika.BasicProperties(delivery_mode=2),  # persistent
        )
        logger.info("Published job to %s: %s @ %s", JOBS_RAW_QUEUE, job_data.get("title"), job_data.get("company"))
    finally:
        connection.close()


# ── Pydantic Models ───────────────────────────────────────────────────────────

class ScrapeRequest(BaseModel):
    keywords: Optional[str] = None
    location: Optional[str] = None
    max_results: int = 25


class JobPayload(BaseModel):
    title: str
    company: str
    location: Optional[str] = None
    description: Optional[str] = None
    requirements: Optional[str] = None
    source_platform: str
    source_url: str
    source_job_id: Optional[str] = None
    job_type: Optional[str] = None
    salary_min: Optional[float] = None
    salary_max: Optional[float] = None
    posted_at: Optional[str] = None
    scraped_at: str


# ── Scraper Implementations ───────────────────────────────────────────────────

async def scrape_indeed(keywords: Optional[str], location: Optional[str], max_results: int) -> list[dict]:
    """
    Scrape Indeed job listings using Playwright.
    Returns a list of job dicts normalised to the standard schema.
    """
    from playwright.async_api import async_playwright

    jobs = []
    search_url = f"https://www.indeed.com/jobs?q={keywords or ''}&l={location or ''}"
    scraped_at = datetime.now(timezone.utc).isoformat()

    async with async_playwright() as pw:
        browser = await pw.chromium.launch(headless=True, args=["--no-sandbox"])
        try:
            page = await browser.new_page()
            await page.set_extra_http_headers({"User-Agent": "Mozilla/5.0 (compatible; AiJobAgent/1.0)"})
            await page.goto(search_url, timeout=30000)
            await page.wait_for_load_state("networkidle", timeout=15000)

            job_cards = await page.query_selector_all("[data-jk]")
            for card in job_cards[:max_results]:
                try:
                    title_el = await card.query_selector("h2.jobTitle span")
                    company_el = await card.query_selector("[data-testid='company-name']")
                    location_el = await card.query_selector("[data-testid='text-location']")
                    link_el = await card.query_selector("a[data-jk]")
                    job_id = await card.get_attribute("data-jk")

                    title = await title_el.inner_text() if title_el else "Unknown"
                    company = await company_el.inner_text() if company_el else "Unknown"
                    loc = await location_el.inner_text() if location_el else location
                    href = await link_el.get_attribute("href") if link_el else ""
                    source_url = f"https://www.indeed.com{href}" if href else search_url

                    jobs.append({
                        "title": title.strip(),
                        "company": company.strip(),
                        "location": loc.strip() if loc else None,
                        "source_platform": "INDEED",
                        "source_url": source_url,
                        "source_job_id": job_id,
                        "scraped_at": scraped_at,
                    })
                except Exception as card_err:
                    logger.warning("Error parsing Indeed job card: %s", card_err)
        finally:
            await browser.close()

    return jobs


async def scrape_linkedin(keywords: Optional[str], location: Optional[str], max_results: int) -> list[dict]:
    """
    Scrape LinkedIn job listings using Playwright.
    Returns a list of job dicts normalised to the standard schema.
    """
    from playwright.async_api import async_playwright

    jobs = []
    search_url = (
        f"https://www.linkedin.com/jobs/search/?keywords={keywords or ''}&location={location or ''}"
    )
    scraped_at = datetime.now(timezone.utc).isoformat()

    async with async_playwright() as pw:
        browser = await pw.chromium.launch(headless=True, args=["--no-sandbox"])
        try:
            page = await browser.new_page()
            await page.set_extra_http_headers({"User-Agent": "Mozilla/5.0 (compatible; AiJobAgent/1.0)"})
            await page.goto(search_url, timeout=30000)
            await page.wait_for_load_state("networkidle", timeout=15000)

            job_cards = await page.query_selector_all(".jobs-search__results-list > li")
            for card in job_cards[:max_results]:
                try:
                    title_el = await card.query_selector("h3.base-search-card__title")
                    company_el = await card.query_selector("h4.base-search-card__subtitle")
                    location_el = await card.query_selector("span.job-search-card__location")
                    link_el = await card.query_selector("a.base-card__full-link")

                    title = await title_el.inner_text() if title_el else "Unknown"
                    company = await company_el.inner_text() if company_el else "Unknown"
                    loc = await location_el.inner_text() if location_el else location
                    source_url = await link_el.get_attribute("href") if link_el else search_url

                    jobs.append({
                        "title": title.strip(),
                        "company": company.strip(),
                        "location": loc.strip() if loc else None,
                        "source_platform": "LINKEDIN",
                        "source_url": source_url.split("?")[0] if source_url else search_url,
                        "scraped_at": scraped_at,
                    })
                except Exception as card_err:
                    logger.warning("Error parsing LinkedIn job card: %s", card_err)
        finally:
            await browser.close()

    return jobs


async def scrape_naukri(keywords: Optional[str], location: Optional[str], max_results: int) -> list[dict]:
    """
    Scrape Naukri job listings using httpx (REST-based public API endpoint).
    Returns a list of job dicts normalised to the standard schema.
    """
    import httpx

    jobs = []
    scraped_at = datetime.now(timezone.utc).isoformat()
    headers = {
        "User-Agent": "Mozilla/5.0 (compatible; AiJobAgent/1.0)",
        "appid": "109",
        "systemid": "Naukri",
    }
    params = {
        "noOfResults": max_results,
        "urlType": "search_by_keyword",
        "searchType": "adv",
        "keyword": keywords or "",
        "location": location or "",
        "pageNo": 0,
    }
    api_url = "https://www.naukri.com/jobapi/v3/search"

    async with httpx.AsyncClient(timeout=30) as client:
        try:
            response = await client.get(api_url, params=params, headers=headers)
            if response.status_code == 200:
                data = response.json()
                for job in data.get("jobDetails", [])[:max_results]:
                    jobs.append({
                        "title": job.get("title", "Unknown"),
                        "company": job.get("companyName", "Unknown"),
                        "location": ", ".join(job.get("placeholders", [{}])[0].get("label", "").split(",")[:2]),
                        "description": job.get("jobDescription", ""),
                        "source_platform": "NAUKRI",
                        "source_url": job.get("jdURL", ""),
                        "source_job_id": str(job.get("jobId", "")),
                        "salary_min": None,
                        "salary_max": None,
                        "scraped_at": scraped_at,
                    })
        except Exception as err:
            logger.error("Error scraping Naukri: %s", err)

    return jobs


# ── API Routes ────────────────────────────────────────────────────────────────

@app.get("/health", tags=["Health"])
async def health_check():
    """Health check endpoint for Docker and load balancer probes."""
    return {"status": "healthy", "service": "scraper-service", "timestamp": datetime.now(timezone.utc).isoformat()}


@app.post("/scrape/indeed", tags=["Scraping"])
async def trigger_indeed_scrape(request: ScrapeRequest, background_tasks: BackgroundTasks):
    """Trigger an Indeed scrape and publish results to RabbitMQ."""
    async def _run():
        try:
            jobs = await scrape_indeed(request.keywords, request.location, request.max_results)
            for job in jobs:
                publish_job(job)
            logger.info("Indeed scrape complete: %d jobs published", len(jobs))
        except Exception as e:
            logger.error("Indeed scrape failed: %s", e)

    background_tasks.add_task(_run)
    return {"message": "Indeed scrape initiated", "max_results": request.max_results}


@app.post("/scrape/linkedin", tags=["Scraping"])
async def trigger_linkedin_scrape(request: ScrapeRequest, background_tasks: BackgroundTasks):
    """Trigger a LinkedIn scrape and publish results to RabbitMQ."""
    async def _run():
        try:
            jobs = await scrape_linkedin(request.keywords, request.location, request.max_results)
            for job in jobs:
                publish_job(job)
            logger.info("LinkedIn scrape complete: %d jobs published", len(jobs))
        except Exception as e:
            logger.error("LinkedIn scrape failed: %s", e)

    background_tasks.add_task(_run)
    return {"message": "LinkedIn scrape initiated", "max_results": request.max_results}


@app.post("/scrape/naukri", tags=["Scraping"])
async def trigger_naukri_scrape(request: ScrapeRequest, background_tasks: BackgroundTasks):
    """Trigger a Naukri scrape and publish results to RabbitMQ."""
    async def _run():
        try:
            jobs = await scrape_naukri(request.keywords, request.location, request.max_results)
            for job in jobs:
                publish_job(job)
            logger.info("Naukri scrape complete: %d jobs published", len(jobs))
        except Exception as e:
            logger.error("Naukri scrape failed: %s", e)

    background_tasks.add_task(_run)
    return {"message": "Naukri scrape initiated", "max_results": request.max_results}


@app.post("/scrape/all", tags=["Scraping"])
async def trigger_all_scrapes(request: ScrapeRequest, background_tasks: BackgroundTasks):
    """Trigger scraping on all platforms simultaneously and publish results to RabbitMQ."""
    import asyncio

    async def _run():
        try:
            results = await asyncio.gather(
                scrape_indeed(request.keywords, request.location, request.max_results),
                scrape_linkedin(request.keywords, request.location, request.max_results),
                scrape_naukri(request.keywords, request.location, request.max_results),
                return_exceptions=True,
            )
            total = 0
            for platform_jobs in results:
                if isinstance(platform_jobs, list):
                    for job in platform_jobs:
                        publish_job(job)
                    total += len(platform_jobs)
                else:
                    logger.error("Platform scrape error: %s", platform_jobs)
            logger.info("All-platform scrape complete: %d jobs published", total)
        except Exception as e:
            logger.error("All-platform scrape failed: %s", e)

    background_tasks.add_task(_run)
    return {"message": "All-platform scrape initiated", "max_results": request.max_results}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=False)
