package ntt.ntt_ms_accounts.service;

import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BankAccountService {

    Flux<BankAccountDto> findAllAccounts();

    Mono<BankAccountDto> saveBankAccount(BankAccountRequestDto request);

    Mono<BankAccountDto> updateBankAccount(String id, BankAccountRequestDto request);

    Mono<Void> deleteBankAccount(String id);

}
