package com.sapo.mock.clothing.category.DTO;

import lombok.Data;

@Data

public class CategoryResponse {
	private Integer id;
	private String name;
	private boolean active;

	private boolean deleted;
}
