
public class BankAccount {

   
    public static final double WITHDRAWAL_LIMIT = 500_000;

    private final String holder;
    private double balance;
    private boolean depositsFinished = false; // set when the depositor is done

    public BankAccount(String holder, double openingBalance) {
        this.holder = holder;
        this.balance = openingBalance;
    }

    public String getHolder() {
        return holder;
    }

    public synchronized double getBalance() {
        return balance;
    }

   
    public synchronized void deposit(double amount) {
        balance += amount;
        System.out.printf("[DEPOSIT ] %-10s MWK %,12.2f | balance: MWK %,12.2f%n",
                Thread.currentThread().getName(), amount, balance);
        notifyAll(); 
    }

   
    public synchronized void withdraw(double amount)
            throws InsufficientFundsException, InterruptedException {

       
        if (amount > WITHDRAWAL_LIMIT) {
            throw new InsufficientFundsException(
                    String.format("Withdrawal of MWK %,.2f exceeds the limit of MWK %,.2f",
                            amount, WITHDRAWAL_LIMIT), amount);
        }

       
        while (balance < amount) {
            if (depositsFinished) {
                throw new InsufficientFundsException(
                        String.format("Balance MWK %,.2f is too low for MWK %,.2f and no more deposits are coming",
                                balance, amount), amount);
            }
            System.out.printf("[WAITING ] %-10s needs MWK %,12.2f but balance is MWK %,12.2f -> wait()%n",
                    Thread.currentThread().getName(), amount, balance);
            wait();
        }

        balance -= amount;
        System.out.printf("[WITHDRAW] %-10s MWK %,12.2f | balance: MWK %,12.2f%n",
                Thread.currentThread().getName(), amount, balance);
    }

    /** Called by the depositor when it has finished, so the withdrawer never waits forever. */
    public synchronized void depositsFinished() {
        depositsFinished = true;
        notifyAll();
    }
}
