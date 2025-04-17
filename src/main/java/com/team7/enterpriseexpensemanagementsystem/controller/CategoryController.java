package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryResponse;
import com.team7.enterpriseexpensemanagementsystem.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getAllCategoriesHandler(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_CATEGORY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findAll(pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/public/categories/name/{name}")
    public ResponseEntity<CategoryDTO> getCategoryByNameHandler(@PathVariable String name) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findByName(name));
    }

    @GetMapping("/public/categories/id/{id}")
    public ResponseEntity<CategoryDTO> getAllCategoryByIdHandler(@PathVariable("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findById(id));
    }

    @PostMapping("/public/categories")
    public ResponseEntity<CategoryDTO> saveCategoryHandler(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.addCategory(categoryDTO));
    }

    @PutMapping("/public/categories")
    public ResponseEntity<CategoryDTO> updateCategoryHandler(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.updateCategory(categoryDTO));
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<String> deleteCategoryHandler(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Deleted Category ID: " + id);
    }
}
