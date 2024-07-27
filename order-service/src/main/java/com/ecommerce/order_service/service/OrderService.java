package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.InventoryResponse;
import com.ecommerce.order_service.dto.OrderLineItemsDto;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import com.ecommerce.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    public Order placeOrder(OrderRequest orderRequest) throws IllegalAccessException {
       Order order = new Order();
       order.setOrderNumber(UUID.randomUUID().toString());
      List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
               .stream().map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();
      order.setOrderLineItemsList(orderLineItems);
/*
      //mapping the list of skucode from orderLineItems to a newly created list
        // for query building purpose in uri*/

      List<String> skuCodes = order.getOrderLineItemsList().stream()
                      .map(orderLineItem -> orderLineItem.getSkuCode()).toList();

     /* // build the synchronos communication with inventory service
        // building the queryparam
        //saving the response in InventoryResponse class*/

       InventoryResponse[] inventoryResponsesList =  webClient.get()
                .uri("http://inventory-service/api/inventory" ,
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

    //matching the reponse from inventoryResponsesList to the inventoryResponse dto class
        boolean allMatch = Arrays.stream(inventoryResponsesList).allMatch(InventoryResponse::isInStock);

        if(allMatch){
      return orderRepository.save(order); }

       else {
           throw new IllegalAccessException("product you're looking for is not on stock");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;
    }
}
