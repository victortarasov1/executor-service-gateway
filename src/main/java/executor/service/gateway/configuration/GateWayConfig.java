package executor.service.gateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class GateWayConfig {
    @Bean
    public RouteLocator easyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(p -> p
                        .path("/executor/proxy/**")
                        .filters(f -> f
                                .rewritePath("/executor/proxy/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                        )
                        .uri("lb://PROXY"))
                .route(p -> p
                        .path("/executor/publisher/**")
                        .filters(f -> f
                                .rewritePath("/executor/publisher/(?<segment>.*)", "/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                        )
                        .uri("lb://PUBLISHER"))
                .build();
    }
}
