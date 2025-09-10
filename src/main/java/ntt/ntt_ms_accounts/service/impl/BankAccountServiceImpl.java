package ntt.ntt_ms_accounts.service.impl;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.repository.BankAccountRepository;
import ntt.ntt_ms_accounts.service.BankAccountService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final CustomerClient customerClient;

    public Mono<BankAccount> saveBankAccount(BankAccount request) {
        return customerClient.getCustomerById(request.getCustomerId())
                .flatMap(customer -> {
                    return repository.save(request);
                });
    }

}
