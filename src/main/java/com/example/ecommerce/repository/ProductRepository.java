package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository  extends JpaRepository<Product, Long> {

    boolean existsByCategoryId(Long categoryId);

    Optional<Product> findByIdAndActiveTrue(Long id);

    @Query("""
            select p from Product p
            where p.active = true
              and (:categoryId is null or p.category.id = :categoryId)
              and (:keyword is null or lower(p.name) like lower(concat('%', :keyword, '%')))                       
              and (:minPrice is null or p.price >= :minPrice)                       
              and (:maxPrice is null or p.price <= :maxPrice)                       
            """)
    Page<Product> search(@Param("categoryId") Long categoryId,
                         @Param("keyword") String keyword,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice,
                         Pageable pageable);
}
