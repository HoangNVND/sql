package com.example.demo.controller;

import com.example.demo.model.entity.Student;
import com.example.demo.model.entity.Teacher;
import com.example.demo.model.filter.StudentFilter;
import com.example.demo.service.StudentService;
import com.example.demo.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TeacherService teacherService;
    @GetMapping()
    public void getMapping() {
        teacherService.createTeacher();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Teacher>> getMapping(StudentFilter filter) {
        return ResponseEntity.ok(teacherService.getAll());
    }

    @GetMapping("/page")
    public Page<Teacher> getUsers(
             StudentFilter spec,
            @PageableDefault(size = 10) Pageable pageable) {
        return teacherService.getPage(spec, pageable);
    }
}
