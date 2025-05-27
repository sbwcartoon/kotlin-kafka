package middleware.messaging.kafka.sync.adapter.out.kafka.exception

import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent

class OrderEventPublishErrorException(
    e: RuntimeException,
    event: OrderCreatedEvent,
) : RuntimeException("Order event publish error occurred with orderId ${event.orderId}", e)