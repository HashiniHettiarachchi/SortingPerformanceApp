import io.CsvLoader;
import io.Dataset;
import perf.PerformanceEvaluator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Headless end-to-end smoke test: CSV loading, column validation, and the
 * performance evaluation pipeline, without opening the GUI. Runs against the
 * Titanic dataset, whose mixed real-world columns (blank ages, quoted names
 * with commas) exercise the interesting paths.
 *
 * <p>Run with {@code java -cp out SmokeTest}. Exits non-zero on any failure.
 * Complements {@link sort.SortCorrectnessTest}, which covers the algorithms
 * themselves.</p>
 */
public final class SmokeTest {

    private static int checks = 0;

    public static void main(String[] args) throws Exception {
        // --- 1. Load the titanic dataset ------------------------------------
        Path titanic = Path.of("sample-data", "titanic.csv");
        Dataset data = new CsvLoader().load(titanic);

        check("row count is 891", data.getRowCount() == 891);
        check("column count is 12", data.getColumnCount() == 12);
        check("header preserved", data.getHeader(3).equals("Name"));

        // Quoted field with an embedded comma must survive parsing intact.
        check("quoted comma field parsed as one value",
                data.getValue(0, 3).contains(",") && data.getValue(0, 3).length() > 0);

        // --- 2. Column sortability ------------------------------------------
        check("PassengerId is sortable", data.isSortableColumn(0));
        check("Name is not sortable", !data.isSortableColumn(3));
        check("Fare is sortable", data.isSortableColumn(9));
        check("Fare is fully numeric", data.getNumericValueCount(9) == 891);
        check("Age is a mixed column (714 of 891 numeric)", !data.isNumericColumn(5)
                && data.getNumericValueCount(5) == 714);

        // --- 3. Evaluation pipeline ----------------------------------------
        double[] fares = data.getNumericColumn(9);
        check("numeric extraction length", fares.length == 891);

        List<PerformanceEvaluator.Result> results = new PerformanceEvaluator().evaluate(fares);
        check("five algorithm results", results.size() == 5);
        for (PerformanceEvaluator.Result r : results) {
            check(r.algorithmName + " sorted correctly and timed", r.correct && r.timeMs >= 0);
        }
        PerformanceEvaluator.Result best = PerformanceEvaluator.findBest(results);
        check("a best algorithm was found", best != null);
        System.out.printf("Sample run on 'Fare' (%d values): best = %s (%.3f ms)%n",
                fares.length, best.algorithmName, best.timeMs);

        // --- 4. Bad-file error handling ------------------------------------
        CsvLoader loader = new CsvLoader();

        Path empty = Files.createTempFile("smoke", ".csv");
        try {
            loader.load(empty);
            check("empty file rejected", false);
        } catch (CsvLoader.CsvLoadException e) {
            check("empty file rejected", true);
        }

        Path headerOnly = Files.createTempFile("smoke", ".csv");
        Files.writeString(headerOnly, "a,b,c\n");
        try {
            loader.load(headerOnly);
            check("header-only file rejected", false);
        } catch (CsvLoader.CsvLoadException e) {
            check("header-only file rejected", true);
        }

        Files.delete(empty);
        Files.delete(headerOnly);

        System.out.println(checks + " SMOKE CHECKS PASSED.");
    }

    private static void check(String description, boolean condition) {
        if (!condition) {
            System.out.println("FAILED: " + description);
            System.exit(1);
        }
        checks++;
    }
}
