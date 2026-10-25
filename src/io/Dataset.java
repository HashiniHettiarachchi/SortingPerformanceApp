package io;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An in-memory table loaded from a .csv file.
 *
 * <p>A dataset stores one header row (column names) plus any number of data
 * rows. It also knows how to decide whether a column is purely numeric and to
 * extract a column as a {@code double[]}, which is the input format the
 * sorting algorithms work on.</p>
 *
 * <p>Assigned area (suggested Member 1 role): dataset handling.</p>
 */
public class Dataset {

    private final List<String> headers;
    /** Data rows; each row has exactly {@code headers.size()} fields. */
    private final List<List<String>> rows;

    /**
     * @param headers column names, never {@code null}
     * @param rows    data rows, never {@code null}
     * @throws IllegalArgumentException if a row width differs from the header width
     */
    public Dataset(List<String> headers, List<List<String>> rows) {
        this.headers = new ArrayList<>(headers);
        this.rows = new ArrayList<>();
        int width = this.headers.size();
        for (List<String> row : rows) {
            if (row.size() != width) {
                throw new IllegalArgumentException(String.format(Locale.ROOT,
                        "Row %d has %d values but the header has %d columns.",
                        this.rows.size() + 1, row.size(), width));
            }
            this.rows.add(new ArrayList<>(row));
        }
    }

    public int getColumnCount() {
        return headers.size();
    }

    public int getRowCount() {
        return rows.size();
    }

    public List<String> getHeaders() {
        return new ArrayList<>(headers);
    }

    public String getHeader(int columnIndex) {
        return headers.get(columnIndex);
    }

    /** Returns the raw value of one cell, or "" for rows with missing trailing values. */
    public String getValue(int rowIndex, int columnIndex) {
        return rows.get(rowIndex).get(columnIndex);
    }

    /**
     * Returns the first {@code limit} rows as text (for the preview table).
     */
    public List<List<String>> getPreviewRows(int limit) {
        int end = Math.min(limit, rows.size());
        List<List<String>> preview = new ArrayList<>(end);
        for (int i = 0; i < end; i++) {
            preview.add(new ArrayList<>(rows.get(i)));
        }
        return preview;
    }

    /**
     * A column is numeric when it has at least one value and every value
     * parses as a number. Blank cells make the column non-numeric, because
     * silently dropping or imputing them would change the data being sorted.
     */
    public boolean isNumericColumn(int columnIndex) {
        if (rows.isEmpty()) {
            return false;
        }
        for (int r = 0; r < rows.size(); r++) {
            if (!isNumeric(getValue(r, columnIndex))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the number of rows in this column whose value fails to parse as
     * a number. Used to build precise error messages.
     */
    public int countNonNumericValues(int columnIndex) {
        int bad = 0;
        for (int r = 0; r < rows.size(); r++) {
            if (!isNumeric(getValue(r, columnIndex))) {
                bad++;
            }
        }
        return bad;
    }

    /**
     * Extracts a validated numeric column as primitives for the sort
     * algorithms.
     *
     * @throws IllegalStateException if the column contains any non-numeric
     *                               value — call {@link #isNumericColumn(int)} first
     */
    public double[] getNumericColumn(int columnIndex) {
        double[] values = new double[rows.size()];
        for (int r = 0; r < rows.size(); r++) {
            values[r] = Double.parseDouble(getValue(r, columnIndex));
        }
        return values;
    }

    /** Shared numeric test: trims whitespace, rejects blanks, accepts int/double/scientific. */
    public static boolean isNumeric(String text) {
        if (text == null) {
            return false;
        }
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        try {
            // Finite only: NaN/Infinity parse successfully but have no
            // consistent ordering, which would make sorting ill-defined.
            return Double.isFinite(Double.parseDouble(trimmed));
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
