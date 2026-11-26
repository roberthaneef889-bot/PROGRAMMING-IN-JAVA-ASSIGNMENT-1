import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;


public class StudentManager extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    private static final String TEXT_FILE = "students.txt";
    private static final String BACKUP_FILE = "students.bak";

    
    private final Vector<StudentRecord> records = new Vector<>();   
    private final Stack<StudentRecord> undoStack = new Stack<>();   
    private final Random random = new Random();                      

  
    private final JTextField nameField = new JTextField(15);
    private final JTextField courseField = new JTextField(15);
    private final JTextField markField = new JTextField(15);
    private final JTextArea outputArea = new JTextArea(12, 55);
    private final JLabel statusLabel = new JLabel("Ready.");

    public StudentManager() {
        super("Student Records Manager");
        buildGui();

        
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int choice = JOptionPane.showConfirmDialog(StudentManager.this,
                        "Close the application? Unsaved records will be lost.",
                        "Exit", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });
    }

    private void buildGui() {
        JPanel fields = new JPanel(new GridLayout(3, 2, 5, 5));
        fields.add(new JLabel("Student name:"));  fields.add(nameField);
        fields.add(new JLabel("Course:"));        fields.add(courseField);
        fields.add(new JLabel("Mark (0-100):"));  fields.add(markField);

        JPanel buttons = new JPanel(new FlowLayout());
        for (String label : new String[] {"Add", "Save", "Load", "Display", "Undo"}) {
            JButton button = new JButton(label);
            button.setActionCommand(label);
            button.addActionListener(this);          // every button -> this ActionListener
            buttons.add(button);
        }

        JPanel top = new JPanel(new BorderLayout());
        top.add(fields, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.SOUTH);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    // ================= EVENT HANDLING =================
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "Add":     addRecord();     break;
            case "Save":    saveRecords();   break;
            case "Load":    loadRecords();   break;
            case "Display": displayRecords(); break;
            case "Undo":    undoLast();      break;
            default:        break;
        }
    }

   
    private void addRecord() {
        
        String name = nameField.getText().trim().replace(',', ' ');
        String course = courseField.getText().trim().replace(',', ' ');
        String markText = markField.getText().trim();

        if (name.isEmpty() || course.isEmpty() || markText.isEmpty()) {
            showError("Please fill in the name, course and mark.");
            return;
        }
        double mark;
        try {
            mark = Double.parseDouble(markText);
        } catch (NumberFormatException ex) {
            showError("Invalid mark '" + markText + "'. Please type a number such as 72.5.");
            return;
        }
        if (mark < 0 || mark > 100) {
            showError("Mark must be between 0 and 100.");
            return;
        }

        StudentRecord record = new StudentRecord(generateUniqueId(), name, course, mark);
        records.add(record);            
        undoStack.push(record);        
        nameField.setText("");
        courseField.setText("");
        markField.setText("");
        status("Added " + record.getId() + " (" + record.getName() + ").");
    }

    /** Random number -> ID such as S4821; repeat until it is not already used. */
    private String generateUniqueId() {
        String id;
        do {
            id = "S" + (1000 + random.nextInt(9000));    
        } while (idExists(id));
        return id;
    }

    private boolean idExists(String id) {
        for (StudentRecord r : records) {
            if (r.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

  
    private void undoLast() {
        if (undoStack.empty()) {
            showError("Nothing to undo.");
            return;
        }
        StudentRecord last = undoStack.peek();    
        undoStack.pop();                          
        records.remove(last);                     
        status("Undid " + last.getId() + " (" + last.getName() + ").");
        displayRecords();
    }

    
    private void displayRecords() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s | %-18s | %-14s | %6s | %s%n",
                "ID", "Name", "Course", "Mark", "Added at"));
        sb.append("-".repeat(78)).append('\n');

        Enumeration<StudentRecord> en = records.elements();
        while (en.hasMoreElements()) {
            sb.append(en.nextElement()).append('\n');
        }
        sb.append(records.size()).append(" record(s).");
        outputArea.setText(sb.toString());
        status("Displayed " + records.size() + " record(s).");
    }

  
    private void saveRecords() {
        StringBuilder all = new StringBuilder();
        Enumeration<StudentRecord> en = records.elements();
        while (en.hasMoreElements()) {
            all.append(en.nextElement().toCsvLine()).append(System.lineSeparator());
        }

       
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TEXT_FILE))) {
            writer.write(all.toString());
        } catch (IOException ex) {
            showError("Could not write " + TEXT_FILE + ": " + ex.getMessage());
            return;
        }

      
        try (FileOutputStream backup = new FileOutputStream(BACKUP_FILE)) {
            backup.write(all.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            showError("Saved " + TEXT_FILE + " but the backup failed: " + ex.getMessage());
            return;
        }
        status("Saved " + records.size() + " record(s) to " + TEXT_FILE + " and " + BACKUP_FILE + ".");
    }

   
    private void loadRecords() {
        Vector<StudentRecord> loaded = new Vector<>();
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(TEXT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(line, ",");
                if (st.countTokens() != 5) {          
                    skipped++;
                    continue;
                }
                String id = st.nextToken();
                String name = st.nextToken();
                String course = st.nextToken();
                String markText = st.nextToken();
                String addedAt = st.nextToken();
                try {
                    loaded.add(new StudentRecord(id, name, course, Double.parseDouble(markText), addedAt));
                } catch (NumberFormatException ex) {
                    skipped++;                        
                }
            }
        } catch (java.io.FileNotFoundException ex) {
            showError(TEXT_FILE + " was not found. Save some records first.");
            return;
        } catch (IOException ex) {
            showError("Could not read " + TEXT_FILE + ": " + ex.getMessage());
            return;
        }

        records.clear();
        records.addAll(loaded);       
        undoStack.clear();            
        displayRecords();
        status("Loaded " + records.size() + " record(s)" + (skipped > 0 ? ", skipped " + skipped + " bad line(s)." : "."));
    }

   
    private void consoleSearch() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Console search ready. Type a student name to search, or 'exit' to stop.");
        while (true) {
            System.out.print("Search name> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String query = scanner.nextLine().trim();
            if (query.equalsIgnoreCase("exit")) {
                break;
            }
            if (query.isEmpty()) {
                continue;
            }
            boolean found = false;
            synchronized (records) {                  // Vector is shared with the GUI thread
                Enumeration<StudentRecord> en = records.elements();
                while (en.hasMoreElements()) {
                    StudentRecord r = en.nextElement();
                    if (r.getName().equalsIgnoreCase(query)) {
                        System.out.println(r);
                        found = true;
                    }
                }
            }
            if (!found) {
                System.out.println("No student named '" + query + "' was found.");
            }
        }
        System.out.println("Console search stopped.");
    }

 
    private void status(String message) {
        statusLabel.setText(message);
    }

    private void showError(String message) {
        status(message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        StudentManager app = new StudentManager();
        SwingUtilities.invokeLater(() -> app.setVisible(true));

        Thread console = new Thread(app::consoleSearch, "ConsoleSearch");
        console.setDaemon(true);      // does not keep the JVM alive after the window closes
        console.start();
    }
}
