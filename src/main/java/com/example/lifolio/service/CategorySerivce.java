package com.example.lifolio.service;

import com.example.lifolio.dto.category.CategoryDto;
import com.example.lifolio.entity.Category;
import com.example.lifolio.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class CategorySerivce {
    private final CategoryRepository categoryRepository;

    public void saveCategory(CategoryDto categoryDTO) {
        Category category = categoryDTO.toEntity();
    }

//    public Long updateCategory(Long id, CategoryDto CategoryDTO);

}

