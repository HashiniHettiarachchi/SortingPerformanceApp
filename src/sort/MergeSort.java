package sort;

/**
 * Merge sort (top-down, recursive): split the array in half, sort each half,
 * then merge the two sorted halves. Guaranteed O(n log n) in every case, at
 * the cost of O(n) extra memory for the merge buffer.
 */
public class MergeSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public void sort(double[] values) {
        if (values.length < 2) {
            return;
        }
        double[] buffer = new double[values.length];
        mergeSort(values, buffer, 0, values.length - 1);
    }

    /** Sorts values[lo..hi] recursively. */
    private void mergeSort(double[] values, double[] buffer, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = lo + (hi - lo) / 2;
        mergeSort(values, buffer, lo, mid);
        mergeSort(values, buffer, mid + 1, hi);
        merge(values, buffer, lo, mid, hi);
    }

    /** Merges the sorted runs values[lo..mid] and values[mid+1..hi]. */
    private void merge(double[] values, double[] buffer, int lo, int mid, int hi) {
        System.arraycopy(values, lo, buffer, lo, hi - lo + 1);

        int left = lo;
        int right = mid + 1;
        for (int i = lo; i <= hi; i++) {
            if (left > mid) {
                values[i] = buffer[right++];
            } else if (right > hi) {
                values[i] = buffer[left++];
            } else if (buffer[left] <= buffer[right]) {
                values[i] = buffer[left++];
            } else {
                values[i] = buffer[right++];
            }
        }
    }
}
