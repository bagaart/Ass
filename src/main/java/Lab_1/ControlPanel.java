package Lab_1;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private Core parent;
    private JButton secondPassBtn;

    public ControlPanel(Core parent) {
        this.parent = parent;
        setupPanel();
        createButtons();
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
}