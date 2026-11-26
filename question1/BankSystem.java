import java.util.InputMismatchException;
import java.util.Scanner;


public class BankSystem {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String holder = "Unknown";

        try {
          
            System.out.print("Enter account holder's name: ");
            holder = in.nextLine().trim();
            if (holder.isEmpty()) {
                holder = "Unknown";
            }
            double opening = readOpeningBalance(in);

            BankAccount account = new BankAccount(holder, opening);
            System.out.printf("%nAccount opened for %s with MWK %,.2f%n", holder, opening);
            System.out.println("------------------------------------------------------------");

            
            Depositor depositor = new Depositor(account);         
            Thread withdrawer = new Thread(new Withdrawer(account), "Withdrawer"); 

            depositor.setPriority(Thread.NORM_PRIORITY + 2);   
            withdrawer.setPriority(Thread.NORM_PRIORITY - 2);  
            System.out.println("Depositor priority : " + depositor.getPriority());
            System.out.println("Withdrawer priority: " + withdrawer.getPriority());
            System.out.println("------------------------------------------------------------");

            depositor.start();
            withdrawer.start();

            
            try {
                depositor.join();
                withdrawer.join();
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted while waiting.");
            }

            System.out.println("------------------------------------------------------------");
            System.out.printf("FINAL BALANCE for %s: MWK %,.2f%n", holder, account.getBalance());

        } catch (InputMismatchException e) {
            System.out.println("Invalid input: a number was expected.");
        } finally {
           
            System.out.println("Thank you for banking with us, " + holder + ". Goodbye!");
            in.close();
        }
    }

    
    private static double readOpeningBalance(Scanner in) {
        while (true) {
            try {
                System.out.print("Enter opening balance (MWK): ");
                double value = in.nextDouble();
                in.nextLine();                       
                if (value < 0) {
                    System.out.println("Balance cannot be negative. Try again.");
                    continue;
                }
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input '" + in.nextLine() + "'. Please type a number, e.g. 25000.");
            }
        }
    }
}
