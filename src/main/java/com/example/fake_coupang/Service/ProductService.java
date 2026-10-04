package com.example.fake_coupang.Service;

import com.example.fake_coupang.product.Product;
import com.example.fake_coupang.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String DUMMY_URL = "https://dummyjson.com/products";

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // 상품 추가
    public Product addProduct(String name, Integer price, Integer quantity) {

        // DummyJSON에 보낼 데이터
        Map<String, Object> request = new HashMap<>();
        request.put("title", name);
        request.put("price", price);
        request.put("stock", quantity);

        // DummyJSON 상품 추가 API 호출
        Map response = restTemplate.postForObject(
                DUMMY_URL + "/add",
                request,
                Map.class
        );

        if (response == null) {
            throw new RuntimeException("DummyJSON 응답 없음");
        }

        // DummyJSON에서 만들어준 ID
        Long dummyId = ((Number) response.get("id")).longValue();

        // 실제 DB에 저장
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