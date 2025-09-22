package ntt.ntt_ms_accounts.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntt.ntt_ms_accounts.client.CreditCardClient;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.dto.PersonDTO;
import ntt.ntt_ms_accounts.entity.SavingsAccount;
import ntt.ntt_ms_accounts.enums.CustomerType;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.mapper.CreateAccountMapper;
import ntt.ntt_ms_accounts.mapper.ListAccountMapper;
import ntt.ntt_ms_accounts.repository.BankAccountRepository;
import ntt.ntt_ms_accounts.service.BankAccountService;
import ntt.ntt_ms_accounts.validation.AccountValidator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ntt.ntt_ms_accounts.dto.CustomerResponseDto;
import ntt.ntt_ms_accounts.business.TransactionLimit;
import ntt.ntt_ms_accounts.business.MaintenanceFee;
import ntt.ntt_ms_accounts.enums.CustomerSubType;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ntt.ntt_ms_accounts.entity.FixedTermAccount;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;


@Slf4j
@Service
@RequiredArgsConstructor
public class BankAccountServiceImpl implements BankAccountService {

    private final BankAccountRepository repository;
    private final CustomerClient customerClient;
    private final ListAccountMapper listAccountmapper;
    private final CreateAccountMapper createAccountMapper;
    private final AccountValidator accountValidator;
    private final CreditCardClient creditCardClient;

    public Flux<BankAccountDto> findAllAccounts() {
        return repository.findAll().map(listAccountmapper::toResponseDto);
    }

    public Mono<BankAccountDto> saveBankAccount(BankAccountRequestDto request) {

        BankAccount entity = createAccountMapper.toEntity(request);

        return customerClient.getCustomerById(entity.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado")))
                .flatMap(customer ->
                    requireCard(customer, entity.getCustomerId())
                    .then(
                    repository.findByCustomerId(entity.getCustomerId())
                            .collectList()
                            .flatMap(existingAccounts -> {
                                accountValidator.validateAccounts(customer, existingAccounts, entity);
                                applyTypeDefaults(entity,customer);
                                return repository.save(entity)
                                        .map(saved -> {
                                            BankAccountDto dto = listAccountmapper.toResponseDto(saved);
                                            if (customer.getType() == CustomerType.BUSINESS) {
                                            List<String> holderDocs = Optional.ofNullable(customer.getHeadlines())
                                                    .orElse(List.of())
                                                    .stream()
                                                    .map(PersonDTO::getDocumentNumber)
                                                    .collect(Collectors.toList());

                                            List<String> signerDocs = Optional.ofNullable(customer.getAuthorizedSigners())
                                                    .orElse(List.of())
                                                    .stream()
                                                    .map(PersonDTO::getDocumentNumber)
                                                    .collect(Collectors.toList());

                                            dto.setHolderDocuments(holderDocs);
                                            dto.setSignerDocuments(signerDocs);}else {
                                                dto.setHolderDocuments(Collections.emptyList());
                                                dto.setSignerDocuments(Collections.emptyList());
                                            }

                                            return dto;
                                        });
                            })
                    )
                );
    }

    private void applyTypeDefaults(BankAccount account, CustomerResponseDto customer) {

        if (account.getBalance()==null){
            account.setBalance(BigDecimal.ZERO);
        }

        switch (account.getAccountType()) {
            case SAVINGS:
                account.setMaintenanceFee(MaintenanceFee.savings());
                account.setMonthlyTransactions(TransactionLimit.savings());

                if (customer.getSubType()== CustomerSubType.VIP && account instanceof SavingsAccount){
                    SavingsAccount s = (SavingsAccount) account;
                    if (s.getRequiredAvgDailyBalance() == null) {
                        s.setRequiredAvgDailyBalance(BigDecimal.ZERO);
                    }
                }
                break;

            case CURRENT:
                account.setMaintenanceFee(MaintenanceFee.current());
                account.setMonthlyTransactions(TransactionLimit.current());

                // PYME
                if (customer.getSubType() == CustomerSubType.PYME) {
                    account.setMaintenanceFee(BigDecimal.ZERO);
                }
                break;

            case FIXED_TERM:
                account.setMaintenanceFee(MaintenanceFee.fixedTerm());
                account.setTransactionLimit(TransactionLimit.fixedTerm());
                account.setMonthlyTransactions(TransactionLimit.fixedTerm());

                if (account instanceof FixedTermAccount) {
                    FixedTermAccount f = (FixedTermAccount) account;
                    if (f.getAllowedTransactionDay() == null) {
                        f.setAllowedTransactionDay(25);
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("Tipo de cuenta desconocido: " + account.getAccountType());
        }

    }

    private Mono<Void> requireCard(CustomerResponseDto customer, String customerId) {
        if (customer.getSubType() != CustomerSubType.VIP && customer.getSubType() != CustomerSubType.PYME) {
            return Mono.empty();
        }
        return creditCardClient.getCreditCardByCustomerId(customerId)
                // 404 -> no existe tarjeta para ese id
                .onErrorResume(WebClientResponseException.NotFound.class, ex ->
                        Mono.error(new IllegalArgumentException(
                                "No existe tarjeta de credito para el cliente (404)."
                        ))
                )
                // 503 -> gateway o upstream no enrutable/no disponible
                .onErrorResume(WebClientResponseException.ServiceUnavailable.class, ex ->
                        Mono.error(new IllegalStateException(
                                "Servicio de tarjetas no disponible o sin instancias (503)."
                        ))
                )
                // problemas de red/URL/DNS (ni siquiera llega a HTTP)
                .onErrorResume(WebClientRequestException.class, ex ->
                        Mono.error(new IllegalStateException(
                                "No se puede contactar al servicio (URL/RED)."
                        ))
                )

                .switchIfEmpty(Mono.error(new IllegalArgumentException(
                        "No existe tarjeta de credito para el cliente."
                )))
                .then();
    }
}
