package ntt.ntt_ms_accounts.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FixedTermAccount extends BankAccount{
    //CuentaPlazoFijo
    private int allowedTransactionDay;        // diaPermitidoMovimiento
    // sin comisión
}
