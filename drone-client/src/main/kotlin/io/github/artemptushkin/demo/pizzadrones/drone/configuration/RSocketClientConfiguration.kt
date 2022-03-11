package io.github.artemptushkin.demo.pizzadrones.drone.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester

@Configuration
class RSocketClientConfiguration {

    @Bean
    fun requester(requesterBuilder: RSocketRequester.Builder): RSocketRequester = requesterBuilder
        //.setupRoute("api.drones.locations.channel")
        .tcp("localhost", 7000)
}