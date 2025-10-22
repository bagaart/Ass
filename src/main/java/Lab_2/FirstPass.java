package Lab_2;

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

    private String addressingMode;

    public void execute(String sourceText, String[][] opTable, String addressingMode) {
        ERROR = "";
        subTable.clear();
        symTable.clear();

        this.opTable = opTable;
        this.addressingMode = addressingMode;

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
                        if (LOCCTR != 0) {
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
                            if (parseNumber(operand_1) < 0 || parseNumber(operand_1) > parseNumber("ffffffh")) {
                                ERROR = i + " -- Ошибка: некорректное числовое значение";
                                return;
                            }
                            break;
                        case "RESW":
                            if (!isNumber(operand_1)) {
                                ERROR = i + " -- Ошибка: RESW требует числового операнда";
                                return;
                            }
                            if (parseNumber(operand_1) < 0) {
                                ERROR = i + " -- Ошибка: RESW требует неотрицательного числа";
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
                    int opSize = Integer.parseInt(opTable[opIndex][2]);
                    if (opSize > 4 || opSize < 1 || opSize == 3) {
                        ERROR = "Ошибка: Некорректная длина операции " + operation;
                        return;
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
                return;
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

        String[] tokens = splitLine(line);
        if (tokens.length == 0 || (tokens.length == 1 && tokens[0].isEmpty())) {
            return true;
        }

        if (tokens[0].equalsIgnoreCase("END")) {
            operation = "END";
            if (tokens.length != 1) {
                ERROR = "Ошибка: Некорректная операндная часть\n";
                return false;
            }
            return true;
        }

        int idx = 0;

        if (idx < tokens.length) {
            if (isLabel(tokens[idx])) {
                int value = findLabel(tokens[idx]);
                if (value != -1) {
                    ERROR = "Ошибка: Данная метка уже объявлена\n";
                    return false;
                }
                label = tokens[idx];
                idx++;
            } else {
                if (findOperation(tokens[idx]) != -1) {
                    ERROR = "";
                } else {
                    ERROR = "Ошибка: Некорректная метка\n";
                    return false;
                }
            }
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

        if (idx < tokens.length) {
            ERROR = "Ошибка: Некорректный формат строки\n";
            return false;
        }

        return true;
    }

    private String[] splitLine(String line) {
        line = line.trim();
        if (line.isEmpty()) return new String[0];

        ArrayList<String> tokens = new ArrayList<>();
        String[] parts = line.split("\\s+", 3);

        for (String part : parts) {
            if (!part.isEmpty()) {
                tokens.add(part);
            }
        }

        if (tokens.size() == 3) {
            String operand = tokens.get(2).trim();

            char quoteChar = 0;
            int firstQuote = -1, lastQuote = -1;
            for (int i = 0; i < operand.length(); i++) {
                char c = operand.charAt(i);
                if (c == '"' || c == '\'') {
                    if (firstQuote == -1) firstQuote = i;
                    lastQuote = i;
                }
            }

            if (firstQuote != -1 && lastQuote != -1 && lastQuote < operand.length() - 1) {
                operand = operand.substring(0, operand.lastIndexOf(quoteChar) + 1);
                tokens.set(2, operand);
            }
        }

        return tokens.toArray(new String[0]);
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
            if (content.length() % 2 != 0) content = "0" + content;
            if (!content.matches("[0-9A-Fa-f]+")) return -1;
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

    private boolean isLabel(String label) {
        if (label == null || label.isEmpty()) return false;

        boolean isRelativeAddressing = label.startsWith("[") && label.endsWith("]");

        String actualLabel = label;
        if (isRelativeAddressing) {
            actualLabel = label.substring(1, label.length() - 1);
            if (actualLabel.isEmpty()) return false;
        }

        if (isRelativeAddressing) {
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

    private boolean isDirective(String s) {
        return DIRECTIVES.contains(s.toUpperCase());
    }

    private int findOperation(String s) {
        if (s == null || s.isEmpty()) return -1;
        for (int i = 0; i < opTable.length; i++) {
            if (s.equalsIgnoreCase(opTable[i][0])) return i;
        }
        return -1;
    }

    private int findLabel(String s) {
        if (s == null) return -1;
        for (int i = 0; i < symTable.size(); i++) {
            ArrayList<String> entry = symTable.get(i);
            if (entry != null && !entry.isEmpty() && s.equalsIgnoreCase(entry.getFirst())) {
                return i;
            }
        }
        return -1;
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

    private boolean isOperand(String s) {
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
