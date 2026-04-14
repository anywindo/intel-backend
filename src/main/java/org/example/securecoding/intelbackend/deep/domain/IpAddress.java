package org.example.securecoding.intelbackend.deep.domain;

import java.util.regex.Pattern;

/**
 * DEEP MODEL — Domain Primitive: IpAddress
 *
 * Enforces IP address validation at the type level.
 * By using a domain primitive instead of a plain string, we ensure that
 * no 'dirty' IP data can ever reach the security logic.
 */
public final class IpAddress {
    private static final Pattern IPV4 = Pattern.compile("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$");
    private final String value;

    public IpAddress(String raw) {
        if (raw == null || !IPV4.matcher(raw).matches()) {
            throw new IllegalArgumentException("Invalid IPv4 address format");
        }
        this.value = raw;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpAddress ipAddress = (IpAddress) o;
        return value.equals(ipAddress.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
