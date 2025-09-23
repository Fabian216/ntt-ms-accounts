package ntt.ntt_ms_accounts.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import ntt.ntt_ms_accounts.dto.CreditCardDto;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


@Service
@RequiredArgsConstructor
public class CreditCardClient {

    private final WebClient.Builder webClientBuilder;

    @CircuitBreaker(name = "creditCards", fallbackMethod = "fallbackGetCard")
    public Mono<CreditCardDto> getCreditCardByCustomerId(String id) {
        return webClientBuilder.build()
                .get().uri("lb://ntt-ms-api-gateway/creditCards/customer/{id}",id)
                .retrieve()
                .bodyToFlux(CreditCardDto.class)
                .next()
                .timeout(Duration.ofSeconds(2))
                .onErrorResume(TimeoutException.class, ex -> fallbackGetCard(id, ex))
                .onErrorResume(WebClientRequestException.class, ex -> fallbackGetCard(id, ex));
    }

    private Mono<CreditCardDto> fallbackGetCard(String id, Throwable ex) {
        return Mono.error(new IllegalStateException("Servicio de tarjetas no disponible", ex));
    }
}
