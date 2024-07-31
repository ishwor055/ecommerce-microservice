package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.InventoryResponse;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStock(List<String> skuCode) {

        List<InventoryResponse> responses = inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                        InventoryResponse.builder()
                                .skuCode(inventory.getSkuCode())
                                .isInStock(inventory.getQuantity() > 0)
                                .build()
                ).toList();

        // Log the responses for debugging
        responses.forEach(response ->
                System.out.println("SKU Code: " + response.getSkuCode() + ", In Stock: " + response.isInStock())
        );

        return responses;
    }
}