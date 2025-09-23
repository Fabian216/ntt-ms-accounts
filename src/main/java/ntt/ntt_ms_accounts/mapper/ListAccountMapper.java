package ntt.ntt_ms_accounts.mapper;

import ntt.ntt_ms_accounts.dto.BankAccountDto;
import ntt.ntt_ms_accounts.dto.CurrentAccountResponseDto;
import ntt.ntt_ms_accounts.dto.FixedTermAccountResponseDto;
import ntt.ntt_ms_accounts.dto.SavingsAccountResponseDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.entity.CurrentAccount;
import ntt.ntt_ms_accounts.entity.FixedTermAccount;
import ntt.ntt_ms_accounts.entity.SavingsAccount;
import org.springframework.stereotype.Component;
import ntt.ntt_ms_accounts.enums.AccountType;

@Component
public class ListAccountMapper {

    public BankAccountDto toResponseDto(BankAccount bankAccount) {
        if (bankAccount == null) {
            throw new IllegalArgumentException("bankAccount no puede ser null");
        }

        AccountType type = bankAccount.getAccountType();
        if (type == null) {
            return mapByInstanceOf(bankAccount);
        }

        switch (type) {
            case SAVINGS:
                if (!(bankAccount instanceof SavingsAccount)) {
                    throw new IllegalArgumentException("AccountType=SAVINGS pero instancia "
                            + bankAccount.getClass().getSimpleName());
                }
                return mapSavings((SavingsAccount) bankAccount);
            case CURRENT:
                if (!(bankAccount instanceof CurrentAccount)) {
                    throw new IllegalArgumentException("AccountType=CURRENT pero instancia "
                            + bankAccount.getClass().getSimpleName());
                }
                return mapCurrent((CurrentAccount) bankAccount);
            case FIXED_TERM:
                if (!(bankAccount instanceof FixedTermAccount)) {
                    throw new IllegalArgumentException("AccountType=FIXED_TERM pero instancia "
                            + bankAccount.getClass().getSimpleName());
                }
                return mapFixed((FixedTermAccount) bankAccount);
            default:
                throw new IllegalArgumentException("Tipo de cuenta no soportado: " + type);
        }
    }

    private BankAccountDto mapByInstanceOf(BankAccount bankAccount) {
        if (bankAccount instanceof SavingsAccount) {return mapSavings((SavingsAccount) bankAccount);}
        if (bankAccount instanceof CurrentAccount) {return mapCurrent((CurrentAccount) bankAccount);}
        if (bankAccount instanceof FixedTermAccount) {return mapFixed((FixedTermAccount) bankAccount);}
        throw new IllegalArgumentException("Instancia de cuenta no soportada: " + bankAccount.getClass().getSimpleName());
    }

    private SavingsAccountResponseDto mapSavings(SavingsAccount s) {
        return SavingsAccountResponseDto.builder()
                .id(s.getId())
                .customerId(s.getCustomerId())
                .accountType(s.getAccountType())
                .accountNumber(s.getAccountNumber())
                .balance(s.getBalance())
                .transactionLimit(s.getTransactionLimit())
                .maintenanceFee(s.getMaintenanceFee())
                .requiredAvgDailyBalance(s.getRequiredAvgDailyBalance())
                .build();
    }

    private CurrentAccountResponseDto mapCurrent(CurrentAccount c) {
        return CurrentAccountResponseDto.builder()
                .id(c.getId())
                .customerId(c.getCustomerId())
                .accountType(c.getAccountType())
                .accountNumber(c.getAccountNumber())
                .balance(c.getBalance())
                .transactionLimit(c.getTransactionLimit())
                .maintenanceFee(c.getMaintenanceFee())
                .build();
    }

    private FixedTermAccountResponseDto mapFixed(FixedTermAccount f) {
        return FixedTermAccountResponseDto.builder()
                .id(f.getId())
                .customerId(f.getCustomerId())
                .accountType(f.getAccountType())
                .accountNumber(f.getAccountNumber())
                .balance(f.getBalance())
                .transactionLimit(f.getTransactionLimit())
                .maintenanceFee(f.getMaintenanceFee())
                .allowedTransactionDay(f.getAllowedTransactionDay())
                .build();
    }
}
