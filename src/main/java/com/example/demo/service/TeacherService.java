package com.example.demo.service;

import com.example.demo.model.Utils.SexEnum;
import com.example.demo.model.entity.Student;
import com.example.demo.model.entity.StudentInfo;
import com.example.demo.model.entity.Teacher;
import com.example.demo.model.filter.StudentFilter;
import com.example.demo.model.filter.StudentSpecification;
import com.example.demo.repository.StudentRepository;
import com.example.demo.repository.TeacherRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class TeacherService {
    private final TeacherRepository teacherRepository;

    @Transactional
    public void createTeacher() {
        for (int i = 11; i < 15; i++) {
            var student = new Teacher(String.valueOf(i),"viethoangn",SexEnum.FEMALE);
            teacherRepository.persist(student);
        }
    }

    public List<Teacher> getAll() {
        return teacherRepository.findAllSafe();
    }

    public Page<Teacher> getPage(StudentFilter filter,Pageable pageable){
        var spec = StudentSpecification.build(filter);
        log.info("Find All Default!");
        teacherRepository.findAll((Specification<Teacher>) spec,pageable);
        log.info("Find All Default!");
        return teacherRepository.findAllSafe((Specification<Teacher>) spec,pageable);
    }
}
