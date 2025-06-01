package com.example.demo.repository;

import com.example.demo.model.entity.Student;
import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository  extends BaseJpaRepository<Student, String>,
        JpaSpecificationExecutor<Student>, BaseSafeRepository<Student,String>{
}
