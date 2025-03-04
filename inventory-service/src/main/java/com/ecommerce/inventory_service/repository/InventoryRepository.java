package com.ecommerce.inventory_service.repository;

import com.ecommerce.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query("select i.skuCode from Inventory i where i.skuCode = :skuCode ")
    Optional<Inventory> findBySkuCode(String skuCode);
}
