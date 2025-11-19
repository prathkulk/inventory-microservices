package com.capgemini.orderservice.service;

import com.capgemini.orderservice.entity.Order;
import com.capgemini.orderservice.entity.OrderLineItems;
import com.capgemini.orderservice.repository.OrderRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "inventory", fallbackMethod = "placeOrderFallback")
    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToEntity)
                .toList();

        order.setOrderLineItemsList(orderLineItems);
        
        // Check stock for each item in the order
        // NOTE: In a real world scenario we might want to batch this check
        for (OrderLineItems item : orderLineItems) {
            try {
                Boolean isInStock = webClientBuilder.build().get()
                        .uri("http://inventory-service/api/inventory/check",
                                uriBuilder -> uriBuilder.queryParam("skuCode", item.getSkuCode()).build())
                        .retrieve()
                        .bodyToMono(Boolean.class)
                        .block();

                if (Boolean.FALSE.equals(isInStock)) {
                    throw new IllegalArgumentException("Product is not in stock, please try again later. SKU: " + item.getSkuCode());
                }
            } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {
                System.err.println("Error calling inventory service. Status: " + e.getStatusCode());
                System.err.println("Response Body: " + e.getResponseBodyAsString());
                throw new RuntimeException("Failed to communicate with Inventory Service", e);
            } catch (Exception e) {
                System.err.println("Error calling inventory service for check: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to communicate with Inventory Service", e);
            }
        }

        // If all items are in stock, we then reduce the stock
         for (OrderLineItems item : orderLineItems) {
             try {
                 webClientBuilder.build().post()
                        .uri("http://inventory-service/api/inventory/reduce")
                        .bodyValue(new StockUpdateRequest(item.getSkuCode(), item.getQuantity()))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .block();
             } catch (Exception e) {
                System.err.println("Error calling inventory service for reduce: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to reduce stock", e);
             }
         }

        orderRepository.save(order);
        return "Order Placed Successfully";
    }

    public String placeOrderFallback(OrderRequest orderRequest, RuntimeException runtimeException) {
        return "Oops! Something went wrong, please order after some time!";
    }

    private OrderLineItems mapToEntity(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
    
    @Data
    public static class OrderRequest {
        private List<OrderLineItemsDto> orderLineItemsDtoList;
    }
    
    @Data
    public static class OrderLineItemsDto {
        private Long id;
        private String skuCode;
        private java.math.BigDecimal price;
        private Integer quantity;
    }
    
    @Data
    public static class StockUpdateRequest {
        private String skuCode;
        private Integer quantity;
        
        public StockUpdateRequest(String skuCode, Integer quantity) {
            this.skuCode = skuCode;
            this.quantity = quantity;
        }
    }
}
