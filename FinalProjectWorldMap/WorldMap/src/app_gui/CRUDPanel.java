package app_gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CRUDPanel extends JPanel {
    private DataManager dataManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, refreshButton;

    //how and which buttons to display on the panel
    public CRUDPanel(DataManager manager) {
        this.dataManager = manager;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        refreshData();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> {
            dataManager.addRecord();
            refreshData();
        });
        editButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                dataManager.editRecord(row);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a record to edit.");
            }
        });
        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                dataManager.deleteRecord(row);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a record to delete.");
            }
        });
        refreshButton.addActionListener(e -> refreshData());
    } //constructor

    public void refreshData() {
        java.util.List<String[]> data = dataManager.getTableData();
        String[] columns = dataManager.getColumnNames();
        tableModel.setDataVector(convertData(data), columns);
    }

    private Object[][] convertData(List<String[]> data) {
        Object[][] array = new Object[data.size()][];
        for (int i = 0; i < data.size(); i++) {
            array[i] = data.get(i);
        }
        return array;
    }
} //end of class
