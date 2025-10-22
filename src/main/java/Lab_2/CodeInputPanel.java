package Lab_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class CodeInputPanel extends JPanel {
    private JTextArea sourceCodeArea;
    private JTable operationTable;
    private DefaultTableModel tableModel;

    public CodeInputPanel() {
        setupPanel();
        createComponents();
        initializeDefaultData();
    }

    private void setupPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Исходные данные"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(350, 800));
    }

    private void createComponents() {
        add(createSourceCodeSection());
        add(Box.createVerticalStrut(5));
        add(createOperationTableSection());
    }

    private JPanel createSourceCodeSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Текст программы"));

        sourceCodeArea = new JTextArea(15, 25);
        sourceCodeArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        sourceCodeArea.setLineWrap(true);

        JScrollPane scrollPane = new JScrollPane(sourceCodeArea);
        scrollPane.setPreferredSize(new Dimension(320, 500));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOperationTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Коды операций"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("Добавить операцию");
        addButton.addActionListener(this::addOperation);

        JButton deleteButton = new JButton("Удалить операцию");
        deleteButton.addActionListener(this::deleteOperation);

        // Кнопка для проверки данных
        JButton validateButton = new JButton("Проверить данные");
        validateButton.addActionListener(e -> validateAllData());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(validateButton);

        String[] columns = {"Мнемоника", "Двоичный код", "Размер"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
        };

        operationTable = new JTable(tableModel);
        operationTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        operationTable.setRowHeight(20);

        operationTable.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()));

        JScrollPane scrollPane = new JScrollPane(operationTable);
        scrollPane.setPreferredSize(new Dimension(320, 200));

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void addOperation(ActionEvent e) {
        tableModel.addRow(new String[]{"", "", ""});

        int lastRow = tableModel.getRowCount() - 1;
        operationTable.setRowSelectionInterval(lastRow, lastRow);
        operationTable.scrollRectToVisible(operationTable.getCellRect(lastRow, 0, true));
    }

    private void deleteOperation(ActionEvent e) {
        int selectedRow = operationTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Выберите операцию для удаления",
                    "Внимание",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private boolean isHex(String s) {
        if (s == null || s.isEmpty()) return false;
        if (!s.matches("[0-9a-fA-F]+")) return false;

        try {
            int value = Integer.parseInt(s, 16);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isOnlyLetters(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.matches("[a-zA-Z]+");
    }

    private boolean validateAllData() {
        int rows = tableModel.getRowCount();
        if (rows == 0) {
            JOptionPane.showMessageDialog(this,
                    "Таблица операций пуста",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Set<String> mnemonics = new HashSet<>();
        Set<String> codes = new HashSet<>();
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            Object mnemonicObj = tableModel.getValueAt(i, 0);
            Object codeObj = tableModel.getValueAt(i, 1);
            Object sizeObj = tableModel.getValueAt(i, 2);

            String mnemonic = (mnemonicObj != null) ? mnemonicObj.toString().trim() : "";
            String code = (codeObj != null) ? codeObj.toString().trim() : "";
            String size = (sizeObj != null) ? sizeObj.toString().trim() : "";

            if (mnemonic.isEmpty()) {
                errorMessage.append("Строка ").append(i + 1).append(": Мнемоника не может быть пустой\n");
                isValid = false;
            } else if (!isValidMnemonic(mnemonic)) {
                errorMessage.append("Строка ").append(i + 1).append(": Мнемоника '").append(mnemonic)
                        .append("' должна содержать только буквы и цифры\n");
                isValid = false;
            } else if (mnemonics.contains(mnemonic.toUpperCase())) {
                errorMessage.append("Строка ").append(i + 1).append(": Мнемоника '").append(mnemonic)
                        .append("' уже используется\n");
                isValid = false;
            } else {
                mnemonics.add(mnemonic.toUpperCase());
            }

            if (code.isEmpty()) {
                errorMessage.append("Строка ").append(i + 1).append(": Код операции не может быть пустым\n");
                isValid = false;
            } else if (!isHex(code)) {
                try {
                    int value = Integer.parseInt(code, 16);
                    if (value == 0) {
                        errorMessage.append("Строка ").append(i + 1).append(": Код операции '").append(code)
                                .append("' должен быть больше нуля\n");
                    } else {
                        errorMessage.append("Строка ").append(i + 1).append(": Код операции '").append(code)
                                .append("' должен быть шестнадцатеричным числом\n");
                    }
                } catch (NumberFormatException e) {
                    errorMessage.append("Строка ").append(i + 1).append(": Код операции '").append(code)
                            .append("' должен быть шестнадцатеричным числом\n");
                }
                isValid = false;
            } else if (codes.contains(code.toUpperCase())) {
                errorMessage.append("Строка ").append(i + 1).append(": Код операции '").append(code)
                        .append("' уже используется\n");
                isValid = false;
            } else {
                codes.add(code.toUpperCase());
            }

            if (size.isEmpty()) {
                errorMessage.append("Строка ").append(i + 1).append(": Размер не может быть пустым\n");
                isValid = false;
            } else {
                try {
                    int sizeValue = Integer.parseInt(size);
                    if (sizeValue <= 0) {
                        errorMessage.append("Строка ").append(i + 1).append(": Размер должен быть положительным числом\n");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    errorMessage.append("Строка ").append(i + 1).append(": Размер '").append(size)
                            .append("' должен быть числом\n");
                    isValid = false;
                }
            }
        }

        if (!isValid) {
            JOptionPane.showMessageDialog(this,
                    "Обнаружены ошибки в данных:\n\n" + errorMessage.toString(),
                    "Ошибки валидации",
                    JOptionPane.ERROR_MESSAGE);
        }

        return isValid;
    }

    public String[] getSourceCode() {
        String text = sourceCodeArea.getText().replaceAll("\\t+", " ").trim();
        return text.split("\\r?\\n");
    }

    public String[][] getOperationCodes() {
        if (!validateAllData()) {
            return null;
        }

        int rows = tableModel.getRowCount();
        String[][] codes = new String[rows][3];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 3; j++) {
                Object value = tableModel.getValueAt(i, j);
                codes[i][j] = (value != null) ? value.toString().trim() : "";
            }
        }
        return codes;
    }

    public void clear() {
        sourceCodeArea.setText("");
        tableModel.setRowCount(0);
    }

    private void initializeDefaultData() {
        String defaultSource = """
                PROG START 100h
                 JMP L1
                A1 RESB 10
                A2 RESW 20
                B1 WORD 4096
                B2 BYTE X"2F4C008A"
                B3 BYTE C"Hello,Assembler!"
                B4 BYTE 128
                L1 LOADR1 B1
                LOADR2 B4
                ADD R1 R2
                SUB R1 R2
                SAVER1 B1
                NOP
                END
                """;

        sourceCodeArea.setText(defaultSource);

        String[][] defaultOps = {
                {"JMP", "01", "4"},
                {"LOADR1", "02", "4"},
                {"LOADR2", "03", "4"},
                {"ADD", "04", "2"},
                {"SAVER1", "05", "4"},
                {"NOP", "06", "1"},
                {"INT", "07", "2"},
                {"SUB", "08", "2"}
        };

        tableModel.setRowCount(0);
        for (String[] row : defaultOps) {
            tableModel.addRow(row);
        }
    }

    private boolean isValidMnemonic(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.matches("[a-zA-Z][a-zA-Z0-9]*");
    }
}