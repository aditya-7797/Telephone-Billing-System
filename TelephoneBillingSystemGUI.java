import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;

public class TelephoneBillingSystemGUI {
    private static Connection connection;
    private static DefaultTableModel tableModel;

    public static void main(String[] args) {
        // Create a connection to the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/telephone_billing?useSSL=false", "root", "sarkar@3355");
            createBillingTable();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Create the main frame
        JFrame frame = new JFrame("Telephone Billing System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Create buttons for adding and viewing call records
        JButton addRecordButton = new JButton("Add Call Record");
        JButton viewRecordsButton = new JButton("View Call Records");

        // Create a JTable for displaying call records
        JTable table = new JTable();
        tableModel = new DefaultTableModel();
        table.setModel(tableModel);

        // Add action listeners for the buttons
        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddCallRecordDialog();
            }
        });

        viewRecordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showCallRecordsDialog();
            }
        });

        // Create a panel for the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addRecordButton);
        buttonPanel.add(viewRecordsButton);

        // Add components to the frame
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.setVisible(true);
    }

    private static void createBillingTable() {
        // Create the billing table if it doesn't exist
        String createTableSQL = "CREATE TABLE IF NOT EXISTS billing (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "customer_name VARCHAR(255), " +
                "phone_number VARCHAR(15), " +
                "call_duration DOUBLE, " +
                "call_cost DOUBLE)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableSQL)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showAddCallRecordDialog() {
        // Implement the dialog for adding a call record here
        String customerName = JOptionPane.showInputDialog("Enter Customer Name:");
        String phoneNumber = JOptionPane.showInputDialog("Enter Phone Number:");
        double callDuration = Double.parseDouble(JOptionPane.showInputDialog("Enter Call Duration:"));
        double callCost = calculateCallCost(callDuration);

        // Save the data to the database
        saveCallRecord(customerName, phoneNumber, callDuration, callCost);

        // Refresh the call records table
        showCallRecordsDialog();
    }

    private static double calculateCallCost(double callDuration) {
        // Implement your call cost calculation logic here
        return callDuration * 0.1; // Example: $0.10 per minute
    }

    private static void saveCallRecord(String customerName, String phoneNumber, double callDuration, double callCost) {
        String insertSQL = "INSERT INTO billing (customer_name, phone_number, call_duration, call_cost) VALUES (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, customerName);
            preparedStatement.setString(2, phoneNumber);
            preparedStatement.setDouble(3, callDuration);
            preparedStatement.setDouble(4, callCost);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void showCallRecordsDialog() {
        // Retrieve call records from the database and display them in a table
        String selectSQL = "SELECT customer_name, phone_number, call_duration, call_cost FROM billing";
        tableModel.setRowCount(0); // Clear the table

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String customerName = resultSet.getString("customer_name");
                String phoneNumber = resultSet.getString("phone_number");
                double callDuration = resultSet.getDouble("call_duration");
                double callCost = resultSet.getDouble("call_cost");
                tableModel.addRow(new Object[]{customerName, phoneNumber, callDuration, callCost});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
