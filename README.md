# Sorting Algorithm Performance Evaluator

IC4302 – Programming III · Group Assignment (CA PIII)

A Java Swing application that loads a `.csv` dataset, lets the user choose a
**numeric** column, sorts it with five algorithms (Insertion, Shell, Merge,
Quick, Heap), measures each algorithm's execution time, and highlights the
fastest one.

## Requirements

- JDK 11 or newer (tested on JDK 21).
- FlatLaf 3.7.2 for the UI theme — already bundled as `lib/flatlaf-3.7.2.jar`;
  nothing to install.

## Build and run

**Windows:** double-click `compile.bat`, then `run.bat`
(or from a terminal in this folder: `compile.bat` && `run.bat`).

**Linux / macOS:** `sh compile.sh && sh run.sh`

**Manual:** compile every `src/**/*.java` with the library on the classpath
(`javac -cp "lib/*" -d out ...`), then run `java -cp "out;lib/*" Main`
(`out:lib/*` on Linux/macOS) — the compile scripts do exactly this.

## Using the application

1. Click **Load CSV File...** and choose a `.csv` file (try
   `sample-data/ai_usage.csv`). The window shows the row/column counts, every
   column name tagged `NUMERIC` / `MIXED` / `text`, and a preview of the
   first 25 rows.
2. Select a column in the left-hand list. Columns are tagged `NUMERIC`
   (every value parses), `MIXED` (numbers mixed with blanks or unit-suffixed
   values like `"2 hrs"` — sortable, the app asks to skip the non-numeric
   ones), or `text` (not sortable).
3. Click **Run All Sorting Algorithms**. Each algorithm sorts its own copy of
   the data; results appear in the table as they finish. The fastest correct
   algorithm is highlighted in green and repeated in the *Best* banner.
4. **Dark mode / Light mode** (top-right) switches the whole UI theme at
   runtime — nice for contrasting demo recordings.

### Edge cases handled

- Non-CSV / unreadable files, empty files, header-only files → clear error dialog.
- Fully text columns → blocked with a "no numeric values" message.
- Mixed columns (numbers + blanks/`"2 hrs"`-style entries) → the user is told
  exactly how many values are not numbers and asked to confirm skipping them.
- No column selected, no dataset loaded → reminder dialogs.
- Very large datasets → warning before running the O(n²) Insertion Sort.

## Project structure

```
src/
├── Main.java                    entry point, launches the GUI
├── SmokeTest.java               headless end-to-end check (CSV → sort → timing)
├── UiSnapshot.java              dev helper: renders the window to PNG screenshots
├── io/                          dataset handling
│   ├── CsvLoader.java           RFC 4180 CSV parser (quotes, CRLF, BOM, blank lines)
│   └── Dataset.java             table model + numeric column validation/extraction
├── sort/                        the five algorithms
│   ├── SortAlgorithm.java       common interface: name + sort(double[])
│   ├── InsertionSort.java       O(n²), O(n) best case
│   ├── ShellSort.java           Knuth gap sequence, ~O(n^1.5)
│   ├── MergeSort.java           top-down, guaranteed O(n log n), O(n) memory
│   ├── QuickSort.java           median-of-three pivot, in place
│   ├── HeapSort.java            max-heap, in place, guaranteed O(n log n)
│   └── SortCorrectnessTest.java algorithm self-test vs Arrays.sort
├── perf/
│   └── PerformanceEvaluator.java  nanoTime timing on fresh copies + correctness check
└── ui/                          the Swing window
    ├── SorterFrame.java         main window; background SwingWorkers keep it responsive
    ├── Theme.java               FlatLaf setup, light/dark palette, theme switch
    └── CardPanel.java           rounded-corner card surface
lib/
└── flatlaf-3.7.2.jar            UI theme library (bundled, MIT license)
sample-data/
├── ai_usage.csv                 "AI Usage & Impact on Students and Professionals" (300 rows x 19 cols)
├── titanic.csv                  Kaggle "Titanic — Machine Learning from Disaster" (891 rows)
├── iris.csv                     Kaggle/classic "Iris" flower dataset (150 rows)
└── housing.csv                  Kaggle "California Housing Prices" (20,640 rows)
compile.bat / run.bat / compile.sh / run.sh
ui_light_data.png / ui_dark_data.png   screenshots of the current UI
```

## Testing

Two self-tests ship with the code:

```
java -cp out sort.SortCorrectnessTest   # 5 algorithms × 7 sizes × 5 data shapes vs Arrays.sort
java -cp out SmokeTest                  # CSV load, column validation, evaluation pipeline
```

Both print PASS and exit 0 when healthy; both exit non-zero on any failure.

## Suggested member roles (for the recorded presentation)

Group of 2 — suggested split (adjust as you prefer):

| Member | Area | Files | Suggested demo |
|--------|------|-------|----------------|
| 1 — Hashini (leader) | Dataset handling & sorting algorithms | `io/*`, `sort/*` | Load a Kaggle CSV; show column tags and mixed-column handling; explain the RFC 4180 parser (quoted cities like `Portland, OR`); walk through the five algorithms and run `sort.SortCorrectnessTest` live. |
| 2 | Performance evaluation & UI | `perf/*`, `ui/*` | Explain nanoTime timing on fresh copies and the correctness verification; demonstrate the full GUI run, results table and best-algorithm banner; show the light/dark theme switch. |

Each member should introduce themselves (name + student number) before their
section, per the assignment guidelines.

## Group workflow (Git)

Repository: `https://github.com/HashiniHettiarachchi/SortingPerformanceApp`
(single `main` branch; Hashini pushes first, the second member merges after).

The second member works like this:

```
git clone https://github.com/HashiniHettiarachchi/SortingPerformanceApp.git
cd SortingPerformanceApp
git config user.name "Your Name"          # commits show under your profile
git config user.email "you@example.com"
# ... add / modify your files ...
git add -A
git commit -m "Describe what you added"
git push
```

Before pushing again afterwards, pull first so both members' work merges:

```
git pull        # resolves or reports any conflicts
git push
```
