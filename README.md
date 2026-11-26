# Programming in Java (351 CS 2204): Assignment 1 & 2

**DMI St. John the Baptist University, Mangochi**
Department of Computer Science and Information Technology
Programme: BSc | Submission deadline: Friday, 27 November 2026

| | |
|---|---|
| **Full name** | `[YOUR FULL NAME]` |
| **Registration number** | `[YOUR REGISTRATION NUMBER]` |

## Repository layout

| Path | Contents |
|---|---|
| `question1/` | Program 1: `BankAccount`, `InsufficientFundsException`, `Depositor`, `Withdrawer`, `BankSystem` |
| `question1/applet/` | `BankApplet.java` and `BankApplet.html` (JDK 8 + appletviewer) |
| `question2/` | Program 2: `StudentRecord`, `StudentManager` (Swing GUI) |
| `docs/` | `Assignment_Essays.docx` (Word version of the essays below) |

## How to run

**Program 1** (any JDK 8+):

```bash
cd question1
javac *.java
java BankSystem
```

**Applet** (JDK 8 only, because applets were removed from current Java):

```bash
cd question1/applet
javac BankApplet.java
appletviewer BankApplet.html
```

**Program 2** (any JDK 8+ with a desktop). The GUI opens in a window; type a student name in the terminal to use the Scanner search:

```bash
cd question2
javac *.java
java StudentManager
```

---

## Question 1: Exception Handling, Multithreading and Applets

### Introduction

Real programs must cope with bad input, run several tasks at once and sometimes run inside another application such as a browser. Java supports all three through exception handling, multithreading and applets. This essay explains each topic and shows how it is used in Program 1, the Multithreaded Bank Account System, where a depositor and a withdrawer share one account.

### Exception handling

An exception is an event that interrupts the normal flow of a program. Java handles exceptions with five keywords: `try`, `catch`, `finally`, `throw` and `throws`. Code that may fail goes in a `try` block. A `catch` block handles a particular exception type. A `finally` block always runs, whether or not an exception happened, so it is the right place for a closing message or for releasing resources.

In `BankSystem`, keyboard input is read inside `try`. If the user types text where a number is expected, `Scanner` throws `InputMismatchException`, which is caught and the user is asked again. The `finally` block prints the closing message every time:

```java
try {
    double value = in.nextDouble();
    return value;
} catch (InputMismatchException e) {
    System.out.println("Invalid input '" + in.nextLine() + "'. Please type a number.");
} finally {
    System.out.println("Thank you for banking with us, " + holder + ". Goodbye!");
}
```

Java also lets programmers define their own exceptions. Extending `Exception` (not `RuntimeException`) makes the exception *checked*, which means the compiler forces every caller to catch it or declare it. `InsufficientFundsException` is a checked exception. The method `withdraw()` declares it with `throws` and raises it with `throw`:

```java
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message, double amount) { super(message); }
}

public synchronized void withdraw(double amount)
        throws InsufficientFundsException, InterruptedException {
    if (amount > WITHDRAWAL_LIMIT) {            // MWK 500,000
        throw new InsufficientFundsException("Exceeds limit", amount);
    }
    ...
}
```

`throw` is a statement that creates and raises one exception object. `throws` is part of a method signature and warns callers which checked exceptions may escape. In the program, the withdrawer tries to take MWK 600,000. That is above the limit, so the exception is thrown, caught in `Withdrawer.run()` and reported as `[REJECTED]`, and the thread carries on with its next transaction.

### Multithreading

A thread is a separate path of execution inside one program. Java offers two ways to create one. A class can **extend `Thread`** and override `run()`, or it can **implement `Runnable`** and be passed to a `Thread` object. Implementing `Runnable` is usually better because the class is still free to extend another class. The program uses both: `Depositor extends Thread` and `Withdrawer implements Runnable`.

```java
Depositor depositor = new Depositor(account);                          // extends Thread
Thread withdrawer = new Thread(new Withdrawer(account), "Withdrawer"); // implements Runnable
depositor.setPriority(Thread.NORM_PRIORITY + 2);   // 7
withdrawer.setPriority(Thread.NORM_PRIORITY - 2);  // 3
depositor.start();
withdrawer.start();
```

Calling `start()` (not `run()`) creates the new thread of execution. **Priorities** range from 1 to 10 and are only hints to the scheduler: a higher priority makes a thread more likely, but not certain, to get the CPU first. `Thread.sleep()` pauses the current thread for a number of milliseconds. Each thread here sleeps between its transactions, which makes the interleaving of deposits and withdrawals easy to see.

**Synchronization.** When two threads change the same balance at once, updates can be lost (a *race condition*). Marking `deposit()` and `withdraw()` as `synchronized` makes each thread take the lock of the `BankAccount` object before entering, so only one thread at a time changes the balance.

**Inter-thread communication.** A withdrawer must not take money that is not there. Inside `withdraw()`, the thread calls `wait()` when the balance is too low. `wait()` releases the lock and puts the thread to sleep. When the depositor adds money, it calls `notifyAll()`, which wakes the waiting thread so it can check again. The check is a `while` loop, not an `if`, because a woken thread must re-test the condition.

```java
while (balance < amount) {
    if (depositsFinished) throw new InsufficientFundsException("No more deposits", amount);
    wait();            // give up the lock and sleep until notified
}
balance -= amount;
...
public synchronized void deposit(double amount) {
    balance += amount;
    notifyAll();       // wake the waiting withdrawer
}
```

A `depositsFinished` flag, set in the depositor's `finally` block, stops the withdrawer from waiting forever if no more deposits are coming. `wait()`, `notify()` and `notifyAll()` may only be called while holding the object's lock, which is why they appear inside synchronized methods.

**join().** The `main` thread must not print the final balance while the others are still working. Calling `depositor.join()` and `withdrawer.join()` makes `main` wait until both threads have finished, so the final balance is always correct:

```java
depositor.join();
withdrawer.join();
System.out.printf("FINAL BALANCE: MWK %,.2f%n", account.getBalance());
```

### Applets

An applet is a small Java program that runs inside a web page, controlled by a browser or by `appletviewer`. It extends `java.applet.Applet` and has no `main()`. Instead the browser calls lifecycle methods:

* `init()` runs once when the applet is loaded and is used to read parameters and set up the screen.
* `start()` runs after `init()` and each time the page becomes visible again.
* `stop()` runs when the page is hidden or left.
* `destroy()` runs once when the applet is removed, to release resources.
* `paint(Graphics g)` draws the output on the screen.

An HTML page passes information to an applet with `<param>` tags inside the `<applet>` tag. The applet reads each value with `getParameter()`:

```html
<applet code="BankApplet.class" width="420" height="140">
  <param name="holder" value="Your Name Here">
  <param name="bank"   value="Malawi National Bank">
</applet>
```

```java
public void init() {
    holder = getParameter("holder");
    bank   = getParameter("bank");
}
public void paint(Graphics g) {
    g.drawString("Bank: " + bank, 20, 40);
    g.drawString("Account holder: " + holder, 20, 80);
}
```

`getParameter()` returns `null` when a parameter is missing, so `BankApplet` supplies default text. Applets are no longer supported in current Java versions, so this part was written for **JDK 8** and is run with `appletviewer BankApplet.html`.

### Conclusion

Program 1 shows how the three topics work together. Exceptions, including a user-defined checked exception, keep the program safe from bad input and impossible withdrawals. Synchronization, `wait()`/`notifyAll()` and `join()` let two threads share one account without corrupting the balance and without taking money that is not there. The applet shows how Java programs receive configuration from a web page through `<param>` tags and follow the applet life cycle.

---

## Question 2: I/O Streams, Utility Classes and Event Handling

### Introduction

Most useful programs store data, organise it in memory and respond to the user. Java provides streams for input and output, the `java.util` classes for working with data, and an event model for graphical programs. This essay explains these topics through Program 2, the Student Records Manager, which has a Swing window for adding, saving, loading, displaying and undoing student records.

### Event handling

Java uses the **delegation event model**. An *event source* (a button, a window) creates an event object when something happens, and passes it to registered *listeners*. A listener is any object that implements a listener interface. The program registers listeners with `addActionListener()` and `addWindowListener()`.

Every button (Add, Save, Load, Display, Undo) is handled by an `ActionListener`. The `actionPerformed()` method reads the button's action command and calls the right method:

```java
JButton button = new JButton("Add");
button.setActionCommand("Add");
button.addActionListener(this);

public void actionPerformed(ActionEvent e) {
    switch (e.getActionCommand()) {
        case "Add":  addRecord();   break;
        case "Save": saveRecords(); break;
        ...
    }
}
```

The `WindowListener` interface has seven methods, but the program needs only `windowClosing()`. The adapter class `WindowAdapter` supplies empty versions of all seven, so the program overrides just one. The default close action is disabled so the program can ask for confirmation first:

```java
setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
addWindowListener(new WindowAdapter() {
    @Override
    public void windowClosing(WindowEvent e) {
        if (JOptionPane.showConfirmDialog(null, "Close?", "Exit",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
});
```

### Utility classes

**Random** produces pseudo-random numbers. `random.nextInt(9000)` gives 0 to 8999, so adding 1000 gives a four-digit ID such as `S4821`. A loop checks the existing records and tries again until the ID is unique.

```java
String id;
do { id = "S" + (1000 + random.nextInt(9000)); } while (idExists(id));
```

**Date and GregorianCalendar.** `GregorianCalendar` represents the current moment. `getTime()` converts it to a `Date`, and `SimpleDateFormat` turns the `Date` into readable text. Each record is stamped when it is created:

```java
GregorianCalendar calendar = new GregorianCalendar();
Date date = calendar.getTime();
String stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
```

**Vector and Enumeration.** A `Vector` is a growable, synchronized array. The program keeps all records in a `Vector<StudentRecord>`. An `Enumeration` is an older interface for stepping through a collection with `hasMoreElements()` and `nextElement()`. `Vector.elements()` returns one, and it is used to display and save the records:

```java
Enumeration<StudentRecord> en = records.elements();
while (en.hasMoreElements()) {
    sb.append(en.nextElement()).append('\n');
}
```

**Stack.** A `Stack` is last-in, first-out. Each added record is pushed onto the stack, so the top is always the newest record. Undo uses `empty()` to check for records, `peek()` to look at the top, and `pop()` to remove it before deleting it from the `Vector`:

```java
undoStack.push(record);          // when adding
...
if (!undoStack.empty()) {
    StudentRecord last = undoStack.peek();
    undoStack.pop();
    records.remove(last);
}
```

**StringTokenizer** splits a string at delimiters. When loading, each line `id,name,course,mark,date` is split at commas. `countTokens()` checks that all five fields are present, so damaged lines are skipped rather than crashing the program.

**Scanner** reads and parses input. A console option runs in a background thread. The user types a name, and the program searches the `Vector` and prints each matching record:

```java
Scanner scanner = new Scanner(System.in);
String query = scanner.nextLine().trim();
// compare with equalsIgnoreCase() and print the matches
```

### I/O streams

A stream is a flow of data between a program and a source or destination such as a file. **Byte streams** (`InputStream` and `OutputStream`) handle raw bytes and suit any kind of data. **Character streams** (`Reader` and `Writer`) handle text and take care of character encoding. Buffered classes such as `BufferedWriter` and `BufferedReader` collect data in memory, which is faster, and add useful methods such as `readLine()`.

The program saves in two ways. The main file `students.txt` is written with a character stream, one record per line with comma-separated fields. A backup `students.bak` is written with a byte stream, `FileOutputStream`:

```java
try (BufferedWriter writer = new BufferedWriter(new FileWriter("students.txt"))) {
    writer.write(all.toString());
}
try (FileOutputStream backup = new FileOutputStream("students.bak")) {
    backup.write(all.toString().getBytes(StandardCharsets.UTF_8));
}
```

To load, a `BufferedReader` wrapped around a `FileReader` reads each line with `readLine()` and the `StringTokenizer` splits it into fields, which rebuild the `Vector`:

```java
try (BufferedReader reader = new BufferedReader(new FileReader("students.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        StringTokenizer st = new StringTokenizer(line, ",");
        ...
    }
}
```

The try-with-resources statement closes each stream automatically, even when an error occurs.

**Error handling.** I/O operations can fail, so every one is inside `try`/`catch` for `IOException`. A missing file (`FileNotFoundException`) gets its own message telling the user to save first. A non-numeric mark such as `abc` causes `Double.parseDouble()` to throw `NumberFormatException`. The program catches it and shows a message instead of adding the record. Marks outside 0 to 100 and empty fields are also rejected. Commas inside names are replaced by spaces so that they cannot break the comma-separated format.

### Conclusion

Program 2 brings several parts of the Java library together. Listeners connect the window and buttons to program logic. `Random`, `Date` and `GregorianCalendar` create unique IDs and time stamps, while `Vector`, `Enumeration` and `Stack` store, display and undo records. Character and byte streams save the data, `StringTokenizer` rebuilds it, and `Scanner` provides a console search. Careful exception handling means that bad marks or file problems produce clear messages and do not crash the program.
