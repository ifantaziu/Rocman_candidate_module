package org.rocman.candidate.utils;

import org.apache.tika.Tika;

public class CVParserUtil {

    private static final Tika tika = new Tika();

    public static String extractText(byte[] fileBytes) {
        try {
            return tika.parseToString(new java.io.ByteArrayInputStream(fileBytes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CV: " + e.getMessage(), e);
        }
    }
}

