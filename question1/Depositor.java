/** Depositor: created by EXTENDING the Thread class. */
public class Depositor extends Thread {

    private final BankAccount account;
    private final double[] amounts = {50_000, 80_000, 30_000, 100_000, 60_000}; // 5 transactions

    public Depositor(BankAccount account) {
        super("Depositor");            // thread name
        this.account = account;
    }

    @Override
    public void run() {                // body of the thread
        try {
            for (double amount : amounts) {
                account.deposit(amount);
                Thread.sleep(300);     // short delay between transactions
            }
        } catch (InterruptedException e) {
            System.out.println("Depositor interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            account.depositsFinished(); // always tell the withdrawer we are done
        }
    }
}
