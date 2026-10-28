# Sorting Algorithm Performance Evaluator

This is a Java Swing application developed for the IC4302 – Programming III group assignment. It allows users to load CSV datasets, select a numeric column, and sort the data using five algorithms: Insertion Sort, Shell Sort, Merge Sort, Quick Sort, and Heap Sort. The application measures execution time, checks sorting accuracy, and identifies the fastest algorithm. It also includes light and dark modes for a better user experience.

## How to Run the Application

1. Install JDK 11 or a newer version.
2. Open the project folder.
3. Double-click `compile.bat` to compile the Java files.
4. Double-click `run.bat` to start the application.
5. Click **Load CSV File** and select a dataset.
6. Select a numeric column and click **Run All Sorting Algorithms** to compare their execution times.

On Linux/macOS use `compile.sh` and `run.sh` instead. FlatLaf is bundled in `lib/`, so nothing else needs to be installed.

## How It Works

1. **Load CSV File...** opens any `.csv` dataset. The window shows the row and column counts, every column name tagged `NUMERIC` / `MIXED` / `text`, and a preview of the first 25 rows.
2. Select a column in the left-hand list. `MIXED` columns (numbers mixed with blanks or values like `"2 hrs"`) are sortable — the app asks before skipping the non-numeric values. Pure `text` columns are not sortable.
3. Click **Run All Sorting Algorithms**. Each algorithm sorts its own copy of the data, the results table shows every execution time, and the fastest correct algorithm is highlighted and shown in the banner.

Broken files, empty datasets and very large files are handled with clear messages instead of crashes.

## Project Structure

```
src/io       CSV loading and the dataset model (RFC 4180 parser)
src/sort     Insertion, Shell, Merge, Quick and Heap sort
src/perf     nanoTime-based performance evaluation with a correctness check
src/ui       Swing GUI with the FlatLaf theme and light/dark switch
lib          FlatLaf 3.7.2 (bundled jar)
sample-data  Test datasets: ai_usage.csv, titanic.csv, iris.csv, housing.csv
```

## Testing

Two self-tests come with the code:

```
java -cp out sort.SortCorrectnessTest   # 5 algorithms x 7 sizes x 5 data shapes vs Arrays.sort
java -cp out SmokeTest                  # CSV load, column validation, evaluation pipeline
```

## Suggested Member Roles (for the recorded presentation)

| Member | Area | Files |
|--------|------|-------|
| 1 — Hashini (leader) | Dataset handling and sorting algorithms | `io/*`, `sort/*` |
| 2 — Supuni | Performance evaluation and UI | `perf/*`, `ui/*` |

Each member introduces themselves (name + student number) before explaining their own part, per the assignment guidelines.

## Group Workflow (Git)

Repository: `https://github.com/HashiniHettiarachchi/SortingPerformanceApp`

```
git clone https://github.com/HashiniHettiarachchi/SortingPerformanceApp.git
cd SortingPerformanceApp
git config user.name "Your Name"
git config user.email "you@example.com"
git add -A
git commit -m "Describe what you added"
git push
```

Pull before pushing again so both members' work merges cleanly.
