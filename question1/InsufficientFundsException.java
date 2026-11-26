
public class InsufficientFundsException extends Exception {

    private static final long serialVersionUID = 1L;
    private final double amount; 

    public InsufficientFundsException(String message, double amount) {
        super(message);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }
}
