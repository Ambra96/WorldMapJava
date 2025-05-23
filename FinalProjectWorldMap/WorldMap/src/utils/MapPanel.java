package utils;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MapPanel extends JPanel {
    private static final String MAP_IMAGE_PATH = "src/pics/world_map.jpg";
    private static final int MARKER_RADIUS = 5;
    private static final int HOVER_RADIUS = 15;
    private Image mapImage;
    private JLabel statusBar;
    private Map<String, Point> countryPoints = new HashMap<>();

    public MapPanel() {
        initComponents();
        loadCountryCoordinates();
    } //constructor

    private void initComponents() {
        setLayout(new BorderLayout());
        mapImage = new ImageIcon(MAP_IMAGE_PATH).getImage();
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the map image stretched to fill the panel
                g.drawImage(mapImage, 0, 0, getWidth(), getHeight(), this);
                // Draw country markers
                g.setColor(Color.RED);
                for (Point p : countryPoints.values()) {
                    g.fillOval(p.x - MARKER_RADIUS, p.y - MARKER_RADIUS,
                            MARKER_RADIUS * 5, MARKER_RADIUS * 5);
                    g.setColor(Color.BLACK);
                    g.drawOval(p.x - MARKER_RADIUS, p.y - MARKER_RADIUS,
                            MARKER_RADIUS * 5, MARKER_RADIUS * 5);
                    g.setColor(Color.RED);
                }
            }
        };

        mapPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String country = getNearestCountry(e.getX(), e.getY());
                if (country != null) {
                    showDiseaseData(country);
                    mapPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    statusBar.setText("No country nearby");
                    mapPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        add(mapPanel, BorderLayout.CENTER);

        // Create a fixed-height status bar so the layout doesn't change
        statusBar = new JLabel("Hover over a marker to view disease statistics");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.setPreferredSize(new Dimension(0, 30)); // fixed height of 30px
        add(statusBar, BorderLayout.SOUTH);
    }

    private void loadCountryCoordinates() {
        try (Connection conn = DBConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT country_name, country_coordinates FROM countries")) {
            while (rs.next()) {
                String coordString = rs.getString("country_coordinates");
                if (coordString != null && coordString.contains(",")) {
                    String[] parts = coordString.split(",");
                    try {
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        countryPoints.put(rs.getString("country_name"), new Point(x, y));
                    } catch (NumberFormatException ex) {
                        System.err.println("Invalid coordinates for: " + rs.getString("country_name"));
                    }
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading coordinates: " + ex.getMessage());
        }
    }

    //Calculates and returns the name of the country whose marker is nearest to the given mouse coordinates
    private String getNearestCountry(int mouseX, int mouseY) {
        String closestCountry = null;
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<String, Point> entry : countryPoints.entrySet()) {
            Point p = entry.getValue();
            double distance = Math.hypot(mouseX - p.x, mouseY - p.y);
            if (distance < HOVER_RADIUS && distance < minDistance) {
                minDistance = distance;
                closestCountry = entry.getKey();
            }
        }
        return closestCountry;
    }

    //it displays disease statistics for the hovered country in a single horizontal row using inline HTML.

    private void showDiseaseData(String country) {
        String query = "SELECT c.country_name, " +
                "SUM(dc.discases_cases) AS total_cases, " +
                "SUM(dc.discases_deaths) AS total_deaths, " +
                "SUM(dc.discases_recoveries) AS total_recoveries, " +
                "d.disease_name " +
                "FROM disease_cases dc " +
                "JOIN countries c ON dc.discases_countries_id = c.country_id " +
                "JOIN diseases d ON dc.discases_diseases_id = d.disease_id " +
                "WHERE c.country_name = ? " +
                "GROUP BY c.country_name, d.disease_name";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, country);
            ResultSet rs = stmt.executeQuery();

            // Build HTML to show statistics in one row
            StringBuilder stats = new StringBuilder("<html><b>").append(country).append(":</b> ");
            while (rs.next()) {
                stats.append("<span style='margin-right:20px;'>")
                        .append("<u>").append(rs.getString("disease_name")).append("</u>")
                        .append(": Cases: ").append(rs.getInt("total_cases"))
                        .append(", Deaths: ").append(rs.getInt("total_deaths"))
                        .append(", Recoveries: ").append(rs.getInt("total_recoveries"))
                        .append("</span>");
            }
            stats.append("</html>");
            statusBar.setText(stats.toString());
        } catch (SQLException ex) {
            statusBar.setText("Error retrieving disease data");
            ex.printStackTrace();
        }
    }
} //End of MapPanel class

