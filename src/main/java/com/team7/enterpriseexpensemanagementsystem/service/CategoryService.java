package com.team7.enterpriseexpensemanagementsystem.service;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;

import java.util.List;

public interface CategoryService {
    public List<Category> findAll();
    public Category findByName(String categoryName);
    public Category findById(Long id);
    public Category addCategory(Category category);
    public Category updateCategory(Category category);
    public void deleteCategory(Long id);
}
