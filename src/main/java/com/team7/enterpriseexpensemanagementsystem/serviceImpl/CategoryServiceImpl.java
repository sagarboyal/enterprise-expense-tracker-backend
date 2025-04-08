package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceAlreadyExistsException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.repository.CategoryRepository;
import com.team7.enterpriseexpensemanagementsystem.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) throw new ResourceNotFoundException("Category not found");
        return categories;
    }

    @Override
    public Category findByName(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category with name: "+categoryName+" not found"));
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: "+id+" not found"));
    }

    @Override
    public Category addCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent())
            throw new ResourceAlreadyExistsException("Category with name: "+category.getName()+" already exists");
        return categoryRepository.save(category);
    }

    @Override
    public Category updateCategory(Category category) {
        Category data = findById(category.getId());
        data.setName(category.getName() != null && !category.getName().isEmpty() ? category.getName() : data.getName());
        return categoryRepository.save(data);
    }

    @Override
    public void deleteCategory(Long id) {
        Category data = findById(id);
        categoryRepository.delete(data);
    }
}
