package HomeWork;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class GUI extends JFrame {

    private JPanel left_panel;
    private JButton select_source_file;
    private LineNumberTextArea source_code;

    private JPanel central_panel;
    private JTable def_table;
    private DefaultTableModel def_model;
    private JTable nam_table;
    private DefaultTableModel nam_model;
    private JTable var_table;
    private DefaultTableModel var_model;

    private JPanel right_panel;
    private JButton select_result_file;
    private LineNumberTextArea result_code;

    private JPanel bottom_panel;
    private JButton step_button;
    private JButton full_pass_button;
    private JButton restart_button;
    private JButton reset_button;
    private JTextArea message_area;

    public GUI(){
        setTitle("Макропроцессор");
        setSize(1600, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());

        left_panel = create_panel("Исходные данные");
        central_panel = create_panel("Процесс обработки");
        right_panel = create_panel("Результат обработки");

        JPanel top_panel = new JPanel(new BorderLayout());

        left_panel.setPreferredSize(new Dimension(400, 600));
        right_panel.setPreferredSize(new Dimension(400, 600));

        top_panel.add(left_panel, BorderLayout.WEST);
        top_panel.add(right_panel, BorderLayout.EAST);
        top_panel.add(central_panel, BorderLayout.CENTER);

        bottom_panel = create_panel("");
        bottom_panel.setPreferredSize(new Dimension(1300, 200));

        add(top_panel, BorderLayout.CENTER);
        add(bottom_panel, BorderLayout.SOUTH);


        init_left_panel(left_panel);
        init_central_panel(central_panel);
        init_right_panel(right_panel);
        init_bottom_panel(bottom_panel);

        setVisible(true);
    }

    private JPanel create_panel(String title){
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 1),
                        title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setPreferredSize(new Dimension(400, 650));
        return panel;
    }

    private void init_left_panel(JPanel panel){
        panel.setLayout(new BorderLayout());

        source_code = new LineNumberTextArea();
        source_code.setBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        );
        panel.add(source_code, BorderLayout.CENTER);

        select_source_file = new JButton("Загрузить файл");
        select_source_file.addActionListener(e -> open_and_read_file(source_code));
        panel.add(select_source_file, BorderLayout.NORTH);

        source_code.getTextArea().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                notifyDataChanged();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                notifyDataChanged();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                notifyDataChanged();
            }
        });
    }

    private void notifyDataChanged() {
        if (on_data_changed != null) {
            on_data_changed.run();
        }
    }

    private void init_right_panel(JPanel panel) {
        panel.setLayout(new BorderLayout());

        result_code = new LineNumberTextArea();
        result_code.setBorder(
                BorderFactory.createEmptyBorder(10, 0, 0, 0)
        );
        panel.add(result_code, BorderLayout.CENTER);
        result_code.setEditable(false);

        select_result_file = new JButton("Сохранить в файл");
        select_result_file.addActionListener(e -> save_to_file(result_code));
        panel.add(select_result_file, BorderLayout.NORTH);
    }

    private void init_bottom_panel(JPanel panel) {
        panel.setLayout(new GridLayout(1, 2, 10, 0));

        step_button = new JButton("Один шаг");
        full_pass_button = new JButton("Полный проход");
        restart_button = new JButton("Перезапуск");

        int buttonWidth = Math.max(
                Math.max(step_button.getPreferredSize().width, full_pass_button.getPreferredSize().width),
                restart_button.getPreferredSize().width
        );
        int buttonHeight = 30;
        Dimension buttonSize = new Dimension(buttonWidth + 20, buttonHeight);

        step_button.setPreferredSize(buttonSize);
        full_pass_button.setPreferredSize(buttonSize);
        restart_button.setPreferredSize(buttonSize);

        JPanel leftColumn = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        leftColumn.add(step_button, gbc);
        leftColumn.add(full_pass_button, gbc);
        leftColumn.add(restart_button, gbc);

        message_area = new JTextArea();
        message_area.setLineWrap(true);
        message_area.setWrapStyleWord(true);
        message_area.setFont(new Font("Consolas", Font.PLAIN, 12));
        message_area.setEditable(false);
        message_area.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 1),
                        "Сообщения прохода"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        JScrollPane scrollPane = new JScrollPane(message_area);
        scrollPane.setPreferredSize(new Dimension(300, 50));

        reset_button = new JButton("Сброс всего");
        reset_button.setPreferredSize(buttonSize);

        JPanel rightColumn = new JPanel(new BorderLayout());
        rightColumn.add(scrollPane, BorderLayout.CENTER);

        JPanel resetWrapper = new JPanel();
        resetWrapper.add(reset_button);
        rightColumn.add(resetWrapper, BorderLayout.SOUTH);

        panel.add(leftColumn);
        panel.add(rightColumn);
    }

    private void init_central_panel(JPanel panel) {

        JPanel def_panel = new JPanel();
        def_panel.setBorder((BorderFactory.createTitledBorder("Таблица макроопределений")));
        String[] def_columns = {"Имя макроса", "Тело макроса"};
        def_model = new DefaultTableModel(def_columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        def_table = new JTable(def_model);
        def_table.setFont(new Font("Consolas", Font.PLAIN, 12));
        def_table.getTableHeader().setReorderingAllowed(false);
        def_table.setRowHeight(20);

        def_table.getTableHeader().setReorderingAllowed(false);

        TableColumnModel columnModel = def_table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(350);

        JScrollPane def_scrollPane = new JScrollPane(def_table);
        Dimension defSize = new Dimension(500, 300);
        def_scrollPane.setPreferredSize(defSize);
        def_scrollPane.setMaximumSize(defSize);
        def_panel.add(def_scrollPane);

        JPanel nam_panel = new JPanel(new BorderLayout());
        nam_panel.setBorder((BorderFactory.createTitledBorder("Таблица имен макросов")));
        String[] nam_columns = {"Имя макроса", "Начало", "Длина"};
        nam_model = new DefaultTableModel(nam_columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        nam_table = new JTable(nam_model);
        nam_table.setFont(new Font("Consolas", Font.PLAIN, 12));
        nam_table.getTableHeader().setReorderingAllowed(false);

        columnModel = nam_table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(75);
        columnModel.getColumn(2).setPreferredWidth(75);

        JScrollPane nam_scrollPane = new JScrollPane(nam_table);
        nam_scrollPane.setPreferredSize(new Dimension(300, 200));
        nam_panel.add(nam_scrollPane);


        JPanel var_panel = new JPanel();
        var_panel.setBorder((BorderFactory.createTitledBorder("Таблица переменных")));
        String[] var_columns = {"Имя переменной", "Тип", "Значение", "Область видимости"};
        var_model = new DefaultTableModel(var_columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        var_table = new JTable(var_model);
        var_table.setFont(new Font("Consolas", Font.PLAIN, 12));
        var_table.getTableHeader().setReorderingAllowed(false);
        var_table.setRowHeight(20);

        columnModel = var_table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(120);

        JScrollPane var_scrollPane = new JScrollPane(var_table);
        Dimension varSize = new Dimension(370, 300);
        var_scrollPane.setPreferredSize(varSize);
        var_scrollPane.setMaximumSize(varSize);
        var_panel.add(var_scrollPane);

        panel.add(nam_panel);
        panel.add(var_panel);
        panel.add(def_panel);

    }

    public void clear() {
        source_code.setText("");
        def_model.setRowCount(0);
        var_model.setRowCount(0);
        nam_model.setRowCount(0);
        result_code.setText("");
        message_area.setText("");
    }

    public void restart(){
        def_model.setRowCount(0);
        var_model.setRowCount(0);
        nam_model.setRowCount(0);
        result_code.setText("");
        message_area.setText("");
    }

    public void set_message(String message, boolean is_error) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String word : message.split("\\s+")) {
            if (count + word.length() > 100) {
                sb.append("\n");
                count = 0;
            }
            if (count > 0) sb.append(" ");
            sb.append(word);
            count += word.length() + 1;
        }
        message_area.setText(sb.toString());
        message_area.setForeground(is_error ? Color.RED : Color.GREEN);
    }

    public void update_def_table(String[][] data) {
        def_model.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 2) {
                def_model.addRow(row);
            }
        }
    }
    public void update_var_table(String[][] data) {
        var_model.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 4) {
                var_model.addRow(row);
            }
        }
    }

    public void update_nam_table(String[][] data) {
        nam_model.setRowCount(0);
        for (String[] row : data) {
            if (row.length == 3) {
                nam_model.addRow(row);
            }
        }
    }

    public void update_result_code(String text) {
        result_code.setText(text);
    }

    public String[] get_source_code() {
        String text = source_code.getText().replaceAll("\\t+", " ");
        return text.split("\\r?\\n");
    }

    private void open_and_read_file(LineNumberTextArea area) {
        File currentDir = new File(System.getProperty("user.dir"));
        JFileChooser fileChooser = new JFileChooser(currentDir);

        fileChooser.setDialogTitle("Выберите текстовый файл");
        fileChooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Text files (*.txt)", "txt")
        );

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
                area.setText(content.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "Ошибка чтения файла",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void save_to_file(LineNumberTextArea area) {
        Object[] options = {"Сохранить в существующий", "Сохранить в новый", "Отмена"};

        int choice = JOptionPane.showOptionDialog(
                this,
                "Как сохранить файл?",
                "Сохранение",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            return;
        }

        File currentDir = new File(System.getProperty("user.dir"));
        JFileChooser fileChooser = new JFileChooser(currentDir);
        fileChooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Text files (*.txt)", "txt")
        );

        File file;

        try {
            if (choice == 0) {
                int result = fileChooser.showOpenDialog(this);
                if (result != JFileChooser.APPROVE_OPTION) return;
                file = fileChooser.getSelectedFile();

            } else {
                int result = fileChooser.showSaveDialog(this);
                if (result != JFileChooser.APPROVE_OPTION) return;
                file = fileChooser.getSelectedFile();

                if (!file.getName().contains(".")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }

                if (file.exists()) {
                    int overwrite = JOptionPane.showConfirmDialog(
                            this,
                            "Файл уже существует. Перезаписать?",
                            "Подтверждение",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (overwrite != JOptionPane.YES_OPTION) return;
                }
            }


            try (java.io.BufferedWriter writer = new java.io.BufferedWriter(
                    new java.io.FileWriter(file))) {
                writer.write(area.getText());
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Файл успешно сохранён:\n" + file.getAbsolutePath(),
                    "Готово",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Ошибка записи файла",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static class LineNumberTextArea extends JPanel {
        private final JTextArea textArea;
        private final JTextArea lineNumbers;

        public LineNumberTextArea() {
            setLayout(new BorderLayout());

            lineNumbers = new JTextArea("1");
            lineNumbers.setEditable(false);
            lineNumbers.setBackground(Color.LIGHT_GRAY);
            lineNumbers.setForeground(Color.BLACK);
            lineNumbers.setOpaque(true);
            lineNumbers.setFont(new Font("Consolas", Font.PLAIN, 14));
            lineNumbers.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            lineNumbers.setFocusable(false);

            textArea = new JTextArea(25, 15);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            textArea.setLineWrap(false);
            textArea.setWrapStyleWord(false);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setRowHeaderView(lineNumbers);
            scrollPane.setRowHeaderView(lineNumbers);

            add(scrollPane, BorderLayout.CENTER);

            textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    updateLineNumbers();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    updateLineNumbers();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    updateLineNumbers();
                }
            });
        }

        private void updateLineNumbers() {
            int lines = textArea.getLineCount();
            StringBuilder numbers = new StringBuilder();
            for (int i = 1; i <= lines; i++) {
                numbers.append(i).append(System.lineSeparator());
            }
            lineNumbers.setText(numbers.toString());
        }

        public JTextArea getTextArea() {
            return textArea;
        }

        public void setText(String text) {
            textArea.setText(text);
            updateLineNumbers();
        }

        public String getText() {
            return textArea.getText();
        }

        public void setEditable(boolean editable) {
            textArea.setEditable(editable);
        }

        public void setLineNumbersEnable(boolean enabled) {
            textArea.setEnabled(enabled);
            textArea.setEditable(enabled);
            textArea.setBackground(enabled ? Color.WHITE : Color.LIGHT_GRAY);
        }
    }

    private Runnable on_data_changed;

    public void set_on_data_changed(Runnable on_data_changed) {
        this.on_data_changed = on_data_changed;
    }

    public void enable_step_button(boolean enable){
        step_button.setEnabled(enable);
    }

    public void enable_full_pass_button(boolean enable) {
        full_pass_button.setEnabled(enable);
    }

    public void enable_select_source_file(boolean enable) {
        select_source_file.setEnabled(enable);
    }

    public void enable_select_result_file(boolean enable) {
        select_result_file.setEnabled(enable);
    }

    public void enable_source_code(boolean enable) {
        source_code.setEditable(enable);
        source_code.setLineNumbersEnable(enable);
    }

    public void setStepButtonListener(Runnable listener) {
        step_button.addActionListener(e -> listener.run());
    }

    public void setFullPassButtonListener(Runnable listener) {
        full_pass_button.addActionListener(e -> listener.run());
    }

    public void setRestartButtonListener(Runnable listener) {
        restart_button.addActionListener(e -> listener.run());
    }

    public void setResetButtonListener(Runnable listener) {
        reset_button.addActionListener(e -> listener.run());
    }

}