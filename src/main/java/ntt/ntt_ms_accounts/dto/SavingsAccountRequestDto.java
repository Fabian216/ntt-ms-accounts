package ntt.ntt_ms_accounts.dto;

import lombok.Data;
import ntt.ntt_ms_accounts.enums.AccountType;

@Data
public class SavingsAccountRequestDto {
    private String id;
    private String customerId;
    private AccountType accountType; // "SAVINGS", "CURRENT", "FIXED_TERM"
    private String accountNumber;           // númeroCuenta
    private double balance;                 // saldo
    private double maintenanceFee;          // comisionMantenimiento
    private int monthlyTransactions;        // movimientosRealizados
    private final int TRANSACTION_LIMIT = 30;  // LIMITE_MOVIMIENTOS
}
