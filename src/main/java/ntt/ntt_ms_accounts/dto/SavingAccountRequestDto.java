package ntt.ntt_ms_accounts.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class SavingAccountRequestDto extends BankAccountRequestDto{
    private BigDecimal requiredAvgDailyBalance;
}
