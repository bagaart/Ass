package Lab_2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Core extends JFrame {
    private CodeInputPanel codeInputPanel;
    private ProcessingPanel processingPanel;
    private ResultsPanel resultsPanel;
    private ControlPanel controlPanel;
    FirstPass firstPass = new FirstPass();
    SecondPass secondPass = new SecondPass();
    boolean firstPas = false;
    String addressingMode;


    public Core() {
        initializeComponents();
        setupInterface();
        configureWindow();
    }

    private void initializeComponents() {
        codeInputPanel = new CodeInputPanel();
        processingPanel = new ProcessingPanel();
        resultsPanel = new ResultsPanel();
        controlPanel = new ControlPanel(this);
        setAddressingMode(controlPanel.getSelectedAddressingMode());
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
        setTitle("Двухпросмотровый ассемблер для программ в перемещаемом формате");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void executeFirstPass() {
        firstPas = false;
        resultsPanel.clear();
        String sourceText = String.join("\n", codeInputPanel.getSourceCode());
        String[][] opTable = codeInputPanel.getOperationCodes();
        if (opTable != null) {
            firstPass.execute(sourceText, opTable, addressingMode);

            if (!firstPass.getERROR().isEmpty()) {
                processingPanel.showFirstPassMessage(firstPass.getERROR(), true);
                resultsPanel.clearSecondPassMessages();
                disableSecondPass();
                return;
            }

            ArrayList<ArrayList<String>> subTable = firstPass.getSubTable();
            ArrayList<ArrayList<String>> symTable = firstPass.getSymTable();

            processingPanel.updateSupportTable(listToArray(subTable, 4));
            processingPanel.updateSymbolTable(listToArray(symTable, 2));

            processingPanel.showFirstPassMessage("Первый проход успешно выполнен.", false);
            resultsPanel.clearSecondPassMessages();

            enableSecondPass();
            firstPas = true;
        }
    }

    public void executeSecondPass() {
        String[][] opTable = codeInputPanel.getOperationCodes();
        String sourceText = String.join("\n", codeInputPanel.getSourceCode());

        if (!firstPass.getERROR().isEmpty() && firstPas) {
            resultsPanel.showSecondPassMessage(firstPass.getERROR(), true);
            return;
        }
        secondPass.execute(firstPass.getSubTable(), firstPass.getSymTable(),
                firstPass.getProgramName(), firstPass.getProgramLength(), firstPass.getStartAddress(), opTable,
                addressingMode);

        if (!secondPass.getERROR().isEmpty()) {
            resultsPanel.showSecondPassMessage(secondPass.getERROR(), true);
            return;
        }

        processingPanel.updateModificationTable(secondPass.getModTable().toArray(new String[0]));
        resultsPanel.displayBinaryCode(secondPass.getObjCode().toArray(new String[0]));
        resultsPanel.showSecondPassMessage("Второй проход успешно выполнен.", false);
        disableSecondPass();
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

    public void setAddressingMode(String addressingMode) {
        this.addressingMode = addressingMode;
    }

    public void enableSecondPass(){
        controlPanel.enableSecondPassButton(true);
    }

    public void disableSecondPass(){
        controlPanel.enableSecondPassButton(false);
    }
}