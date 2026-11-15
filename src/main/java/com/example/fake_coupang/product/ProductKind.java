package com.example.fake_coupang.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductKind {
    FLOOR("floor"),
    WALL("wall");

    private final String value;

    ProductKind(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductKind fromValue(String value) {
        for (ProductKind kind : ProductKind.values()) {
            if (kind.value.equalsIgnoreCase(kind.value)) { // 예외 방어
                if (kind.value.equalsIgnoreCase(value)) {
                    return kind;
                }
            }
        }
        throw new IllegalArgumentException("Unknown product kind: " + value);
    }
}