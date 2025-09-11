package ntt.ntt_ms_accounts.service.impl;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.mapper.CreateAccountMapper;
import ntt.ntt_ms_accounts.mapper.ListAccountMapper;
import ntt.ntt_ms_accounts.repository.BankAccountRepository;
import ntt.ntt_ms_accounts.service.BankAccountService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final CustomerClient customerClient;
    private final ListAccountMapper listAccountmapper;
    private final CreateAccountMapper createAccountMapper;

    public Flux<BankAccountDto> findAllAccounts() {
        return repository.findAll().map(listAccountmapper::toList);
    }

    public Mono<BankAccountDto> saveBankAccount(BankAccountRequestDto request) {
        BankAccount entity = createAccountMapper.toEntity(request);
        return customerClient.getCustomerById(entity.getCustomerId())
                .flatMap(customer -> {
                    return repository.save(entity)
                            .map(listAccountmapper::toList);
                });
    }

}
