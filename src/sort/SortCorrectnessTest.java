package sort;

import java.util.Arrays;
import java.util.Random;

/**
 * Command-line self-test for the five sorting algorithms.
 *
 * <p>Run with {@code java -cp out sort.SortCorrectnessTest}. Each algorithm
 * is executed on a battery of edge-case and randomised arrays, and every
 * result is compared element-by-element against {@link Arrays#sort}.
 * Exits non-zero if any check fails, so it can double as a CI gate.</p>
 */
public final class SortCorrectnessTest {

    private static final SortAlgorithm[] ALGORITHMS = {
            new InsertionSort(),
            new ShellSort(),
            new MergeSort(),
            new QuickSort(),
            new HeapSort()
    };

    private SortCorrectnessTest() {
    }

    public static void main(String[] args) {
        int failures = 0;
        for (SortAlgorithm algorithm : ALGORITHMS) {
            failures += test(algorithm);
        }
        if (failures == 0) {
            System.out.println("ALL ALGORITHMS PASSED (" + ALGORITHMS.length + " algorithms).");
        } else {
            System.out.println(failures + " CHECK(S) FAILED.");
            System.exit(1);
        }
    }

    private static int test(SortAlgorithm algorithm) {
        int failures = 0;
        int[] shapes = {
                0,       // empty array
                1,       // single element
                2,       // two elements
                3,       // odd size
                50,      // small
                1_000,   // duplicates-heavy
                10_001   // odd, larger
        };
        Random random = new Random(42);   // fixed seed -> reproducible runs

        for (int size : shapes) {
            double[][] cases = buildCases(size, random);
            for (double[] input : cases) {
                double[] expected = Arrays.copyOf(input, input.length);
                Arrays.sort(expected);
                double[] actual = Arrays.copyOf(input, input.length);
                algorithm.sort(actual);
                if (!Arrays.equals(expected, actual)) {
                    failures++;
                    System.out.printf("FAIL %s on %s%n", algorithm.getName(), describe(input));
                }
            }
        }

        System.out.printf("%-15s %s%n", algorithm.getName() + ":", failures == 0 ? "PASS" : "FAIL");
        return failures;
    }

    /** One size, every interesting data shape. */
    private static double[][] buildCases(int size, Random random) {
        double[] randomData = new double[size];
        for (int i = 0; i < size; i++) {
            randomData[i] = random.nextDouble() * 1000;
        }
        double[] sorted = randomData.clone();
        Arrays.sort(sorted);
        double[] reversed = new double[size];
        for (int i = 0; i < size; i++) {
            reversed[i] = sorted[size - 1 - i];
        }
        double[] allEqual = new double[size];
        Arrays.fill(allEqual, 7.0);
        // Few distinct values -> many duplicates (stress for partition schemes).
        double[] fewDistinct = new double[size];
        for (int i = 0; i < size; i++) {
            fewDistinct[i] = random.nextInt(5);
        }

        return new double[][]{randomData, sorted, reversed, allEqual, fewDistinct};
    }

    private static String describe(double[] input) {
        if (input.length == 0) return "empty array";
        if (input.length == 1) return "single element";
        return "array of length " + input.length;
    }
}
