package com.aryanjais.aijobagent.service;

import com.aryanjais.aijobagent.entity.CoverLetter;
import com.aryanjais.aijobagent.entity.TailoredResume;
import com.aryanjais.aijobagent.exception.FileStorageException;
import com.aryanjais.aijobagent.repository.CoverLetterRepository;
import com.aryanjais.aijobagent.repository.TailoredResumeRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    private final TailoredResumeRepository tailoredResumeRepository;
    private final CoverLetterRepository coverLetterRepository;

    @Value("${app.storage.pdf-output-dir}")
    private String pdfOutputDir;

    @Transactional
    public String generateResumePdf(TailoredResume tailoredResume) {
        if (tailoredResume.getPdfFilePath() != null) {
            return tailoredResume.getPdfFilePath();
        }

        String filename = "resume_" + tailoredResume.getId() + "_" + UUID.randomUUID() + ".pdf";
        String filePath = generatePdf(tailoredResume.getTailoredContent(),
                "Tailored Resume", filename);

        tailoredResume.setPdfFilePath(filePath);
        tailoredResumeRepository.save(tailoredResume);
        log.info("Resume PDF generated: {}", filePath);
        return filePath;
    }

    @Transactional
    public String generateCoverLetterPdf(CoverLetter coverLetter) {
        if (coverLetter.getPdfFilePath() != null) {
            return coverLetter.getPdfFilePath();
        }

        String filename = "cover_letter_" + coverLetter.getId() + "_" + UUID.randomUUID() + ".pdf";
        String filePath = generatePdf(coverLetter.getContent(),
                "Cover Letter", filename);

        coverLetter.setPdfFilePath(filePath);
        coverLetterRepository.save(coverLetter);
        log.info("Cover letter PDF generated: {}", filePath);
        return filePath;
    }

    private String generatePdf(String content, String title, String filename) {
        try {
            Path outputPath = Paths.get(pdfOutputDir);
            Files.createDirectories(outputPath);
            Path filePath = outputPath.resolve(filename);

            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(filePath.toFile()));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            Paragraph titleParagraph = new Paragraph(title, titleFont);
            titleParagraph.setSpacingAfter(20);
            document.add(titleParagraph);

            // Split content into paragraphs and add them
            String[] paragraphs = content.split("\\n\\n");
            for (String para : paragraphs) {
                if (!para.trim().isEmpty()) {
                    Paragraph p = new Paragraph(para.trim(), bodyFont);
                    p.setSpacingAfter(10);
                    p.setLeading(16);
                    document.add(p);
                }
            }

            document.close();
            return filePath.toString();

        } catch (DocumentException | IOException e) {
            throw new FileStorageException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    public byte[] readPdfBytes(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new FileStorageException("Failed to read PDF file: " + filePath, e);
        }
    }
}
