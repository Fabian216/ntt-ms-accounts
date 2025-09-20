package ntt.ntt_ms_accounts.business;

public class TransactionLimit {

    private static Integer savingsLimit = 30;
    private static Integer currentLimit = 0;
    private static Integer fixedTermLimit = 1;

    private TransactionLimit() {}

    public static Integer savings() {
        return savingsLimit;
    }

    public static Integer current() {
        return currentLimit;
    }

    public static Integer fixedTerm() {
        return fixedTermLimit;
    }

}
