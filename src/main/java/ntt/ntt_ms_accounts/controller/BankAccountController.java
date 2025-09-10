package ntt.ntt_ms_accounts.controller;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.service.BankAccountService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService service;

    @PostMapping
    public Mono<BankAccount> createBankAccount(@RequestBody BankAccount request) {
        return service.saveBankAccount(request);
    }

}
