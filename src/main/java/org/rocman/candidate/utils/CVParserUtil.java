package org.rocman.candidate.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.tika.Tika;

import java.io.InputStream;

@Log4j2
public class CVParserUtil {

    private static final Tika tika = new Tika();

    public static String extractText(InputStream inputStream) {
        try {
            log.debug("Starting Tika parsing...");
            String result = tika.parseToString(inputStream);
            log.debug("Tika parsing completed, extracted {} characters", result.length());
            return result;
        } catch (Exception e) {
            log.error("Tika failed to parse document. Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse CV: " + e.getMessage(), e);
        }
    }
}

