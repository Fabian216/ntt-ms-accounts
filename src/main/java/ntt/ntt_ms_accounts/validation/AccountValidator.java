package ntt.ntt_ms_accounts.validation;

import ntt.ntt_ms_accounts.dto.CustomerResponseDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.entity.FixedTermAccount;
import ntt.ntt_ms_accounts.entity.SavingsAccount;
import ntt.ntt_ms_accounts.enums.AccountType;
import ntt.ntt_ms_accounts.enums.CustomerType;
import ntt.ntt_ms_accounts.enums.CustomerSubType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import java.util.Collections;


@Slf4j
@Component
public class AccountValidator {

    public void validateAccounts(CustomerResponseDto customerResponseDto,
                                 List<BankAccount> existingAccounts,
                                 BankAccount newAccount) {

        // null-safety
        if (customerResponseDto == null) {
            throw new IllegalArgumentException("customerResponseDto es requerido");
        }
        if (newAccount == null) {
            throw new IllegalArgumentException("newAccount es requerido");
        }
        if (customerResponseDto.getType() == null) {
            throw new IllegalArgumentException("El tipo de cliente es requerido");
        }
        if (newAccount.getAccountType() == null) {
            throw new IllegalArgumentException("El tipo de cuenta es requerido");
        }
        if (newAccount.getCustomerId() == null || newAccount.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("El id del cliente es requerido");
        }

        List<BankAccount> accounts = existingAccounts == null ? Collections.emptyList() : existingAccounts;

        // montos no negativos
        BigDecimal balance = newAccount.getBalance();
        if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
        BigDecimal fee = newAccount.getMaintenanceFee();
        if (fee != null && fee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La comision de mantenimiento no puede ser negativa");
        }
        Integer limit = newAccount.getTransactionLimit();
        if (limit != null && limit < 0) {
            throw new IllegalArgumentException("El limite de transacciones no puede ser negativo");
        }

        CustomerType customerType = customerResponseDto.getType();
        AccountType accountType = newAccount.getAccountType();

        // reglas especificas por tipo de cuenta
        if (accountType == AccountType.FIXED_TERM) {
            if (!(newAccount instanceof FixedTermAccount)) {
                throw new IllegalArgumentException("Se esperaba FixedTermAccount para FIXED_TERM");
            }
            FixedTermAccount f = (FixedTermAccount) newAccount;
            Integer day = f.getAllowedTransactionDay();
            if (day == null) {
                throw new IllegalArgumentException("allowedTransactionDay es obligatorio para cuentas a plazo fijo");
            }
            if (day < 1 || day > 31) {
                throw new IllegalArgumentException("allowedTransactionDay debe estar entre 1 y 31");
            }
        }

        // reglas por tipo de cliente
        switch (customerType) {
            case PERSONAL:
                // unica cuenta de ahorros
                if (accountType == AccountType.SAVINGS &&
                        accounts.stream().anyMatch(a -> a.getAccountType() == AccountType.SAVINGS)) {
                    throw new IllegalArgumentException("El cliente personal ya tiene una cuenta de ahorro");
                }
                // unica cuenta corriente
                if (accountType == AccountType.CURRENT &&
                        accounts.stream().anyMatch(a -> a.getAccountType() == AccountType.CURRENT)) {
                    throw new IllegalArgumentException("El cliente personal ya tiene una cuenta corriente");
                }

                // VIP requiere promedio diario > 0 si es savings
                if (customerResponseDto.getSubType() == CustomerSubType.VIP && newAccount instanceof SavingsAccount) {
                    SavingsAccount s = (SavingsAccount) newAccount;
                    BigDecimal req = s.getRequiredAvgDailyBalance();
                    if (req == null || req.compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Para VIP, el saldo promedio diario requerido debe ser mayor a 0");
                    }
                }
                break;

            case BUSINESS:
                validateBusinessOwnersAndSigners(customerResponseDto);

                // restricciones de tipos para empresarial
                if (accountType == AccountType.SAVINGS) {
                    throw new IllegalArgumentException("El cliente empresarial no puede tener cuentas de ahorro");
                }
                if (accountType == AccountType.FIXED_TERM) {
                    throw new IllegalArgumentException("El cliente empresarial no puede tener cuentas a plazo fijo");
                }

                break;

            default:
                throw new IllegalArgumentException("Tipo de cliente no soportado: " + customerType);
        }

        log.debug("Validacion de cuentas OK: customerId={}, type={}, accountType={}",
                newAccount.getCustomerId(), customerType, accountType);
    }

    public void validateBusinessOwnersAndSigners(CustomerResponseDto customer) {
        // titulares requeridos (>= 1)
        if (customer.getHeadlines() == null || customer.getHeadlines().isEmpty()) {
            throw new IllegalArgumentException("Para clientes EMPRESARIALES, se requiere al menos un titular");
        }

        // firmantes autorizados:
        if (customer.getAuthorizedSigners() == null) {
            throw new IllegalArgumentException("Para clientes EMPRESARIALES, la lista de firmantes no debe ser null");
        }

        // if (customer.getAuthorizedSigners().isEmpty()) {
        //     throw new IllegalArgumentException("Para clientes EMPRESARIALES, se requiere al menos un firmante autorizado");
        // }
    }
}