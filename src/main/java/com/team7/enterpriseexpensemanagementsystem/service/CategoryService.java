package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryResponse;


public interface CategoryService {
    CategoryResponse findAll();
    CategoryDTO findByName(String categoryName);
    CategoryDTO findById(Long id);
    CategoryDTO addCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(CategoryDTO categoryDTO);
    void deleteCategory(Long id);
}
