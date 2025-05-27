package middleware.messaging.kafka.sync.adapter.out.persistence.repository

import middleware.messaging.kafka.sync.adapter.out.persistence.entity.OrderJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderJpaRepository : JpaRepository<OrderJpaEntity, String>