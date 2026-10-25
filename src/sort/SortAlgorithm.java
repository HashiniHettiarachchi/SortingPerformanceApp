package sort;

/**
 * Common contract for every sorting algorithm in the application.
 *
 * <p>Implementations sort a {@code double[]} ascending, in place. Working on
 * primitives (instead of boxed {@code Double} or {@code List}) keeps the
 * performance measurements meaningful and comparable across algorithms.</p>
 *
 * <p>Assigned area (suggested Member 2 role): sorting algorithms.</p>
 */
public interface SortAlgorithm {

    /** Human-readable algorithm name shown in the results table. */
    String getName();

    /** Sorts the array ascending, in place. */
    void sort(double[] values);
}
