/**
 * User-defined CHECKED exception (extends Exception, not RuntimeException).
 * The compiler forces every caller of BankAccount.withdraw() to catch it
 * or to declare it with "throws".
 */
public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;
    private final double amount; // the amount that was requested

    public InsufficientFundsException(String message, double amount) {
        super(message);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
