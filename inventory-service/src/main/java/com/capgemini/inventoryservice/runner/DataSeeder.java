package com.capgemini.inventoryservice.runner;

import com.capgemini.inventoryservice.entity.InventoryItem;
import com.capgemini.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {
    private final InventoryRepository inventoryRepository;

    public DataSeeder(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- STARTING DATABASE SEEDER ---");

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setSkuCode("IPHONE_11_RED");
        inventoryItem.setQuantity(100);

        inventoryRepository.save(inventoryItem);
        System.out.println("Saved Item: " + inventoryItem.getSkuCode());

        InventoryItem retrievedItem = inventoryRepository.findBySkuCode("IPHONE_15_RED").orElse(null);

        if (retrievedItem != null) {
            System.out.println("SUCCESS! Retrieved from DB: " + retrievedItem.getSkuCode() + " | Qty: " + retrievedItem.getQuantity());
        } else {
            System.out.println("FAILURE! Could not find item.");
        }

        System.out.println("--- END DATABASE SEEDER ---");
    }
}
