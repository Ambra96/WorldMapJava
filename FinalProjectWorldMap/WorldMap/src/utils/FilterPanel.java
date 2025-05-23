package utils;

import app_gui.MainWindow;
import db.DBConnection;
import org.jdatepicker.impl.*;

import javax.swing.*;
import java.sql.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Properties;

public class FilterPanel extends JPanel {
    private JComboBox<String> diseaseCombo;
    private JTextField countryField;
    private JDatePickerImpl datePicker;
    private JButton filterButton;
    private MainWindow mainWindow;

    public FilterPanel(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Disease Filter
        diseaseCombo = new JComboBox<>();
        loadDiseases();
        add(new JLabel("Disease:"));
        add(diseaseCombo);

        // Country Filter
        countryField = new JTextField(15);
        add(new JLabel("Country:"));
        add(countryField);

        // Date Filter
        Properties props = new Properties();
        props.put("text.today", "Today");
        JDatePanelImpl datePanel = new JDatePanelImpl(new UtilCalendarModel(), props);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
        add(new JLabel("Date:"));
        add(datePicker);

        // Filter Button
        filterButton = new JButton("Apply Filter");
        filterButton.addActionListener(e -> mainWindow.applyFilter(
                getSelectedDisease(),
                getSelectedCountry(),
                getSelectedDate()
        ));
        add(filterButton);
    }//end of constructor

    private void loadDiseases() {
        diseaseCombo.addItem("All Diseases");
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT disease_name FROM diseases")) {
            while (rs.next()) {
                diseaseCombo.addItem(rs.getString(1));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public String getSelectedDisease() {
        return (String) diseaseCombo.getSelectedItem();
    }

    public String getSelectedCountry() {
        return countryField.getText().trim();
    }

    public Calendar getSelectedDate() {
        return (Calendar) datePicker.getModel().getValue();
    }
}//end of filter class
