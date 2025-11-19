package com.capgemini.inventoryservice.service;

import com.capgemini.inventoryservice.entity.InventoryItem;
import com.capgemini.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public InventoryItem addStock(String skuCode, Integer quantity) {
        Optional<InventoryItem> inventoryItemOptional = inventoryRepository.findBySkuCode(skuCode);
        
        InventoryItem inventoryItem;
        if (inventoryItemOptional.isPresent()) {
            inventoryItem = inventoryItemOptional.get();
            inventoryItem.setQuantity(inventoryItem.getQuantity() + quantity);
        } else {
            inventoryItem = new InventoryItem();
            inventoryItem.setSkuCode(skuCode);
            inventoryItem.setQuantity(quantity);
        }
        
        return inventoryRepository.save(inventoryItem);
    }

    @Transactional
    public InventoryItem removeStock(String skuCode, Integer quantity) {
        InventoryItem inventoryItem = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new RuntimeException("Inventory item not found for skuCode: " + skuCode));

        if (inventoryItem.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for skuCode: " + skuCode);
        }

        inventoryItem.setQuantity(inventoryItem.getQuantity() - quantity);
        return inventoryRepository.save(inventoryItem);
    }

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(item -> item.getQuantity() > 0)
                .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public Integer getStock(String skuCode) {
         return inventoryRepository.findBySkuCode(skuCode)
                .map(InventoryItem::getQuantity)
                .orElse(0);
    }
}
