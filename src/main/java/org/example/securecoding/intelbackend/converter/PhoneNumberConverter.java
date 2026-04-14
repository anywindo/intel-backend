package org.example.securecoding.intelbackend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.securecoding.intelbackend.domain.PhoneNumber;

@Converter(autoApply = true)
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {

    @Override
    public String convertToDatabaseColumn(PhoneNumber attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new PhoneNumber(dbData);
    }
}
