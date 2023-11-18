import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class TelephoneBillingSystemSwing {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/telephone_billing";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = "sarkar@3355";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGUI() throws ClassNotFoundException, SQLException {
        JFrame frame = new JFrame("Telephone Billing System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel(new GridLayout(3, 1));

        JButton addBillButton = new JButton("Add New Bill");
        JButton viewBillsButton = new JButton("View Bills");
        JButton exitButton = new JButton("Exit");

        addBillButton.addActionListener(e -> addNewBill());

        viewBillsButton.addActionListener(e -> viewBills());

        exitButton.addActionListener(e -> System.exit(0));

        panel.add(addBillButton);
        panel.add(viewBillsButton);
        panel.add(exitButton);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);
    }

    private static void addNewBill() {
        JFrame addBillFrame = new JFrame("Add New Bill");
        addBillFrame.setSize(300, 200);
        addBillFrame.setLayout(new GridLayout(5, 2));

        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField durationField = new JTextField();

        JButton addButton = new JButton("Add Bill");
        addButton.addActionListener(e -> {
            try {
                String customerName = nameField.getText();
                String phoneNumber = phoneField.getText();
                int callDuration = Integer.parseInt(durationField.getText());

                addNewBillToDatabase(customerName, phoneNumber, callDuration);
                addBillFrame.dispose();
            } catch (NumberFormatException | SQLException ex) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        addBillFrame.add(new JLabel("Customer Name:"));
        addBillFrame.add(nameField);
        addBillFrame.add(new JLabel("Phone Number:"));
        addBillFrame.add(phoneField);
        addBillFrame.add(new JLabel("Call Duration (minutes):"));
        addBillFrame.add(durationField);
        addBillFrame.add(addButton);

        addBillFrame.setVisible(true);
    }

    private static void addNewBillToDatabase(String customerName, String phoneNumber, int callDuration) throws SQLException {
        double totalAmount = callDuration * 0.7;

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String sql = "INSERT INTO bills (customer_name, phone_number, call_duration, total_amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, customerName);
                preparedStatement.setString(2, phoneNumber);
                preparedStatement.setInt(3, callDuration);
                preparedStatement.setDouble(4, totalAmount);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Bill added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to add bill. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private static void viewBills() {
        JFrame viewBillsFrame = new JFrame("View Bills");
        viewBillsFrame.setSize(400, 300);
        JTextArea textArea = new JTextArea();

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM bills";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                textArea.append("ID\tCustomer Name\tPhone Number\tCall Duration\tTotal Amount\n");

                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String customerName = resultSet.getString("customer_name");
                    String phoneNumber = resultSet.getString("phone_number");
                    int callDuration = resultSet.getInt("call_duration");
                    double totalAmount = resultSet.getDouble("total_amount");

                    textArea.append(id + "\t" + customerName + "\t" + phoneNumber + "\t" + callDuration + "\t" + totalAmount + "\n");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JScrollPane scrollPane = new JScrollPane(textArea);
        viewBillsFrame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        viewBillsFrame.setVisible(true);
    }
}
