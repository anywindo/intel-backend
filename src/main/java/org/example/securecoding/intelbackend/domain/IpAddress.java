package org.example.securecoding.intelbackend.domain;

import java.util.regex.Pattern;

/**
 * DEEP MODEL PLACEHOLDER: IpAddress
 * 
 * Secure by Design Principle:
 * Prevents basic Primitive Obsession by ensuring IP addresses
 * are always structurally valid IPv4 before any logic consumes them.
 */
public final class IpAddress {
    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private final String value;

    public IpAddress(String raw) {
        if (raw == null || !IPV4.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid IP address");
        }
        this.value = raw;
    }

    public String getValue() {
        return value;
    }
}
