package ntt.ntt_ms_accounts.controller;

import lombok.RequiredArgsConstructor;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.service.BankAccountService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.MediaType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class BankAccountController {

    private final BankAccountService service;

    @GetMapping
    public Flux<BankAccountDto> getAllAccounts() {
        return service.findAllAccounts();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BankAccountDto> createBankAccount(@RequestBody BankAccountRequestDto request) {
        return service.saveBankAccount(request);
    }

    @PutMapping("/{id}")
    public Mono<BankAccountDto> updateBankAccount(@PathVariable String id,
                                                  @RequestBody BankAccountRequestDto request) {
        return service.updateBankAccount(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteBankAccount(@PathVariable String id) {
        return service.deleteBankAccount(id);
    }

}
