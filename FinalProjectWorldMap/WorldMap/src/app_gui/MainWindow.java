package app_gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import db.DBConnection;
import utils.FilterPanel;
import utils.MapPanel;

public class MainWindow extends JFrame {
    private MapPanel mapPanel;
    private FilterPanel filterPanel;

    // Always create the stats table so every role sees the results.
    private JTable statsTable = new JTable();

    private JButton adminManagementButton;
    private JButton analystManagementButton;

    private String userRole;

    public MainWindow(String role) {
        this.userRole = role;
        initializeWindow();
        initializeComponents();
        setupLayout();
    }

    private void initializeWindow() {
        setTitle("Global Disease Monitoring System - WELCOME " + userRole.toUpperCase());
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initializeComponents() {
        mapPanel = new MapPanel();
        // Pass this MainWindow to FilterPanel so it can call applyFilter
        filterPanel = new FilterPanel(this);

        // Create management buttons only for admin or analyst.
        if (userRole.equalsIgnoreCase("admin")) {
            adminManagementButton = new JButton(new ImageIcon("src/pics/admin.png"));
            adminManagementButton.setText("");
            adminManagementButton.addActionListener(e -> new AdminPanel().setVisible(true));
        } else if (userRole.equalsIgnoreCase("analyst")) {
            analystManagementButton = new JButton(new ImageIcon("src/pics/analyst.png"));
            analystManagementButton.setText("");
            analystManagementButton.addActionListener(e -> new AnalystPanel().setVisible(true));
        }
        // Visitor: no management buttons.
    }

    private void setupLayout() {
        setLayout(new BorderLayout(5, 5));

        // Top: Filter panel
        add(filterPanel, BorderLayout.NORTH);

        // Center: Map panel in a scroll pane
        add(new JScrollPane(mapPanel), BorderLayout.CENTER);

        // Bottom panel: always show the results table
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        if (adminManagementButton != null) {
            buttonPanel.add(adminManagementButton);
        }
        if (analystManagementButton != null) {
            buttonPanel.add(analystManagementButton);
        }
        southPanel.add(buttonPanel, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(statsTable);
        tableScroll.setPreferredSize(new Dimension(0, 60));
        southPanel.add(tableScroll, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);
    }

    //This method populates the bottom stats table with query results

    public void applyFilter(String selectedDisease, String selectedCountry, Calendar selectedDate) {
        StringBuilder sb = new StringBuilder(
                "SELECT c.country_name, d.disease_name, d.disease_description, " +
                        "dc.discases_cases, dc.discases_deaths, dc.discases_recoveries, dc.discases_reportdt, " +
                        "IFNULL(r.report_comment, '') AS report_comment " +
                        "FROM disease_cases dc " +
                        "JOIN countries c ON dc.discases_countries_id = c.country_id " +
                        "JOIN diseases d ON dc.discases_diseases_id = d.disease_id " +
                        "LEFT JOIN reports r ON (r.report_disease_id = d.disease_id AND r.report_country_id = c.country_id " +
                        "                        AND r.report_reportdt = dc.discases_reportdt) " +
                        "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (!"All Diseases".equalsIgnoreCase(selectedDisease)) {
            sb.append(" AND d.disease_name = ? ");
            params.add(selectedDisease);
        }
        if (selectedCountry != null && !selectedCountry.trim().isEmpty()) {
            sb.append(" AND c.country_name = ? ");
            params.add(selectedCountry.trim());
        }
        if (selectedDate != null) {
            sb.append(" AND dc.discases_reportdt = ? ");
            java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTimeInMillis());
            params.add(sqlDate);
        }

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

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
            statsTable.setModel(model);

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No data found for the selected filters.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "DB Error while applying filters: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
} //End of MainWindow class
