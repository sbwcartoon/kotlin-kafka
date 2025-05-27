package middleware.messaging.kafka.common.fallback

import org.apache.kafka.clients.consumer.ConsumerRecord

data class KafkaFallbackLoggingCommand(
    val record: ConsumerRecord<*, *>,
    val exception: Exception,
)