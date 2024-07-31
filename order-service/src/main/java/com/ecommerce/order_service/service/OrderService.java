package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.InventoryResponse;
import com.ecommerce.order_service.dto.OrderLineItemsDto;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import com.ecommerce.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private static final Logger LOGGER = Logger.getLogger(OrderService.class.getName());

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuider;

    public Order placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        //convert OrderLineItemsDto to OrderLineItems
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream().map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();
        order.setOrderLineItemsList(orderLineItems);

        // Mapping the list of skuCode from orderLineItems to a newly created list
        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(orderLineItem -> orderLineItem.getSkuCode()).collect(Collectors.toList());

        // Build the WebClient request with multiple query parameters
        InventoryResponse[] inventoryResponsesList;
        try {
            inventoryResponsesList = webClientBuider.build().get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .doOnNext(response -> System.out.println("Received Inventory Response: " + Arrays.toString(response)))
                    .block();
        }
        catch(Exception e){
            LOGGER.log(Level.SEVERE, "Error while calling inventory service", e);
            throw new IllegalStateException("Error while checking stock availability");
        }

        if (inventoryResponsesList == null) {
            throw new IllegalStateException("No response from inventory service");
        }

        // Create a list of SKU codes that are in stock
        List<String> inStockSkuCodes = Arrays.stream(inventoryResponsesList)
                .filter(InventoryResponse::isInStock)
                .map(InventoryResponse::getSkuCode)
                .collect(Collectors.toList());

        // Check if all items are in stock
        boolean allMatch = skuCodes.stream().allMatch(inStockSkuCodes::contains);

        if (allMatch ) {
            return orderRepository.save(order);
        } else {
            throw new IllegalAccessException("One or more products are out of stock");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        System.out.println(orderLineItems +" test "+ orderLineItemsDto);

        return orderLineItems;
    }
}
