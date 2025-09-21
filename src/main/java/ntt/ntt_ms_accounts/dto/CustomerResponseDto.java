package ntt.ntt_ms_accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ntt.ntt_ms_accounts.enums.CustomerType;
import ntt.ntt_ms_accounts.enums.CustomerSubType;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDto {
    private String id;
    private CustomerType type;
    private CustomerSubType subType;
    private List<PersonDTO> headlines = new ArrayList<>();
    private List<PersonDTO> authorizedSigners = new ArrayList<>();
}
