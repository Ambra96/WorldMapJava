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
import utils.DateLabelFormatter;

public class AnalystPanel extends JFrame {
    private JTable reportTable;
    private JDatePickerImpl startPicker, endPicker;

    public AnalystPanel() {
        setTitle("Analyst Management");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Top-- Date pickers + Generate button
        JPanel datePanel = new JPanel(new FlowLayout());
        Properties dateProps = new Properties();
        dateProps.put("text.today", "Today");
        dateProps.put("text.month", "Month");
        dateProps.put("text.year", "Year");

        startPicker = new JDatePickerImpl(
                new JDatePanelImpl(new UtilCalendarModel(), dateProps),
                new DateLabelFormatter()
        );
        endPicker = new JDatePickerImpl(
                new JDatePanelImpl(new UtilCalendarModel(), dateProps),
                new DateLabelFormatter()
        );

        datePanel.add(new JLabel("Start:"));
        datePanel.add(startPicker);
        datePanel.add(new JLabel("End:"));
        datePanel.add(endPicker);

        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> generateReport());
        datePanel.add(generateBtn);

        add(datePanel, BorderLayout.NORTH);

        // Center: Table
        reportTable = new JTable();
        add(new JScrollPane(reportTable), BorderLayout.CENTER);

        // Bottom: Import/Export CSV
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton importBtn = new JButton("Import Report (CSV)");
        importBtn.addActionListener(e -> importReportsCSV());
        JButton exportBtn = new JButton("Export Report (CSV)");
        exportBtn.addActionListener(e -> exportReportsCSV());
        bottomPanel.add(importBtn);
        bottomPanel.add(exportBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    } //end of constructor

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
} //End of AnalystPanel class
