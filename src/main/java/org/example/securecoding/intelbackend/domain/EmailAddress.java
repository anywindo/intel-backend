package org.example.securecoding.intelbackend.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

/**
 * DEEP MODEL: EmailAddress (Domain Primitive)
 * 
 * Secure by Design Principle:
 * This object encapsulates a validated email address. By enforcing the pattern 
 * in the constructor and making the object immutable, we ensure that no part 
 * of the system can pass around a string that "looks like" an email but is invalid.
 */
public final class EmailAddress {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern PATTERN = Pattern.compile(EMAIL_REGEX);

    private final String value;

    public EmailAddress(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format (Deep Model verification failed)");
        }
        this.value = value.trim();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailAddress that = (EmailAddress) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
