package com.example.demo.model.converter;

import com.example.demo.model.entity.StudentInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter để tự động chuyển đổi giữa StudentInfo <-> JSON String khi lưu vào/đọc ra database.
 */
@Converter(autoApply = true)
public class StudentInfoConverter implements AttributeConverter<
        StudentInfo, String> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(StudentInfoConverter.class);

    /**
     * Convert StudentInfo -> String JSON để lưu vào DB
     */
    @Override
    public String convertToDatabaseColumn(StudentInfo studentInfo) {
        if (studentInfo == null) {
            return null;
        }
        try {
            // Chuyển object thành JSON string
            return mapper.writeValueAsString(studentInfo);
        } catch (JsonProcessingException e) {
            log.error("Error serializing StudentInfo to JSON: {}", e.getMessage(), e);
            // Nếu muốn throw exception, có thể ném PersistenceException
            throw new IllegalArgumentException("Could not convert StudentInfo to JSON String.", e);
        }
    }

    /**
     * Convert String JSON từ DB -> StudentInfo
     */
    @Override
    public StudentInfo convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            // Chuyển JSON string thành object
            return mapper.readValue(dbData, StudentInfo.class);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing JSON to StudentInfo: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Could not convert JSON String to StudentInfo.", e);
        }
    }
}
