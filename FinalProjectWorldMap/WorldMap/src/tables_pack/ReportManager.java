package tables_pack;

import app_gui.DataManager;
import db.DBConnection;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportManager implements DataManager {

    @Override
    public List<String[]> getTableData() {
        List<String[]> data = new ArrayList<>();
        String query = "SELECT r.report_id, u.user_name, d.disease_name, c.country_name, r.report_comment, r.report_reportdt " +
                "FROM reports r " +
                "JOIN users u ON r.report_user_id = u.user_id " +
                "JOIN diseases d ON r.report_disease_id = d.disease_id " +
                "JOIN countries c ON r.report_country_id = c.country_id";
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                data.add(new String[]{
                        String.valueOf(rs.getInt("report_id")),
                        rs.getString("user_name"),
                        rs.getString("disease_name"),
                        rs.getString("country_name"),
                        rs.getString("report_comment"),
                        String.valueOf(rs.getDate("report_reportdt"))
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading reports: " + ex.getMessage());
        }
        return data;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"ID", "User", "Disease", "Country", "Comment", "Date"};
    }

    //these two buttons are hidden because users can manage to add reports using import CSV button
    @Override
    public void addRecord() {
        // Hide Add button by doing nothing
        // (The DeleteOnlyCRUDPanel does not show Add and Edit buttons)
    }

    @Override
    public void editRecord(int rowIndex) {
        // Hide Edit button
    }


    @Override
    public void deleteRecord(int rowIndex) {
        List<String[]> data = getTableData();
        if (rowIndex < 0 || rowIndex >= data.size()) return;
        String[] record = data.get(rowIndex);

        int confirm = JOptionPane.showConfirmDialog(null,
                "Delete report ID: " + record[0] + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM reports WHERE report_id=?")) {
                stmt.setInt(1, Integer.parseInt(record[0]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error deleting report: " + ex.getMessage());
            }
        }
    }
}
