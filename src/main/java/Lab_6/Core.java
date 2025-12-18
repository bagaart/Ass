package Lab_6;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Core extends JFrame {
    private CodeInputPanel codeInputPanel;
    private ProcessingPanel processingPanel;
    private ResultsPanel resultsPanel;
    private ControlPanel controlPanel;

    String ERROR = "";
    String[][] opTable;
    String addressingMode;
    List<String[]> symTable = new ArrayList<>();
    List<String[]> resultTable = new ArrayList<>();
    List<String[]> modTable = new ArrayList<>();

    int startAddress = 0;
    int endAddress = 0;
    int currentAddress = 0;
    int lineID;
    int linesCount;


    boolean startFlag = false;
    boolean endFlag = false;

    int MAX_MEMORY_ADR = 16777215;

    String label;
    String operation;
    String operand_1;
    String operand_2;

    String currentModule;

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
        addressingMode = controlPanel.getSelectedAddressingMode();

        codeInputPanel.setOnDataChanged(() -> {
            if (lineID != 0) {
                controlPanel.enableStepButton(false);
                controlPanel.enableRunButton(false);
            }
        });
    }

    private void setupInterface() {
        setLayout(new BorderLayout(10, 10));

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.X_AXIS));
        mainContent.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        codeInputPanel.setMaximumSize(new Dimension(350,Integer.MAX_VALUE));
        codeInputPanel.setPreferredSize(new Dimension(350, 700));

        mainContent.add(codeInputPanel);
        mainContent.add(Box.createRigidArea(new Dimension(15, 0)));
        mainContent.add(processingPanel);
        mainContent.add(Box.createRigidArea(new Dimension(15, 0)));
        mainContent.add(resultsPanel);

        add(mainContent, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

    }

    private int currentLineIndex = 0;
    private String[] sourceLines;

    public void stepOneLine() {
        setAddressingMode(controlPanel.getSelectedAddressingMode());
        if (sourceLines == null) {
            sourceLines = codeInputPanel.getSourceCode();
            linesCount = sourceLines.length;
            currentLineIndex = 0;
            lockInputs(true);
        }

        if (currentLineIndex >= linesCount) {
            lockInputs(false);
            return;
        }

        String line = sourceLines[currentLineIndex];
        lineID = currentLineIndex;
        step(line);

        if (!ERROR.isEmpty()) {
            ERROR = lineID + " -- " + ERROR;
            processingPanel.showErrorMessage(ERROR, true);
            controlPanel.enableStepButton(false);
            controlPanel.enableRunButton(false);
            return;
        } else {
            processingPanel.showErrorMessage("Выполнена строка " + (lineID), false);
        }

        updateTables();
        currentLineIndex++;

        if (endFlag || currentLineIndex >= linesCount) {
            controlPanel.enableStepButton(false);
            controlPanel.enableRunButton(false);
            processingPanel.showErrorMessage("Успешно завершено.", false);
            lockInputs(false);
        }
    }

    public void runToEnd() {
        setAddressingMode(controlPanel.getSelectedAddressingMode());
        if (opTable == null) {
            opTable = codeInputPanel.getOperationCodes();
            if (opTable == null) return;
        }

        if (sourceLines == null) {
            sourceLines = codeInputPanel.getSourceCode();
            linesCount = sourceLines.length;
            currentLineIndex = 0;
            lockInputs(true);
        }

        while (currentLineIndex < linesCount && !endFlag && ERROR.isEmpty()) {
            lineID = currentLineIndex;
            step(sourceLines[currentLineIndex]);
            currentLineIndex++;
        }

        updateTables();
        lockInputs(false);

        if (!ERROR.isEmpty()) {
            ERROR = lineID + " -- " + ERROR;
            controlPanel.enableStepButton(false);
            controlPanel.enableRunButton(false);
            processingPanel.showErrorMessage(ERROR, true);
        } else {
            processingPanel.showErrorMessage("Успешно завершено.", false);
        }

        controlPanel.enableStepButton(false);
        controlPanel.enableRunButton(false);
    }

    public void resetAll() {
        ERROR = "";
        symTable.clear();
        resultTable.clear();
        sourceLines = null;
        currentLineIndex = 0;
        startFlag = false;
        endFlag = false;
        processingPanel.clear();
        resultsPanel.clear();
        modTable.clear();
        lockInputs(false);
        controlPanel.enableStepButton(true);
        controlPanel.enableRunButton(true);
        setAddressingMode(controlPanel.getSelectedAddressingMode());
    }

    private void lockInputs(boolean lock) {
        codeInputPanel.setEnabled(!lock);
        controlPanel.setEnabled(!lock);
    }

    private void updateTables() {
        if (!symTable.isEmpty()) {
            String[][] symData = new String[symTable.size()][5];
            for (int i = 0; i < symTable.size(); i++) {
                symData[i][0] = symTable.get(i)[0];
                symData[i][1] = symTable.get(i)[1];
                symData[i][2] = symTable.get(i)[2];
                symData[i][3] = symTable.get(i)[3];
                symData[i][4] = symTable.get(i)[4];
            }
            processingPanel.updateSymbolTable(symData);
        }

        if (!modTable.isEmpty()) {
            String[][] modData = new String[modTable.size()][3];
            for (int i = 0; i < modTable.size(); i++) {
                String[] row = modTable.get(i);
                if (row != null && row.length >= 3) {
                    modData[i] = new String[]{row[0], row[1], row[2]};
                } else {
                    modData[i] = new String[]{"", "", ""};
                }

            }
            processingPanel.updateModificationTable(modData);
        }

        if (!resultTable.isEmpty()) {
            resultsPanel.updateResultTable(resultTable);
        }
    }

    private void configureWindow() {
        setTitle("Однопросмотровый ассемблер для программ в полном перемещаемом формате");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 1000);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    public void setAddressingMode(String addressingMode) {
        this.addressingMode = addressingMode;
    }


    private void step(String line){
        opTable = codeInputPanel.getOperationCodes();
        if (opTable == null){
            ERROR = "Пустая таблица ТКО";
            return;
        }
        ERROR = "";

        if (lineID == 0) {
            symTable.clear();
            resultTable.clear();

            symTable = new ArrayList<>();
            resultTable = new ArrayList<>();

        }

        if (line.isEmpty()) {
            lineID += 1;
            return;
        }

        if (prepareLine(line)) {
            if (!ERROR.isEmpty()) return;
            if (operation.equalsIgnoreCase("START")) {
                if (startFlag) {
                    ERROR = "Ошибка: Повторное использование START";
                    return;
                }
                if (label.isEmpty()) {
                    ERROR = "Ошибка: Не задано имя программы";
                    return;
                }

                if (isNumber(operand_1)) {
                    currentAddress = parseNumber(operand_1);
                    if (currentAddress != 0) {
                        ERROR = "Ошибка: Неверный адрес начала программы";
                        return;
                    }
                    startAddress = currentAddress;
                } else {
                    ERROR ="Ошибка: Адрес должен быть числом";
                    return;
                }

                if (!operand_2.isEmpty()) {
                    ERROR = "Ошибка: Некорректная операндная часть";
                    return;
                }
                currentModule = label;
                startFlag = true;
                symTable.add(new String[]{label, label, "", "", "Модуль"});
                int addres = parseNumber(operand_1);
                rowToResultTable("H", label, hexAddress(addres), "", "", "");
            }

            if (operation.equalsIgnoreCase("END")) {
                if (!startFlag) {
                    ERROR = "Ошибка: END без START";
                    return;
                }
                if (!label.isEmpty()) {
                    ERROR = "Ошибка: метка у директивы END";
                    return;
                }

                if (!operand_1.isEmpty()) {
                    ERROR = "Ошибка: некорректная операндная часть";
                    return;
                }

                if (!operand_2.isEmpty()) {
                    ERROR = "Ошибка: некорректная операндная часть";
                    return;
                }

                for (String[] check : symTable) {
                    if (check[0].equals(check[1])) continue;
                    if (check[2].isEmpty() && check[0].equals(currentModule) && (check[4].isEmpty() || check[4].equals("ВИ"))) {
                        ERROR = "Ошибка: Неопределенная метка " + check[1] + " в модуле " + check[0];
                        return;
                    }
                }

                for (int j = 0; j < modTable.size(); j++){
                    if (modTable.get(j)[0].equals(currentModule)) {
                        rowToResultTable("M", modTable.get(j)[1], modTable.get(j)[2], "", "", "");
                    }
                }

                endFlag = true;
                endAddress = currentAddress;

                for (String[] row: resultTable){
                    if (row[1].equals(currentModule)) {
                        row[3] = hexAddress(currentAddress - startAddress);
                    }
                }

                rowToResultTable("E", hexAddress(startAddress), "", "", "", "");

                return;
            }

            if (operation.equalsIgnoreCase("CSECT")) {

                if (label.isEmpty()){
                    ERROR = "Ошибка: Не задано имя модуля";
                    return;
                }

                if (!operand_1.isEmpty()) {
                    ERROR = "Ошибка: некорректная операндная часть";
                    return;
                }

                if (!operand_2.isEmpty()) {
                    ERROR = "Ошибка: некорректная операндная часть";
                    return;
                }

                for (String[] check : symTable) {
                    if (check[0].equals(check[1])) continue;
                    if (check[2].isEmpty() && check[0].equals(currentModule) && (check[4].isEmpty() || check[4].equals("ВИ"))) {
                        ERROR = "Ошибка: Неопределенная метка " + check[1] + " в модуле " + check[0];
                        return;
                    }
                }

                for (String[] row: resultTable){
                    if (row[1].equals(currentModule)) {
                        row[3] = hexAddress(currentAddress - startAddress);
                    }
                }

                for (String[] strings : modTable) {
                    if (strings[0].equals(currentModule)) {
                        rowToResultTable("M", strings[1], strings[2], "", "", "");
                    }
                }

                rowToResultTable("E", hexAddress(startAddress), "", "", "", "");
                rowToResultTable("", "", "", "", "", "");

                currentModule = label;
                startAddress = 0;
                endAddress = 0;
                currentAddress = 0;

                symTable.add(new String[]{label, label, "", "", "Модуль"});
                rowToResultTable("H", label, hexAddress(startAddress), "", "", "");

                return;
            }

            if (!label.isEmpty() && startFlag && !operation.equalsIgnoreCase("START")) {
                String actualLabel = getActualLabel(label);
                if (findLabel(actualLabel) == -1) {
                    if (isExtDef(actualLabel, symTable)) {
                        for (int i = 0; i < symTable.size(); i++){
                            String[] row = symTable.get(i);
                            if (row[0].equals(currentModule) && row[1].equals(actualLabel) && row[4].equals("ВИ"))
                                row[2] = hexAddress(currentAddress);
                        }
                        for (int i = 0; i < resultTable.size(); i++){
                            String[] row = resultTable.get(i);
                            if (row[0].equals("D") && row[1].equals(actualLabel)) row[2] = hexAddress(currentAddress);
                        }
                    } else {
                        symTable.add(new String[]{currentModule, actualLabel, hexAddress(currentAddress), "", ""});
                    }

                    Iterator<String[]> iterator = symTable.iterator();
                    while (iterator.hasNext()) {
                        String[] check = iterator.next();
                        if (check[1].equals(actualLabel) && check[2].isEmpty() && check[0].equals(currentModule)) {
                            for (String[] l : resultTable) {
                                    String resultLabel = l[4];
                                    String commandAddress = l[1];
                                    if (isRelativeAddressing(resultLabel)) {
                                        resultLabel = getActualLabel(resultLabel);
                                        if (resultLabel.equals(actualLabel)) {
                                            l[4] = hexAddress(
                                                    Integer.parseInt(findLabelAddress(actualLabel), 16) -
                                                            (Integer.parseInt(commandAddress, 16) + Integer.parseInt(l[2], 16) / 2));
                                        }
                                    } else {
                                        if (resultLabel.equals(actualLabel)) {
                                            l[4] = hexAddress(currentAddress);
                                        }
                                    }

                            }
                            if (!isExtDef(actualLabel, symTable) || (isExtDef(actualLabel, symTable) && !findLabelAddress(actualLabel).isEmpty())) {
                                iterator.remove();
                            } else {
                                check[3] = "";
                            }
                        }
                    }
                } else {
                    ERROR = "Ошибка: Метка уже существует";
                    return;
                }
            }

            int increment = 0;
            if (isDirective(operation)) {
                switch (operation.toUpperCase()) {
                    case "WORD":
                        increment = 3;
                        if (parseNumber(operand_1) < 0 || parseNumber(operand_1) > parseNumber("ffffffh")) {
                            ERROR = "Ошибка: некорректное числовое значение";
                            return;
                        }
                        if (!operand_2.isEmpty()) {
                            ERROR = "Ошибка: Некорректная операндная часть";
                            return;
                        }
                        rowToResultTable("T", hexAddress(currentAddress), "06", "", hexAddress(parseNumber(operand_1)), "");
                        break;
                    case "RESW":
                        if (!isNumber(operand_1)) {
                            ERROR = "Ошибка: RESW требует числового операнда";
                            return;
                        }
                        if (parseNumber(operand_1) < 0) {
                            ERROR = "Ошибка: RESW требует неотрицательного числа";
                            return;
                        }
                        if (!operand_2.isEmpty()) {
                            ERROR = "Ошибка: Некорректная операндная часть";
                            return;
                        }
                        increment = 3 * parseNumber(operand_1);
                        rowToResultTable("T", hexAddress(currentAddress), String.format("%02X", increment * 2), "", "", "");
                        break;
                    case "RESB":
                        if (!isNumber(operand_1)) {
                            ERROR = "Ошибка: RESB требует числового операнда";
                            return;
                        }
                        if (!operand_2.isEmpty()) {
                            ERROR = "Ошибка: Некорректная операндная часть";
                            return;
                        }
                        increment = parseNumber(operand_1);
                        rowToResultTable("T", hexAddress(currentAddress), String.format("%02X", increment * 2), "", "", "");
                        break;
                    case "BYTE":
                        int size = calcByteSize(operand_1);
                        if (size == -1) {
                            ERROR = "Ошибка: Некорректный операнд BYTE " + operand_1;
                            return;
                        }
                        String code = getByteObjectCode(operand_1);
                        if (code == null) {
                            ERROR = "Ошибка: некорректный BYTE операнд " + operand_1;
                            return;
                        }
                        if (!operand_2.isEmpty()) {
                            ERROR = "Ошибка: Некорректная операндная часть";
                            return;
                        }
                        rowToResultTable("T", hexAddress(currentAddress), String.format("%02X", size * 2), "", code, "");
                        increment = size;
                        break;
                    case "EXTREF":
                        if (!label.isEmpty()) {
                            ERROR = "Ошибка: нельзя поcтавить метку на EXTREF";
                            return;
                        }
                        if ((operand_1 == null || operand_1.isEmpty()) && (operand_2 == null || operand_2.isEmpty())) {
                            ERROR = "Ошибка: EXTREF требует список имён";
                            return;
                        }
                        String allRefOperands = operand_1;
                        if (operand_2 != null && !operand_2.isEmpty()) {
                            allRefOperands += "," + operand_2;
                        }
                        String[] refSymbols = allRefOperands.split(",");
                        for (String sym : refSymbols) {
                            sym = sym.trim();
                            if (sym.isEmpty()) continue;
                            if (!sym.isEmpty()) {
                                char first = operand_1.charAt(0);
                                if (!(isLetter(first) || isSpecialSymbol(first))) {
                                    ERROR = "Ошибка: Некорректное имя метки\n";
                                    return;
                                }
                            }
                            if (!isLabel(sym)) {
                                ERROR = "Ошибка: Некорректное имя во внешней ссылке: " + sym;
                                return;
                            }
                            if (isExtDef(sym, symTable)) {
                                ERROR ="Ошибка: внешняя ссылка " + sym +
                                        " уже определена как внешнее имя в модуле " + currentModule;
                                return;
                            }
                            if (findLabel(sym) != -1) {
                                ERROR = "Ошибка: Символ " + sym + " уже существует в текущем модуле";
                                return;
                            }
                            symTable.add(new String[]{currentModule, sym, hexAddress(0), "", "ВС"});
                            boolean inserted = false;

                            for (int i = 0; i < resultTable.size(); i++) {
                                String[] row = resultTable.get(i);

                                if (row[0].equals("H") && row[1].equals(currentModule)) {

                                    int insertPos = i + 1;

                                    while (insertPos < resultTable.size()
                                            && resultTable.get(insertPos)[0].equals("D")) {
                                        insertPos++;
                                    }

                                    while (insertPos < resultTable.size()
                                            && resultTable.get(insertPos)[0].equals("R")) {
                                        insertPos++;
                                    }

                                    resultTable.add(insertPos, new String[]{"R", sym, "", "", ""});
                                    inserted = true;
                                    break;
                                }
                            }

                            if (!inserted) {
                                resultTable.add(new String[]{"R", sym, "", "", ""});
                            }
                        }
                        break;
                    case "EXTDEF":
                        if (!label.isEmpty()) {
                            ERROR = "Ошибка: нельзя поcтавить метку на EXTDEF";
                            return;
                        }
                        if ((operand_1 == null || operand_1.isEmpty()) && (operand_2 == null || operand_2.isEmpty())) {
                            ERROR = "Ошибка: EXTDEF требует список имён";
                            return;
                        }
                        String allOperands = operand_1;
                        if (operand_2 != null && !operand_2.isEmpty()) {
                            allOperands += "," + operand_2;
                        }

                        String[] defSymbols = allOperands.split(",");
                        for (String sym : defSymbols) {
                            sym = sym.trim();
                            if (sym.isEmpty()) continue;
                            if (!sym.isEmpty()) {
                                char first = operand_1.charAt(0);
                                if (!(isLetter(first) || isSpecialSymbol(first))) {
                                    ERROR = "Ошибка: Некорректное имя метки\n";
                                    return;
                                }
                            }
                            if (!isLabel(sym)) {
                                ERROR = "Ошибка: Некорректное имя во внешнем определении: " + sym;
                                return;
                            }

                            for (String[] existing : symTable) {
                                String existingName = existing[1];
                                String existingType = existing[4];
                                String existingModule = existing[0];
                                if (isExtRef(sym, symTable)) {
                                    ERROR ="Ошибка: внешнее имя " + sym +
                                            " уже определено как внешняя ссылка в модуле " + existingModule;
                                    return;
                                }
                                if ("ВИ".equals(existingType) && existingName.equalsIgnoreCase(sym)) {
                                    ERROR ="Ошибка: внешнее имя " + sym +
                                                " уже определено в модуле " + existingModule;
                                    return;
                                }
                            }
                            int existingIdx = findLabel(sym);
                            if (existingIdx != -1) {
                                String[] entry = symTable.get(existingIdx);
                                entry[3] = "ВИ";
                                continue;
                            }
                            symTable.add(new String[] {currentModule, sym, "", "", "ВИ"});
                            boolean inserted = false;

                            for (int i = 0; i < resultTable.size(); i++) {
                                String[] row = resultTable.get(i);

                                if (row[0].equals("H") && row[1].equals(currentModule)) {

                                    int insertPos = i + 1;

                                    while (insertPos < resultTable.size()
                                            && resultTable.get(insertPos)[0].equals("D")) {
                                        insertPos++;
                                    }

                                    resultTable.add(insertPos, new String[]{"D", sym, "", "", ""});
                                    inserted = true;
                                    break;
                                }
                            }

                            if (!inserted) {
                                resultTable.add(new String[]{"D", sym, "", "", ""});
                            }
                        }

                        break;
                }
            } else {
                int opr = 0;
                int opIndex = findOperation(operation);
                int size = Integer.parseInt(opTable[findOperation(operation)][2]);
                if (opIndex == -1) {
                    ERROR = "Ошибка: Неизвестная операция " + operation;
                    return;
                } else {
                    operation = opTable[opIndex][1];
                }
                int opSize = Integer.parseInt(opTable[opIndex][2]);
                if (opSize > 4 || opSize < 1 || opSize == 3){
                    ERROR = "Ошибка: Некорректная длина операции " + operation;
                    return;
                }
                opr = Integer.parseInt(operation, 16);
                if (size == 4){
                    if (!isLabel(operand_1)){
                        ERROR = "Ошибка: некорректный операнд " + operand_1;
                        return;
                    }
                    if (!operand_2.isEmpty()){
                        ERROR = "Ошибка: лишний операнд";
                        return;
                    }
                }

                if (size == 2){
                    if (isNumber(operand_1) && operand_2.isEmpty()){
                        ERROR = "";
                    } else {
                        if (isRegister(operand_1) && isRegister(operand_2)) {
                            ERROR = "";
                        } else {
                            ERROR = "Ошибка: некорректный формат операндной части";
                            return;
                        }
                    }
                }

                if (size == 1){
                    if (!operand_1.isEmpty() || !operand_2.isEmpty()) {
                        ERROR = "Ошибка: некорректный формат операндной части";
                        return;
                    }
                }
                String op1 = operand_1;
                operand_1 = resolveOperand(operand_1, currentAddress + size);
                if (!ERROR.isEmpty()) {
                    ERROR = ERROR;
                    return;
                }

                operand_2 = resolveOperand(operand_2, currentAddress + size);
                if (!ERROR.isEmpty()) {
                    ERROR = ERROR;
                    return;
                }
                String code = String.format("%02X", opr) + (operand_1.isEmpty() ? "" : operand_1) + (operand_2.isEmpty() ? "" : operand_2);
                if (code.length() > size * 2 + 2) {
                    ERROR ="Ошибка: превышена длина команды ";
                    return;
                }
                if (!operation.isEmpty()) {
                    if (size == 4) {
                        if (op1.equals(currentModule)) {
                            ERROR = "Ошибка: символическое имя не может совпадать с именем модуля ";
                            return;
                        }
                        if (isRelativeAddressing(op1)){
                            if (opr * 4 + 2 > 255 ) {
                                ERROR = "Ошибка: некорректный код операции в ТКО ";
                                return;
                            }
                            opr = opr * 4 + 2;
                        } else {
                            if (opr * 4 + 1 > 255) {
                                ERROR = "Ошибка: некорректный код операции в ТКО ";
                                return;
                            }
                            opr = opr * 4 + 1;
                            if (isExtRef(op1, symTable)) {
                                modTable.add(new String[] {currentModule, hexAddress(currentAddress), op1});
                            } else {
                                modTable.add(new String[]{currentModule, hexAddress(currentAddress), ""});
                            }
                        }
                    }
                    else{
                        if (opr * 4 > 255 ) {
                            ERROR = " -- " + "Ошибка: некорректный код операции в ТКО ";
                            return;
                        }
                        opr *= 4;
                    }
                }
                increment = Integer.parseInt(opTable[opIndex][2]);
                if (currentAddress + increment > MAX_MEMORY_ADR) {
                    ERROR = "Ошибка: Переполнение памяти";
                    return;
                }
                rowToResultTable("T", hexAddress(currentAddress), String.format("%02X", size * 2), String.format("%02X", opr), operand_1, operand_2);
            }

            currentAddress += increment;
        } else {
            ERROR = ERROR;
            return;
        }

        if (lineID == linesCount - 1) {
            if (!endFlag){
                ERROR = "Ошибка: Отсутствует директива END";
                return;
            }
            for (int i = 0; i < symTable.size(); i++){
                String[] check = symTable.get(i);
                if (check[2] == null || check[2].isEmpty()){
                    ERROR = "Ошибка: Неопределенная метка " + check[0];
                    return;
                }
            }
        }
    }

    private boolean isRelativeAddressing(String label){
        return label.startsWith("[") && label.endsWith("]");
    }

    private String getActualLabel(String label){
        String actualLabel = label;
        if (isRelativeAddressing(label)) {
            actualLabel = label.substring(1, label.length() - 1);
            if (actualLabel.isEmpty()) return null;
        }
        return actualLabel;
    }

    private String resolveOperand(String op, int size) {
        if (op == null || op.isEmpty()) return "";

        op = op.trim();
        if (op.equals("?")) {
            return "";
        } else if (isRegister(op)) {
            return getRegisterCode(op);
        } else if (isNumber(op)) {
            String ans = String.format("%02X", parseNumber(op));
            if (ans.length() > 2) {
                ERROR = "Ошибка: Некорректный операнд " + op;
                return "";
            }
            return ans;
        } else if (isLabel(op)) {
            if (isRelativeAddressing(op)) {
                String actualLabel = getActualLabel(op);
                if (isExtRef(actualLabel, symTable)) {
                    ERROR = "Ошибка: Косвенная адресация со внешними ссылками недопустима";
                    return "";
                }
                String addr = findLabelAddress(actualLabel);
                if (addr == null) {
                    symTable.add(new String[]{currentModule, actualLabel, "", hexAddress(currentAddress), ""});
                    return op;
                } else {
                    int relativeAddress = Integer.parseInt(addr, 16) - size;
                    return String.format("%06X",relativeAddress);
                }
            } else {
                String addr = findLabelAddress(op);
                if (addr == null) {
                    symTable.add(new String[]{currentModule, op, "", hexAddress(currentAddress), ""});
                    return op;
                }
                return addr;
            }
        }

        return op;
    }

    private String getRegisterCode(String reg) {
        reg = reg.toUpperCase().substring(1);
        int num = Integer.parseInt(reg);
        return String.format("%01X", num);
    }

    private String findLabelAddress(String label) {
        for (int i = 0; i < symTable.size(); i++) {
            if (symTable.get(i)[1].equalsIgnoreCase(label) && !symTable.get(i)[2].isEmpty() && symTable.get(i)[0].equals(currentModule)) {
                return symTable.get(i)[2];
            }
        }
        return null;
    }

    private int findLabel(String s){
        if (s == null) return -1;
        for (int i = 0; i < symTable.size(); i++) {
            String[] entry = symTable.get(i);
            if (entry != null && entry.length > 0 && s.equalsIgnoreCase(entry[1]) && !entry[2].isEmpty() && symTable.get(i)[0].equals(currentModule)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isExtRef(String label, List<String[]> symTable) {
        for (String[] row : symTable) {
            if (row[1].equalsIgnoreCase(label) && row[0].equalsIgnoreCase(currentModule) && row[4].equalsIgnoreCase("ВС")) {
                return true;
            }
        }
        return false;
    }

    private boolean isExtDef(String label, List<String[]> symTable) {
        for (String[] row : symTable) {
            if (row[1].equalsIgnoreCase(label) && row[0].equalsIgnoreCase(currentModule) && row[4].equalsIgnoreCase("ВИ")) {
                return true;
            }
        }
        return false;
    }

    private String getByteObjectCode(String operand) {
        if (operand == null) return null;
        String s = operand.trim();
        if (s.isEmpty()) return null;

        if (isNumber(s)) {
            int val = parseNumber(s);
            if (val < 0 || val > 255) {
                ERROR = "Ошибка: BYTE число вне диапазона 0–255: " + s;
                return null;
            }
            return String.format("%02X", val & 0xFF);
        }

        char typePrefix = 0;
        if (s.length() > 1 && (s.charAt(0) == 'X' || s.charAt(0) == 'x'
                || s.charAt(0) == 'C' || s.charAt(0) == 'c')) {
            typePrefix = s.charAt(0);
            s = s.substring(1).trim();
        }

        if (!(s.startsWith("\"") || s.startsWith("'"))) {
            ERROR = "Ошибка: BYTE строка должна начинаться с кавычки: " + operand;
            return null;
        }

        char quote = s.charAt(0);
        if (s.charAt(s.length() - 1) != quote) {
            ERROR = "Ошибка: BYTE — кавычки не сбалансированы: " + operand;
            return null;
        }

        String content = s.substring(1, s.length() - 1);

        if (typePrefix == 'X' || typePrefix == 'x') {
            content = content.replaceAll("\\s+", "");
            if (!content.matches("[0-9A-Fa-f]+")) {
                ERROR = "Ошибка: недопустимые символы в X\"...\": " + content;
                return null;
            }
            return content;
        }

        StringBuilder hex = new StringBuilder();
        for (char c : content.toCharArray()) {
            hex.append(String.format("%02X", (int) c));
        }

        return hex.toString();
    }

    private int calcByteSize(String operand) {
        if (operand == null) return -1;
        String s = operand.trim();
        if (s.isEmpty()) return -1;

        int start = s.indexOf('\'');
        int end = s.lastIndexOf('\'');
        if (start == -1 || end == -1 || end <= start) {
            start = s.indexOf('"');
            end = s.lastIndexOf('"');
        }

        char first = Character.toUpperCase(s.charAt(0));

        if ((first == 'X') && start != -1 && end > start) {
            String content = s.substring(start + 1, end).trim();
            if (!content.matches("[0-9A-Fa-f]+")) return -1;
            if ((content.length() - 2) % 2 > 0) return -1;
            return content.length() / 2;
        }

        if ((first == 'C') && start != -1 && end > start) {
            String content = s.substring(start + 1, end);
            return content.length();
        }

        if ((first == 'B') && start != -1 && end > start) {
            String content = s.substring(start + 1, end).trim();
            if (!content.matches("[01]+")) return -1;
            return (content.length() + 7) / 8;
        }

        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            String content = s.substring(1, s.length() - 1);
            return content.length();
        }

        if (isNumber(s)) {
            int val = parseNumber(s);
            if (val < 0 || val > 0xFF) return -1;
            return 1;
        }

        return -1;
    }

    private String hexAddress(int address){
        return String.format("%06X", address);
    }

    private void rowToResultTable(String letter,
                                  String address,
                                  String commandLength,
                                  String commandCode,
                                  String operand_1,
                                  String operand_2){
        resultTable.add(new String[]{
                letter, address, commandLength, commandCode, operand_1, operand_2
        });
    }

    private int parseNumber(String s) {
        if (s == null || s.isEmpty()) return 0;
        s = s.trim().toLowerCase();

        try {
            if (s.startsWith("0x")) {
                return Integer.parseInt(s.substring(2), 16);
            } else if (s.endsWith("h")) {
                return Integer.parseInt(s.substring(0, s.length() - 1), 16);
            } else if (s.startsWith("0b")) {
                return Integer.parseInt(s.substring(2), 2);
            } else if (s.endsWith("b")) {
                return Integer.parseInt(s.substring(0, s.length() - 1), 2);
            } else {
                return Integer.parseInt(s, 10);
            }
        } catch (NumberFormatException e) {
            ERROR = "Ошибка: недопустимое числовое значение: " + s;
            return 0;
        }
    }

    private boolean prepareLine(String line){
        label = "";
        operation = "";
        operand_1 = "";
        operand_2 = "";
        line = line.trim();
        List<String> tokens = splitLine(line);
        if (tokens.isEmpty() || (tokens.size() == 1 && tokens.getFirst().isEmpty())) {
            return true;
        }

        int idx = 0;
        if (idx < tokens.size()) {
            if (isLabel(tokens.get(idx))) {
                char first = tokens.get(idx).charAt(0);
                if (!(isLetter(first) || isSpecialSymbol(first))){
                    ERROR = "Ошибка: Некорректное имя метки\n";
                    return false;
                }
                int value = findLabel(tokens.get(idx));
                if (tokens.size() >= idx + 1) {
                    if (!tokens.get(idx + 1).equalsIgnoreCase("CSECT")) {
                        if (value != -1) {
                            ERROR = "Ошибка: Данная метка уже объявлена\n";
                            return false;
                        }
                    }
                    else {
                        for (String[] row: symTable){
                            if (row[0].equals(tokens.get(idx))) {
                                ERROR = "Ошибка: Данная метка уже объявлена\n";
                                return false;
                            }
                        }
                    }
                } else {
                    if (value != -1) {
                        ERROR = "Ошибка: Данная метка уже объявлена\n";
                        return false;
                    }
                }
                label = tokens.get(idx).toUpperCase();
                idx++;
            } else {
                if (isDirective(tokens.get(idx))) {
                    operation = tokens.get(idx).toUpperCase();
                    idx++;
                } else if (findOperation(tokens.get(idx)) == -1) {
                    ERROR = "Ошибка: Некорректная метка\n";
                    return false;
                }
            }
        }

        if (idx < tokens.size()) {
            int value = findOperation(tokens.get(idx));
            if (value != -1) {
                operation = opTable[value][0].toUpperCase();
                idx++;
            } else if (isDirective(tokens.get(idx))) {
                operation = tokens.get(idx).toUpperCase();
                idx++;
            } else if (isOperand(tokens.get(idx))) {
            } else {
                ERROR = "Ошибка: Неизвестная операция / отсутствует операция\n";
                return false;
            }
        }

        if (idx < tokens.size()){
            if (isOperand(tokens.get(idx))) {
                operand_1 = tokens.get(idx);
                idx++;
            } else {
                ERROR = "Ошибка: Некорректный операнд " + operand_1;
                return false;
            }
        }

        if (idx < tokens.size()){
            if (isOperand(tokens.get(idx))) {
                operand_2 = tokens.get(idx);
                idx++;
            } else {
                ERROR = "Ошибка: Некорректный операнд " + operand_2;
                return false;
            }
        }

        if (!operand_1.isEmpty() && isLabel(operand_1)) {
            if (!checkAddressingMode(operand_1)) {
                return false;
            }
        }

        if (!operand_2.isEmpty() && isLabel(operand_2)) {
            if (!checkAddressingMode(operand_2)) {
                return false;
            }
        }

        if (idx < tokens.size()) {
            ERROR = "Ошибка: Некорректный формат строки\n";
            return false;
        }

        return  true;
    }

    private boolean isOperand(String s){
        if (isNumber(s)) return true;
        if (isRegister(s)) return true;
        if (isLabel(s)) return true;
        if (isStringOperand(s)) return true;
        return false;
    }

    private boolean isStringOperand(String s) {
        if (s == null) return false;
        s = s.trim();
        if (s.length() < 3) return false;

        char first = Character.toUpperCase(s.charAt(0));

        int start = s.indexOf('"');
        int end = s.lastIndexOf('"');
        if (start == -1 || end == -1) {
            start = s.indexOf('\'');
            end = s.lastIndexOf('\'');
        }

        if (start == -1 || end == -1 || end <= start) return false;

        if (first == 'C' || first == 'X' || first == 'B') return true;

        return (s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"));
    }

    private boolean isNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        s = s.trim();
        return isBinary(s) || isDecimal(s) || isHex(s);
    }

    private boolean isBinary(String s) {
        if (s == null || s.isEmpty()) return false;
        String str = s.trim().toLowerCase();

        if (str.startsWith("0b")) {
            String digits = str.substring(2);
            return !digits.isEmpty() && digits.matches("[01]+");
        }

        if (str.endsWith("b")) {
            String digits = str.substring(0, str.length() - 1);
            return !digits.isEmpty() && digits.matches("[01]+");
        }

        return str.matches("[01]+");
    }

    private boolean isDecimal(String s) {
        if (s == null || s.isEmpty()) return false;
        return s.trim().matches("[0-9]+");
    }

    private boolean isHex(String s) {
        if (s == null || s.isEmpty()) return false;
        String str = s.trim();
        if (str.toLowerCase().startsWith("0x")) {
            String digits = str.substring(2);
            return !digits.isEmpty() && digits.matches("[0-9a-fA-F]+");
        }
        if (str.toLowerCase().endsWith("h")) {
            String digits = str.substring(0, str.length() - 1);
            return !digits.isEmpty() && digits.matches("[0-9a-fA-F]+");
        }
        return false;
    }

    private boolean isLabel(String label) {
        if (label == null || label.isEmpty()) return false;

        String actualLabel = getActualLabel(label);
        if (actualLabel == null) return false;

        if (isRelativeAddressing(label)) {
            if (label.length() > 33) return false;
        } else {
            if (actualLabel.length() > 31) return false;
        }

        char first = actualLabel.charAt(0);
        if (!(isLetter(first) || isSpecialSymbol(first))) return false;

        for (char c : actualLabel.toCharArray()) {
            if (!isValidChar(c)) return false;
        }

        if (isRegister(actualLabel)) return false;

        if (isDirective(actualLabel)) return false;

        if (findOperation(actualLabel) != -1) return false;

        return true;
    }

    private int findOperation(String s){
        if (s == null || s.isEmpty()) return -1;
        for (int i = 0; i < opTable.length; i++){
            if (s.equalsIgnoreCase(opTable[i][0])) return i;
        }
        return -1;
    }

    private boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private boolean isValidChar(char c) {
        return isLetter(c) || isDigit(c) || isSpecialSymbol(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isSpecialSymbol(char c) {
        return c == '?' || c == '@' || c == '.' || c == '_' || c == '$';
    }

    private boolean isDirective(String s){
        return DIRECTIVES.contains(s.toUpperCase());
    }

    private static final Set<String> DIRECTIVES = Set.of(
            "START", "END", "BYTE", "WORD", "RESB", "RESW", "CSECT", "EXTDEF", "EXTREF"
    );

    private boolean isRegister(String s) {
        return s != null && s.toUpperCase().matches("R([0-9]|1[0-5])");
    }

    private static List<String> splitLine(String line) {
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;

        int i = 0, n = line.length();
        StringBuilder sb = new StringBuilder();
        while (i < n){
            if (i < n && Character.isWhitespace(line.charAt(i))) {
                if (!sb.isEmpty()) {
                    tokens.add(sb.toString());
                    sb = new StringBuilder();
                }
                i++;
                continue;
            }
            char c = line.charAt(i);
            if (c == '"' || c == '\'') {
                StringBuilder sbn = new StringBuilder();
                char quote = c;
                while (i < n) {
                    char ch = line.charAt(i);
                    sbn.append(ch);
                    i++;

                    if (ch == quote && i == line.lastIndexOf(quote) + 1) {
                        sb.append(sbn);
                        tokens.add(sb.toString());
                        sb = new StringBuilder();
                        break;
                    }
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        if (!sb.isEmpty()) tokens.add(sb.toString());
        return tokens;
    }

    private boolean checkAddressingMode(String operand) {
        if (operand == null || operand.isEmpty()) return true;

        boolean isRelative = operand.startsWith("[") && operand.endsWith("]");
        boolean isDirect = !isRelative && isLabel(operand);
        switch (addressingMode) {
            case "Прямая":
                if (isRelative) {
                    ERROR = "Ошибка: Относительная адресация [] не допускается при прямом режиме";
                    return false;
                }
                break;

            case "Относительная":
                if (isDirect) {
                    ERROR = "Ошибка: Прямая адресация не допускается при относительном режиме. Используйте [метка]";
                    return false;
                }
                break;

            case "Смешанная":
                break;

            default:
                ERROR = "Ошибка: Неизвестный режим адресации: " + addressingMode;
                return false;
        }

        return true;
    }

}
