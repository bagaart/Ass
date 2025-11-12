package Lab_4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessingPanel extends JPanel {
    private JTable symbolTable;
    private JTextArea errorMessages;
    private DefaultTableModel symbolModel;

    public ProcessingPanel() {
        setupPanel();
        createComponents();
    }

    private void setupPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Процесс обработки"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(300, 700));
    }

    private void createComponents() {
        add(createSymbolTableSection());
        add(Box.createVerticalStrut(10));
        add(createErrorMessagesSection());
    }

    private JPanel createSymbolTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Таблица символов"));

        String[] columns = {"Метка", "Адрес", "Адрес вставки"};
        symbolModel = new DefaultTableModel(columns, 0);
        symbolTable = new JTable(symbolModel);
        symbolTable.setFont(new Font("Consolas", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(symbolTable);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createErrorMessagesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Сообщения прохода"));

        errorMessages = new JTextArea(4, 25);
        errorMessages.setEditable(false);
        errorMessages.setFont(new Font("SansSerif", Font.PLAIN, 11));
        errorMessages.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(errorMessages);
        scrollPane.setPreferredSize(new Dimension(320, 100));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void showErrorMessage(String message, boolean isError) {
        errorMessages.setText(message + "\n");
        errorMessages.setForeground(isError ? Color.RED : new Color(0, 128, 0));
    }

    public void clear() {
        symbolModel.setRowCount(0);
        errorMessages.setText("");
    }

    public void updateSymbolTable(String[][] data) {
        symbolModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 3) {
                symbolModel.addRow(row);
            }
        }
    }
}