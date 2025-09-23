package ntt.ntt_ms_accounts.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import ntt.ntt_ms_accounts.enums.AccountType;

import java.math.BigDecimal;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "accountType",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SavingAccountRequestDto.class, name = "SAVINGS"),
        @JsonSubTypes.Type(value = FixedTermAccountRequestDto.class, name = "FIXED_TERM"),
        @JsonSubTypes.Type(value = CurrentAccountRequestDto.class, name = "CURRENT")

})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountRequestDto {
    private String customerId;
    private AccountType accountType; // "SAVINGS", "CURRENT", "FIXED_TERM"
    private String accountNumber;    // númeroCuenta
    private BigDecimal balance;      // saldo
    private BigDecimal maintenanceFee;     // comisionMantenimiento
    private Integer  transactionLimit;
}
