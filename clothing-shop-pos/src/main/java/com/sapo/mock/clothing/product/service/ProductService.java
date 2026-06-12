package com.sapo.mock.clothing.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.sapo.mock.clothing.entity.Product;
import com.sapo.mock.clothing.product.DTO.ProductResponse;
import com.sapo.mock.clothing.product.repository.ProductRepository;
import com.sapo.mock.clothing.specification.ProductSpecification;

@Service
public class ProductService implements IProductService {
	@Autowired
	private ProductRepository productRepository;

	public ProductResponse toProductResponse(Product product) {
		if (product == null) {
			return null;
		}

		ProductResponse response = new ProductResponse();
		response.setId(product.getId());
		response.setSku(product.getSku());
		response.setName(product.getName());
		response.setCategory(product.getCategory());
		response.setColor(product.getColor());
		response.setSize(product.getSize());
		response.setSalePrice(product.getSalePrice());
		response.setImportPrice(product.getImportPrice());
		response.setDescription(product.getDescription());
		response.setImageUrls(product.getImageUrls());

		if (product.getLowStockThreshold() != null) {
			response.setLowStockThreshold(product.getLowStockThreshold());
		}
		if (product.getIsDeleted() != null) {
			response.setIsDeleted(product.getIsDeleted());
		}

		response.setCreatedAt(product.getCreatedAt());
		response.setUpdatedAt(product.getUpdatedAt());

		// Xử lý mapping user ID
		if (product.getUpdatedBy() != null) {
			response.setUpdatedByUserID(product.getUpdatedBy().getId());
		}

		return response;
	}

	@Override
	public Page<ProductResponse> getAllProducts(Pageable pageable, String search, String productName, String sku,
			String category) {
		Specification<Product> spe = ProductSpecification.build(search, productName, sku, category);
		return productRepository.findAll(spe, pageable).map(this::toProductResponse);

	}

}
