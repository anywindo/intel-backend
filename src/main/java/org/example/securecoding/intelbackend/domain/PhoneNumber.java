package org.example.securecoding.intelbackend.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.regex.Pattern;

/**
 * DEEP MODEL: PhoneNumber (Domain Primitive)
 * 
 * Replaces String phoneNumber / phone.
 * Enforces international or standard phone formats to prevent malformed data injection.
 */
public final class PhoneNumber {

    // Simple regex for international or local numbers (digits, +, -)
    private static final String PHONE_REGEX = "^[+]?[0-9\\s-]+$";
    private static final Pattern PATTERN = Pattern.compile(PHONE_REGEX);

    private final String value;

    public PhoneNumber(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (!PATTERN.matcher(value).matches() || value.trim().length() < 7) {
            throw new IllegalArgumentException("Invalid phone number format (Deep Model verification failed)");
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
        PhoneNumber that = (PhoneNumber) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
