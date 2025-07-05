package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.dto.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.response.PagedResponse;

public interface CategoryService {
    PagedResponse<CategoryDTO> filterCategories(String name, Long id, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO findById(Long id);
    CategoryDTO addCategory(CategoryDTO categoryDTO);
    CategoryDTO updateCategory(CategoryDTO categoryDTO);
    void deleteCategory(Long id);
    CategoryDTO findByCategoryName(String categoryName);
}
