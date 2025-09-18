package ntt.ntt_ms_accounts.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.mapper.CreateAccountMapper;
import ntt.ntt_ms_accounts.mapper.ListAccountMapper;
import ntt.ntt_ms_accounts.repository.BankAccountRepository;
import ntt.ntt_ms_accounts.service.BankAccountService;
import ntt.ntt_ms_accounts.validation.AccountValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final CustomerClient customerClient;
    private final ListAccountMapper listAccountmapper;
    private final CreateAccountMapper createAccountMapper;
    private final AccountValidator accountValidator;

    public Flux<BankAccountDto> findAllAccounts() {
        return repository.findAll().map(listAccountmapper::toResponseDto);
    }

    public Mono<BankAccountDto> saveBankAccount(BankAccountRequestDto request) {
        BankAccount entity = createAccountMapper.toEntity(request);
        return customerClient.getCustomerById(entity.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado")))
                .flatMap(customer ->
                    repository.findByCustomerId(entity.getCustomerId())
                            .collectList()
                            .flatMap(existingAccounts -> {
                                accountValidator.validateAccounts(customer, existingAccounts, entity);
                                return repository.save(entity)
                                        .map(listAccountmapper::toResponseDto);
                            })
                );
    }

}
