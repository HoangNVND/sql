package com.example.demo.model.filter;

import com.example.demo.model.entity.Student;
import com.example.demo.model.filter.StudentFilter;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class StudentSpecification {

    public static Specification<?> build(StudentFilter filter) {
        if (filter == null || isEmptyFilter(filter)) {
            return null; // sẽ được hiểu là lấy tất cả
        }

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + filter.getName() + "%"));
            }
            if (filter.getName() != null && !filter.getName().isBlank()) {
                predicates.add(cb.like(root.get("name"), "%" + filter.getName() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean isEmptyFilter(StudentFilter filter) {
        return (filter.getName() == null || filter.getName().isBlank());
    }
}
