package com.example.certmanager.util;

import com.example.certmanager.exception.CertificateParseException;

import java.math.BigInteger;
import java.util.regex.Pattern;

public final class CertificateUtils {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile(
            "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$"
    );

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^(?:\\d{1,3}\\.){3}\\d{1,3}$"
    );

    private CertificateUtils() {
    }

    public static boolean validateDomainName(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }

        String trimmedDomain = domain.trim();

        if (trimmedDomain.length() > 253) {
            return false;
        }

        if (DOMAIN_PATTERN.matcher(trimmedDomain).matches()) {
            return true;
        }

        return IP_PATTERN.matcher(trimmedDomain).matches();
    }

    public static String parseSerialNumber(String serialNumber) {
        if (serialNumber == null || serialNumber.isBlank()) {
            throw new CertificateParseException("Serial number cannot be null or empty");
        }

        String cleaned = serialNumber.trim();

        if (cleaned.startsWith("0x") || cleaned.startsWith("0X")) {
            cleaned = cleaned.substring(2);
        }

        cleaned = cleaned.replaceAll(":", "").replaceAll("-", "").replaceAll("\\s+", "");

        if (cleaned.isEmpty()) {
            throw new CertificateParseException("Invalid serial number format");
        }

        try {
            BigInteger bigInt = new BigInteger(cleaned, 16);
            return bigInt.toString(16).toUpperCase();
        } catch (NumberFormatException e) {
            throw new CertificateParseException("Invalid serial number: " + serialNumber, e);
        }
    }

    public static String formatSerialNumber(BigInteger serialNumber) {
        if (serialNumber == null) {
            throw new CertificateParseException("Serial number cannot be null");
        }

        String hex = serialNumber.toString(16).toUpperCase();

        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < hex.length(); i += 2) {
            if (i > 0) {
                formatted.append(":");
            }
            formatted.append(hex, i, Math.min(i + 2, hex.length()));
        }

        return formatted.toString();
    }

    public static String normalizeDomainName(String domain) {
        if (domain == null || domain.isBlank()) {
            return domain;
        }

        String normalized = domain.trim().toLowerCase();

        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    public static boolean isWildcardDomain(String domain) {
        return domain != null && domain.trim().startsWith("*.");
    }

    public static boolean domainMatches(String pattern, String domain) {
        if (pattern == null || domain == null) {
            return false;
        }

        String normalizedPattern = normalizeDomainName(pattern);
        String normalizedDomain = normalizeDomainName(domain);

        if (normalizedPattern.equals(normalizedDomain)) {
            return true;
        }

        if (isWildcardDomain(normalizedPattern)) {
            String basePattern = normalizedPattern.substring(2);
            return normalizedDomain.endsWith(basePattern) &&
                    normalizedDomain.length() > basePattern.length() &&
                    normalizedDomain.charAt(normalizedDomain.length() - basePattern.length() - 1) == '.';
        }

        return false;
    }
}
