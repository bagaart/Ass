package Lab_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessingPanel extends JPanel {
    private JTable supportTable;
    private JTable symbolTable;
    private JTable modificationTable;
    private JTextArea firstPassMessages;
    private DefaultTableModel supportModel;
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
        setPreferredSize(new Dimension(350, 800));
    }

    private void createComponents() {
        add(createSupportTableSection());
        add(Box.createVerticalStrut(10));
        add(createTablesSection()); // Объединенная секция для таблиц символов и модификаций
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

    private JPanel createTablesSection() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));

        JPanel symbolPanel = new JPanel(new BorderLayout());
        symbolPanel.setBorder(BorderFactory.createTitledBorder("Таблица символов"));

        String[] symbolColumns = {"Метка", "Адрес"};
        symbolModel = new DefaultTableModel(symbolColumns, 0);
        symbolTable = new JTable(symbolModel);
        symbolTable.setFont(new Font("Consolas", Font.PLAIN, 11));

        JScrollPane symbolScrollPane = new JScrollPane(symbolTable);
        symbolScrollPane.setPreferredSize(new Dimension(200, 170));
        symbolPanel.add(symbolScrollPane, BorderLayout.CENTER);

        JPanel modificationPanel = new JPanel(new BorderLayout());
        modificationPanel.setBorder(BorderFactory.createTitledBorder("Таблица модификаций"));

        String[] modificationColumns = {"Адрес"};
        modificationModel = new DefaultTableModel(modificationColumns, 0);
        modificationTable = new JTable(modificationModel);
        modificationTable.setFont(new Font("Consolas", Font.PLAIN, 11));

        JScrollPane modificationScrollPane = new JScrollPane(modificationTable);
        modificationScrollPane.setPreferredSize(new Dimension(120, 170));
        modificationPanel.add(modificationScrollPane, BorderLayout.CENTER);

        panel.add(symbolPanel);
        panel.add(modificationPanel);

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
        modificationModel.setRowCount(0);
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

    public void updateModificationTable(String[] addresses) {
        modificationModel.setRowCount(0);
        for (String address : addresses) {
            modificationModel.addRow(new String[]{address});
        }
    }

    public void updateModificationTable(String[][] data) {
        modificationModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length >= 1) {
                modificationModel.addRow(new String[]{row[0]});
            }
        }
    }
}