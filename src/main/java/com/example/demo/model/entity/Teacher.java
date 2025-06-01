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
public class Teacher {
    @Id
    String id;
    String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "sexual", columnDefinition = "TEXT")
    SexEnum sexual;
}
