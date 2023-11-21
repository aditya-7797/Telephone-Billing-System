import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.DecimalFormat;

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

        JPanel panel = new JPanel(new GridLayout(4, 1));

        JButton addBillButton = new JButton("Add New Bill");
        JButton viewBillsButton = new JButton("View Bills");
        JButton deleteBillButton = new JButton("Delete Bill");
        JButton exitButton = new JButton("Exit");

        addBillButton.addActionListener(e -> addNewBill());
        viewBillsButton.addActionListener(e -> viewBills());
        deleteBillButton.addActionListener(e -> deleteBill());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(addBillButton);
        panel.add(viewBillsButton);
        panel.add(deleteBillButton);
        panel.add(exitButton);

        frame.getContentPane().add(BorderLayout.CENTER, panel);
        frame.setVisible(true);
    }

    private static void addNewBill() {
        JFrame addBillFrame = new JFrame("Add New Bill");
        addBillFrame.setSize(300, 200);
        addBillFrame.setLayout(new GridLayout(6, 2));

        JLabel nameLabel = new JLabel("Customer Name:");
        JTextField nameField = new JTextField();
        JLabel phoneLabel = new JLabel("Phone Number:");
        JTextField phoneField = new JTextField();
        JLabel durationLabel = new JLabel("Call Duration (minutes):");
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

        addBillFrame.add(nameLabel);
        addBillFrame.add(nameField);
        addBillFrame.add(phoneLabel);
        addBillFrame.add(phoneField);
        addBillFrame.add(durationLabel);
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
        viewBillsFrame.setSize(600, 400);
        JTable table;

        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
            String sql = "SELECT * FROM bills";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                ResultSet resultSet = preparedStatement.executeQuery();

                // Get column names
                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();
                String[] columnNames = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    columnNames[i - 1] = metaData.getColumnName(i);
                }

                // Get data
                DefaultTableModel model = new DefaultTableModel();
                model.setColumnIdentifiers(columnNames);
                while (resultSet.next()) {
                    Object[] rowData = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        if (metaData.getColumnType(i) == Types.DOUBLE) {
                            double value = resultSet.getDouble(i);
                            rowData[i - 1] = formatDouble(value);
                        } else {
                            rowData[i - 1] = resultSet.getObject(i);
                        }
                    }
                    model.addRow(rowData);
                }

                table = new JTable(model);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        JScrollPane scrollPane = new JScrollPane(table);
        viewBillsFrame.getContentPane().add(BorderLayout.CENTER, scrollPane);
        viewBillsFrame.setVisible(true);
    }

    private static String formatDouble(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value);
    }

    private static void deleteBill() {
        String input = JOptionPane.showInputDialog("Enter the ID of the bill to delete:");
        if (input != null && !input.isEmpty()) {
            int billId = Integer.parseInt(input);
            try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD)) {
                String sql = "DELETE FROM bills WHERE id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, billId);
                    int rowsAffected = preparedStatement.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(null, "Bill deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "No bill found with the specified ID.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
