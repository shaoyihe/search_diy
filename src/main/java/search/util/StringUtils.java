package search.util;

import java.util.Iterator;
import java.util.List;

public class StringUtils {

    public static <T> String join(List<T> list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); ++i) {
            T item = list.get(i);
            sb.append(item.toString());
            if (i != list.size() - 1) {
                sb.append(separator);
            }
        }

        return sb.toString();
    }
}
