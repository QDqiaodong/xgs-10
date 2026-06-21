package com.nostalgia.util;

import com.nostalgia.entity.SourceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SourceTypeConverter implements AttributeConverter<SourceType, String> {

    @Override
    public String convertToDatabaseColumn(SourceType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name();
    }

    @Override
    public SourceType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return SourceType.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
