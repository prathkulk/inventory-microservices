package com.capgemini.inventoryservice.controller;

import com.capgemini.inventoryservice.entity.InventoryItem;
import com.capgemini.inventoryservice.service.InventoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItem addStock(@RequestBody StockUpdateRequest request) {
        return inventoryService.addStock(request.getSkuCode(), request.getQuantity());
    }

    @PostMapping("/reduce")
    @ResponseStatus(HttpStatus.OK)
    public InventoryItem reduceStock(@RequestBody StockUpdateRequest request) {
        return inventoryService.removeStock(request.getSkuCode(), request.getQuantity());
    }

    @GetMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public Integer getStock(@PathVariable String skuCode) {
        return inventoryService.getStock(skuCode);
    }

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@RequestParam String skuCode) {
        return inventoryService.isInStock(skuCode);
    }

    @Data
    public static class StockUpdateRequest {
        private String skuCode;
        private Integer quantity;
    }
}
