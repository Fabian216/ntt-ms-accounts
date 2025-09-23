package ntt.ntt_ms_accounts.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FixedTermAccountRequestDto extends BankAccountRequestDto{
    private Integer  allowedTransactionDay;

}
