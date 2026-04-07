# ── Build Stage ─────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY job-processor/pom.xml job-processor/pom.xml
COPY document-service/pom.xml document-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml
RUN mvn dependency:go-offline -B -pl common,api-gateway -am
COPY common/src common/src
COPY api-gateway/src api-gateway/src
RUN mvn package -DskipTests -B -pl common,api-gateway -am

# ── Runtime Stage ───────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
RUN mkdir -p /app/storage/resumes /app/storage/pdfs && chown -R appuser:appgroup /app
COPY --from=build /app/api-gateway/target/*.jar app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
