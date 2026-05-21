package com.b9.json.jsonplatform.order.application.handler;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusContext {
    private String trackingNumber;
    private Integer jastiperRating;
    private Integer productRating;
}