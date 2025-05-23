package app_gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DeleteOnlyCRUDPanel extends JPanel {
    private DataManager dataManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton deleteButton, refreshButton;

    public DeleteOnlyCRUDPanel(DataManager manager) {
        this.dataManager = manager;
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        refreshData();
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        deleteButton = new JButton("Delete");
        refreshButton = new JButton("Refresh");
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

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
        List<String[]> data = dataManager.getTableData();
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
