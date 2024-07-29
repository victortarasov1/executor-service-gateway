package executor.service.gateway.configuration;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.function.Function;

import static org.springframework.http.HttpMethod.GET;

@Configuration
public class GateWayConfig {
    @Bean
    public RouteLocator easyBankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(getRoute("/executor/proxy/**", "/executor/proxy/(?<segment>.*)", "lb://PROXY"))
                .route(getRoute("/executor/publisher/**", "/executor/publisher/(?<segment>.*)", "lb://PUBLISHER"))
                .build();
    }

    private Function<PredicateSpec, Buildable<Route>> getRoute(String path, String regex, String uri) {
        return p -> p
                .path(path)
                .filters(getSpecFunction(regex))
                .uri(uri);
    }

    private Function<GatewayFilterSpec, UriSpec> getSpecFunction(String regex) {
        return f -> f
                .rewritePath(regex, "/${segment}")
                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                .retry(retryConfig -> retryConfig.setRetries(3)
                        .setMethods(GET)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(100),2, true))
                .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(userKeyResolver()))
                .circuitBreaker(config -> config.setFallbackUri("fallback:/contactSupport"));
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(1, 1, 1);
    }


    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("user"))
                .defaultIfEmpty("anonymous");
    }
}
