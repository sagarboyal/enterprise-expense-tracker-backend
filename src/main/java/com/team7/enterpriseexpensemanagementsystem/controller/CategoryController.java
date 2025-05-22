package com.team7.enterpriseexpensemanagementsystem.controller;

import com.team7.enterpriseexpensemanagementsystem.config.AppConstants;
import com.team7.enterpriseexpensemanagementsystem.dto.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.response.CategoryPagedResponse;
import com.team7.enterpriseexpensemanagementsystem.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/public/categories")
    public ResponseEntity<CategoryPagedResponse> getAllCategoriesHandler(
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY_CATEGORY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.filterCategories(name, id, pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/admin/categories/id/{id}")
    public ResponseEntity<CategoryDTO> getCategoryByIdHandler(@PathVariable("id") Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(categoryService.findById(id));
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDTO> saveCategoryHandler(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.addCategory(categoryDTO));
    }

    @PutMapping("/admin/categories")
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
