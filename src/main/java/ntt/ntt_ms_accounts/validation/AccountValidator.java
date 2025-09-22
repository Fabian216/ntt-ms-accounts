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


@Slf4j
@Component
public class AccountValidator {

    public void validateAccounts(CustomerResponseDto customerResponseDto,
                                 List<BankAccount> existingAccounts,
                                 BankAccount newAccount) {

        if (customerResponseDto.getType() == null) {
            throw new IllegalArgumentException("El tipo de cliente es requerido");
        }

        if (newAccount.getAccountType() == null) {
            throw new IllegalArgumentException("El tipo de cuenta es requerido");
        }

        if (newAccount.getCustomerId() == null || newAccount.getCustomerId().isBlank()) {
            throw new IllegalArgumentException("El id del cliente es requerido");
        }

        BigDecimal balance = newAccount.getBalance();

        if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }

        CustomerType customerType = customerResponseDto.getType();
        AccountType accountType = newAccount.getAccountType();

        if (newAccount.getAccountType() == AccountType.FIXED_TERM) {
            if (!(newAccount instanceof FixedTermAccount)) {
                throw new IllegalArgumentException("Se esperaba FixedTermAccount para FIXED_TERM");
            }
            FixedTermAccount f = (FixedTermAccount) newAccount;

            Integer day = f.getAllowedTransactionDay();
            if (day == null) {
                throw new IllegalArgumentException("allowedTransactionDay es obligatorio para cuentas a plazo fijo");
            }}

            switch (customerType) {
            case PERSONAL:
                //valida si existe cuenta ahorros
                if (accountType.equals(AccountType.SAVINGS) &&
                        existingAccounts.stream().anyMatch(a -> a.getAccountType().equals(AccountType.SAVINGS))) {
                    throw new IllegalArgumentException("El cliente personal ya tiene una cuenta de ahorro");
                }

                //Valida si existe cuenta corriente
                if (accountType.equals(AccountType.CURRENT) &&
                        existingAccounts.stream().anyMatch(x -> x.getAccountType().equals(AccountType.CURRENT))) {
                    throw new IllegalArgumentException("El cliente personal ya tiene una cuenta corriente");
                }

                if (customerResponseDto.getSubType() == CustomerSubType.VIP) {

                    if (newAccount instanceof SavingsAccount) {
                        SavingsAccount s = (SavingsAccount) newAccount;
                        BigDecimal req = s.getRequiredAvgDailyBalance();
                        if (req == null || req.compareTo(BigDecimal.ZERO) <= 0) {
                            throw new IllegalArgumentException("Para clientes VIP, el promedio diario requerido no puede ser cero");
                        }
                    }

                }

                break;

            case BUSINESS:

                validateBusinessOwnersAndSigners(customerResponseDto);

                //restringir creacion de cuenta ahorro
                if (accountType.equals(AccountType.SAVINGS)) {
                    throw new IllegalArgumentException("El cliente empresarial no puede tener cuentas de ahorro");
                }
                //restringir creacion de cuenta plazo fijo
                if (accountType.equals(AccountType.FIXED_TERM)) {
                    throw new IllegalArgumentException("El cliente empresarial no puede tener cuentas a plazo fijo");
                }

                break;

            default:
                throw new IllegalArgumentException("Tipo de cliente no soportado: " + customerResponseDto.getType());
        }

    }

    public void validateBusinessOwnersAndSigners(CustomerResponseDto customer) {

        if (customer.getHeadlines() == null || customer.getHeadlines().isEmpty()) {
            throw new IllegalArgumentException("Para clientes EMPRESARIALES, se requiere al menos un titular");
        }
        if (customer.getAuthorizedSigners() == null || customer.getAuthorizedSigners().isEmpty()) {
            throw new IllegalArgumentException("Para clientes EMPRESARIALES, debe de tener cero o más firmantes autorizados.");
        }

    }

}
