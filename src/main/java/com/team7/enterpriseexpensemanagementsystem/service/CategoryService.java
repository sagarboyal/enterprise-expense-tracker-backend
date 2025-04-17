package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryResponse;


public interface CategoryService {
    public CategoryResponse findAll();
    public CategoryDTO findByName(String categoryName);
    public CategoryDTO findById(Long id);
    public CategoryDTO addCategory(CategoryDTO categoryDTO);
    public CategoryDTO updateCategory(CategoryDTO categoryDTO);
    public void deleteCategory(Long id);
}
