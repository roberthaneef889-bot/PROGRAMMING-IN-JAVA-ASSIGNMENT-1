import java.applet.Applet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/**
 * BankApplet: reads the holder's name and the bank's name from <param> tags.
 * NOTE: applets are removed/unsupported in modern Java. Compile and run with
 * JDK 8:   javac BankApplet.java   then   appletviewer BankApplet.html
 */
public class BankApplet extends Applet {

    private String holder;
    private String bank;

    /** Called once when the applet is loaded: read parameters, set up the UI. */
    @Override
    public void init() {
        holder = getParameter("holder");
        bank = getParameter("bank");
        if (holder == null) holder = "Unknown holder";   // parameter missing
        if (bank == null)   bank = "Unknown bank";
        setBackground(Color.WHITE);
        System.out.println("init(): applet loaded");
    }

    /** Called after init() and every time the page becomes visible again. */
    @Override
    public void start() {
        System.out.println("start(): applet running");
    }

    /** Called when the page is hidden or the browser leaves it. */
    @Override
    public void stop() {
        System.out.println("stop(): applet paused");
    }

    /** Called once when the applet is unloaded: release resources. */
    @Override
    public void destroy() {
        System.out.println("destroy(): applet removed");
    }

    /** Draws the text on the applet's surface. */
    @Override
    public void paint(Graphics g) {
        g.setColor(Color.BLUE);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Bank: " + bank, 20, 40);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.drawString("Account holder: " + holder, 20, 80);
    }
}
