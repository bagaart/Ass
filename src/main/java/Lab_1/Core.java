package Lab_1;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Core extends JFrame {
    private CodeInputPanel codeInputPanel;
    private ProcessingPanel processingPanel;
    private ResultsPanel resultsPanel;
    private ControlPanel controlPanel;

    public Core() {
        initializeComponents();
        setupInterface();
        configureWindow();
    }

    private void initializeComponents() {
        // engine = new Core();
        codeInputPanel = new CodeInputPanel();
        processingPanel = new ProcessingPanel();
        resultsPanel = new ResultsPanel();
        controlPanel = new ControlPanel(this);
    }

    private void setupInterface() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainContent = new JPanel(new GridLayout(1, 3, 15, 0));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        mainContent.add(codeInputPanel);
        mainContent.add(processingPanel);
        mainContent.add(resultsPanel);

        add(mainContent, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void configureWindow() {
        setTitle("Двухпросмотровый ассемблер для программ в абсолютном формате");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void executeFirstPass() {
        String sourceText = String.join("\n", codeInputPanel.getSourceCode());
        String[][] opTable = codeInputPanel.getOperationCodes();

        FirstPass firstPass = new FirstPass();
        firstPass.execute(sourceText, opTable);

        if (!firstPass.getERROR().isEmpty()) {
            processingPanel.showFirstPassMessage(firstPass.getERROR(), true);
            resultsPanel.clearSecondPassMessages();
            controlPanel.enableSecondPassButton(false);
            return;
        }

        ArrayList<ArrayList<String>> subTable = firstPass.getSubTable();
        ArrayList<ArrayList<String>> symTable = firstPass.getSymTable();

        processingPanel.updateSupportTable(listToArray(subTable, 4));
        processingPanel.updateSymbolTable(listToArray(symTable, 2));

        processingPanel.showFirstPassMessage("Первый проход успешно выполнен.", false);
        resultsPanel.clearSecondPassMessages();

        controlPanel.enableSecondPassButton(true);
    }

    public void executeSecondPass() {
        String[][] opTable = codeInputPanel.getOperationCodes();
        String sourceText = String.join("\n", codeInputPanel.getSourceCode());

        FirstPass firstPass = new FirstPass();
        firstPass.execute(sourceText, opTable);
        if (!firstPass.getERROR().isEmpty()) {
            resultsPanel.showSecondPassMessage("Ошибка первого прохода: " + firstPass.getERROR(), true);
            return;
        }

        SecondPass secondPass = new SecondPass();
        secondPass.execute(firstPass.getSubTable(), firstPass.getSymTable(),
                firstPass.getProgramName(), firstPass.getProgramLength(), firstPass.getStartAddress());

        if (!secondPass.getERROR().isEmpty()) {
            resultsPanel.showSecondPassMessage("Ошибка второго прохода: " + secondPass.getERROR(), true);
            return;
        }

        resultsPanel.displayBinaryCode(secondPass.getObjCode().toArray(new String[0]));
        resultsPanel.showSecondPassMessage("Второй проход успешно выполнен.", false);
    }

    private String[][] listToArray(ArrayList<ArrayList<String>> list, int cols) {
        String[][] array = new String[list.size()][cols];
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < cols; j++) {
                array[i][j] = list.get(i).get(j);
            }
        }
        return array;
    }

    public void clearAllData() {
        codeInputPanel.clear();
        processingPanel.clear();
        resultsPanel.clear();
        controlPanel.enableSecondPassButton(false);
    }



}