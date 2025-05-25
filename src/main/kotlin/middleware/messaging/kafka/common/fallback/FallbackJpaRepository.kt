package middleware.messaging.kafka.common.fallback

import org.springframework.data.jpa.repository.JpaRepository

interface FallbackJpaRepository : JpaRepository<FailedMessageJpaEntity, Long>