package ntt.ntt_ms_accounts.service;

import ntt.ntt_ms_accounts.entity.BankAccount;
import reactor.core.publisher.Mono;

public interface BankAccountService {

    Mono<BankAccount> saveBankAccount(BankAccount request);

}
