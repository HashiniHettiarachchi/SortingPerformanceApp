package sort;

/**
 * Heap sort: first rearrange the array into a max-heap (every parent is at
 * least as large as its children), then repeatedly swap the root (largest
 * remaining element) to the end of the unsorted region and restore the heap.
 *
 * <p>Runs in O(n log n) in every case using O(1) extra memory. The heap is
 * stored implicitly in the array itself: children of index {@code i} live at
 * {@code 2i + 1} and {@code 2i + 2}.</p>
 */
public class HeapSort implements SortAlgorithm {

    @Override
    public String getName() {
        return "Heap Sort";
    }

    @Override
    public void sort(double[] values) {
        int n = values.length;

        // Phase 1: heapify — sift every non-leaf node down, from the bottom up.
        for (int i = n / 2 - 1; i >= 0; i--) {
            siftDown(values, i, n);
        }

        // Phase 2: repeatedly move the heap's max (root) to the end and shrink.
        for (int end = n - 1; end > 0; end--) {
            double tmp = values[0];
            values[0] = values[end];
            values[end] = tmp;
            siftDown(values, 0, end);
        }
    }

    /** Restores the max-heap property for the subtree rooted at index i, given heap size n. */
    private void siftDown(double[] values, int i, int n) {
        while (true) {
            int largest = i;
            int left = 2 * i + 1;
            int right = 2 * i + 2;

            if (left < n && values[left] > values[largest]) {
                largest = left;
            }
            if (right < n && values[right] > values[largest]) {
                largest = right;
            }
            if (largest == i) {
                return;
            }
            double tmp = values[i];
            values[i] = values[largest];
            values[largest] = tmp;
            i = largest;
        }
    }
}
