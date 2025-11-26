package Lab_6;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessingPanel extends JPanel {
    private JTable symbolTable;
    private JTable modificationTable;
    private JTextArea errorMessages;
    private DefaultTableModel symbolModel;
    private DefaultTableModel modificationModel;

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
        setPreferredSize(new Dimension(500, 700));
    }

    private void createComponents() {
        add(createSymbolTableSection());
        add(Box.createVerticalStrut(10));
        add(createModificationTableSection());
        add(createErrorMessagesSection());
    }

    private JPanel createSymbolTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Таблица символов"));

        String[] columns = {"Модуль", "Метка", "Адрес", "Адрес вставки", "Тип"};
        symbolModel = new DefaultTableModel(columns, 0);
        symbolTable = new JTable(symbolModel);
        symbolTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        symbolTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(symbolTable);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createModificationTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Таблица модификаций"));

        String[] modificationColumns = {"Модуль", "Адрес", "Метка"};
        modificationModel = new DefaultTableModel(modificationColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        modificationTable = new JTable(modificationModel);
        modificationTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        modificationTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(modificationTable);
        scrollPane.setPreferredSize(new Dimension(300, 200));
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
        modificationModel.setRowCount(0);
        errorMessages.setText("");
    }

    public void updateSymbolTable(String[][] data) {
        symbolModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 5) {
                symbolModel.addRow(row);
            }
        }
    }

    public void updateModificationTable(String[][] data) {
        modificationModel.setRowCount(0);
        for (String[] row : data) {
            modificationModel.addRow(row);
        }
    }
}