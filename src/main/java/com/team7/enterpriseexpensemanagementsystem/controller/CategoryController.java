package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;
import com.team7.enterpriseexpensemanagementsystem.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    public ResponseEntity<List<Category>> getAllCategoriesHandler() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findAll());
    }

    @GetMapping("/public/categories/name/{name}")
    public ResponseEntity<Category> getCategoryByNameHandler(@PathVariable String name) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findByName(name));
    }

    @GetMapping("/public/categories/id/{id}")
    public ResponseEntity<Category> getAllCategoryByIdHandler(@PathVariable("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findById(id));
    }

    @PostMapping("/public/categories")
    public ResponseEntity<Category> saveCategoryHandler(@RequestBody Category category) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.addCategory(category));
    }

    @PutMapping("/public/categories")
    public ResponseEntity<Category> updateCategoryHandler(@RequestBody Category category) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.updateCategory(category));
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<String> deleteCategoryHandler(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Deleted Category ID: " + id);
    }
}
