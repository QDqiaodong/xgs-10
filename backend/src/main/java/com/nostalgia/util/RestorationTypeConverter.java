package com.nostalgia.util;

import com.nostalgia.entity.RestorationRecord;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RestorationTypeConverter implements AttributeConverter<RestorationRecord.RestorationType, String> {

    @Override
    public String convertToDatabaseColumn(RestorationRecord.RestorationType type) {
        if (type == null) {
            return null;
        }
        return type.name();
    }

    @Override
    public RestorationRecord.RestorationType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        try {
            return RestorationRecord.RestorationType.valueOf(dbData.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RestorationRecord.RestorationType.CUSTOM;
        }
    }
}
