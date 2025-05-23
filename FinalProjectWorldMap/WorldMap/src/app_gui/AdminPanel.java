package app_gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.util.Calendar;
import java.util.Properties;

import db.DBConnection;
import org.jdatepicker.impl.*;
import tables_pack.*;
import utils.DateLabelFormatter;

public class AdminPanel extends JFrame {
    public AdminPanel() {
        setTitle("Administration Panel");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        initializeTabs();
    }

    private void initializeTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Statistics Management", new StatsManagementTab());
        tabs.addTab("Data Management", new DataManagementTab());
        tabs.addTab("User Management", new UserManagementTab());
        add(tabs);
    } //constructor

    //statistics managenent
    private class StatsManagementTab extends JPanel {
        private JTable reportTable;
        private JDatePickerImpl startPicker, endPicker;

        public StatsManagementTab() {
            setLayout(new BorderLayout());

            // Date pickers
            Properties dateProps = new Properties();
            dateProps.put("text.today", "Today");
            dateProps.put("text.month", "Month");
            dateProps.put("text.year", "Year");

            JPanel datePanel = new JPanel(new FlowLayout());
            startPicker = new JDatePickerImpl(new JDatePanelImpl(new UtilCalendarModel(), dateProps), new DateLabelFormatter());
            endPicker = new JDatePickerImpl(new JDatePanelImpl(new UtilCalendarModel(), dateProps), new DateLabelFormatter());
            datePanel.add(new JLabel("Start:"));
            datePanel.add(startPicker);
            datePanel.add(new JLabel("End:"));
            datePanel.add(endPicker);

            JButton generateButton = new JButton("Generate Report");
            generateButton.addActionListener(e -> generateReport());
            datePanel.add(generateButton);

            add(datePanel, BorderLayout.NORTH);

            // Center: Table
            reportTable = new JTable();
            add(new JScrollPane(reportTable), BorderLayout.CENTER);

            // Bottom: Import/Export CSV buttons
            JPanel bottomPanel = new JPanel(new FlowLayout());
            JButton importCsvBtn = new JButton("Import Report (CSV)");
            importCsvBtn.addActionListener(e -> importReportsCSV());
            JButton exportCsvBtn = new JButton("Export Report (CSV)");
            exportCsvBtn.addActionListener(e -> exportReportsCSV());
            bottomPanel.add(importCsvBtn);
            bottomPanel.add(exportCsvBtn);

            add(bottomPanel, BorderLayout.SOUTH);
        }

        private void generateReport() {
            if (startPicker.getModel().getValue() == null || endPicker.getModel().getValue() == null) {
                JOptionPane.showMessageDialog(this, "Please select both start and end dates.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Calendar startCal = (Calendar) startPicker.getModel().getValue();
            Calendar endCal = (Calendar) endPicker.getModel().getValue();
            java.sql.Date startDate = new java.sql.Date(startCal.getTimeInMillis());
            java.sql.Date endDate = new java.sql.Date(endCal.getTimeInMillis());

            String sql = "SELECT c.country_name, d.disease_name, d.disease_description, " +
                    "dc.discases_cases, dc.discases_deaths, dc.discases_recoveries, dc.discases_reportdt, " +
                    "IFNULL(r.report_comment, '') AS report_comment " +
                    "FROM disease_cases dc " +
                    "JOIN countries c ON dc.discases_countries_id = c.country_id " +
                    "JOIN diseases d ON dc.discases_diseases_id = d.disease_id " +
                    "LEFT JOIN reports r ON (r.report_disease_id = d.disease_id AND r.report_country_id = c.country_id " +
                    "                        AND r.report_reportdt = dc.discases_reportdt) " +
                    "WHERE dc.discases_reportdt BETWEEN ? AND ?";


            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, startDate);
                stmt.setDate(2, endDate);

                ResultSet rs = stmt.executeQuery();
                DefaultTableModel model = new DefaultTableModel(
                        new String[]{"Country", "Disease", "Disease Desc", "Cases", "Deaths", "Recoveries", "Report Date", "Report Comment"},
                        0
                );
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("country_name"),
                            rs.getString("disease_name"),
                            rs.getString("disease_description"),
                            rs.getInt("discases_cases"),
                            rs.getInt("discases_deaths"),
                            rs.getInt("discases_recoveries"),
                            rs.getDate("discases_reportdt"),
                            rs.getString("report_comment")
                    });
                }
                reportTable.setModel(model);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        //method for importing csv file in db
        private void importReportsCSV() {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (Connection conn = DBConnection.connect();
                     java.io.FileReader fr = new java.io.FileReader(file);
                     java.util.Scanner scanner = new java.util.Scanner(fr)) {
                    conn.setAutoCommit(true);
                    int insertedCount = 0;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine().trim();
                        if (line.isEmpty()) continue;

                        // Skip header lines if detected by checking if first token is numeric
                        String[] parts = line.split(",");
                        try {
                            Integer.parseInt(parts[0].trim());
                        } catch (NumberFormatException nfe) {

                            continue;
                        }

                        if (parts.length < 5) {
                            System.err.println("Invalid CSV row (not enough columns): " + line);
                            continue;
                        }

                        int userId = Integer.parseInt(parts[0].trim());
                        int diseaseId = Integer.parseInt(parts[1].trim());
                        int countryId = Integer.parseInt(parts[2].trim());
                        String comment = parts[3].trim();
                        String dateStr = parts[4].trim();

                        String sql = "INSERT INTO reports (report_user_id, report_disease_id, report_country_id, report_comment, report_reportdt) VALUES (?,?,?,?,?)";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setInt(1, userId);
                            stmt.setInt(2, diseaseId);
                            stmt.setInt(3, countryId);
                            stmt.setString(4, comment);
                            stmt.setDate(5, java.sql.Date.valueOf(dateStr));
                            int affectedRows = stmt.executeUpdate();
                            if (affectedRows > 0) {
                                insertedCount++;
                            }
                        }
                    }
                    scanner.close();
                    JOptionPane.showMessageDialog(this, "CSV Import Successful! " + insertedCount + " rows inserted.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error importing CSV: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }

        //export csv file
        private void exportReportsCSV() {
            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.endsWith(".csv")) {
                    filePath += ".csv";
                }
                String query = "SELECT r.report_id, u.user_name, d.disease_name, c.country_name, " +
                        "r.report_comment, r.report_reportdt " +
                        "FROM reports r " +
                        "JOIN users u ON r.report_user_id = u.user_id " +
                        "JOIN diseases d ON r.report_disease_id = d.disease_id " +
                        "JOIN countries c ON r.report_country_id = c.country_id";
                try (Connection conn = DBConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery();
                     java.io.FileWriter writer = new java.io.FileWriter(filePath)) {

                    writer.write("ReportID,User,Disease,Country,Comment,Date\n");
                    while (rs.next()) {
                        writer.write(rs.getInt("report_id") + "," +
                                rs.getString("user_name") + "," +
                                rs.getString("disease_name") + "," +
                                rs.getString("country_name") + "," +
                                rs.getString("report_comment") + "," +
                                rs.getDate("report_reportdt") + "\n");
                    }
                    JOptionPane.showMessageDialog(this, "Export successful!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Data Management
    private class DataManagementTab extends JPanel {
        public DataManagementTab() {
            setLayout(new BorderLayout());
            JTabbedPane tablesPane = new JTabbedPane();

            tablesPane.addTab("Diseases", new CRUDPanel(new DiseaseManager()));
            tablesPane.addTab("Countries", new CRUDPanel(new CountryManager()));
            // Use DeleteOnlyCRUDPanel for Reports to hide Add and Edit buttons
            tablesPane.addTab("Reports", new DeleteOnlyCRUDPanel(new ReportManager()));

            add(tablesPane, BorderLayout.CENTER);
        }
    }

    // User Management
    private class UserManagementTab extends JPanel {
        private DefaultTableModel userModel = new DefaultTableModel(
                new Object[]{"User ID", "Username", "Role"}, 0
        );

        public UserManagementTab() {
            setLayout(new BorderLayout());
            loadUsers();

            JTable userTable = new JTable(userModel);
            JPanel buttonPanel = new JPanel();

            JButton addButton = new JButton("Add User");
            addButton.addActionListener(e -> showUserDialog(null));

            JButton editButton = new JButton("Edit User");
            editButton.addActionListener(e -> {
                int row = userTable.getSelectedRow();
                if (row >= 0) {
                    showUserDialog(row);
                }
            });

            JButton deleteButton = new JButton("Delete User");
            deleteButton.addActionListener(e -> {
                int row = userTable.getSelectedRow();
                if (row >= 0) {
                    int confirm = JOptionPane.showConfirmDialog(null,
                            "Delete user: " + userModel.getValueAt(row, 1) + "?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        deleteUser(row);
                    }
                }
            });

            buttonPanel.add(addButton);
            buttonPanel.add(editButton);
            buttonPanel.add(deleteButton);

            add(buttonPanel, BorderLayout.NORTH);
            add(new JScrollPane(userTable), BorderLayout.CENTER);
        }

        private void loadUsers() {
            userModel.setRowCount(0);
            try (Connection conn = DBConnection.connect();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT user_id, user_name, user_role FROM users")) {
                while (rs.next()) {
                    userModel.addRow(new Object[]{
                            rs.getInt("user_id"),
                            rs.getString("user_name"),
                            rs.getString("user_role")
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        private void showUserDialog(Integer row) {
            JTextField userField = new JTextField();
            JPasswordField passField = new JPasswordField();
            JComboBox<String> roleCombo = new JComboBox<>(new String[]{"admin", "analyst", "guest"});

            Object[] fields = {
                    "Username:", userField,
                    "Password:", passField,
                    "Role:", roleCombo
            };

            if (row != null) {
                userField.setText((String) userModel.getValueAt(row, 1));
                roleCombo.setSelectedItem(userModel.getValueAt(row, 2));
            }

            if (JOptionPane.showConfirmDialog(null, fields,
                    (row == null ? "Add" : "Edit") + " User",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                try (Connection conn = DBConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(
                             row == null
                                     ? "INSERT INTO users (user_name, user_password, user_role) VALUES (?,?,?)"
                                     : "UPDATE users SET user_name=?, user_password=?, user_role=? WHERE user_id=?"
                     )) {

                    stmt.setString(1, userField.getText().trim());
                    stmt.setString(2, new String(passField.getPassword()));
                    stmt.setString(3, (String) roleCombo.getSelectedItem());

                    if (row != null) {
                        stmt.setInt(4, (int) userModel.getValueAt(row, 0));
                    }
                    stmt.executeUpdate();
                    loadUsers();

                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
                }
            }
        }

        private void deleteUser(int row) {
            int userId = (int) userModel.getValueAt(row, 0);
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
                loadUsers();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Error deleting user: " + ex.getMessage());
            }
        }
    }
} //end of class admin panel
