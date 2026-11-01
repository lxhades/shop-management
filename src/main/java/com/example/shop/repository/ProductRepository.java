package com.example.shop.repository;

import com.example.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryId(Long id);

    @Query(
            "select p from Product p join fetch p.category where (:categoryId is null or"
                    + " p.category.id=:categoryId) and lower(p.name) like lower(concat('%',:q,'%')) order by"
                    + " p.id desc")
    List<Product> search(@Param("q") String q, @Param("categoryId") Long categoryId);
}
