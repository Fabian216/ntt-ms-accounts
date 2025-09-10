package ntt.ntt_ms_accounts.client;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.PersonalCustomerResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<PersonalCustomerResponseDto> getCustomerById(String id) {
        return webClientBuilder.build()
                .get()
                .uri("lb://ntt-ms-api-gateway/customers/personal/{id}", id)
                .retrieve()
                .bodyToMono(PersonalCustomerResponseDto.class);
    }

}
