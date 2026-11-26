/**
 * A bank account shared by two threads.
 * All methods that touch the balance are synchronized, so only one thread
 * can be inside them at a time (mutual exclusion on the account object).
 * wait()/notifyAll() provide inter-thread communication.
 */
public class BankAccount {

    /** Largest single withdrawal this account will ever allow (MWK). */
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

    /** Add money, then wake up any thread waiting for the balance to grow. */
    public synchronized void deposit(double amount) {
        balance += amount;
        System.out.printf("[DEPOSIT ] %-10s MWK %,12.2f | balance: MWK %,12.2f%n",
                Thread.currentThread().getName(), amount, balance);
        notifyAll(); // inter-thread communication: tell the withdrawer
    }

    /**
     * Take money out.
     * @throws InsufficientFundsException if the amount is above the account limit,
     *         or if the balance is too low and no more deposits are coming.
     * @throws InterruptedException       if the waiting thread is interrupted.
     */
    public synchronized void withdraw(double amount)
            throws InsufficientFundsException, InterruptedException {

        // 1. A request above the limit can never be served, so reject it at once.
        if (amount > WITHDRAWAL_LIMIT) {
            throw new InsufficientFundsException(
                    String.format("Withdrawal of MWK %,.2f exceeds the limit of MWK %,.2f",
                            amount, WITHDRAWAL_LIMIT), amount);
        }

        // 2. Balance too low: wait() releases the lock and sleeps until notified.
        //    A while-loop (not "if") is used because a thread can wake up and find
        //    the condition still false (spurious wake-up / another thread got in first).
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
