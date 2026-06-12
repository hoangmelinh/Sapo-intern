package com.sapo.mock.clothing.product.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sapo.mock.clothing.common.dto.response.RestResponse;
import com.sapo.mock.clothing.product.DTO.ProductResponse;
import com.sapo.mock.clothing.product.service.IProductService;

@RestController
@RequestMapping("api/v1/products")
public class ProductController {
	@Autowired
	private IProductService productService;

	@GetMapping()
	public ResponseEntity<RestResponse<Page<ProductResponse>>> getAllProducts(Pageable pageable,
			@RequestParam(required = false) String search, @RequestParam(required = false) String productName,
			@RequestParam(required = false) String sku, @RequestParam(required = false) String category) {
		Page<ProductResponse> products = productService.getAllProducts(pageable, search, productName, sku, category);
		RestResponse<Page<ProductResponse>> response = new RestResponse<>(200, null,
				"Lấy danh sách sản phẩm thành công", products);
		return ResponseEntity.ok(response);

	}

}
