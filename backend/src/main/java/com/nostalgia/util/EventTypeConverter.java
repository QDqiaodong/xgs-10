package com.nostalgia.util;

import com.nostalgia.entity.TimelineEvent;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class EventTypeConverter implements AttributeConverter<TimelineEvent.EventType, String> {

    @Override
    public String convertToDatabaseColumn(TimelineEvent.EventType attribute) {
        if (attribute == null) {
            return TimelineEvent.EventType.USAGE.name();
        }
        return attribute.name();
    }

    @Override
    public TimelineEvent.EventType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return TimelineEvent.EventType.USAGE;
        }
        try {
            return TimelineEvent.EventType.valueOf(dbData.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TimelineEvent.EventType.USAGE;
        }
    }
}
