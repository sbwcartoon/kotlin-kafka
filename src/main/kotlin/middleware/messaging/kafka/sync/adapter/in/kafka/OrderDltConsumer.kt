package middleware.messaging.kafka.sync.adapter.`in`.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderDltConsumer(@Value("\${spring.kafka.consumer.group-id}") val originalGroupId: String) {

    @KafkaListener(
        topics = ["\${kafka.topic.order-created}.DLT"],
        groupId = "\${spring.kafka.consumer.dlt-group-id}",
    )
    fun consume(record: ConsumerRecord<String, String>, e: Exception?) {
        val dltLog = mapOf(
            "topic" to record.topic(),
            "key" to record.key(),
            "value" to record.value(),
            "partition" to record.partition(),
            "offset" to record.offset(),
            "consumerTimestamp" to Instant.ofEpochMilli(record.timestamp()).toString(),
            "consumerGroupId" to originalGroupId,
            "processedAt" to Instant.now().toString(),
            "errorMessage" to e?.message,
            "errorStacktrace" to e?.stackTraceToString(),
        )
        println(dltLog)
    }
}