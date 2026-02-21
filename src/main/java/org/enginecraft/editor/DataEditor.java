package org.enginecraft.editor;

import org.enginecraft.util.SqlUtil;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;
import java.util.List;

public class DataEditor extends JFrame {
    private final JComboBox<String> groupSelector = new JComboBox<>();
    private final JComboBox<String> subGroupSelector = new JComboBox<>();

    private final JTable table = new JTable();
    private DefaultTableModel model;
    private JButton btnToggleDarkMode = new JButton("Dark Mode");
    private boolean darkMode = false;

    private final Connection conn;

    // Store all data including first two columns internally
    private Object[][] rawData;
    private String[] rawColumnNames;

    // Map: group name -> list of tables
    private Map<String, List<String>> tableGroups;

    public DataEditor() throws SQLException {
        setTitle("Data Editor");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        conn = SqlUtil.getConnection();

        loadTableGroups();
        setupGroupSelectors();

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Group:"));
        topPanel.add(groupSelector);
        topPanel.add(new JLabel("Select Table:"));
        topPanel.add(subGroupSelector);
        JButton btnRefresh = new JButton("Refresh");
        topPanel.add(btnRefresh);
        JButton btnSave = new JButton("Save Changes");
        topPanel.add(btnSave);
        topPanel.add(btnToggleDarkMode);

        add(topPanel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(
                table,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        add(scrollPane, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> {
            String selectedGroup = (String) groupSelector.getSelectedItem();
            String selectedTable = (String) subGroupSelector.getSelectedItem();
            if (selectedGroup != null && selectedTable != null) {
                try {
                    loadTableData(selectedGroup + "_" + selectedTable);
                } catch (SQLException ex) {
                    showError("Error refreshing table: " + ex.getMessage());
                }
            }
        });

        btnSave.addActionListener(e -> {
            try {
                saveChanges();
            } catch (SQLException ex) {
                showError("Error saving changes: " + ex.getMessage());
            }
        });

        btnToggleDarkMode.addActionListener(e -> {
            darkMode = !darkMode;

            if (darkMode) {
                btnToggleDarkMode.setText("Light Mode");
            } else {
                btnToggleDarkMode.setText("Dark Mode");
            }

            updateLookAndFeel();
        });

        // Load first group and table on startup
        if (!tableGroups.isEmpty()) {
            groupSelector.setSelectedIndex(0);
        }

        setVisible(true);
    }

    private void loadTableGroups() throws SQLException {
        List<String> tables = getTableNames();

        // Step 1: Build suffix -> table list map
        Map<String, List<String>> suffixToTables = new HashMap<>();
        for (String table : tables) {
            String[] parts = table.split("_");
            for (int i = 0; i < parts.length; i++) {
                String suffix = String.join("_", Arrays.copyOfRange(parts, i, parts.length));
                suffixToTables.computeIfAbsent(suffix, k -> new ArrayList<>()).add(table);
            }
        }

        // Step 2: Keep only suffixes that appear in 2+ tables, sort longest first
        List<String> commonSuffixes = suffixToTables.entrySet().stream()
                .filter(e -> e.getValue().size() >= 2)
                .map(Map.Entry::getKey)
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .toList();

        // Step 3: Assign longest matching suffix to each table
        Map<String, String> tableToGroupPrefix = new HashMap<>();
        for (String table : tables) {
            String matchedSuffix = null;
            for (String suffix : commonSuffixes) {
                if (table.endsWith(suffix)) {
                    matchedSuffix = suffix;
                    break;
                }
            }
            if (matchedSuffix != null) {
                String prefix = table.substring(0, table.length() - matchedSuffix.length());
                if (prefix.endsWith("_")) prefix = prefix.substring(0, prefix.length() - 1);
                tableToGroupPrefix.put(table, prefix);
            }
        }

        // Step 4: Build initial groups from prefixes
        Map<String, List<String>> groups = new HashMap<>();
        for (Map.Entry<String, String> entry : tableToGroupPrefix.entrySet()) {
            String table = entry.getKey();
            String prefix = entry.getValue();
            groups.computeIfAbsent(prefix, k -> new ArrayList<>()).add(table);
        }

        // Step 5: Merge singletons into existing groups if they match prefix partially
        Map<String, List<String>> finalGroups = new HashMap<>();
        for (String table : groups.keySet()) {
            boolean found = false;
            for (String groupPrefix : groups.keySet()) {
                if (
                    (!table.equals(groupPrefix) && table.startsWith(groupPrefix))
                ) {
                    List<String> groupTables = groups.get(table);
                    if (groupTables == null || groupTables.isEmpty()) {
                        groups.get(groupPrefix).add(table);
                    }
                    else {
                        groups.get(groupPrefix).addAll(groupTables);
                    }
                    finalGroups.put(groupPrefix, new ArrayList<>(Collections.singletonList(table)));
                    found = true;
                    break;
                }
            }

            if (!found) finalGroups.put(table, groups.get(table));
        }

        // Step 6: Sort tables in each group and sort groups
        Map<String, List<String>> cleanedGroups = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : finalGroups.entrySet()) {
            String prefix = entry.getKey();
            List<String> tablesInGroup = entry.getValue();

            String prefixWithUnderscore = prefix + "_";

            List<String> cleaned = tablesInGroup.stream()
                    .map(table -> {
                        if (table.startsWith(prefixWithUnderscore)) {
                            return table.substring(prefixWithUnderscore.length());
                        }
                        return table; // safety fallback
                    })
                    .sorted()
                    .toList();

            cleanedGroups.put(prefix, new ArrayList<>(cleaned));
        }

        tableGroups = new TreeMap<>(cleanedGroups);
    }

    private void setupGroupSelectors() {
        groupSelector.removeAllItems();
        tableGroups.keySet().forEach(groupSelector::addItem);

        groupSelector.addActionListener(e -> {
            String selectedGroup = (String) groupSelector.getSelectedItem();
            if (selectedGroup == null) return;

            List<String> tablesInGroup = tableGroups.get(selectedGroup);
            if (tablesInGroup == null) return;

            tablesInGroup.sort(String::compareTo);
            subGroupSelector.removeAllItems();
            tablesInGroup.forEach(subGroupSelector::addItem);

            if (!tablesInGroup.isEmpty()) {
                subGroupSelector.setSelectedIndex(0);
                try {
                    loadTableData(selectedGroup + "_" + tablesInGroup.getFirst());
                } catch (SQLException ex) {
                    showError("Error loading table data: " + ex.getMessage());
                }
            }
        });

        subGroupSelector.addActionListener(e -> {
            String selectedGroup = (String) groupSelector.getSelectedItem();
            String selectedTable = (String) subGroupSelector.getSelectedItem();
            if (selectedGroup != null && selectedTable != null) {
                try {
                    loadTableData(selectedGroup + "_" + selectedTable);
                } catch (SQLException ex) {
                    showError("Error loading table data: " + ex.getMessage());
                }
            }
        });
    }

    private List<String> getTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_TYPE='BASE TABLE'")) {
            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        }
        return tableNames;
    }

    private void loadTableData(String tableName) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM \"" + tableName + "\"")) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            rawColumnNames = new String[columnCount];
            for (int i = 1; i <= columnCount; i++) {
                rawColumnNames[i - 1] = meta.getColumnName(i);
            }

            List<Object[]> rows = new ArrayList<>();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getObject(i);
                }
                rows.add(row);
            }
            rawData = rows.toArray(new Object[0][]);

            // Show all columns except first two
            int visibleColumnCount = columnCount - 2;
            String[] visibleColumnNames = new String[visibleColumnCount];
            System.arraycopy(rawColumnNames, 2, visibleColumnNames, 0, visibleColumnCount);

            Object[][] visibleData = new Object[rawData.length][visibleColumnCount];
            for (int r = 0; r < rawData.length; r++) {
                System.arraycopy(rawData[r], 2, visibleData[r], 0, visibleColumnCount);
            }

            model = new DefaultTableModel(visibleData, visibleColumnNames);
            table.setModel(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setAutoCreateRowSorter(true);
            autoResizeColumns(table);
        }
    }

    private void autoResizeColumns(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int col = 0; col < table.getColumnCount(); col++) {
            int maxWidth = 50; // minimum width

            TableColumn column = columnModel.getColumn(col);

            // Header width
            TableCellRenderer headerRenderer = column.getHeaderRenderer();
            if (headerRenderer == null) {
                headerRenderer = table.getTableHeader().getDefaultRenderer();
            }
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0, col);
            maxWidth = Math.max(maxWidth, headerComp.getPreferredSize().width);

            // Cell width
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, col);
                Component comp = cellRenderer.getTableCellRendererComponent(table, table.getValueAt(row, col), false, false, row, col);
                maxWidth = Math.max(maxWidth, comp.getPreferredSize().width);
            }

            maxWidth += 10; // padding

            column.setPreferredWidth(maxWidth);
        }
    }

    private void saveChanges() throws SQLException {
        if (model == null) return;

        String tableName = (String) subGroupSelector.getSelectedItem();
        if (tableName == null) return;

        // First two columns are keys, so we include them in WHERE clause if possible
        // For now, we use just the first column as primary key as before, but can extend to both if needed

        String pkColumn = rawColumnNames[0];  // first column name

        // Build UPDATE SQL for all visible columns + keys if needed
        StringBuilder sql = new StringBuilder("UPDATE \"" + tableName + "\" SET ");

        // Visible columns start at rawColumnNames index 2
        for (int col = 2; col < rawColumnNames.length; col++) {
            sql.append("\"").append(rawColumnNames[col]).append("\" = ?");
            if (col < rawColumnNames.length - 1) sql.append(", ");
        }
        sql.append(" WHERE \"").append(pkColumn).append("\" = ?");

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int rowCount = model.getRowCount();

            for (int row = 0; row < rowCount; row++) {
                // Set updated column values from visible model columns
                // Note visible model columns start at index 0 but rawData columns start at 2
                int paramIndex = 1;
                for (int col = 0; col < model.getColumnCount(); col++) {
                    ps.setObject(paramIndex++, model.getValueAt(row, col));
                }

                // Set primary key value from hidden first column (index 0 in rawData)
                Object pkValue = rawData[row][0];
                ps.setObject(paramIndex, pkValue);

                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            JOptionPane.showMessageDialog(this, "Saved " + results.length + " rows successfully.");
        }
    }

    private void updateLookAndFeel() {
        try {
            if (darkMode) {
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                UIManager.put("control", new Color(50, 50, 50));
                UIManager.put("info", new Color(50, 50, 50));
                UIManager.put("nimbusBase", new Color(18, 30, 49));
                UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
                UIManager.put("nimbusDisabledText", new Color(128, 128, 128));
                UIManager.put("nimbusFocus", new Color(115, 164, 209));
                UIManager.put("nimbusGreen", new Color(176, 179, 50));
                UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
                UIManager.put("nimbusLightBackground", new Color(50, 50, 50));
                UIManager.put("nimbusOrange", new Color(191, 98, 4));
                UIManager.put("nimbusRed", new Color(169, 46, 34));
                UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
                UIManager.put("nimbusSelectionBackground", new Color(104, 93, 156));

                UIManager.put("textForeground", Color.WHITE);
                UIManager.put("Table.foreground", Color.WHITE);
                UIManager.put("Table.background", new Color(50, 50, 50));
                UIManager.put("Table.selectionForeground", Color.WHITE);
                UIManager.put("Table.selectionBackground", new Color(104, 93, 156));
                UIManager.put("Label.foreground", Color.WHITE);
                UIManager.put("ComboBox.foreground", Color.WHITE);
                UIManager.put("ComboBox.background", new Color(60, 60, 60));
                UIManager.put("Button.foreground", Color.WHITE);
                UIManager.put("Button.background", new Color(70, 70, 70));
                UIManager.put("ScrollBar.thumb", new Color(80, 80, 80));
                UIManager.put("ScrollBar.track", new Color(50, 50, 50));
            } else {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("control", null);
                UIManager.put("info", null);
                UIManager.put("nimbusBase", null);
                UIManager.put("nimbusAlertYellow", null);
                UIManager.put("nimbusDisabledText", null);
                UIManager.put("nimbusFocus", null);
                UIManager.put("nimbusGreen", null);
                UIManager.put("nimbusInfoBlue", null);
                UIManager.put("nimbusLightBackground", null);
                UIManager.put("nimbusOrange", null);
                UIManager.put("nimbusRed", null);
                UIManager.put("nimbusSelectedText", null);
                UIManager.put("nimbusSelectionBackground", null);

                UIManager.put("textForeground", Color.BLACK);
                UIManager.put("Table.foreground", Color.BLACK);
                UIManager.put("Table.background", Color.WHITE);
                UIManager.put("Table.selectionForeground", Color.WHITE);
                UIManager.put("Table.selectionBackground", new Color(0, 120, 215));
                UIManager.put("Label.foreground", Color.BLACK);
                UIManager.put("ComboBox.foreground", Color.BLACK);
                UIManager.put("ComboBox.background", Color.WHITE);
                UIManager.put("Button.foreground", Color.BLACK);
                UIManager.put("Button.background", Color.LIGHT_GRAY);
                UIManager.put("ScrollBar.thumb", Color.GRAY);
                UIManager.put("ScrollBar.track", Color.WHITE);
            }

            SwingUtilities.updateComponentTreeUI(this);
            updateComponentColorsForTheme();

            if (darkMode) {
                table.setForeground(Color.WHITE);
                table.setBackground(new Color(50, 50, 50));
                table.setSelectionForeground(Color.WHITE);
                table.setSelectionBackground(new Color(104, 93, 156));

                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    header.setForeground(Color.WHITE);
                    header.setBackground(new Color(50, 50, 50));
                    header.repaint();
                }
            } else {
                table.setForeground(Color.BLACK);
                table.setBackground(Color.WHITE);
                table.setSelectionForeground(Color.WHITE);
                table.setSelectionBackground(new Color(0, 120, 215));

                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    header.setForeground(Color.BLACK);
                    header.setBackground(UIManager.getColor("control"));
                    header.repaint();
                }
            }

            table.repaint();

        } catch (Exception ex) {
            showError("Failed to update theme: " + ex.getMessage());
        }
    }

    private void updateComponentColorsForTheme() {
        Color fg = darkMode ? Color.WHITE : Color.BLACK;
        Color bg = darkMode ? new Color(50, 50, 50) : UIManager.getColor("control");

        Component[] comps = getContentPane().getComponents();
        for (Component comp : comps) {
            if (comp instanceof JPanel) {
                for (Component child : ((JPanel) comp).getComponents()) {
                    updateComponentColor(child, fg, bg);
                }
            } else {
                updateComponentColor(comp, fg, bg);
            }
        }

        // Also update table header explicitly
        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setForeground(fg);
            header.setBackground(bg);
            header.repaint();
        }
    }

    private void updateComponentColor(Component comp, Color fg, Color bg) {
        if (comp instanceof JLabel || comp instanceof JButton || comp instanceof JComboBox) {
            comp.setForeground(fg);
            comp.setBackground(bg);
            if (comp instanceof JComboBox) {
                ((JComboBox<?>) comp).getEditor().getEditorComponent().setForeground(fg);
                ((JComboBox<?>) comp).getEditor().getEditorComponent().setBackground(bg);
            }
            comp.repaint();
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new DataEditor();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, "Failed to start Data Editor: " + e.getMessage());
            }
        });
    }
}