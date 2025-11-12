package Lab_4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class Step {

    String ERROR = "";
    String line;
    String[][] opTable;
    String addressingMode;
    ArrayList<ArrayList<String>> symTable;
    ArrayList<ArrayList<String>> resultTable;
    int oldAddress;

    int currentAddress;

    String label;
    String operation;
    String operand_1;
    String operand_2;

    int startAddress = 0;
    int endAddress = 0;

    boolean startFlag = false;
    boolean endFlag = false;

    int MAX_MEMORY_ADR = 16777215;

    public String execute(
            String line,
            int lineID,
            String[][] opTable,
            ArrayList<ArrayList<String>> symTable,
            ArrayList<ArrayList<String>> resultTable,
            String addressingMode)
    {
        this.opTable = opTable;
        this.symTable = symTable;
        this.resultTable = resultTable;
        this.addressingMode = addressingMode;

        if (line.isEmpty()) return "";

        if (endFlag) return "";

        if (prepareLine(line)){
            if (!ERROR.isEmpty()){
                return ERROR;
            };

            if (operation.equalsIgnoreCase("START")) {
                if (startFlag) {
                    ERROR = lineID + " -- Ошибка: Повторное использование START";
                    return ERROR;
                }
                startFlag = true;

                if (isNumber(operand_1)) {
                    currentAddress = parseNumber(operand_1);
                    if (currentAddress == 0) {
                        ERROR = lineID + " -- Ошибка: Неверный адрес начала программы";
                        return ERROR;
                    }
                    startAddress = currentAddress;
                } else {
                    ERROR = lineID + " -- Ошибка: Адрес должен быть числом";
                    return ERROR;
                }
//                programName = label;
//                subTable.add(new ArrayList<>(Arrays.asList("", "START", operand_1, operand_2
//                )));
//                continue;
            }

        } else {

        }

        return ERROR;
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
        List<String> tokens = splitLine(line);
        if (tokens.isEmpty() || (tokens.size() == 1 && tokens.getFirst().isEmpty())) {
            return true;
        }

        int idx = 0;

        if (idx < tokens.size()) {
            if (isLabel(tokens.get(idx))) {
                int value = findLabel(tokens.get(idx));
                if (value != -1) {
                    ERROR = "Ошибка: Данная метка уже объявлена\n";
                    return false;
                }
                label = tokens.get(idx);
                idx++;
            } else {
                if (findOperation(tokens.get(idx)) == -1) {
                    ERROR = "Ошибка: Некорректная метка\n";
                    return false;
                }
            }
        }

        if (idx < tokens.size()) {
            int value = findOperation(tokens.get(idx));
            if (value != -1) {
                operation = opTable[value][0];
                idx++;
            } else if (isDirective(tokens.get(idx))) {
                operation = tokens.get(idx);
                idx++;
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
                ERROR = "Ошибка: Некорректный операнд\n";
                return false;
            }
        }

        if (idx < tokens.size()){
            if (isOperand(tokens.get(idx))) {
                operand_1 = tokens.get(idx);
                idx++;
            } else {
                ERROR = "Ошибка: Некорректный операнд\n";
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
            return !digits.isEmpty() &&
                    digits.matches("[0-9a-fA-F]+") &&
                    Character.isDigit(digits.charAt(0));
        }
        return str.matches("[0-9a-fA-F]+");
    }

    private int findLabel(String s){
        if (s == null) return -1;
        for (int i = 0; i < symTable.size(); i++) {
            ArrayList<String> entry = symTable.get(i);
            if (entry != null && !entry.isEmpty() && s.equalsIgnoreCase(entry.getFirst()) && !entry.get(1).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private boolean isLabel(String label) {
        if (label == null || label.isEmpty()) return false;
        if (label.length() > 31) return false;

        char first = label.charAt(0);
        if (!(isLetter(first) || isSpecialSymbol(first))) return false;

        for (char c : label.toCharArray()) {
            if (!isValidChar(c)) return false;
        }

        if (isRegister(label)) return false;

        if (isDirective(label)) return false;

        if (findOperation(label) != -1) return false;

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
            "START", "END", "BYTE", "WORD", "RESB", "RESW"
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

    public int getStartAddress() {
        return startAddress;
    }
}
