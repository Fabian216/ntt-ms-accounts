package ntt.ntt_ms_accounts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SavingsAccountResponseDto extends BankAccountDto{
    private final int TRANSACTION_LIMIT = 30;
}
