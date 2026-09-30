package com.example.fake_coupang.product;
import com.example.fake_coupang.Service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 전체 조회 (GET /api/products)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // 상품 단건 조회 (GET /api/products/{id})
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    // 수량 수정 (PATCH /api/products/{id}/quantity?quantity=10)
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<Product> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(productService.updateQuantity(id, quantity));
    }

    // 상품 이름 수정 (PATCH /api/products/{id}/name?name=수정할이름)
    @PatchMapping("/{id}/name")
    public ResponseEntity<Product> updateName(
            @PathVariable Long id,
            @RequestParam String name) {
        return ResponseEntity.ok(productService.updateName(id, name));
    }

    // 상품 가격 수정 (PATCH /api/products/{id}/price?price=15000)
    @PatchMapping("/{id}/price")
    public ResponseEntity<Product> updatePrice(
            @PathVariable Long id,
            @RequestParam Integer price) {
        return ResponseEntity.ok(productService.updatePrice(id, price));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // 204 No Content 반환
    }
}