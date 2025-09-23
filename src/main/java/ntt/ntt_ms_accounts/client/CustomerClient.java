package ntt.ntt_ms_accounts.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.CustomerResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.concurrent.TimeoutException;
import java.time.Duration;
import org.springframework.web.reactive.function.client.WebClientRequestException;

@Service
@RequiredArgsConstructor
public class CustomerClient {

    private final WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "customers", fallbackMethod = "fallbackGetCustomer")
    public Mono<CustomerResponseDto> getCustomerById(String id) {
        return webClientBuilder.build()
                .get()
                .uri("lb://ntt-ms-api-gateway/customers/{id}", id)
                .retrieve()
                .bodyToMono(CustomerResponseDto.class)
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(TimeoutException.class, ex -> fallbackGetCustomer(id, ex))
                .onErrorResume(WebClientRequestException.class, ex -> fallbackGetCustomer(id, ex));
    }
    private Mono<CustomerResponseDto> fallbackGetCustomer(String id, Throwable ex) {
        return Mono.error(new IllegalStateException("Servicio de clientes no disponible", ex));
    }

}
