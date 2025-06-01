package com.example.demo.model.entity;

import com.example.demo.model.Utils.SexEnum;
import com.example.demo.model.converter.StudentInfoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Student {
    @Id
    String id;
    String name;
    @Convert(converter = StudentInfoConverter.class)
    @Column(name = "student_info", columnDefinition = "TEXT")
    StudentInfo studentInfo;
    @Enumerated(EnumType.STRING)
    @Column(name = "sexual", columnDefinition = "TEXT")
    SexEnum sexual;
}
