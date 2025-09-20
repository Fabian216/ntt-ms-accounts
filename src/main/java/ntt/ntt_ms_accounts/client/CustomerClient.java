package ntt.ntt_ms_accounts.client;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.CustomerResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerClient {

    private final WebClient.Builder webClientBuilder;

    public Mono<CustomerResponseDto> getCustomerById(String id) {
        return webClientBuilder.build()
                .get()
                .uri("lb://ntt-ms-api-gateway/customers/{id}", id)
                .retrieve()
                .bodyToMono(CustomerResponseDto.class);
    }

}
