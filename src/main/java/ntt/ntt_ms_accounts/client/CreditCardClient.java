package ntt.ntt_ms_accounts.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ntt.ntt_ms_accounts.dto.CreditCardDto;


@Service
@RequiredArgsConstructor
public class CreditCardClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<CreditCardDto> getCreditCardByCustomerId(String id) {
        return webClientBuilder.build()
                .get().uri("lb://ntt-ms-api-gateway/creditCards/customer/{id}",id)
                .retrieve()
                .bodyToFlux(CreditCardDto.class)
                .next();
    }
}
