package ntt.ntt_ms_accounts.mapper;

import ntt.ntt_ms_accounts.dto.BankAccountRequestDto;
import ntt.ntt_ms_accounts.entity.BankAccount;
import ntt.ntt_ms_accounts.entity.CurrentAccount;
import ntt.ntt_ms_accounts.entity.FixedTermAccount;
import ntt.ntt_ms_accounts.entity.SavingsAccount;
import org.springframework.stereotype.Component;

@Component
public class CreateAccountMapper {

    public BankAccount toEntity(BankAccountRequestDto request) {

        switch (request.getAccountType()) {
            case SAVINGS:
                return SavingsAccount.builder()
                        .customerId(request.getCustomerId())
                        .accountType(request.getAccountType())
                        .accountNumber(request.getAccountNumber())
                        .balance(request.getBalance())
                        .build();
            case CURRENT:
                return CurrentAccount.builder()
                        .customerId(request.getCustomerId())
                        .accountType(request.getAccountType())
                        .accountNumber(request.getAccountNumber())
                        .balance(request.getBalance())
                        .build();
            case FIXED_TERM:
                return FixedTermAccount.builder()
                        .customerId(request.getCustomerId())
                        .accountType(request.getAccountType())
                        .accountNumber(request.getAccountNumber())
                        .balance(request.getBalance())
                        .fixedDayAllowed(request.getFixedDayAllowed())
                        .build();
            default:
                throw new IllegalArgumentException("Tipo de cuenta no soportado: " + request.getAccountType());
        }
    }

}
