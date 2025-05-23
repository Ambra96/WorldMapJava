package app_gui;

import java.util.List;

public interface DataManager {
    List<String[]> getTableData(); //Returns all rows as a list of String arrays so (like a JTable) it can display them

    String[] getColumnNames(); //Provides the headers for the table

    void addRecord(); //Should implement the logic to insert a new record into the data source

    void editRecord(int rowIndex); //allows editing a record at the specified index

    void deleteRecord(int rowIndex); //Should delete a record at the specified index
}
