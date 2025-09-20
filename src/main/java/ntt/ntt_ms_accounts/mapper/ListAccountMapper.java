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

@Component
public class ListAccountMapper {

    public BankAccountDto toResponseDto(BankAccount bankAccount) {
        if (bankAccount instanceof SavingsAccount) {
            SavingsAccount s = (SavingsAccount) bankAccount;
            return SavingsAccountResponseDto.builder()
                    .id(s.getId())
                    .customerId(s.getCustomerId())
                    .accountType(s.getAccountType())
                    .accountNumber(s.getAccountNumber())
                    .balance(s.getBalance())
                    .maintenanceFee(s.getMaintenanceFee())
                    .transactionLimit(s.getTransactionLimit())
                    .monthlyTransactions(s.getMonthlyTransactions())
                    .build();
        } else if (bankAccount instanceof CurrentAccount) {
            CurrentAccount c = (CurrentAccount) bankAccount;
            return CurrentAccountResponseDto.builder()
                    .id(c.getId())
                    .customerId(c.getCustomerId())
                    .accountType(c.getAccountType())
                    .accountNumber(c.getAccountNumber())
                    .balance(c.getBalance())
                    .maintenanceFee(c.getMaintenanceFee())
                    .transactionLimit(c.getTransactionLimit())
                    .monthlyTransactions(c.getMonthlyTransactions())
                    .build();
        } else if (bankAccount instanceof FixedTermAccount) {
            FixedTermAccount f = (FixedTermAccount) bankAccount;
            return FixedTermAccountResponseDto.builder()
                    .id(f.getId())
                    .customerId(f.getCustomerId())
                    .accountType(f.getAccountType())
                    .accountNumber(f.getAccountNumber())
                    .balance(f.getBalance())
                    .maintenanceFee(f.getMaintenanceFee())
                    .transactionLimit(f.getTransactionLimit())
                    .monthlyTransactions(f.getMonthlyTransactions())
                    .allowedTransactionDay(f.getAllowedTransactionDay())
                    .build();
        }
        return null;
    }

}
