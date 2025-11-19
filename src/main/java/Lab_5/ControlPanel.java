package Lab_5;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private Core parent;
    JButton stepBtn;
    JButton runBtn;
    JButton restartBtn;
    private JComboBox<String> addressingModeComboBox;

    public ControlPanel(Core parent) {
        this.parent = parent;
        setupPanel();
        createButtons();
        createAddressingModeSelector();
    }

    private void setupPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 15));
        setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        setBackground(new Color(240, 240, 240));
    }

    private void createButtons() {
        stepBtn = createNormalButton("Один шаг");
        runBtn = createNormalButton("Запуск / Продолжить");
        restartBtn = createNormalButton("Перезапуск");

        add(stepBtn);
        add(runBtn);
        add(restartBtn);

        stepBtn.addActionListener(e -> parent.stepOneLine());
        runBtn.addActionListener(e -> parent.runToEnd());
        restartBtn.addActionListener(e -> parent.resetAll());
    }

    public void enableStepButton(boolean enable) {
        stepBtn.setEnabled(enable);
    }

    public void enableRunButton(boolean enable) {
        runBtn.setEnabled(enable);
    }

    public void enableRestartButton(boolean enable) {
        restartBtn.setEnabled(enable);
    }


    private JButton createNormalButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(200, 35));
        button.setFocusPainted(true);

        return button;
    }

    private void createAddressingModeSelector() {
        JLabel label = new JLabel("Способ адресации:");
        label.setFont(new Font("Arial", Font.PLAIN, 12));

        String[] addressingModes = {"Прямая", "Относительная", "Смешанная"};
        addressingModeComboBox = new JComboBox<>(addressingModes);
        addressingModeComboBox.setSelectedItem("Смешанная");
        addressingModeComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
        addressingModeComboBox.setPreferredSize(new Dimension(150, 30));

        addressingModeComboBox.addActionListener(e -> {
            String selectedMode = (String) addressingModeComboBox.getSelectedItem();
            parent.setAddressingMode(selectedMode);
        });

        add(label);
        add(addressingModeComboBox);
    }

    public String getSelectedAddressingMode() {
        return (String) addressingModeComboBox.getSelectedItem();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        addressingModeComboBox.setEnabled(enabled);
    }
}