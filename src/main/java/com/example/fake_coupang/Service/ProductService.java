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

    // 상품 이름 수정
    public Product updateName(Long id, String name) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        product.updateName(name);

        return productRepository.save(product);
    }

    // 상품 가격 수정
    public Product updatePrice(Long id, Integer price) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품 없음"));

        product.updatePrice(price);

        return productRepository.save(product);
    }

    // 낮은 가격 상품 TOP N 조회
    public List<Product> getCheapProducts(int n) {
        return productRepository.findAllByOrderByPriceAsc()
                .stream()
                .limit(n)
                .toList();
    }

    // 높은 가격 상품 TOP N 조회
    public List<Product> getExpensiveProducts(int n) {
        return productRepository.findAllByOrderByPriceDesc()
                .stream()
                .limit(n)
                .toList();
    }

    // 삭제
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}