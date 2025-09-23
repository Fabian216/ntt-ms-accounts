package ntt.ntt_ms_accounts.service;

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
        return repository.findAll()
                .map(listAccountmapper::toResponseDto)
                .doOnError(e -> log.error("Error listando cuentas: {}", e.getMessage(), e));
    }

    public Mono<BankAccountDto> saveBankAccount(BankAccountRequestDto request) {
        log.info("Creating account with number {}", request.getAccountNumber());
        log.debug("Account details: {}", request);

        final BankAccount newAccount;

        try {
            newAccount = createAccountMapper.toEntity(request);
        } catch (Exception ex) {
            log.warn("Fallo al mapear request a entidad: {}", ex.getMessage(), ex);
            return Mono.error(ex);
        }

        return customerClient.getCustomerById(newAccount.getCustomerId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado")))
                .flatMap(customer ->
                        repository.findByCustomerId(newAccount.getCustomerId())
                                .collectList()
                                .flatMap(existing -> {
                                    // delegar todas las validaciones de negocio
                                    accountValidator.validateAccounts(customer, existing, newAccount);

                                    // aplicar defaults solo si es null
                                    applyTypeDefaults(newAccount, customer);

                                    // requerir tarjeta si corresponde (VIP/PYME)
                                    return requireCard(customer, newAccount.getCustomerId())
                                            .then(Mono.defer(() -> repository.save(newAccount)))
                                            .map(saved -> enrichAndMap(saved, customer));
                                })
                )
                .doOnError(e -> log.error("Error creando cuenta {}: {}", newAccount.getAccountNumber(), e.getMessage(), e));
    }

    public Mono<BankAccountDto> updateBankAccount(String id, BankAccountRequestDto request) {
        if (id == null || id.isBlank()){ return Mono.error(new IllegalArgumentException("id es requerido"));}
        if (request == null) {return Mono.error(new IllegalArgumentException("request no puede ser null"));}

        return repository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cuenta no encontrada: " + id)))
                .flatMap(existing -> {
                    // mapear cambios sobre la entidad existente
                    try {
                        createAccountMapper.updateEntity(existing, request);
                    } catch (Exception ex) {
                        log.warn("Fallo al actualizar entidad {}: {}", id, ex.getMessage(), ex);
                        return Mono.error(ex);
                    }

                    // cargar cliente y cuentas existentes del mismo cliente
                    return customerClient.getCustomerById(existing.getCustomerId())
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cliente no encontrado")))
                            .flatMap(customer ->
                                    repository.findByCustomerId(existing.getCustomerId())
                                            .filter(acc -> !id.equals(acc.getId()))
                                            .collectList()
                                            .flatMap(others -> {
                                                // validar negocio sobre la entidad ya mutada
                                                accountValidator.validateAccounts(customer, others, existing);

                                                // aplicar defaults solo si hay nulls
                                                applyTypeDefaults(existing, customer);

                                                return repository.save(existing)
                                                        .map(saved -> enrichAndMap(saved, customer));
                                            })
                            );
                })
                .doOnError(e -> log.error("Error actualizando cuenta {}: {}", id, e.getMessage(), e));
    }

    public Mono<Void> deleteBankAccount(String id) {
        if (id == null || id.isBlank()) { return Mono.error(new IllegalArgumentException("id es requerido"));}
        log.info("Delete account with Id {}", id);
        return repository.deleteById(id);
    }


    private void applyTypeDefaults(BankAccount account, CustomerResponseDto customer) {

        if (account.getBalance()==null){
            account.setBalance(BigDecimal.ZERO);
        }

        switch (account.getAccountType()) {
            case SAVINGS:
                if (account.getMaintenanceFee() == null)
                {account.setMaintenanceFee(MaintenanceFee.savings());}
                if (account.getTransactionLimit() == null)
                {account.setTransactionLimit(TransactionLimit.savings());}
                if (customer.getSubType() == CustomerSubType.VIP && account instanceof SavingsAccount) {
                    SavingsAccount s = (SavingsAccount) account;
                    if
                        (s.getRequiredAvgDailyBalance() == null){
                        s.setRequiredAvgDailyBalance(BigDecimal.ZERO);}
                }
                break;

            case CURRENT:
                if (account.getMaintenanceFee() == null)
                {account.setMaintenanceFee(MaintenanceFee.current());}
                if (account.getTransactionLimit() == null)
                {account.setTransactionLimit(TransactionLimit.current());}
                if (customer.getSubType() == CustomerSubType.PYME)
                {account.setMaintenanceFee(BigDecimal.ZERO);}
                break;

            case FIXED_TERM:
                if (account.getMaintenanceFee() == null)
                {account.setMaintenanceFee(MaintenanceFee.fixedTerm());}
                if (account.getTransactionLimit() == null)
                {account.setTransactionLimit(TransactionLimit.fixedTerm());}
                if (account instanceof FixedTermAccount) {
                    FixedTermAccount f = (FixedTermAccount) account;
                    if (f.getAllowedTransactionDay() == null)
                    {f.setAllowedTransactionDay(25);}
                }
                break;

            default:
                throw new IllegalArgumentException("Tipo de cuenta desconocido: " + account.getAccountType());
        }

    }

    private Mono<Void> requireCard(CustomerResponseDto customer, String customerId) {
        if (customer.getSubType() != CustomerSubType.VIP && customer.getSubType() != CustomerSubType.PYME) {
            return Mono.empty();}
            return Mono.defer(() -> {
                Mono<?> mono = creditCardClient.getCreditCardByCustomerId(customerId);
                if (mono == null) {
                    return Mono.error(new IllegalStateException("CreditCardClient retorno null (se esperaba Mono)"));
                }
                return mono
                        .onErrorResume(WebClientResponseException.NotFound.class,
                                ex -> Mono.error(new IllegalArgumentException("No existe tarjeta de credito para el cliente (404).")))
                        .onErrorResume(WebClientResponseException.ServiceUnavailable.class,
                                ex -> Mono.error(new IllegalStateException("Servicio de tarjetas no disponible (503).")))
                        .onErrorResume(WebClientRequestException.class,
                                ex -> Mono.error(new IllegalStateException("No se puede contactar al servicio (URL/RED).")))
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("No existe tarjeta de credito para el cliente.")))
                        .then();
            });
    }

    private BankAccountDto enrichAndMap(BankAccount saved, CustomerResponseDto customer) {
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
            dto.setSignerDocuments(signerDocs);
        } else {
            dto.setHolderDocuments(Collections.emptyList());
            dto.setSignerDocuments(Collections.emptyList());
        }
        return dto;
    }
}
