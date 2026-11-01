package com.example.shop.controller;

import com.example.shop.dto.ProductForm;
import com.example.shop.entity.Category;
import com.example.shop.entity.Product;
import com.example.shop.service.ShopService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
    private final ShopService s;

    public WebController(ShopService s) {
        this.s = s;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/products";
    }

    @GetMapping("/categories")
    public String categories(Model m) {
        m.addAttribute("rows", s.categories());
        return "categories/list";
    }

    @GetMapping("/categories/new")
    public String categoryNew(Model m) {
        m.addAttribute("category", new Category());
        return "categories/form";
    }

    @GetMapping("/categories/{id}/edit")
    public String categoryEdit(@PathVariable Long id, Model m) {
        m.addAttribute("category", s.category(id));
        return "categories/form";
    }

    @PostMapping("/categories/save")
    public String categorySave(
            @Valid @ModelAttribute("category") Category c, BindingResult b, RedirectAttributes r) {
        if (b.hasErrors()) return "categories/form";
        s.saveCategory(c.getId(), c);
        r.addFlashAttribute("message", "Đã lưu danh mục");
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String categoryDelete(@PathVariable Long id, RedirectAttributes r) {
        try {
            s.deleteCategory(id);
            r.addFlashAttribute("message", "Đã xóa danh mục");
        } catch (ResponseStatusException | DataIntegrityViolationException e) {
            r.addFlashAttribute(
                    "message", "Không thể xóa: danh mục đang được sử dụng hoặc không tồn tại.");
        }
        return "redirect:/categories";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) Long categoryId,
            Model m) {
        m.addAttribute("rows", s.products(q, categoryId));
        m.addAttribute("categories", s.categories());
        m.addAttribute("q", q);
        m.addAttribute("categoryId", categoryId);
        return "products/list";
    }

    private String form(Model m, ProductForm f, Long id) {
        m.addAttribute("product", f);
        m.addAttribute("id", id);
        m.addAttribute("categories", s.categories());
        return "products/form";
    }

    @GetMapping("/products/new")
    public String productNew(Model m) {
        return form(m, new ProductForm(), null);
    }

    @GetMapping("/products/{id}/edit")
    public String productEdit(@PathVariable Long id, Model m) {
        Product p = s.product(id);
        ProductForm f = new ProductForm();
        f.name = p.getName();
        f.price = p.getPrice();
        f.stock = p.getStock();
        f.categoryId = p.getCategory().getId();
        return form(m, f, id);
    }

    @PostMapping("/products/save")
    public String productSave(
            @RequestParam(required = false) Long id,
            @Valid @ModelAttribute("product") ProductForm f,
            BindingResult b,
            Model m,
            RedirectAttributes r) {
        if (b.hasErrors()) {
            m.addAttribute("id", id);
            m.addAttribute("categories", s.categories());
            return "products/form";
        }
        try {
            s.saveProduct(id, f);
        } catch (ResponseStatusException e) {
            b.rejectValue("categoryId", "invalid", "Danh mục không tồn tại");
            m.addAttribute("id", id);
            m.addAttribute("categories", s.categories());
            return "products/form";
        }
        r.addFlashAttribute("message", "Đã lưu sản phẩm");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String productDelete(@PathVariable Long id, RedirectAttributes r) {
        s.deleteProduct(id);
        r.addFlashAttribute("message", "Đã xóa sản phẩm");
        return "redirect:/products";
    }
}
