package com.example.demo.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

/**
 * BaseSafeRepository<T, ID> mở rộng từ Hypersistence’s BaseJpaRepository<T, ID>
 * và bổ sung hai phương thức “an toàn”:
 *   1) findAllSafe(): đọc toàn bộ, log ID bản ghi lỗi.
 *   2) findAllSafe(spec, pageable): giống JpaSpecificationExecutor.findAll, nhưng
 *      log ID bản ghi lỗi và bỏ qua chúng.
 *
 * @param <T>  Entity type
 * @param <ID> ID type
 */
@NoRepositoryBean
public interface BaseSafeRepository<T, ID> extends BaseJpaRepository<T, ID> {

    /**
     * Tương tự findAll(), nhưng nếu gặp lỗi enum mapping, nó sẽ catch exception,
     * log ID, và tiếp tục với bản ghi khác (skip bản ghi lỗi).
     */
    List<T> findAllSafe();

    /**
     * Tương tự findAll(Specification<T>, Pageable), nhưng nếu gặp lỗi mapping một
     * bản ghi, nó sẽ catch exception, log ID, và bỏ qua bản ghi đó, vẫn trả về Page<T>
     * gồm các entity hợp lệ, với tổng count ban đầu (gồm cả bản ghi lỗi).
     *
     * @param spec     Điều kiện lọc (có thể null)
     * @param pageable Pagination + Sort
     * @return Page<T> gồm các entity hợp lệ
     */
    Page<T> findAllSafe(Specification<T> spec, Pageable pageable);
}
