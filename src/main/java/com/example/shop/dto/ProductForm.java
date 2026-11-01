package com.example.shop.dto;

import jakarta.validation.constraints.*;

public class ProductForm {
    @NotBlank(message = "Tên không được trống")
    @Size(max = 255)
    public String name;

    @NotNull
    @Positive(message = "Giá phải lớn hơn 0")
    public Double price;

    @NotNull
    @PositiveOrZero(message = "Tồn kho không được âm")
    public Integer stock = 0;

    @NotNull(message = "Hãy chọn danh mục")
    public Long categoryId;

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double v) {
        price = v;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer v) {
        stock = v;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long v) {
        categoryId = v;
    }
}
