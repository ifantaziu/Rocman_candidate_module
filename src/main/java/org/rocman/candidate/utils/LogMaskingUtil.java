package org.rocman.candidate.utils;

public class LogMaskingUtil {
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf("@");
        if (at <= 1) return "***" + email.substring(at);
        return email.substring(0, 1) + "***" + email.substring(at);
    }
}

