package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.OrderLineItemsDto;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import com.ecommerce.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    public Order placeOrder(OrderRequest orderRequest) {
       Order order = new Order();
       order.setOrderNumber(UUID.randomUUID().toString());
      List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
               .stream().map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();
      order.setOrderLineItemsList(orderLineItems);
      orderRepository.save(order);
        return order;
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());

        return orderLineItems;
    }
}
