package org.example.securecoding.intelbackend.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.securecoding.intelbackend.domain.PasswordHash;

@Converter(autoApply = true)
public class PasswordHashConverter implements AttributeConverter<PasswordHash, String> {

    @Override
    public String convertToDatabaseColumn(PasswordHash attribute) {
        return attribute == null ? null : attribute.getValue();
    }

    @Override
    public PasswordHash convertToEntityAttribute(String dbData) {
        return dbData == null ? null : new PasswordHash(dbData);
    }
}
