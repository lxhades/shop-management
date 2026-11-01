package com.example.shop.controller;

import com.example.shop.dto.ProductForm;
import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ShopService s;

    public ApiController(ShopService s) {
        this.s = s;
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return s.categories();
    }

    @GetMapping("/categories/{id}")
    public Category category(@PathVariable Long id) {
        return s.category(id);
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@Valid @RequestBody Category c) {
        return s.saveCategory(null, c);
    }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @Valid @RequestBody Category c) {
        return s.saveCategory(id, c);
    }

    @DeleteMapping("/categories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        s.deleteCategory(id);
    }

    @GetMapping("/products")
    public List<Product> products(
            @RequestParam(defaultValue = "") String q, @RequestParam(required = false) Long categoryId) {
        return s.products(q, categoryId);
    }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable Long id) {
        return s.product(id);
    }

    @PostMapping("/products")
    @ResponseStatus(HttpStatus.CREATED)
    public Product createProduct(@Valid @RequestBody ProductForm f) {
        return s.saveProduct(null, f);
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(@PathVariable Long id, @Valid @RequestBody ProductForm f) {
        return s.saveProduct(id, f);
    }

    @DeleteMapping("/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        s.deleteProduct(id);
    }
}
