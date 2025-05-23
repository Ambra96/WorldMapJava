package tables_pack;

import app_gui.DataManager;
import db.DBConnection;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Methods to manage country panel,,to add, edit, delete data
public class CountryManager implements DataManager {
    @Override
    public List<String[]> getTableData() {
        List<String[]> data = new ArrayList<>();
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT country_id, country_name, country_continent, country_population FROM countries")) {
            while (rs.next()) {
                data.add(new String[]{
                        String.valueOf(rs.getInt("country_id")),
                        rs.getString("country_name"),
                        rs.getString("country_continent"),
                        String.valueOf(rs.getInt("country_population"))
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error loading countries: " + ex.getMessage());
        }
        return data;
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"ID", "Country", "Continent", "Population"};
    }

    @Override
    public void addRecord() {
        JTextField countryField = new JTextField();
        JTextField continentField = new JTextField();
        JTextField populationField = new JTextField();

        Object[] message = {
                "Country Name:", countryField,
                "Continent:", continentField,
                "Population:", populationField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Add Country", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO countries (country_name, country_continent, country_population) VALUES (?,?,?)")) {
                stmt.setString(1, countryField.getText().trim());
                stmt.setString(2, continentField.getText().trim());
                stmt.setInt(3, Integer.parseInt(populationField.getText().trim()));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error adding country: " + ex.getMessage());
            }
        }
    }

    @Override
    public void editRecord(int rowIndex) {
        List<String[]> data = getTableData();
        if (rowIndex < 0 || rowIndex >= data.size()) return;
        String[] record = data.get(rowIndex);

        JTextField countryField = new JTextField(record[1]);
        JTextField continentField = new JTextField(record[2]);
        JTextField populationField = new JTextField(record[3]);

        Object[] message = {
                "Country Name:", countryField,
                "Continent:", continentField,
                "Population:", populationField
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Edit Country", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE countries SET country_name=?, country_continent=?, country_population=? WHERE country_id=?")) {
                stmt.setString(1, countryField.getText().trim());
                stmt.setString(2, continentField.getText().trim());
                stmt.setInt(3, Integer.parseInt(populationField.getText().trim()));
                stmt.setInt(4, Integer.parseInt(record[0]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error editing country: " + ex.getMessage());
            }
        }
    }

    @Override
    public void deleteRecord(int rowIndex) {
        List<String[]> data = getTableData();
        if (rowIndex < 0 || rowIndex >= data.size()) return;
        String[] record = data.get(rowIndex);

        int option = JOptionPane.showConfirmDialog(null,
                "Delete country: " + record[1] + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM countries WHERE country_id=?")) {
                stmt.setInt(1, Integer.parseInt(record[0]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error deleting country: " + ex.getMessage());
            }
        }
    }
} //end of class
