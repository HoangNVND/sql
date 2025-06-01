package com.example.demo.repository;

import com.example.demo.model.entity.Student;
import com.example.demo.model.entity.Teacher;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends BaseJpaRepository<Teacher, String>,
        JpaSpecificationExecutor<Teacher>, BaseSafeRepository<Teacher,String>{
}
