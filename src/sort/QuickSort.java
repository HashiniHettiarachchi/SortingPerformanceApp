package sort;

/**
 * Quick sort (recursive, in place): pick a pivot, partition the array so
 * smaller elements end up left of the pivot and larger ones right, then sort
 * the two sides.
 *
 * <p>The pivot is the <em>median of three</em> (first, middle, last element).
 * A naive fixed-pivot version degrades to O(n&sup2;) on sorted or
 * reverse-sorted input — a realistic case here, since a user can re-sort a
 * column that is already sorted. Median-of-three keeps the common cases at
 * O(n log n).</p>
 */
public class QuickSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Quick Sort";
    }

    @Override
    public void sort(double[] values) {
        quickSort(values, 0, values.length - 1);
    }

    private void quickSort(double[] values, int low, int high) {
        while (low < high) {
            int pivotIndex = partition(values, low, high);
            // Recurse into the smaller side and loop on the larger one, so
            // the recursion depth stays O(log n) even in the worst case.
            if (pivotIndex - low < high - pivotIndex) {
                quickSort(values, low, pivotIndex - 1);
                low = pivotIndex + 1;
            } else {
                quickSort(values, pivotIndex + 1, high);
                high = pivotIndex - 1;
            }
        }
    }

    /** Median-of-three partition; returns the pivot's final index. */
    private int partition(double[] values, int low, int high) {
        int mid = low + (high - low) / 2;
        // Order low/mid/high so values[mid] becomes the median of the three.
        if (values[mid] < values[low]) swap(values, mid, low);
        if (values[high] < values[low]) swap(values, high, low);
        if (values[high] < values[mid]) swap(values, high, mid);
        // Park the pivot just before 'high' so it is out of the scan's way.
        swap(values, mid, high - 1);
        double pivot = values[high - 1];

        // Lomuto partition: everything left of i is < pivot.
        int i = low;
        for (int j = low; j < high - 1; j++) {
            if (values[j] < pivot) {
                swap(values, i, j);
                i++;
            }
        }
        swap(values, i, high - 1);   // pivot into its final slot
        return i;
    }

    private void swap(double[] values, int a, int b) {
        double tmp = values[a];
        values[a] = values[b];
        values[b] = tmp;
    }
}
