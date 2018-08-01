package search.util;

public class Asserts {
    public static void isTrue(boolean judge, String msg) {
        if (!judge) throw new IllegalArgumentException(msg);
    }
}
