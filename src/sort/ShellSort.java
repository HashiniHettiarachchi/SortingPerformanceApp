package sort;

/**
 * Shell sort: insertion sort over gapped subsequences, shrinking the gap each
 * pass until it reaches 1, where it degenerates into a plain insertion sort —
 * but by then the array is almost sorted, so that final pass is cheap.
 *
 * <p>This implementation uses Knuth's gap sequence (1, 4, 13, 40, 121, ...)
 * generated with {@code h = 3h + 1}. Complexity is roughly O(n^1.5),
 * far better than plain insertion sort on large arrays.</p>
 */
public class ShellSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Shell Sort";
    }

    @Override
    public void sort(double[] values) {
        int n = values.length;

        // Build the largest Knuth gap that fits: 1, 4, 13, 40, ...
        int gap = 1;
        while (gap < n / 3) {
            gap = gap * 3 + 1;
        }

        while (gap >= 1) {
            // Gapped insertion sort: insert element i into its gap-subsequence.
            for (int i = gap; i < n; i++) {
                double key = values[i];
                int j = i;
                while (j >= gap && values[j - gap] > key) {
                    values[j] = values[j - gap];
                    j -= gap;
                }
                values[j] = key;
            }
            gap /= 3;
        }
    }
}
