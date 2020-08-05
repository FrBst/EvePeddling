import java.util.Comparator;

public class MarginDesc implements Comparator<Deal> {
    public int compare(Deal a, Deal b) {
        if (a.getMargin() > b.getMargin())
            return -1;
        if (a.getMargin() < b.getMargin())
            return 1;
        return 0;
    }
}
