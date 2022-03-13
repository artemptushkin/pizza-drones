package io.github.artemptushkin.demo.pizzadrones.drone.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester

@Configuration
class RSocketClientConfiguration {

    @Value("\${tower-server.port}")
    var serverPort: Int = -1

    @Bean
    fun requester(requesterBuilder: RSocketRequester.Builder): RSocketRequester = requesterBuilder
        .tcp("localhost", serverPort)
}