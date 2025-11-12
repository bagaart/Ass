package Lab_4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class ResultsPanel extends JPanel {
    private JTable objCode;
    private DefaultTableModel objCodeModel;

    public ResultsPanel() {
        setupPanel();
        add(createObjTable());
    }

    private void setupPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Результаты трансляции"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setPreferredSize(new Dimension(500, 900));
    }

    private JPanel createObjTable() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Сгенерированный код"));

        String[] columns = {
                "<html>Тип</html>",
                "<html>Адрес<br>ячейки</html>",
                "<html>Длина<br>(полубайт)</html>",
                "<html>Код<br>операции</html>",
                "<html>Операнд<br>1</html>",
                "<html>Операнд<br>2</html>"
        };

        objCodeModel = new DefaultTableModel(columns, 0);
        objCode = new JTable(objCodeModel);
        objCode.setFont(new Font("Consolas", Font.PLAIN, 11));
        objCode.setRowHeight(20);

        TableColumnModel columnModel = objCode.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(10);
        columnModel.getColumn(1).setPreferredWidth(40);
        columnModel.getColumn(2).setPreferredWidth(50);
        columnModel.getColumn(3).setPreferredWidth(50);
        columnModel.getColumn(4).setPreferredWidth(90);
        columnModel.getColumn(5).setPreferredWidth(90);

        JScrollPane scrollPane = new JScrollPane(objCode);
        scrollPane.setPreferredSize(new Dimension(480, 760));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;

    }

    public void updateResultTable(java.util.List<String[]> data) {
        objCodeModel.setRowCount(0);
        for (String[] row : data) {
            objCodeModel.addRow(row);
        }
    }

    public void clear() {
        objCodeModel.setRowCount(0);
    }
}