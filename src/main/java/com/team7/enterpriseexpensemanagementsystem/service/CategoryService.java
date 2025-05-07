package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.response.CategoryResponse;


public interface CategoryService {
    CategoryResponse findAll(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO findByName(String categoryName);
    CategoryDTO findById(Long id);
    CategoryDTO addCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(CategoryDTO categoryDTO);
    void deleteCategory(Long id);
}
