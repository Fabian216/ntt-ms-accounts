package ntt.ntt_ms_accounts.service.impl;
import ntt.ntt_ms_accounts.client.CreditCardClient;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.dto.CustomerResponseDto;
import ntt.ntt_ms_accounts.entity.SavingsAccount;
import ntt.ntt_ms_accounts.enums.AccountType;
import ntt.ntt_ms_accounts.enums.CustomerType;
import ntt.ntt_ms_accounts.enums.CustomerSubType;
import ntt.ntt_ms_accounts.mapper.CreateAccountMapper;
import ntt.ntt_ms_accounts.mapper.ListAccountMapper;
import ntt.ntt_ms_accounts.repository.BankAccountRepository;
import ntt.ntt_ms_accounts.service.BankAccountServiceImpl;
import ntt.ntt_ms_accounts.validation.AccountValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
public class BankAccountServiceImplTest {

    @Mock BankAccountRepository repository;
    @Mock CustomerClient customerClient;
    @Mock CreditCardClient creditCardClient;
    @Mock ListAccountMapper listAccountMapper;
    @Mock CreateAccountMapper createAccountMapper;
    @Mock AccountValidator accountValidator;

    @InjectMocks
    BankAccountServiceImpl service;

    @Test
    void creaCuenta_vipSinTarjeta_lanzaError() {
        // request
        BankAccountRequestDto req = new BankAccountRequestDto();
        req.setCustomerId("C1");
        req.setAccountType(AccountType.SAVINGS);
        req.setAccountNumber("ACC-1");
        req.setBalance(BigDecimal.ZERO);

        // entity devuelta por el mapper
        SavingsAccount entity = SavingsAccount.builder()
                .customerId("C1")
                .accountType(AccountType.SAVINGS)
                .accountNumber("ACC-1")
                .balance(BigDecimal.ZERO)
                //.requiredAvgDailyBalance(new BigDecimal("1"))
                .build();

        when(createAccountMapper.toEntity(req)).thenReturn(entity);

        // cliente VIP (IMPORTANTE: type no puede ser null)
        CustomerResponseDto vip = new CustomerResponseDto();
        vip.setType(CustomerType.PERSONAL);
        vip.setSubType(CustomerSubType.VIP);
        when(customerClient.getCustomerById("C1")).thenReturn(Mono.just(vip));

        when(repository.findByCustomerId("C1")).thenReturn(Flux.empty());

        // delegamos reglas al validador
        doNothing().when(accountValidator).validateAccounts(eq(vip), anyList(), eq(entity));

        // sin tarjeta: Mono.empty()
        when(creditCardClient.getCreditCardByCustomerId("C1")).thenReturn(Mono.empty());

        StepVerifier.create(service.saveBankAccount(req))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(IllegalArgumentException.class);
                    assertThat(ex.getMessage()).contains("No existe tarjeta de credito");
                })
                .verify();

        verify(repository, never()).save(any());
    }

    @Test
    void creaCuenta_clientePersonalRegular_ok() {
        BankAccountRequestDto req = new BankAccountRequestDto();
        req.setCustomerId("C2");
        req.setAccountType(AccountType.SAVINGS);
        req.setAccountNumber("ACC-2");
        req.setBalance(BigDecimal.ZERO);

        SavingsAccount entity = SavingsAccount.builder()
                .customerId("C2")
                .accountType(AccountType.SAVINGS)
                .accountNumber("ACC-2")
                .balance(BigDecimal.ZERO)
                .build();

        when(createAccountMapper.toEntity(req)).thenReturn(entity);

        CustomerResponseDto normal = new CustomerResponseDto();
        normal.setType(CustomerType.PERSONAL);
        normal.setSubType(CustomerSubType.REGULAR);
        when(customerClient.getCustomerById("C2")).thenReturn(Mono.just(normal));

        when(repository.findByCustomerId("C2")).thenReturn(Flux.empty());

        // validador no lanza
        doNothing().when(accountValidator).validateAccounts(eq(normal), anyList(), eq(entity));

        // para REGULAR no se requiere tarjeta

        when(repository.save(entity)).thenReturn(Mono.just(entity));

        BankAccountDto dto = new BankAccountDto();
        dto.setCustomerId("C2");
        dto.setAccountType(AccountType.SAVINGS);
        dto.setAccountNumber("ACC-2");
        when(listAccountMapper.toResponseDto(entity)).thenReturn(dto);

        StepVerifier.create(service.saveBankAccount(req))
                .expectNextMatches(r ->
                        "C2".equals(r.getCustomerId())
                                && r.getAccountType() == AccountType.SAVINGS
                                && "ACC-2".equals(r.getAccountNumber())
                )
                .verifyComplete();

        verify(repository).save(entity);
        verifyNoInteractions(creditCardClient);
    }
}