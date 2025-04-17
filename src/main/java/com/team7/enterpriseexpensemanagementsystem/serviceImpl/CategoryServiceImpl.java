package com.team7.enterpriseexpensemanagementsystem.serviceImpl;

import com.team7.enterpriseexpensemanagementsystem.entity.Category;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceAlreadyExistsException;
import com.team7.enterpriseexpensemanagementsystem.exception.ResourceNotFoundException;
import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryDTO;
import com.team7.enterpriseexpensemanagementsystem.payload.category.CategoryResponse;
import com.team7.enterpriseexpensemanagementsystem.repository.CategoryRepository;
import com.team7.enterpriseexpensemanagementsystem.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ModelMapper modelMapper) {
        this.categoryRepository = categoryRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CategoryResponse findAll() {
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) throw new ResourceNotFoundException("Category not found");
        List<CategoryDTO> response = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        return CategoryResponse.builder()
                .content(response)
                .build();
    }

    @Override
    public CategoryDTO findByName(String categoryName) {
        Category category = categoryRepository.findByName(categoryName)
                .orElseThrow(() -> new ResourceNotFoundException("Category with name: "+categoryName+" not found"));

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with id: "+id+" not found"));

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO addCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.findByName(categoryDTO.getName()).isPresent())
            throw new ResourceAlreadyExistsException("Category with name: "+categoryDTO.getName()+" already exists");
        Category category = categoryRepository.save(modelMapper.map(categoryDTO, Category.class));

        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO) {
        Category data = modelMapper.map(findById(categoryDTO.getId()), Category.class);
        data.setName(
                categoryDTO.getName() != null && !categoryDTO.getName().isEmpty()
                        ? categoryDTO.getName() : data.getName());

        return modelMapper.map(categoryRepository.save(data), CategoryDTO.class);
    }

    @Override
    public void deleteCategory(Long id) {
        Category data = modelMapper.map(findById(id), Category.class);
        categoryRepository.delete(data);
    }
}
