package io.github.artemptushkin.demo.pizzadrones.configuration

import org.jgroups.JChannel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JGroupsConfiguration {

    @Bean
    fun jChannel(): JChannel {
        return JChannel()
            .name("drone-events")
            .connect("tower-cluster")
    }
}