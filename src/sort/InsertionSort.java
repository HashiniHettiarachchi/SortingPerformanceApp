package sort;

/**
 * Insertion sort: builds the sorted array one element at a time by taking
 * each element and shifting larger neighbours right until its slot is found.
 *
 * <p>Best case O(n) on already-sorted data, worst/average O(n&sup2;). On the
 * large datasets the other algorithms handle easily this one becomes slow —
 * which is exactly the comparison the performance evaluation is meant to
 * demonstrate.</p>
 */
public class InsertionSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Insertion Sort";
    }

    @Override
    public void sort(double[] values) {
        for (int i = 1; i < values.length; i++) {
            double key = values[i];
            int j = i - 1;
            while (j >= 0 && values[j] > key) {
                values[j + 1] = values[j];
                j--;
            }
            values[j + 1] = key;
        }
    }
}
