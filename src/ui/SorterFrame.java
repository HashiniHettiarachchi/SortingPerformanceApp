package ui;

import io.CsvLoader;
import io.Dataset;
import perf.PerformanceEvaluator;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Main application window.
 *
 * <p>Layout: a header band (title, dataset summary, load button, theme
 * switch), a column card and a data-preview card side by side, and a results
 * card at the bottom with the run button, an activity indicator and the
 * best-algorithm banner.</p>
 *
 * <p>All long-running work (file parsing, sorting) happens on background
 * {@link SwingWorker}s so the window stays responsive; Swing then routes the
 * updates back onto the event dispatch thread.</p>
 *
 * <p>Assigned area (suggested Member 3 role): user interface.</p>
 */
public class SorterFrame extends JFrame {

    /** Rows shown in the preview table (the full dataset stays in memory). */
    private static final int PREVIEW_ROWS = 25;
    /** Above this size, warn before running the O(n^2) Insertion Sort. */
    private static final int INSERTION_SORT_WARNING_SIZE = 50_000;
    private static final int CARD_ARC = 18;

    private final CsvLoader csvLoader = new CsvLoader();
    private final PerformanceEvaluator evaluator = new PerformanceEvaluator();

    private Dataset dataset;
    /** Row index (in the results table) of the fastest correct algorithm; -1 = none. */
    private int bestResultIndex = -1;

    // Header
    private JPanel headerPanel;
    private final JLabel titleLabel = new JLabel("Sorting Algorithm Performance Evaluator");
    private final JLabel summaryLabel = new JLabel("Load a dataset to begin");
    private final JButton loadButton = new JButton("Load CSV File...");
    private final JButton themeToggle = new JButton("Dark mode");

    // Cards
    private CardPanel columnCard;
    private CardPanel previewCard;
    private CardPanel resultsCard;
    private SectionTitle columnsTitle;
    private SectionTitle previewTitle;

    // Column selection
    private final DefaultListModel<String> columnListModel = new DefaultListModel<>();
    private final JList<String> columnList = new JList<>(columnListModel);

    // Data preview
    private final JTable previewTable = new JTable();

    // Sorting / results
    private final JButton sortButton = new JButton("Run All Sorting Algorithms");
    private final JProgressBar busyBar = new JProgressBar();
    private final ResultsTableModel resultsModel = new ResultsTableModel();
    private final JTable resultsTable = new JTable(resultsModel);
    private final BannerLabel banner = new BannerLabel();

    public SorterFrame() {
        super("Sorting Algorithm Performance Evaluator — IC4302");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCards(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(sortButton);

        loadButton.addActionListener(e -> chooseAndLoadFile());
        sortButton.addActionListener(e -> runSorting());

        setSize(1160, 760);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);   // centre on screen

        applyPalette();   // components were just created with the right LAF
    }

    // ------------------------------------------------------------------
    // Construction helpers
    // ------------------------------------------------------------------

    private JComponent buildHeader() {
        headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setBackground(Theme.headerAccent());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 19f));
        summaryLabel.setFont(summaryLabel.getFont().deriveFont(Font.PLAIN, 13f));

        JPanel titles = new JPanel(new GridLayout(2, 1, 2, 2));
        titles.setOpaque(false);
        titles.add(titleLabel);
        titles.add(summaryLabel);

        loadButton.setFont(loadButton.getFont().deriveFont(Font.BOLD));
        // A regular FlatLaf button (white pill) reads best on the accent band.

        themeToggle.putClientProperty("JButton.buttonType", "borderless");
        themeToggle.setForeground(Color.WHITE);
        themeToggle.setOpaque(false);
        themeToggle.addActionListener(e -> setDarkMode(!Theme.isDark()));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 8, 0));
        buttons.setOpaque(false);
        buttons.add(loadButton);
        buttons.add(themeToggle);

        headerPanel.add(titles, BorderLayout.CENTER);
        headerPanel.add(buttons, BorderLayout.EAST);
        return headerPanel;
    }

    /** Transparent wrapper that keeps outer spacing outside a rounded card. */
    private JPanel wrap(JComponent card, int top, int left, int bottom, int right) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
        wrap.add(card, BorderLayout.CENTER);
        return wrap;
    }

    private JComponent buildCards() {
        columnCard = new CardPanel(CARD_ARC);
        columnCard.setLayout(new BorderLayout());
        columnCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        columnsTitle = new SectionTitle("Columns", "choose one numeric column to sort");
        columnList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        columnList.setCellRenderer(new ColumnCellRenderer());
        columnCard.add(columnsTitle, BorderLayout.NORTH);
        columnCard.add(new JScrollPane(columnList), BorderLayout.CENTER);

        previewCard = new CardPanel(CARD_ARC);
        previewCard.setLayout(new BorderLayout());
        previewCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        previewTitle = new SectionTitle("Data preview", "first " + PREVIEW_ROWS + " rows");
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        previewTable.setEnabled(false);
        styleTable(previewTable, 24);
        previewCard.add(previewTitle, BorderLayout.NORTH);
        previewCard.add(new JScrollPane(previewTable), BorderLayout.CENTER);

        resultsCard = new CardPanel(CARD_ARC);
        resultsCard.setLayout(new BorderLayout(8, 8));
        resultsCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        sortButton.setFont(sortButton.getFont().deriveFont(Font.BOLD));
        busyBar.setIndeterminate(true);
        busyBar.setVisible(false);
        JPanel runLeft = new JPanel(new BorderLayout(12, 0));
        runLeft.setOpaque(false);
        runLeft.add(sortButton, BorderLayout.WEST);
        runLeft.add(busyBar, BorderLayout.CENTER);

        JPanel runRow = new JPanel(new BorderLayout(12, 0));
        runRow.setOpaque(false);
        runRow.add(runLeft, BorderLayout.CENTER);
        runRow.add(banner, BorderLayout.EAST);

        styleTable(resultsTable, 26);
        JScrollPane resultsScroll = new JScrollPane(resultsTable);
        resultsScroll.setPreferredSize(new Dimension(0, 180));

        resultsCard.add(runRow, BorderLayout.NORTH);
        resultsCard.add(resultsScroll, BorderLayout.CENTER);

        JPanel cards = new JPanel(new BorderLayout(0, 0));
        cards.setOpaque(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                wrap(columnCard, 16, 16, 8, 8), wrap(previewCard, 16, 8, 8, 16));
        split.setDividerLocation(320);
        split.setResizeWeight(0.0);
        split.setBorder(null);
        split.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(split, BorderLayout.CENTER);

        cards.add(top, BorderLayout.CENTER);
        cards.add(wrap(resultsCard, 8, 16, 16, 16), BorderLayout.SOUTH);
        return cards;
    }

    private void styleTable(JTable table, int rowHeight) {
        table.setRowHeight(rowHeight);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(header.getFont().deriveFont(Font.BOLD, 12.5f));
        table.setDefaultRenderer(Object.class, new StripedCellRenderer());
    }

    // ------------------------------------------------------------------
    // File loading
    // ------------------------------------------------------------------

    /**
     * Loads a dataset synchronously — the button path loads asynchronously in
     * a SwingWorker instead. Used by tooling that drives the frame directly.
     * Must be called on the EDT.
     */
    public void loadDataset(java.nio.file.Path file) throws Exception {
        Dataset loaded = csvLoader.load(file);
        loaded.setNumericCounts(computeNumericCounts(loaded));
        onDatasetLoaded(file.toFile(), loaded);
    }

    /** Cached parseable-value count per column, computed in one pass. */
    private static int[] computeNumericCounts(Dataset loaded) {
        int[] counts = new int[loaded.getColumnCount()];
        for (int c = 0; c < counts.length; c++) {
            counts[c] = loaded.getNumericValueCount(c);
        }
        return counts;
    }

    /**
     * Selects the column and starts a sorting run, skipping non-numeric
     * values instead of asking — the programmatic counterpart of
     * {@link #loadDataset(java.nio.file.Path)}. Must be called on the EDT.
     */
    public void sortColumn(int columnIndex) {
        columnList.setSelectedIndex(columnIndex);
        startSortRun(dataset.getNumericColumnSkipping(columnIndex), dataset.getHeader(columnIndex));
    }

    /** Opens a .csv file chooser and loads the file on a background thread. */
    private void chooseAndLoadFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a dataset (.csv)");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (.csv)", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;   // user cancelled
        }
        File file = chooser.getSelectedFile();

        setBusy(true);
        summaryLabel.setText("Loading " + file.getName() + " ...");

        new SwingWorker<Dataset, Void>() {
            @Override
            protected Dataset doInBackground() throws Exception {
                Dataset loaded = csvLoader.load(file.toPath());
                // Cache per-column numeric counts once, off the EDT.
                loaded.setNumericCounts(computeNumericCounts(loaded));
                return loaded;
            }

            @Override
            protected void done() {
                try {
                    onDatasetLoaded(file, get());
                } catch (Exception ex) {
                    onDatasetFailed(file, ex);
                }
            }
        }.execute();
    }

    private void onDatasetLoaded(File file, Dataset loaded) {
        dataset = loaded;
        summaryLabel.setText(String.format(Locale.ROOT, "%s  —  %,d rows x %d columns",
                file.getName(), dataset.getRowCount(), dataset.getColumnCount()));
        summaryLabel.setToolTipText(file.getAbsolutePath());

        columnListModel.clear();
        for (int c = 0; c < dataset.getColumnCount(); c++) {
            columnListModel.addElement(dataset.getHeader(c));
        }

        previewTable.setModel(new PreviewTableModel(dataset));
        for (int c = 0; c < previewTable.getColumnCount(); c++) {
            previewTable.getColumnModel().getColumn(c).setPreferredWidth(110);
        }

        resetResults();
        setBusy(false);
    }

    private void onDatasetFailed(File file, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String message;
        if (cause instanceof CsvLoader.CsvLoadException) {
            message = cause.getMessage();
        } else {
            message = "Could not load \"" + file.getName() + "\": " + cause.getMessage();
        }
        dataset = null;
        resetResults();
        setBusy(false);
        summaryLabel.setText("No file loaded");
        JOptionPane.showMessageDialog(this, message, "File could not be loaded",
                JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------
    // Sorting and performance evaluation
    // ------------------------------------------------------------------

    /**
     * Validates the selection, then sorts on a background thread. Columns
     * that mix numbers with blanks or unit-suffixed values ("2 hrs") are
     * offered with those values skipped; pure text columns are rejected.
     */
    private void runSorting() {
        if (dataset == null) {
            JOptionPane.showMessageDialog(this, "Please load a .csv file first.", "No dataset",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int columnIndex = columnList.getSelectedIndex();
        if (columnIndex < 0) {
            JOptionPane.showMessageDialog(this, "Please select a column to sort.", "No column selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String columnName = dataset.getHeader(columnIndex);
        int total = dataset.getRowCount();
        int good = dataset.getNumericValueCount(columnIndex);
        int bad = total - good;

        double[] values;
        if (good == 0) {
            JOptionPane.showMessageDialog(this,
                    String.format(Locale.ROOT,
                            "Column \"%s\" contains no numeric values.%n"
                                    + "Please select a numeric column to sort.", columnName),
                    "Non-numeric column", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (bad == 0) {
            values = dataset.getNumericColumn(columnIndex);
        } else {
            int answer = JOptionPane.showConfirmDialog(this,
                    String.format(Locale.ROOT,
                            "Column \"%s\" mixes numbers with %,d values that are not numbers "
                                    + "(blank cells or values like \"2 hrs\").%n%n"
                                    + "Sort the %,d numeric values and skip the rest?",
                            columnName, bad, good),
                    "Mixed column", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
            values = dataset.getNumericColumnSkipping(columnIndex);
        }
        if (total > INSERTION_SORT_WARNING_SIZE) {
            int answer = JOptionPane.showConfirmDialog(this,
                    String.format(Locale.ROOT,
                            "The dataset has %,d rows. Insertion Sort is O(n\u00B2) and may take several "
                                    + "minutes.%nRun all algorithms anyway?", total),
                    "Large dataset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return;
            }
        }
        startSortRun(values, columnName);
    }

    /** Runs all algorithms on {@code values} in the background, updating the results UI. */
    private void startSortRun(double[] values, String columnName) {
        resetResults();
        setBusy(true);
        banner.info("Sorting column \"" + columnName + "\" (" + values.length + " values) ...");

        new SwingWorker<List<PerformanceEvaluator.Result>, PerformanceEvaluator.Result>() {
            @Override
            protected List<PerformanceEvaluator.Result> doInBackground() {
                Consumer<PerformanceEvaluator.Result> listener = this::publish;
                return evaluator.evaluate(values, listener);
            }

            @Override
            protected void process(List<PerformanceEvaluator.Result> chunks) {
                for (PerformanceEvaluator.Result r : chunks) {
                    resultsModel.addResult(r);
                }
            }

            @Override
            protected void done() {
                try {
                    List<PerformanceEvaluator.Result> results = get();
                    showBest(results);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    JOptionPane.showMessageDialog(SorterFrame.this,
                            "Sorting failed: " + cause.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                setBusy(false);
            }
        }.execute();
    }

    private void showBest(List<PerformanceEvaluator.Result> results) {
        PerformanceEvaluator.Result best = PerformanceEvaluator.findBest(results);
        if (best == null) {
            banner.warn("No algorithm produced a correct result.");
            return;
        }
        bestResultIndex = results.indexOf(best);
        resultsTable.repaint();
        banner.success(String.format(Locale.ROOT, "\u2605  Best: %s  (%.3f ms)",
                best.algorithmName, best.timeMs));
    }

    private void resetResults() {
        bestResultIndex = -1;
        resultsModel.clear();
        banner.idle();
    }

    /** Toggles the action buttons and activity indicator while a job runs. */
    private void setBusy(boolean busy) {
        loadButton.setEnabled(!busy);
        sortButton.setEnabled(!busy && dataset != null);
        themeToggle.setEnabled(!busy);
        busyBar.setVisible(busy);
    }

    /** Switches the whole window between light and dark mode. */
    public void setDarkMode(boolean dark) {
        Theme.apply(dark);
        refreshPalette();
    }

    /** Full refresh after a light/dark switch: LAF update plus palette re-tint. */
    private void refreshPalette() {
        SwingUtilities.updateComponentTreeUI(this);
        // updateComponentTreeUI can reset LAF-installed renderers; re-apply ours.
        styleTable(previewTable, 24);
        styleTable(resultsTable, 26);
        columnList.setCellRenderer(new ColumnCellRenderer());
        getRootPane().setDefaultButton(sortButton);
        applyPalette();
    }

    /** Re-reads every custom colour from the palette. */
    private void applyPalette() {
        themeToggle.setText(Theme.isDark() ? "Light mode" : "Dark mode");
        // FlatLaf window decorations leave the content pane non-opaque, so the
        // window background must be forced on to get the gray card backdrop.
        JComponent content = (JComponent) getContentPane();
        content.setOpaque(true);
        content.setBackground(Theme.windowBackground());
        headerPanel.setBackground(Theme.headerAccent());
        summaryLabel.setForeground(Theme.blend(Color.WHITE, Theme.headerAccent(), 0.3f));
        columnsTitle.refresh();
        previewTitle.refresh();
        for (CardPanel card : new CardPanel[]{columnCard, previewCard, resultsCard}) {
            card.setBackground(Theme.cardBackground());
        }
        banner.idle();
    }

    // ------------------------------------------------------------------
    // Table models and renderers
    // ------------------------------------------------------------------

    /** Read-only model for the raw data preview. */
    private static class PreviewTableModel extends AbstractTableModel {
        private final Dataset dataset;

        PreviewTableModel(Dataset dataset) {
            this.dataset = dataset;
        }

        @Override
        public int getRowCount() {
            return Math.min(PREVIEW_ROWS, dataset.getRowCount());
        }

        @Override
        public int getColumnCount() {
            return dataset.getColumnCount();
        }

        @Override
        public String getColumnName(int column) {
            return dataset.getHeader(column);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return dataset.getValue(rowIndex, columnIndex);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    /** Model backing the performance results table. */
    private static class ResultsTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"Algorithm", "Time (ms)", "Correctly sorted"};

        private final List<PerformanceEvaluator.Result> results = new ArrayList<>();

        void clear() {
            results.clear();
            fireTableDataChanged();
        }

        void addResult(PerformanceEvaluator.Result result) {
            results.add(result);
            fireTableRowsInserted(results.size() - 1, results.size() - 1);
        }

        @Override
        public int getRowCount() {
            return results.size();
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return COLUMNS[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PerformanceEvaluator.Result r = results.get(rowIndex);
            switch (columnIndex) {
                case 0: return r.algorithmName;
                case 1: return String.format(Locale.ROOT, "%.3f", r.timeMs);
                default: return r.correct ? "Yes" : "NO — output mismatch";
            }
        }
    }

    /** Zebra striping shared by both tables; best-results highlight on top. */
    private class StripedCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (isSelected) {
                return c;
            }
            if (table == resultsTable && row == bestResultIndex) {
                c.setBackground(Theme.successTint());
                c.setFont(c.getFont().deriveFont(Font.BOLD));
                return c;
            }
            c.setFont(c.getFont().deriveFont(Font.PLAIN));
            c.setBackground(row % 2 == 0 ? Theme.cardBackground() : Theme.blend(
                    Theme.cardBackground(), Theme.isDark() ? Color.WHITE : Color.BLACK, 0.04f));
            return c;
        }
    }

    /**
     * Tags each column by content: NUMERIC (every value parses), MIXED
     * (some values parse — sortable with skips), text (nothing parses).
     */
    private class ColumnCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (dataset != null && index >= 0 && index < dataset.getColumnCount()) {
                int total = dataset.getRowCount();
                int good = dataset.getNumericValueCount(index);

                String tag;
                String tagColor;
                if (good == total) {
                    tag = "NUMERIC";
                    tagColor = String.format("#%06X", Theme.successText().getRGB() & 0xFFFFFF);
                } else if (good > 0) {
                    tag = "MIXED";
                    Color amber = Theme.isDark() ? new Color(0xFBBF24) : new Color(0xB45309);
                    tagColor = String.format("#%06X", amber.getRGB() & 0xFFFFFF);
                } else {
                    tag = "text";
                    tagColor = String.format("#%06X", Theme.mutedText().getRGB() & 0xFFFFFF);
                }
                setText("<html><span style='font-weight:bold;'>"
                        + escapeHtml(value.toString())
                        + "</span>&nbsp;&nbsp;<span style='color:" + tagColor
                        + ";font-size:0.85em;'>" + tag + "</span></html>");
            }
            return c;
        }
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** Two-line section heading used at the top of each card. */
    private static class SectionTitle extends JPanel {
        private final JLabel main = new JLabel();
        private final JLabel sub = new JLabel();

        SectionTitle(String title, String subtitle) {
            super(new GridLayout(2, 1, 2, 2));
            setOpaque(false);
            main.setText(title);
            sub.setText(subtitle);
            add(main);
            add(sub);
            setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            refresh();
        }

        void refresh() {
            main.setFont(main.getFont().deriveFont(Font.BOLD, 15f));
            sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 12f));
            main.setForeground(Theme.isDark() ? Color.WHITE : new Color(0x111827));
            sub.setForeground(Theme.mutedText());
        }
    }

    /** Rounded status banner in the results card (idle / busy / success / warning). */
    private class BannerLabel extends CardPanel {
        private final JLabel text = new JLabel(" ");

        BannerLabel() {
            super(CARD_ARC);
            setLayout(new BorderLayout());
            text.setFont(text.getFont().deriveFont(Font.BOLD, 13f));
            setBorder(BorderFactory.createEmptyBorder(2, 14, 2, 14));
            add(text, BorderLayout.CENTER);
            setPreferredSize(new Dimension(460, 40));
            idle();
        }

        void idle() {
            text.setText(" ");
            setBackground(Theme.cardBackground());
            text.setForeground(Theme.mutedText());
            revalidate();
        }

        void info(String message) {
            show(message, Theme.accentTint(), Theme.accent());
        }

        void success(String message) {
            show(message, Theme.successTint(), Theme.successText());
        }

        void warn(String message) {
            show(message, new Color(0x7F1D1D), new Color(0xFCA5A5));
        }

        private void show(String message, Color background, Color foreground) {
            text.setText(message);
            text.setForeground(foreground);
            setBackground(background);
            revalidate();
        }
    }
}
