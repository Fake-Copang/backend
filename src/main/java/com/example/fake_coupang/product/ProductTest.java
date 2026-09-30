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
//        for(int i=0;i<10;i++){
//            addProduct((long)i,i+"번째 상품",i*1000,i*10);
//        }



    }

    void addProduct(Long dummyId,String name,int price,int quantity) {
        Product product = Product.builder()
                .dummyId(dummyId)
                .name(name)
                .price(price)
                .quantity(quantity)
                .build();


        productRepository.save(product);
        System.out.println("상품 저장 완료");
    }
}