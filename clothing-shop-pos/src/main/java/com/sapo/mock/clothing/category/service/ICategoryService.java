package com.sapo.mock.clothing.category.service;

import java.util.List;

import com.sapo.mock.clothing.category.DTO.CategoryRequest;
import com.sapo.mock.clothing.category.DTO.CategoryResponse;

public interface ICategoryService {

	List<CategoryResponse> getAllCategory();

	CategoryResponse creatCategory(CategoryRequest request);

	CategoryResponse getCategoryById(Integer id);

	CategoryResponse updateCategory(Integer id, CategoryRequest request);

	CategoryResponse deleteCategory(Integer id);

	void hardDeleteCategory(Integer id);

	CategoryResponse toggleCategoryActive(Integer id, boolean active);

}
