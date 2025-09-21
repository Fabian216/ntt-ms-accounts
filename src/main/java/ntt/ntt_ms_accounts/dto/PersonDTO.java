package ntt.ntt_ms_accounts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonDTO {
    private String fullName;
    private String documentType;
    private String documentNumber;
}
