package com.example.demo.repository;

import io.hypersistence.utils.spring.repository.BaseJpaRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * BaseSafeRepositoryImpl: Kế thừa BaseJpaRepositoryImpl (Hypersistence)
 * và implement BaseSafeRepository để thêm logic findAllSafe().
 *
 * @param <T>  Entity type
 * @param <ID> ID type
 */
@Slf4j
public class BaseSafeRepositoryImpl<T, ID>
        extends BaseJpaRepositoryImpl<T, ID>
        implements BaseSafeRepository<T, ID> {

    private final EntityManager entityManager;
    private final Class<T> domainClass;
    private final JpaEntityInformation<T, ?> entityInformation;
    /**
     * Constructor chuẩn của Hypersistence BaseJpaRepositoryImpl:
     * Spring Data khi khởi tạo sẽ truyền vào JpaEntityInformation và EntityManager.
     */
    public BaseSafeRepositoryImpl(JpaEntityInformation<T, ?> entityInformation,
                                  EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        // Lấy domainClass trực tiếp từ JpaEntityInformation
        this.domainClass = entityInformation.getJavaType();
        this.entityInformation = entityInformation;
    }

    /**
     * Tương tự findAll(), nhưng dùng native query để lấy raw data và map thủ công.
     * Nếu gặp lỗi enum khi map, catch và log ID, rồi bỏ qua (skip) bản ghi đó.
     */
    @Override
    public List<T> findAllSafe() {
        // 1. Lấy tên bảng (từ annotation @Table nếu có, nếu không dùng tên class lowercase)
        String tableName = getTableName(domainClass);

        // 2. Lấy tên cột: nếu có @Column(name="..."), dùng name; ngược lại dùng field name
        List<String> columns = getColumnNames(domainClass);

        // 3. Build native SQL: SELECT cột1, cột2, ... FROM tableName
        String sql = "SELECT " + String.join(", ", columns) + " FROM " + tableName;

        // 4. Thực thi native query, trả về List<Object[]> mỗi phần tử là một row
        Query query = entityManager.createNativeQuery(sql);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        List<T> result = new ArrayList<>();

        // 5. Duyệt từng row, map vào entity
        for (Object[] row : rows) {
            try {
                T entity = mapRowToEntity(row, columns);
                result.add(entity);
            } catch (Exception ex) {
                // Nếu gặp exception (thường do enum không hợp lệ), log ID của bản ghi
                Object idValue = getIdValue(row, columns);
                System.err.println("❌ Lỗi mapping bản ghi ID=" + idValue + ": " + ex.getMessage());
                // Bỏ qua và tiếp tục với bản ghi khác
            }
        }

        return result;
    }

    @Override
    public Page<T> findAllSafe(Specification<T> spec, Pageable pageable) {
        // --- 1. Xây dựng CriteriaQuery<Long> để đếm tổng số bản ghi thỏa spec ---
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> countRoot = countQuery.from(domainClass);
        countQuery.select(cb.count(countRoot));

        if (spec != null) {
            Predicate countPredicate = spec.toPredicate(countRoot, countQuery, cb);
            if (countPredicate != null) {
                countQuery.where(countPredicate);
            }
        }
        Long totalCount = entityManager.createQuery(countQuery).getSingleResult();

        // Nếu totalCount = 0, trả về Page rỗng luôn
        if (totalCount == 0) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        // --- 2. Xây dựng CriteriaQuery<ID> để lấy list ID thỏa spec + pageable + sort ---
        CriteriaQuery<Object> idQuery = cb.createQuery(Object.class);
        Root<T> idRoot = idQuery.from(domainClass);
        // Lấy tên thuộc tính ID từ entityInformation
        String idAttrName = Objects.requireNonNull(entityInformation.getIdAttribute()).getName();
        idQuery.select(idRoot.get(idAttrName));

        if (spec != null) {
            Predicate idPredicate = spec.toPredicate(idRoot, idQuery, cb);
            if (idPredicate != null) {
                idQuery.where(idPredicate);
            }
        }
        // Áp sort từ pageable
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    orders.add(cb.asc(idRoot.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(idRoot.get(order.getProperty())));
                }
            });
            idQuery.orderBy(orders);
        }

        TypedQuery<Object> typedIdQuery = entityManager.createQuery(idQuery);
        typedIdQuery.setFirstResult((int) pageable.getOffset());
        typedIdQuery.setMaxResults(pageable.getPageSize());
        List<Object> rawIds = typedIdQuery.getResultList();

        // --- 3. Duyệt rawIds, gọi findById trên từng ID, catch lỗi, log, skip ---
        List<T> safeContent = new ArrayList<>();
        for (var rawId : rawIds) {
            try {
                // Gọi super.findById (trong BaseJpaRepositoryImpl) hoặc entityManager.find
                // Lấy entity đầy đủ (nếu mapping enum lỗi, exception sẽ ném ra ở đây).
                T entity = super.findById((ID) rawId).orElse(null);
//                if (entity != null) {
//                    entityManager.refresh(entity); // Buộc tải lại từ DB, tạo SQL
//                    safeContent.add(entity);
//                }
                if (entity != null) {
                    safeContent.add(entity);
                }
            } catch (Exception ex) {
                System.err.println("❌ Lỗi mapping bản ghi ID=" + rawId + ": " + ex.getMessage());
            }
        }

        // --- 4. Trả về Page<T> với nội dung safeContent, pageable và tổng totalCount---
        return new PageImpl<>(safeContent, pageable, totalCount);
    }

    // ============================
    // Helper methods (private)
    // ============================

    /**
     * Lấy tên bảng từ annotation @Table (nếu có). Nếu không, fallback tên class lowercase.
     */
    private String getTableName(Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table != null && !table.name().isEmpty()) {
            return table.name();
        }
        // Fallback: ClassName → lowercase (có thể custom nếu cần naming khác)
        return clazz.getSimpleName().toLowerCase();
    }

    /**
     * Duyệt qua tất cả field của entity, nếu có @Column(name="...") thì lấy name,
     * ngược lại dùng field.getName().
     */
    private List<String> getColumnNames(Class<T> clazz) {
        List<String> columns = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            jakarta.persistence.Column column = field.getAnnotation(jakarta.persistence.Column.class);
            if (column != null && !column.name().isEmpty()) {
                columns.add(column.name());
            } else {
                columns.add(field.getName());
            }
        }
        return columns;
    }

    /**
     * Map một dòng raw (Object[] row) vào entity T:
     * - Với field enum, dùng Enum.valueOf(). Nếu enum value không hợp lệ → ném exception.
     * - Với field không phải enum, gán trực tiếp (cần cân nhắc với Date, BigDecimal, Boolean,... nếu muốn chuyển kiểu).
     */
    @SuppressWarnings("unchecked")
    private T mapRowToEntity(Object[] row, List<String> columns) throws Exception {
        // Tạo instance mới của entity
        T entity = domainClass.getDeclaredConstructor().newInstance();

        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i);
            Object value = row[i];

            // Tìm field tương ứng trong class (dựa trên @Column hoặc tên field)
            Field field = findFieldByColumnName(domainClass, colName);
            if (field == null) {
                // Nếu không tìm thấy field tương ứng, bỏ qua
                continue;
            }

            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (fieldType.isEnum()) {
                // Xử lý enum: nếu value null → để null, nếu không convert thử
                if (value == null) {
                    field.set(entity, null);
                } else {
                    try {
                        @SuppressWarnings("rawtypes")
                        Class<Enum> enumClass = (Class<Enum>) fieldType;
                        Object enumValue = Enum.valueOf(enumClass, value.toString());
                        field.set(entity, enumValue);
                    } catch (IllegalArgumentException e) {
                        // Khi enum không hợp lệ sẽ ném vào đây
                        throw new IllegalArgumentException(
                                "Invalid enum value '" + value + "' for field " + field.getName());
                    }
                }
            } else {
                // Gán giá trị trực tiếp (đơn giản). Với các kiểu đặc biệt (Date, Boolean, v.v.), bạn có thể customize thêm.
                field.set(entity, value);
            }
        }
        return entity;
    }

    /**
     * Tìm field trong entity class dựa trên tên cột:
     * - Nếu field có @Column(name="xxx") trùng với columnName → trả field đó
     * - Ngược lại, nếu tên field (field.getName()) trùng columnName → trả field đó.
     */
    private Field findFieldByColumnName(Class<T> clazz, String columnName) {
        for (Field field : clazz.getDeclaredFields()) {
            jakarta.persistence.Column column = field.getAnnotation(jakarta.persistence.Column.class);
            if (column != null && column.name().equalsIgnoreCase(columnName)) {
                return field;
            }
            if (field.getName().equalsIgnoreCase(columnName)) {
                return field;
            }
        }
        return null;
    }

    /**
     * Trong một row raw (Object[]), lấy giá trị ID (giả sử tên cột là "id").
     * Nếu bạn dùng cột ID khác tên, có thể customize lại logic ở đây.
     */
    private Object getIdValue(Object[] row, List<String> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).equalsIgnoreCase("id")) {
                return row[i];
            }
        }
        return null;
    }
}
