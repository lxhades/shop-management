package com.example.shop.service;

import com.example.shop.dto.ProductForm;
import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.repository.CategoryRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ShopService {
    private final CategoryRepository categories;
    private final ProductRepository products;

    public ShopService(CategoryRepository c, ProductRepository p) {
        categories = c;
        products = p;
    }

    public List<Category> categories() {
        return categories.findAll();
    }

    public Category category(Long id) {
        return categories
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy danh mục"));
    }

    public Product product(Long id) {
        return products
                .findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm"));
    }

    public List<Product> products(String q, Long categoryId) {
        return products.search(q == null ? "" : q, categoryId);
    }

    public Category saveCategory(Long id, Category input) {
        Category c = id == null ? new Category() : category(id);
        c.setName(input.getName());
        c.setDescription(input.getDescription());
        return categories.save(c);
    }

    public void deleteCategory(Long id) {
        Category c = category(id);
        if (products.existsByCategoryId(id))
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Danh mục đang có sản phẩm; hãy xóa hoặc chuyển sản phẩm trước.");
        categories.delete(c);
        categories.flush();
    }

    public Product saveProduct(Long id, ProductForm f) {
        Product v = id == null ? new Product() : product(id);
        v.setName(f.name);
        v.setPrice(f.price);
        v.setStock(f.stock);
        v.setCategory(category(f.categoryId));
        return products.save(v);
    }

    public void deleteProduct(Long id) {
        products.delete(product(id));
    }
}
