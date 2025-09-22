package ntt.ntt_ms_accounts.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SavingsAccount extends BankAccount{
    //Cuenta Ahorro
    private BigDecimal requiredAvgDailyBalance;
    // sin comisión
}
