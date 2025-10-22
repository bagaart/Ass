package Lab_2;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private Core parent;
    private JButton secondPassBtn;
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
        JButton firstPassBtn = createNormalButton("Выполнить первый проход");
        secondPassBtn = createNormalButton("Выполнить второй проход");
        JButton clearBtn = createNormalButton("Очистить все");

        secondPassBtn.setEnabled(false);

        firstPassBtn.addActionListener(e -> parent.executeFirstPass());
        secondPassBtn.addActionListener(e -> parent.executeSecondPass());
        clearBtn.addActionListener(e -> parent.clearAllData());

        add(firstPassBtn);
        add(secondPassBtn);
        add(clearBtn);
    }

    public void enableSecondPassButton(boolean enable) {
        secondPassBtn.setEnabled(enable);
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
            parent.disableSecondPass();
        });

        add(label);
        add(addressingModeComboBox);
    }

    public String getSelectedAddressingMode() {
        return (String) addressingModeComboBox.getSelectedItem();
    }
}