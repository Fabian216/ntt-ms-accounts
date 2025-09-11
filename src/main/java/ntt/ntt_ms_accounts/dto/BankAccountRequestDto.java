package ntt.ntt_ms_accounts.dto;

import lombok.Data;
import ntt.ntt_ms_accounts.enums.AccountType;

@Data
public class BankAccountRequestDto {
    private String customerId;
    private AccountType accountType; // "SAVINGS", "CURRENT", "FIXED_TERM"
    private String accountNumber;           // númeroCuenta
    private double balance;                 // saldo
}
