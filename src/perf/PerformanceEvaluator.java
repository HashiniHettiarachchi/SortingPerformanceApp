package perf;

import sort.SortAlgorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Runs every sorting algorithm on the same input and measures how long each
 * one takes.
 *
 * <p>Fairness rules:</p>
 * <ul>
 *   <li>each algorithm receives its <em>own copy</em> of the data, because a
 *       sort mutates the array it is given;</li>
 *   <li>timing uses {@link System#nanoTime()}, the monotonic clock intended
 *       for elapsed-time measurement (not {@code currentTimeMillis()}, which
 *       is a wall clock and can jump);</li>
 *   <li>every result is verified against {@link Arrays#sort} so a timing is
 *       only reported for an algorithm that actually sorted the data
 *       correctly.</li>
 * </ul>
 *
 * <p>Assigned area (suggested Member 3 role): performance evaluation.</p>
 */
public class PerformanceEvaluator {

    /** Timing result for one algorithm. */
    public static class Result {
        public final String algorithmName;
        /** Elapsed time in milliseconds. */
        public final double timeMs;
        /** True when the output matched {@link Arrays#sort}. */
        public final boolean correct;

        Result(String algorithmName, double timeMs, boolean correct) {
            this.algorithmName = algorithmName;
            this.timeMs = timeMs;
            this.correct = correct;
        }
    }

    private final List<SortAlgorithm> algorithms;

    /** Evaluates the standard five algorithms in the assignment order. */
    public PerformanceEvaluator() {
        this(new sort.InsertionSort(), new sort.ShellSort(), new sort.MergeSort(),
                new sort.QuickSort(), new sort.HeapSort());
    }

    /** Evaluates an explicit list of algorithms (also handy for extra credit trials). */
    public PerformanceEvaluator(SortAlgorithm... algorithms) {
        this.algorithms = new ArrayList<>(Arrays.asList(algorithms));
    }

    /**
     * Sorts a fresh copy of {@code data} with every algorithm, timing each
     * run. Input data is left untouched.
     */
    public List<Result> evaluate(double[] data) {
        return evaluate(data, result -> { });
    }

    /**
     * Same as {@link #evaluate(double[])}, but calls {@code listener} after
     * each algorithm finishes so a UI can display results progressively.
     * The listener runs on the caller's thread.
     */
    public List<Result> evaluate(double[] data, Consumer<Result> listener) {
        List<Result> results = new ArrayList<>(algorithms.size());
        for (SortAlgorithm algorithm : algorithms) {
            double[] copy = Arrays.copyOf(data, data.length);

            long start = System.nanoTime();
            algorithm.sort(copy);
            long elapsed = System.nanoTime() - start;

            double[] expected = Arrays.copyOf(data, data.length);
            Arrays.sort(expected);
            boolean correct = Arrays.equals(expected, copy);

            Result result = new Result(algorithm.getName(), elapsed / 1_000_000.0, correct);
            results.add(result);
            listener.accept(result);
        }
        return results;
    }

    /** @return the fastest correct result, or {@code null} if none sorted correctly. */
    public static Result findBest(List<Result> results) {
        Result best = null;
        for (Result result : results) {
            if (result.correct && (best == null || result.timeMs < best.timeMs)) {
                best = result;
            }
        }
        return best;
    }
}
