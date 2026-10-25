package io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads a .csv file into a {@link Dataset}.
 *
 * <p>The parser follows RFC 4180 for the constructs that appear in real
 * Kaggle exports:</p>
 * <ul>
 *   <li>fields wrapped in double quotes (a quoted field may contain commas
 *       or line breaks),</li>
 *   <li>escaped quotes inside quoted fields ({@code ""} means a literal
 *       {@code "}),</li>
 *   <li>Windows and Unix line endings,</li>
 *   <li>completely empty lines, which are skipped,</li>
 *   <li>a byte-order mark (BOM) at the start of the file, which is stripped.</li>
 * </ul>
 *
 * <p>The first non-empty line is treated as the header row. If a data row has
 * fewer fields than the header (common in hand-edited CSVs), it is padded
 * with empty strings so the table stays rectangular.</p>
 *
 * <p>Assigned area (suggested Member 1 role): file upload and dataset
 * handling.</p>
 */
public final class CsvLoader {

    /** Thrown when a file cannot be read or is not a usable CSV table. */
    public static class CsvLoadException extends Exception {
        public CsvLoadException(String message) {
            super(message);
        }
    }

    public Dataset load(Path file) throws CsvLoadException {
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            StringBuilder field = new StringBuilder();
            List<String> record = new ArrayList<>();
            boolean inQuotes = false;
            int c = reader.read();

            if (c == 0xFEFF) {
                // Skip the UTF-8 byte-order mark if present.
                c = reader.read();
            }

            while (c != -1) {
                if (inQuotes) {
                    if (c == '"') {
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');   // "" inside quotes -> literal quote
                        } else {
                            inQuotes = false;    // closing quote
                            c = next;            // ',' etc. already consumed
                            continue;
                        }
                    } else {
                        field.append((char) c);
                    }
                } else if (c == '"' && field.length() == 0) {
                    inQuotes = true;             // opening quote of a field
                } else if (c == ',') {
                    record.add(field.toString());
                    field.setLength(0);
                } else if (c == '\r') {
                    int next = reader.read();
                    if (next != '\n' && next != -1) {
                        // Old Mac-style lone CR: treat it as a line break too.
                        endRecord(record, field, records);
                    }
                    c = next;
                    continue;
                } else if (c == '\n') {
                    endRecord(record, field, records);
                } else {
                    field.append((char) c);
                }
                c = reader.read();
            }
            if (field.length() > 0 || !record.isEmpty()) {
                endRecord(record, field, records);   // last line without newline
            }
        } catch (IOException e) {
            throw new CsvLoadException("Could not read the file: " + e.getMessage());
        }

        if (records.isEmpty()) {
            throw new CsvLoadException("The file is empty. Please choose a .csv file with a header row and data.");
        }

        List<String> headers = records.get(0);
        int width = headers.size();
        if (width == 0) {
            throw new CsvLoadException("The first line of the file is empty, so no column names could be read.");
        }

        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            List<String> raw = records.get(i);
            // Pad short rows so every row matches the header width.
            while (raw.size() < width) {
                raw.add("");
            }
            rows.add(raw.subList(0, width));
        }

        if (rows.isEmpty()) {
            throw new CsvLoadException(
                    "The file contains a header row but no data rows. Please choose a file that includes data.");
        }
        return new Dataset(headers, rows);
    }

    private void endRecord(List<String> record, StringBuilder field, List<List<String>> records) {
        record.add(field.toString());
        field.setLength(0);
        // Skip lines that are entirely empty (e.g. trailing newline pairs).
        boolean blank = record.size() == 1 && record.get(0).trim().isEmpty();
        if (!blank) {
            records.add(new ArrayList<>(record));
        }
        record.clear();
    }
}
