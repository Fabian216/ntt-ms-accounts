package ntt.ntt_ms_accounts.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ntt.ntt_ms_accounts.client.CustomerClient;
import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.dto.PersonDTO;
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

                                applyTypeDefaults(entity, customer);

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
                );
    }

    private void applyTypeDefaults(BankAccount account, CustomerResponseDto customer) {
        switch (account.getAccountType()) {
            case SAVINGS:
                if (account.getMaintenanceFee() == null) {
                    account.setMaintenanceFee(MaintenanceFee.savings());
                }
                account.setMonthlyTransactions(TransactionLimit.savings());

                break;

            case CURRENT:
                if (account.getMaintenanceFee() == null) {
                    account.setMaintenanceFee(MaintenanceFee.current());
                }
                // PYME
                if (customer.getSubType() == CustomerSubType.PYME) {
                    account.setMaintenanceFee(BigDecimal.ZERO);
                }
                account.setMonthlyTransactions(TransactionLimit.current());
                break;

            case FIXED_TERM:
                if (account.getMaintenanceFee() == null) {
                    account.setMaintenanceFee(MaintenanceFee.fixedTerm());
                }

                account.setMonthlyTransactions(TransactionLimit.fixedTerm());
                account.setFixedDayAllowed(25); // día fijo por defecto

                break;

            default:
                throw new IllegalArgumentException("Tipo de cuenta desconocido: " + account.getAccountType());
        }

    }
}
