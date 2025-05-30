package middleware.messaging.kafka

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KotlinKafkaApplication

fun main(args: Array<String>) {
    runApplication<KotlinKafkaApplication>(*args)
}
