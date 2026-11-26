import java.util.InputMismatchException;
import java.util.Scanner;

/** Program 1: Multithreaded Bank Account System (main class). */
public class BankSystem {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String holder = "Unknown";

        try {
            // ---- keyboard input, protected by try / catch ----
            System.out.print("Enter account holder's name: ");
            holder = in.nextLine().trim();
            if (holder.isEmpty()) {
                holder = "Unknown";
            }
            double opening = readOpeningBalance(in);

            BankAccount account = new BankAccount(holder, opening);
            System.out.printf("%nAccount opened for %s with MWK %,.2f%n", holder, opening);
            System.out.println("------------------------------------------------------------");

            // ---- create the two threads ----
            Depositor depositor = new Depositor(account);          // extends Thread
            Thread withdrawer = new Thread(new Withdrawer(account), "Withdrawer"); // Runnable

            // different priorities (a hint to the scheduler, not a guarantee)
            depositor.setPriority(Thread.NORM_PRIORITY + 2);   // 7
            withdrawer.setPriority(Thread.NORM_PRIORITY - 2);  // 3
            System.out.println("Depositor priority : " + depositor.getPriority());
            System.out.println("Withdrawer priority: " + withdrawer.getPriority());
            System.out.println("------------------------------------------------------------");

            depositor.start();
            withdrawer.start();

            // ---- join(): main waits until BOTH threads have finished ----
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
            // finally ALWAYS runs: normal end, exception, or early return
            System.out.println("Thank you for banking with us, " + holder + ". Goodbye!");
            in.close();
        }
    }

    /** Keeps asking until a valid, non-negative number is typed. */
    private static double readOpeningBalance(Scanner in) {
        while (true) {
            try {
                System.out.print("Enter opening balance (MWK): ");
                double value = in.nextDouble();
                in.nextLine();                       // discard the rest of the line
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
