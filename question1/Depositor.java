
public class Depositor extends Thread {

    private final BankAccount account;
    private final double[] amounts = {50_000, 80_000, 30_000, 100_000, 60_000}; 

    public Depositor(BankAccount account) {
        super("Depositor");            
        this.account = account;
    }

    @Override
    public void run() {              
        try {
            for (double amount : amounts) {
                account.deposit(amount);
                Thread.sleep(300);     
            }
        } catch (InterruptedException e) {
            System.out.println("Depositor interrupted.");
            Thread.currentThread().interrupt();
        } finally {
            account.depositsFinished(); 
        }
    }
}
