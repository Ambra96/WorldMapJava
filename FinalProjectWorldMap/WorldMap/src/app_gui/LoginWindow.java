package app_gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

import db.DBConnection;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginWindow() {
        setTitle("Global Disease Monitoring - Login");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeComponents();
    } //constructor

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Background Panel
        JPanel backgroundPanel = new JPanel() {
            private Image backgroundImage = new ImageIcon("src/pics/background.jpg").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        add(backgroundPanel);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setOpaque(false);

        Font labelFont = new Font("Arial", Font.BOLD, 16);
        JLabel usernameLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        usernameLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);

        usernameField = new JTextField();
        passwordField = new JPasswordField();
        Dimension fieldSize = new Dimension(200, 30);
        usernameField.setPreferredSize(fieldSize);
        passwordField.setPreferredSize(fieldSize);

        JButton loginButton = new JButton(new ImageIcon("src/pics/login.png"));
        JButton cancelButton = new JButton(new ImageIcon("src/pics/close.png"));
        makeButtonTransparent(loginButton);
        makeButtonTransparent(cancelButton);

        loginButton.addActionListener(e -> performLogin());
        cancelButton.addActionListener(e -> System.exit(0));

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(loginButton);
        formPanel.add(cancelButton);

        backgroundPanel.add(formPanel);
    }

    private void makeButtonTransparent(JButton button) {
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    //method to perform login
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String query = "SELECT user_role FROM users WHERE user_name = ? AND user_password = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("user_role");
                JOptionPane.showMessageDialog(this, "Login Successful as " + role + "!");
                new MainWindow(role).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginWindow().setVisible(true));
    }
} //end of login class
