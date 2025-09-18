package ntt.ntt_ms_accounts.validation;

import ntt.ntt_ms_accounts.dto.CustomerResponseDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.enums.AccountType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountValidator {

    public void validateAccounts(CustomerResponseDto customerResponseDto,
                                 List<BankAccount> existingAccounts,
                                 BankAccount newAccount) {

        switch (customerResponseDto.getType()) {
            case PERSONAL:
                //valida si existe cuenta ahorros
                if (newAccount.getAccountType().equals(AccountType.SAVINGS) &&
                        existingAccounts.stream().anyMatch(a -> a.getAccountType().equals(AccountType.SAVINGS))) {
                    throw new IllegalArgumentException("El cliente personal ya tiene una cuenta de ahorro");
                }
                //Valida si existe cuenta corriente
                if (newAccount.getAccountType().equals(AccountType.CURRENT) &&
                        existingAccounts.stream().anyMatch(x -> x.getAccountType().equals(AccountType.CURRENT))) {
                    throw new IllegalArgumentException("El cliente personal ya tiene una cuenta corriente");
                }
                break;

            case BUSINESS:
                //restringir creacion de cuenta ahorro
                if (newAccount.getAccountType().equals(AccountType.SAVINGS)) {
                    throw new IllegalArgumentException("El cliente empresarial no puede tener cuentas de ahorro");
                }
                //restringir creacion de cuenta corriente
                if (newAccount.getAccountType().equals(AccountType.FIXED_TERM)) {
                    throw new IllegalArgumentException("El cliente empresarial no puede tener cuentas a plazo fijo");
                }
                break;
            default:
                throw new IllegalArgumentException("Tipo de cliente no soportado: " + customerResponseDto.getType());
        }
    }

}
