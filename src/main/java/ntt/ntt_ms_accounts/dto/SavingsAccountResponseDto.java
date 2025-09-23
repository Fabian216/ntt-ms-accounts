package ntt.ntt_ms_accounts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SavingsAccountResponseDto extends BankAccountDto{
    private BigDecimal requiredAvgDailyBalance;
}
