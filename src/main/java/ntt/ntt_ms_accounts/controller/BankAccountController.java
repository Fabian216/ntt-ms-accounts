package ntt.ntt_ms_accounts.controller;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.service.BankAccountService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService service;

    @GetMapping
    public Flux<BankAccountDto> getAllAccounts() {
        return service.findAllAccounts();
    }

    @PostMapping
    public Mono<BankAccountDto> createBankAccount(@RequestBody BankAccountRequestDto request) {
        return service.saveBankAccount(request);
    }

}
