package Lab_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessingPanel extends JPanel {
    private JTable supportTable;
    private JTable symbolTable;
    private JTextArea firstPassMessages;
    private DefaultTableModel supportModel;
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
        setPreferredSize(new Dimension(350, 800));
    }

    private void createComponents() {
        add(createSupportTableSection());
        add(Box.createVerticalStrut(10));
        add(createSymbolTableSection());
        add(Box.createVerticalStrut(10));
        add(createFirstPassMessagesSection());
    }

    private JPanel createSupportTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Промежуточная таблица"));

        String[] columns = {"Адрес", "Инструкция", "Аргумент1", "Аргумент2"};
        supportModel = new DefaultTableModel(columns, 0);
        supportTable = new JTable(supportModel);
        supportTable.setFont(new Font("Consolas", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(supportTable);
        scrollPane.setPreferredSize(new Dimension(320, 450));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSymbolTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Таблица символов"));

        String[] columns = {"Метка", "Адрес"};
        symbolModel = new DefaultTableModel(columns, 0);
        symbolTable = new JTable(symbolModel);
        symbolTable.setFont(new Font("Consolas", Font.PLAIN, 11));

        JScrollPane scrollPane = new JScrollPane(symbolTable);
        scrollPane.setPreferredSize(new Dimension(320, 170));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFirstPassMessagesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Сообщения первого прохода"));

        firstPassMessages = new JTextArea(4, 25);
        firstPassMessages.setEditable(false);
        firstPassMessages.setFont(new Font("SansSerif", Font.PLAIN, 11));
        firstPassMessages.setBackground(new Color(240, 240, 240));

        JScrollPane scrollPane = new JScrollPane(firstPassMessages);
        scrollPane.setPreferredSize(new Dimension(320, 100));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void showFirstPassMessage(String message, boolean isError) {
        firstPassMessages.setText(message + "\n");
        firstPassMessages.setForeground(isError ? Color.RED : new Color(0, 128, 0));
    }

    public void clear() {
        supportModel.setRowCount(0);
        symbolModel.setRowCount(0);
        firstPassMessages.setText("");
    }

    public void updateSupportTable(String[][] data) {
        supportModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 4) {
                supportModel.addRow(row);
            }
        }
    }

    public void updateSymbolTable(String[][] data) {
        symbolModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 2) {
                symbolModel.addRow(row);
            }
        }
    }
}