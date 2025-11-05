package Lab_3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProcessingPanel extends JPanel {
    private JTable supportTable;
    private JTable symbolTable;
    private JTable modificationTable;
    private JTable externalsTable;
    private JTextArea firstPassMessages;

    private DefaultTableModel supportModel;
    private DefaultTableModel symbolModel;
    private DefaultTableModel modificationModel;
    private DefaultTableModel externalsModel;

    public ProcessingPanel() {
        setupPanel();
        createComponents();
    }

    private void setupPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Процесс обработки"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(700, 800));
    }

    private void createComponents() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));

        topPanel.add(createSupportTableSection(), BorderLayout.CENTER);

        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.add(createModificationTableSection());
        rightColumn.setPreferredSize(new Dimension(300, 0));
        rightColumn.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        topPanel.add(rightColumn, BorderLayout.EAST);

        add(topPanel, BorderLayout.CENTER);

        JPanel bottomColumn = new JPanel();
        bottomColumn.setLayout(new BoxLayout(bottomColumn, BoxLayout.Y_AXIS));
        bottomColumn.add(Box.createVerticalStrut(10));
        bottomColumn.add(createSymbolTableSection());
        bottomColumn.add(Box.createVerticalStrut(10));
        bottomColumn.add(createFirstPassMessagesSection());
        bottomColumn.setMaximumSize(new Dimension(700, 350));
        bottomColumn.setPreferredSize(new Dimension(700, 350));

        add(bottomColumn, BorderLayout.SOUTH);
    }

    private JPanel createSupportTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Вспомогательная таблица"));
        panel.setPreferredSize(new Dimension(300, 360));
        panel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        String[] columns = {"Адрес", "Инструкция", "Аргумент1", "Аргумент2"};
        supportModel = new DefaultTableModel(columns, 0);
        supportTable = new JTable(supportModel);
        supportTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane scroll = new JScrollPane(supportTable);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createModificationTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Таблица модификаций"));
        panel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        panel.setPreferredSize(new Dimension(300, 150));

        String[] modificationColumns = {"Модуль", "Адрес", "Метка"};
        modificationModel = new DefaultTableModel(modificationColumns, 0);
        modificationTable = new JTable(modificationModel);
        modificationTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        panel.add(new JScrollPane(modificationTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSymbolTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Таблица символических имен"));
        panel.setPreferredSize(new Dimension(0, 120));

        String[] symbolColumns = {"Модуль", "Метка", "Адрес", "Тип"};
        symbolModel = new DefaultTableModel(symbolColumns, 0);
        symbolTable = new JTable(symbolModel);
        symbolTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        panel.add(new JScrollPane(symbolTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFirstPassMessagesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Сообщения первого прохода"));
        panel.setPreferredSize(new Dimension(0, 120));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        firstPassMessages = new JTextArea();
        firstPassMessages.setEditable(false);
        firstPassMessages.setFont(new Font("SansSerif", Font.PLAIN, 11));
        firstPassMessages.setBackground(new Color(240, 240, 240));
        panel.add(new JScrollPane(firstPassMessages), BorderLayout.CENTER);

        return panel;
    }

    // === Методы для обновления данных ===

    public void showFirstPassMessage(String message, boolean isError) {
        firstPassMessages.setText(message + "\n");
        firstPassMessages.setForeground(isError ? Color.RED : new Color(0, 128, 0));
    }

    public void clear() {
        supportModel.setRowCount(0);
        symbolModel.setRowCount(0);
        modificationModel.setRowCount(0);
        externalsModel.setRowCount(0);
        firstPassMessages.setText("");
    }

    public void updateSupportTable(String[][] data) {
        supportModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 4) supportModel.addRow(row);
        }
    }

    public void updateSymbolTable(String[][] data) {
        symbolModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 4) symbolModel.addRow(row);
        }
    }

    public void updateModificationTable(String[][] data) {
        modificationModel.setRowCount(0);
        for (String row[] : data) {
            modificationModel.addRow(row);
        }
    }

    public void updateExternalsTable(String[][] data) {
        externalsModel.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 2) externalsModel.addRow(row);
        }
    }
}
