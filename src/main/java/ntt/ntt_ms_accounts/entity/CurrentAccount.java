package ntt.ntt_ms_accounts.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CurrentAccount extends BankAccount{
    //Cuenta Corriente
    //Con comisión de mantenimiento
}
