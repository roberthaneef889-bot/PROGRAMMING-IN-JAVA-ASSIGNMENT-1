import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;


public class StudentRecord {

    private final String id;
    private final String name;
    private final String course;
    private final double mark;
    private final String addedAt;   


    public StudentRecord(String id, String name, String course, double mark) {
        this(id, name, course, mark, now());
    }

    public StudentRecord(String id, String name, String course, double mark, String addedAt) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.mark = mark;
        this.addedAt = addedAt;
    }


    private static String now() {
        GregorianCalendar calendar = new GregorianCalendar();   // current moment
        Date date = calendar.getTime();                         // Calendar -> Date
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public String getCourse()  { return course; }
    public double getMark()    { return mark; }
    public String getAddedAt() { return addedAt; }


    public String toCsvLine() {
        return id + "," + name + "," + course + "," + mark + "," + addedAt;
    }

    @Override
    public String toString() {
        return String.format("%-6s | %-18s | %-14s | %6.1f | %s", id, name, course, mark, addedAt);
    }
}
