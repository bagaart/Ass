package Lab_2;

import java.util.ArrayList;

public class SecondPass {
    String ERROR = "";
    ArrayList<String> objCode = new ArrayList<>();
    String[][] opTable = null;
    String addressingMode;
    ArrayList<String> modTable = new ArrayList<>();

    public void execute(ArrayList<ArrayList<String>> subTable,
                        ArrayList<ArrayList<String>> symTable,
                        String programName,
                        String programLength,
                        int startAddress,
                        String[][] opTable,
                        String addressingMode) {

        ERROR = "";
        objCode.clear();
        modTable.clear();
        this.opTable = opTable;
        this.addressingMode = addressingMode;

        if (subTable == null || subTable.isEmpty()) {
            ERROR = "Ошибка: Не выполнен первый проход";
            return;
        }

        objCode.add(String.format("H %-1s %06X %s",
                programName != null ? programName : "",
                startAddress,
                programLength != null ? programLength : "000000"));

        int i = 0;
        for (ArrayList<String> row : subTable) {
            i++;

            boolean op1IsRelative = false;
            boolean op2IsRelative = false;

            String address = row.get(0);
            String operation = row.get(1);
            String op1 = row.get(2);
            String op2 = row.get(3);
            int opr = 0;
            if (operation.equalsIgnoreCase("START") || operation.equalsIgnoreCase("END")) {
                continue;
            }

            String code = "";

            if (isDirective(operation)) {
                switch (operation.toUpperCase()) {
                    case "WORD":
                        code = String.format("%06X", parseNumber(op1));
                        break;
                    case "RESW":
                    case "RESB":
                        code = "";
                        break;
                    case "BYTE":
                        code = getByteObjectCode(op1);
                        if (code == null) {
                            ERROR = i + " -- " + "Ошибка: некорректный BYTE операнд " + op1;
                            return;
                        }
                        break;
                }
            } else {
                opr = Integer.parseInt(operation, 16);
                int size = Integer.parseInt(opTable[findOperation(operation)][2]);
                if (size == 4){
                    if (!isLabel(op1)){
                        ERROR = i + " -- " + "Ошибка: некорректный операнд";
                        return;
                    }
                    if (!op2.isEmpty()){
                        ERROR = i + " -- " + "Ошибка: лишний операнд";
                        return;
                    }
                    if (op1.startsWith("[") && op1.endsWith("]")){
                        op1IsRelative = true;
                    } else {
                        modTable.add(address);
                    }
                }

                if (size == 2){
                    if (isNumber(op1) && op2.isEmpty()){
                        ERROR = "";
                    } else {
                        if (isRegister(op1) && isRegister(op2)) {
                            ERROR = "";
                        } else {
                            ERROR = i + " -- " + "Ошибка: некорректный формат операндной части";
                            return;
                        }
                    }
                }

                if (size == 1){
                    if (!op1.isEmpty() || !op2.isEmpty()) {
                        ERROR = i + " -- " + "Ошибка: некорректный формат операндной части";
                        return;
                    }
                }

                op1 = resolveOperand(op1, symTable, Integer.parseInt(address, 16) + size);
                if (ERROR.length() > 0) {
                    ERROR = i + " -- " + ERROR;
                    return;
                }

                op2 = resolveOperand(op2, symTable, Integer.parseInt(address, 16) + size);
                if (ERROR.length() > 0) {
                    ERROR = i + " -- " + ERROR;
                    return;
                }

                code = String.format("%02X", opr) + (op1.isEmpty() ? "" : op1) + (op2.isEmpty() ? "" : op2);
            }

            StringBuilder sb = new StringBuilder();
            if (isDirective(operation)) {
                sb.append("T ").append(address).append(" ");
                if (!code.isEmpty()) sb.append(String.format("%02X", code.length())).append(" ");
                sb.append(code);
            } else {
                int idx = findOperation(operation);
                int size = Integer.parseInt(opTable[idx][2]) * 2 + 2;
                if (code.length() > size) {
                    ERROR = i + " -- " + "Ошибка: превышена длина команды ";
                    return;
                }

                sb.append("T ").append(address).append(" ");
                sb.append(String.format("%02X", code.length())).append(" ");
                if (!operation.isEmpty()) {
                    if (Integer.parseInt(opTable[idx][2]) == 4) {
                        if (!op1IsRelative) {
                            if (opr * 4 + 1 > 255) {
                                ERROR = " -- " + "Ошибка: некорректный код операции в ТКО ";
                                return;
                            }
                            sb.append(String.format("%02X", opr * 4 + 1));
                        }
                        else {
                            if (opr * 4 + 2 > 255) {
                                ERROR = " -- " + "Ошибка: некорректный код операции в ТКО ";
                                return;
                            }
                            sb.append(String.format("%02X", opr * 4 + 2));
                        }
                    }
                    else{
                        if (opr * 4 > 255 ) {
                            ERROR = " -- " + "Ошибка: некорректный код операции в ТКО ";
                            return;
                        }
                        sb.append(String.format("%02X", opr * 4));
                    }
                }
                if (!op1.isEmpty()) sb.append(" ").append(op1);
                if (!op2.isEmpty()) sb.append(" ").append(op2);
            }

            if (!sb.isEmpty()) objCode.add(sb.toString());
        }

        for (int j = 0; j < modTable.size(); j++){
            objCode.add("M " + modTable.get(j));
        }

        objCode.add(String.format("E %06X", startAddress));
    }

    private int findOperation(String s){
        if (s == null || s.isEmpty()) return -1;
        for (int i = 0; i < opTable.length; i++){
            if (s.equalsIgnoreCase(opTable[i][1])) return i;
        }
        return -1;
    }

    private String resolveOperand(String op, ArrayList<ArrayList<String>> symTable, int size) {
        if (op == null || op.isEmpty()) return "";

        op = op.trim();
        if (op.equals("?")) {
            return "";
        } else if (isRegister(op)) {
            return getRegisterCode(op);
        } else if (isNumber(op)) {
            return String.format("%04X", parseNumber(op));
        } else if (isLabel(op)) {
            if (op.startsWith("[") && op.endsWith("]")) {
                op = op.substring(1, op.length() - 1);
                String addr = findLabelAddress(op, symTable);
                if (addr == null) {
                    ERROR = "Ошибка: Метка '" + op + "' не найдена";
                } else {
                    int relativeAddress = Integer.parseInt(addr, 16) - size;
                    System.out.println(relativeAddress);
                    return String.format("%06X",relativeAddress);
                }
            } else {
                String addr = findLabelAddress(op, symTable);
                if (addr == null) {
                    ERROR = "Ошибка: Метка '" + op + "' не найдена";
                }
                return addr;
            }
        }

        return op;
    }


    private String findLabelAddress(String label, ArrayList<ArrayList<String>> symTable) {
        for (ArrayList<String> row : symTable) {
            if (row.get(0).equalsIgnoreCase(label)) {
                return row.get(1);
            }
        }
        return null;
    }

    private boolean isDirective(String s) {
        return s.equalsIgnoreCase("WORD") || s.equalsIgnoreCase("BYTE")
                || s.equalsIgnoreCase("RESW") || s.equalsIgnoreCase("RESB");
    }

    private int parseNumber(String s) {
        if (s == null || s.isEmpty()) return 0;
        s = s.trim().toLowerCase();
        try {
            if (s.startsWith("0x")) return Integer.parseInt(s.substring(2), 16);
            else if (s.endsWith("h")) return Integer.parseInt(s.substring(0, s.length() - 1), 16);
            else if (s.startsWith("0b")) return Integer.parseInt(s.substring(2), 2);
            else if (s.endsWith("b")) return Integer.parseInt(s.substring(0, s.length() - 1), 2);
            else return Integer.parseInt(s, 10);
        } catch (NumberFormatException e) {
            ERROR = "Ошибка: недопустимое числовое значение: " + s;
            return 0;
        }
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
            s = s.substring(1).trim(); // убрать X или C
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
            return content.toUpperCase();
        }

        StringBuilder hex = new StringBuilder();
        for (char c : content.toCharArray()) {
            hex.append(String.format("%02X", (int) c));
        }

        return hex.toString();
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

    private boolean isNumber(String s) {
        if (s == null || s.isEmpty()) return false;
        s = s.trim();

        return isDecimal(s) || isHex(s) || isBinary(s);
    }

    private boolean isBinary(String s) {
        if (s == null || s.isEmpty()) return false;
        String str = s.trim().toLowerCase();

        if (str.charAt(0) == '0' && str.charAt(1) == 'b') {
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
        return s != null && s.matches("[0-9]+");
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
        return false;
    }

    private boolean isRegister(String s) {
        return s != null && s.toUpperCase().matches("R([0-9]|1[0-5])");
    }

    private String getRegisterCode(String reg) {
        reg = reg.toUpperCase().substring(1);
        int num = Integer.parseInt(reg);
        return String.format("%1X", num);
    }

    public ArrayList<String> getObjCode() {
        return objCode;
    }

    public String getERROR() {
        return ERROR;
    }

    public ArrayList<String> getModTable(){
        return modTable;
    }
}
