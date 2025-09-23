package ntt.ntt_ms_accounts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import ntt.ntt_ms_accounts.enums.AccountType;

import java.math.BigDecimal;
import java.util.List;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "accountType",
        visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = SavingsAccountResponseDto.class, name = "SAVINGS"),
    @JsonSubTypes.Type(value = CurrentAccountResponseDto.class, name = "CURRENT"),
    @JsonSubTypes.Type(value = FixedTermAccountResponseDto.class, name = "FIXED_TERM")
})

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class BankAccountDto {
    private String id;
    // Relación con el cliente
    private String customerId;
    // Tipo de cuenta
    private AccountType accountType;        // "SAVINGS", "CURRENT", "FIXED_TERM"
    private String accountNumber;           // númeroCuenta
    private BigDecimal balance;             // saldo
    private BigDecimal maintenanceFee;      // comisionMantenimiento
    private Integer  transactionLimit;           //Limite de transacciones
    private List<String> holderDocuments;
    private List<String> signerDocuments;

}
