package ntt.ntt_ms_accounts.business;

public class TransactionLimit {

    private static Integer savingsLimit = 30;

    private TransactionLimit() {}

    public static Integer savings() {
        return savingsLimit;
    }

}
