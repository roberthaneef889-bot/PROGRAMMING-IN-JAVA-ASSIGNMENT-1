
public class Withdrawer implements Runnable {

    private final BankAccount account;
    
    private final double[] amounts = {40_000, 70_000, 25_000, 90_000, 600_000, 60_000};

    public Withdrawer(BankAccount account) {
        this.account = account;
    }

    @Override
    public void run() {
        for (double amount : amounts) {
            try {
                account.withdraw(amount);          
                Thread.sleep(200);                 
            } catch (InsufficientFundsException e) {
                System.out.println("[REJECTED ] " + e.getMessage());
            } catch (InterruptedException e) {
                System.out.println("Withdrawer interrupted.");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
