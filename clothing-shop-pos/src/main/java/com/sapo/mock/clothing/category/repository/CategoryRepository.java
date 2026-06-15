package com.sapo.mock.clothing.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sapo.mock.clothing.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
	boolean existsByParent_Id(Integer parentId);
}
