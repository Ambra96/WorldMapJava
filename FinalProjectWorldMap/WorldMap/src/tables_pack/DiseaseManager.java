package tables_pack;

import app_gui.DataManager;
import db.DBConnection;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Methods to manage diseases panel,,to add, edit, delete data
public class DiseaseManager implements DataManager {

    @Override
    public List<String[]> getTableData() {
        List<String[]> data = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT disease_id, disease_name, disease_description, disease_dt FROM diseases")) {

            while (rs.next()) {
                data.add(new String[]{
                        String.valueOf(rs.getInt("disease_id")),
                        rs.getString("disease_name"),
                        rs.getString("disease_description"),
                        String.valueOf(rs.getDate("disease_dt"))
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading diseases: " + ex.getMessage());
        }
        return data;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"ID", "Name", "Description", "DiscoveryDate"};
    }

    @Override
    public void addRecord() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        JTextField dateField = new JTextField(); // "YYYY-MM-DD"

        Object[] message = {
                "Disease Name:", nameField,
                "Description:", descField,
                "Discovery Date (YYYY-MM-DD):", dateField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Add Disease", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String diseaseName = nameField.getText().trim();
            String diseaseDesc = descField.getText().trim();
            String diseaseDt = dateField.getText().trim();

            if (diseaseDt.isEmpty()) {
                // If blank, use today's date
                diseaseDt = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            }

            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO diseases (disease_name, disease_description, disease_dt) VALUES (?,?,?)")) {
                stmt.setString(1, diseaseName);
                stmt.setString(2, diseaseDesc);
                stmt.setDate(3, java.sql.Date.valueOf(diseaseDt));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error adding disease: " + ex.getMessage());
            }
        }
    }

    @Override
    public void editRecord(int rowIndex) {
        List<String[]> data = getTableData();
        if (rowIndex < 0 || rowIndex >= data.size()) return;
        String[] record = data.get(rowIndex);

        JTextField nameField = new JTextField(record[1]);
        JTextField descField = new JTextField(record[2]);
        JTextField dateField = new JTextField(record[3]);

        Object[] message = {
                "Disease Name:", nameField,
                "Description:", descField,
                "Discovery Date (YYYY-MM-DD):", dateField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Edit Disease", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String diseaseName = nameField.getText().trim();
            String diseaseDesc = descField.getText().trim();
            String diseaseDt = dateField.getText().trim();

            if (diseaseDt.isEmpty()) {
                diseaseDt = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
            }

            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE diseases SET disease_name=?, disease_description=?, disease_dt=? WHERE disease_id=?")) {
                stmt.setString(1, diseaseName);
                stmt.setString(2, diseaseDesc);
                stmt.setDate(3, java.sql.Date.valueOf(diseaseDt));
                stmt.setInt(4, Integer.parseInt(record[0]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error editing disease: " + ex.getMessage());
            }
        }
    }

    @Override
    public void deleteRecord(int rowIndex) {
        List<String[]> data = getTableData();
        if (rowIndex < 0 || rowIndex >= data.size()) return;
        String[] record = data.get(rowIndex);

        int option = JOptionPane.showConfirmDialog(null,
                "Delete disease: " + record[1] + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM diseases WHERE disease_id=?")) {
                stmt.setInt(1, Integer.parseInt(record[0]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error deleting disease: " + ex.getMessage());
            }
        }
    }
}
