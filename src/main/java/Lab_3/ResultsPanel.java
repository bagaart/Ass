package Lab_3;

import javax.swing.*;
import java.awt.*;

public class ResultsPanel extends JPanel {
    private JTextArea binaryCodeArea;
    private JTextArea secondPassMessages;

    public ResultsPanel() {
        setupPanel();
        createComponents();
    }

    private void setupPanel() {
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Результаты компиляции"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        setPreferredSize(new Dimension(400, 800));
        setMaximumSize(new Dimension(400, Integer.MAX_VALUE));
    }

    private void createComponents() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JPanel binaryPanel = new JPanel(new BorderLayout());
        binaryPanel.setBorder(BorderFactory.createTitledBorder("Сгенерированный код"));

        binaryCodeArea = new JTextArea();
        binaryCodeArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        binaryCodeArea.setEditable(false);
        binaryCodeArea.setBackground(new Color(250, 250, 250));

        JScrollPane binaryScroll = new JScrollPane(binaryCodeArea);
        binaryPanel.add(binaryScroll, BorderLayout.CENTER);

        JPanel messagesPanel = new JPanel(new BorderLayout());
        messagesPanel.setBorder(BorderFactory.createTitledBorder("Сообщения второго прохода"));
        messagesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        secondPassMessages = new JTextArea(4, 25);
        secondPassMessages.setFont(new Font("SansSerif", Font.PLAIN, 11));
        secondPassMessages.setEditable(false);
        secondPassMessages.setBackground(new Color(240, 240, 240));

        JScrollPane messagesScroll = new JScrollPane(secondPassMessages);
        messagesPanel.add(messagesScroll, BorderLayout.CENTER);
        messagesScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        messagesScroll.setPreferredSize(new Dimension(500, 100));

        mainPanel.add(binaryPanel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(messagesPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    public void displayBinaryCode(String[] binaryCode) {
        StringBuilder sb = new StringBuilder();
        for (String line : binaryCode) {
            sb.append(line).append("\n");
        }
        binaryCodeArea.setText(sb.toString());
    }

    public void showSecondPassMessage(String message, boolean isError) {
        secondPassMessages.setText(message + "\n");
        secondPassMessages.setForeground(isError ? Color.RED : new Color(0, 128, 0));
    }

    public void clear() {
        binaryCodeArea.setText("");
        secondPassMessages.setText("");
    }

    public void clearSecondPassMessages() {
        secondPassMessages.setText("");
    }
}
