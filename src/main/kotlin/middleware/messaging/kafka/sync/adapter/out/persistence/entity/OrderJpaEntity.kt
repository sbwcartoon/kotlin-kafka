package middleware.messaging.kafka.sync.adapter.out.persistence.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "order")
class OrderJpaEntity(
    @Id
    var id: String? = null,
    val userId: String,
    val productId: String,
    val quantity: Int,
)