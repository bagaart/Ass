package Lab_1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class FirstPass {

    private static final Set<String> DIRECTIVES = Set.of(
            "START", "END", "BYTE", "WORD", "RESB", "RESW"
    );

    ArrayList<ArrayList<String>> symTable = new ArrayList<>();
    ArrayList<ArrayList<String>> subTable = new ArrayList<>();
    String ERROR = "";

    String[][] opTable = null;

    String label;
    String operation;
    String operand_1;
    String operand_2;

    private int StartAddress = 0;
    private String programName;
    private String programLength;

    public void execute(String sourceText, String[][] opTable) {
        ERROR = "";
        subTable.clear();
        symTable.clear();

        this.opTable = opTable;

        StartAddress = 0;
        int endAddress = 0;
        int LOCCTR = 0;

        int StartFlag = 0;
        int EndFlag = 0;

        if (sourceText == null || sourceText.isEmpty()) {
            ERROR = "Ошибка: исходный текст пуст";
            return;
        }

        String[] code = sourceText.split("\\n+");

        for (int i = 0; i < code.length; i++) {

            int MAX_MEMORY_ADR = 16777215;
            if (LOCCTR > MAX_MEMORY_ADR) {
                ERROR = i + " -- Ошибка: Переполнение памяти";
                return;
            }

            if (EndFlag == 1) break;

            String line = code[i].trim();
            if (line.isEmpty()) continue;

            if (prepareLine(code[i])) {
                if (!ERROR.isEmpty()) break;
                String hexAddress = String.format("%06X", LOCCTR);

                if (!label.isEmpty() && StartFlag == 1) {
                    if (findLabel(label) == -1) {
                        symTable.add(new ArrayList<>(Arrays.asList(label, hexAddress)));
                    } else {
                        ERROR = i + " -- Ошибка: Метка уже существует";
                        return;
                    }
                }

                if (operation.equalsIgnoreCase("START")) {
                    if (StartFlag == 1) {
                        ERROR = i + " -- Ошибка: Повторное использование START";
                        return;
                    }
                    StartFlag = 1;

                    if (isNumber(operand_1)) {
                        LOCCTR = parseNumber(operand_1);
                        if (LOCCTR == 0) {
                            ERROR = i + " -- Ошибка: Неверный адрес начала программы";
                            return;
                        }
                        StartAddress = LOCCTR;
                    } else {
                        ERROR = i + " -- Ошибка: Адрес должен быть числом";
                        return;
                    }
                    programName = label;
                    subTable.add(new ArrayList<>(Arrays.asList("", "START", operand_1, operand_2
                    )));
                    continue;
                }

                if (operation.equalsIgnoreCase("END")) {
                    if (StartFlag == 0) {
                        ERROR = i + " -- Ошибка: END без START";
                        return;
                    }
                    EndFlag = 1;
                    endAddress = LOCCTR;

                    subTable.add(new ArrayList<>(Arrays.asList(
                            "", "END", String.format("%06X", StartAddress), ""
                    )));
                    break;
                }

                int increment = 0;

                if (isDirective(operation)) {
                    switch (operation.toUpperCase()) {
                        case "WORD":
                            increment = 3;
                            break;
                        case "RESW":
                            if (!isNumber(operand_1)) {
                                ERROR = i + " -- Ошибка: RESW требует числового операнда";
                                return;
                            }
                            increment = 3 * parseNumber(operand_1);
                            break;
                        case "RESB":
                            if (!isNumber(operand_1)) {
                                ERROR = i + " -- Ошибка: RESB требует числового операнда";
                                return;
                            }
                            increment = parseNumber(operand_1);
                            break;
                        case "BYTE":
                            int size = calcByteSize(operand_1);
                            if (size == -1) {
                                ERROR = i + " -- Ошибка: Некорректный операнд BYTE";
                                return;
                            }
                            increment = size;
                            break;
                    }
                } else {
                    int opIndex = findOperation(operation);
                    if (opIndex == -1) {
                        ERROR = i + " -- Ошибка: Неизвестная операция " + operation;
                        return;
                    } else {
                        operation = opTable[opIndex][1];
                    }
                    increment = Integer.parseInt(opTable[opIndex][2]);
                }

                subTable.add(new ArrayList<>(Arrays.asList(
                        hexAddress, operation, operand_1, operand_2
                )));

                if (LOCCTR + increment > MAX_MEMORY_ADR) {
                    ERROR = i + " -- Ошибка: Переполнение памяти";
                    return;
                }
                LOCCTR += increment;

            } else {
                ERROR = i + " -- " + ERROR;
            }
        }

        if (EndFlag == 0) {
            ERROR = "Ошибка: Отсутствует директива END";
        }
        programLength = String.format("%06X", endAddress - StartAddress);
    }

    private boolean prepareLine(String line) {
        label = "";
        operation = "";
        operand_1 = "";
        operand_2 = "";

        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty())) {
            return true;
        }

        int idx = 0;

        if (idx < tokens.length && isLabel(tokens[idx])) {
            int value = findLabel(tokens[idx]);
            if (value != -1) {
                ERROR = "Ошибка: Данная метка уже объявлена\n";
                return false;
            }
            label = tokens[idx];
            idx++;
        }

        if (idx < tokens.length) {
            int index = findOperation(tokens[idx]);
            if (index != -1) {
                operation = opTable[index][0];
                idx++;
            } else if (isDirective(tokens[idx])) {
                operation = tokens[idx];
                idx++;
            } else {
                ERROR = "Ошибка: Неизвестная операция / отсутствует операция\n";
                return false;
            }
        }

        if (idx < tokens.length) {
            if (isOperand(tokens[idx])) {
                operand_1 = tokens[idx];
                idx++;
            } else {
                ERROR = "Ошибка: Некорректный операнд\n";
                return false;
            }
        }

        if (idx < tokens.length) {
            if (isOperand(tokens[idx])) {
                operand_2 = tokens[idx];
                idx++;
            } else {
                ERROR = "Ошибка: Некорректный операнд\n";
                return false;
            }
        }

        if (idx < tokens.length) {
            ERROR = "Ошибка: Некорректный формат строки\n";
            return false;
        }

        return true;
    }

    private int calcByteSize(String operand) {
        if (operand == null || operand.length() < 3) return -1;
        operand = operand.trim();

        char prefix = Character.toUpperCase(operand.charAt(0));
        int start = operand.indexOf('\'');
        int end = operand.lastIndexOf('\'');

        if (start == -1 || end == -1) {
            start = operand.indexOf('"');
            end = operand.lastIndexOf('"');
        }

        if (start == -1 || end == -1 || end <= start) return -1;

        String content = operand.substring(start + 1, end).trim();

        if (prefix != 'C' && prefix != 'X' && prefix != 'B') {
            prefix = 'C';
        }

        switch (prefix) {
            case 'C':
                return content.length();

            case 'X':
                if (content.length() % 2 != 0) {
                    content = "0" + content;
                }
                if (!content.matches("[0-9A-Fa-f]+")) return -1;
                return content.length() / 2;

            case 'B':
                if (!content.matches("[01]+")) return -1;
                return (content.length() + 7) / 8;

            default:
                return -1;
        }
    }

    private boolean isLabel(String label) {
        if (label == null || label.isEmpty()) return false;
        if (label.length() > 31) return false;

        char first = label.charAt(0);
        if (!(isLetter(first) || isSpecialSymbol(first))) return false;

        for (char c : label.toCharArray()) {
            if (!isValidChar(c)) return false;
        }

        if (isDirective(label)) return false;

        if (findOperation(label) != -1) return false;

        return true;
    }

    private boolean isDirective(String s){
        return DIRECTIVES.contains(s.toUpperCase());
    }

    private int findOperation(String s){
        if (s == null || s.isEmpty()) return -1;
        for (int i = 0; i < opTable.length; i++){
            if (s.equalsIgnoreCase(opTable[i][0])) return i;
        }
        return -1;
    }

    private int findLabel(String s){
        if (s == null) return -1;
        for (int i = 0; i < symTable.size(); i++) {
            ArrayList<String> entry = symTable.get(i);
            if (entry != null && !entry.isEmpty() && s.equalsIgnoreCase(entry.getFirst())) {
                return i;
            }
        }
        return -1;
    }

    private boolean isStringOperand(String s){
        if (s == null) return false;
        s = s.trim();
        if (s.length() < 2) return false;

        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);

        if ((first == '"' && last == '"') || (first == '\'' && last == '\'')) {
            return true;
        }

        return false;
    }

    private boolean isLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    private boolean isSpecialSymbol(char c) {
        return c == '?' || c == '@' || c == '.' || c == '_' || c == '$';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isValidChar(char c) {
        return isLetter(c) || isDigit(c) || isSpecialSymbol(c);
    }

    private boolean isOperand(String s){
        if (isNumber(s)) return true;
        if (isRegister(s)) return true;
        if (isLabel(s)) return true;
        if (isStringOperand(s)) return true;
        return false;
    }

    private boolean isRegister(String s) {
        return s != null && s.toUpperCase().matches("R([0-9]|1[0-5])");
    }

    private boolean isNumber(String s) {
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
            return !digits.isEmpty() &&
                    digits.matches("[0-9a-fA-F]+") &&
                    Character.isDigit(digits.charAt(0));
        }
        return str.matches("[0-9a-fA-F]+");
    }

    public String getERROR() {
        return ERROR;
    }

    public ArrayList<ArrayList<String>> getSymTable() {
        return symTable;
    }

    public ArrayList<ArrayList<String>> getSubTable() {
        return subTable;
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


    public String getProgramLength() {
        return programLength;
    }

    public String getProgramName() {
        return programName;
    }

    public int getStartAddress() {
        return StartAddress;
    }
}
