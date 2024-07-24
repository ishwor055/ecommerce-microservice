package com.ecommerce.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class ProductRequest {
    private int id;
    private String name;
    private String description;
    private BigDecimal price;
}
