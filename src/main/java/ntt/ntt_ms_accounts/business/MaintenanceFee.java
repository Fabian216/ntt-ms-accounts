package ntt.ntt_ms_accounts.business;

import java.math.BigDecimal;

public class MaintenanceFee {

    public static BigDecimal savings(){
        return new BigDecimal("0.00");
    }

    public static BigDecimal current() {
        return new BigDecimal("8.00");
    }

    public static BigDecimal fixedTerm() {
        return new BigDecimal("0.00");
    }

}
