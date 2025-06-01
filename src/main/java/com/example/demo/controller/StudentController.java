package com.example.demo.controller;

import com.example.demo.model.entity.Student;
import com.example.demo.model.filter.StudentFilter;
import com.example.demo.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    @GetMapping()
    public void getMapping() {
        studentService.getStudent();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Student>> getMapping(StudentFilter filter) {
        return ResponseEntity.ok(studentService.getAll());
    }

    @GetMapping("/page")
    public Page<Student> getUsers(
             StudentFilter spec,
            @PageableDefault(size = 2) Pageable pageable) {
        return studentService.getPage(spec, pageable);
    }
}
