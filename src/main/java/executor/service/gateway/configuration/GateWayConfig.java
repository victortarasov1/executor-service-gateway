package executor.service.gateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.UriSpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.function.Function;

@Configuration
public class GateWayConfig {
    @Bean
    public RouteLocator easyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(p -> p
                        .path("/executor/proxy/**")
                        .filters(getSpecFunction("/executor/proxy/(?<segment>.*)"))
                        .uri("lb://PROXY"))
                .route(p -> p
                        .path("/executor/publisher/**")
                        .filters(getSpecFunction("/executor/publisher/(?<segment>.*)"))
                        .uri("lb://PUBLISHER"))
                .build();
    }

    private static Function<GatewayFilterSpec, UriSpec> getSpecFunction(String regex) {
        return f -> f
                .rewritePath(regex, "/${segment}")
                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString());
    }
}
