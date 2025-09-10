package ntt.ntt_ms_accounts.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntt.ntt_ms_accounts.enums.AccountType;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "accountType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SavingsAccount.class, name = "SAVINGS"),
        @JsonSubTypes.Type(value = CurrentAccount.class, name = "CURRENT"),
        @JsonSubTypes.Type(value = FixedTermAccount.class, name = "FIXED_TERM")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "accounts")
public class BankAccount {//cuenta bancaria
    private String id;
    // Relación con el cliente
    private String customerId;
    // Tipo de cuenta
    private AccountType accountType; // "SAVINGS", "CURRENT", "FIXED_TERM"
    private String accountNumber;           // númeroCuenta
    private double balance;                 // saldo
    private double maintenanceFee;          // comisionMantenimiento
    private int monthlyTransactions;        // movimientosRealizados
}
