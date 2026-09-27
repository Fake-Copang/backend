package com.example.fake_coupang.Service;

import com.example.fake_coupang.product.Product;
import com.example.fake_coupang.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 추가
    public Product addProduct(Long dummyId, String name, Integer price, Integer quantity) {

        Product product = Product.builder()
                .dummyId(dummyId)
                .name(name)
                .price(price)
                .quantity(quantity)
                .build();

        return productRepository.save(product);
    }

    // 전체 조회
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // 하나 조회
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));
    }

    // 수량 수정
    public Product updateQuantity(Long id, Integer quantity) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        product.updateQuantity(quantity);

        return productRepository.save(product);
    }

    // 삭제
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}