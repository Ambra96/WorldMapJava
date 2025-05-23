//URL = "jdbc:mysql://127.0.0.1:3306/DiseaseSystemDB";
//PASSWORD = "n@stini0192!";

package db;

import java.sql.*;

public class DBConnection {
    // Database connection details
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/DiseaseSystemDB";
    private static final String USER = "root";
    private static final String PASSWORD = "n@stini0192!";
    private static Connection connection;

    // Method to establish a connection to the database
    public static Connection connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Database connection failed!");
        }
        return connection;
    }

} //end of DBConnection class
