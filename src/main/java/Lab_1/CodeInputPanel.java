package Lab_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CodeInputPanel extends JPanel {
    private JTextArea sourceCodeArea;
    private JTable operationTable;

    public CodeInputPanel() {
        setupPanel();
        createComponents();
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

        String[] columns = {"Мнемоника", "Двоичный код", "Размер"};
        DefaultTableModel model = new DefaultTableModel(columns, 10) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };

        operationTable = new JTable(model);
        operationTable.setFont(new Font("Consolas", Font.PLAIN, 11));
        operationTable.setRowHeight(20);

        JScrollPane scrollPane = new JScrollPane(operationTable);
        scrollPane.setPreferredSize(new Dimension(320, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public String[] getSourceCode() {
        String[] lines = sourceCodeArea.getText().split("\\n");
        return lines;
    }

    public String[][] getOperationCodes() {
        DefaultTableModel model = (DefaultTableModel) operationTable.getModel();
        int rows = model.getRowCount();
        String[][] codes = new String[rows][3];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < 3; j++) {
                Object value = model.getValueAt(i, j);
                codes[i][j] = (value != null) ? value.toString() : "";
            }
        }
        return codes;
    }

    public void clear() {
        sourceCodeArea.setText("");
        DefaultTableModel model = (DefaultTableModel) operationTable.getModel();
        model.setRowCount(0);
        model.setRowCount(10);
    }
}