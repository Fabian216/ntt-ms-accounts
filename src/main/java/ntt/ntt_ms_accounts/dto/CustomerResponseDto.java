package ntt.ntt_ms_accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntt.ntt_ms_accounts.enums.CustomerType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {
    private String id;
    private CustomerType type;
}
