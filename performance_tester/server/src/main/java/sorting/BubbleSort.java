package sorting;

import java.util.Collections;
import java.util.List;

public class BubbleSort {
    static public void sort(List<Integer> array) {
        int n = array.size();

        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (array.get(j) > array.get(j + 1))
                    Collections.swap(array, j, j + 1);
    }
}
