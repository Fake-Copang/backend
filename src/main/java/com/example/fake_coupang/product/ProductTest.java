package com.example.fake_coupang.product;

import com.example.fake_coupang.product.Product;
import com.example.fake_coupang.product.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductTest implements CommandLineRunner {

    private final ProductRepository productRepository;

    public ProductTest(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {

        Product product = Product.builder()
                .dummyId(1L)
                .name("테스트 상품")
                .price(10000)
                .quantity(5)
                .build();

        productRepository.save(product);

        System.out.println("상품 저장 완료");
    }
}