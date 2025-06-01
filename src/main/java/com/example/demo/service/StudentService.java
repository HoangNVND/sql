package com.example.demo.service;

import com.example.demo.model.Utils.SexEnum;
import com.example.demo.model.entity.Student;
import com.example.demo.model.entity.StudentInfo;
import com.example.demo.model.filter.StudentFilter;
import com.example.demo.model.filter.StudentSpecification;
import com.example.demo.repository.StudentRepository;
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
public class StudentService {
    private final StudentRepository studentRepository;

    @Transactional
    public void getStudent() {
        var studentInfo = new StudentInfo("18","Nam Dinh");
        for (int i = 11; i < 15; i++) {
            var student = new Student(String.valueOf(i),"viethoangn",studentInfo, SexEnum.FEMALE);
            studentRepository.persist(student);
        }
    }

    public List<Student> getAll() {
        return studentRepository.findAllSafe();
    }

    public Page<Student> getPage(StudentFilter filter,Pageable pageable){
        var spec = StudentSpecification.build(filter);
        log.info("Find All Default!");
        studentRepository.findAll((Specification<Student>) spec,pageable);
        log.info("Find All Default!");
        return studentRepository.findAllSafe((Specification<Student>) spec,pageable);
    }
}
