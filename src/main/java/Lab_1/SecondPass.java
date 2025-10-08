package Lab_1;

import java.util.ArrayList;

public class SecondPass {
    String ERROR = "";
    ArrayList<String> objCode = new ArrayList<>();

    public void execute(ArrayList<ArrayList<String>> subTable,
                        ArrayList<ArrayList<String>> symTable,
                        String programName,
                        String programLength,
                        int startAddress) {

        ERROR = "";
        objCode.clear();

        if (subTable == null || subTable.isEmpty()) {
            ERROR = "Ошибка: Не выполнен первый проход";
            return;
        }

        objCode.add(String.format("H %-1s %06X %s",
                programName != null ? programName : "",
                startAddress,
                programLength != null ? programLength : "000000"));

        for (ArrayList<String> row : subTable) {
            String address = row.get(0);
            String operation = row.get(1);
            String op1 = row.get(2);
            String op2 = row.get(3);

            if (operation.equalsIgnoreCase("START") || operation.equalsIgnoreCase("END")) {
                continue;
            }

            op1 = resolveOperand(op1, symTable);
            if (ERROR.length() > 0) return;

            op2 = resolveOperand(op2, symTable);
            if (ERROR.length() > 0) return;

            String code = "";

            if (isDirective(operation)) {
                switch (operation.toUpperCase()) {
                    case "WORD":
                        code = String.format("%06X", parseNumber(op1));
                        break;
                    case "BYTE":
                        code = getByteObjectCode(op1);
                        if (code == null) {
                            ERROR = "Ошибка: некорректный BYTE операнд " + op1;
                            return;
                        }
                        break;
                    case "RESW":
                    case "RESB":
                        code = "";
                        break;
                }
            } else {
                code = operation + (op1.isEmpty() ? "" : op1) + (op2.isEmpty() ? "" : op2);
            }

            StringBuilder sb = new StringBuilder();
            sb.append("T ").append(address).append(" ");
            sb.append(String.format("%02X", code.length() / 2)).append(" ");
            if (isDirective(operation)) {
                sb.append(code);
            } else {
                if (!op1.isEmpty()) sb.append(op1);
                if (!op2.isEmpty()) sb.append(" ").append(op2);
            }

            objCode.add(sb.toString());
        }

        objCode.add(String.format("E %06X", startAddress));
    }

    private String resolveOperand(String op, ArrayList<ArrayList<String>> symTable) {
        if (op == null || op.isEmpty()) return "";

        op = op.trim();
        if (op.equals("?")) {
            return "";
        } else if (isRegister(op)) {
            return getRegisterCode(op);
        } else if (isNumber(op)) {
            return String.format("%X", parseNumber(op));
        } else if (isLabel(op)) {
            String addr = findLabelAddress(op, symTable);
            if (addr == null) {
                ERROR = "Ошибка: Метка '" + op + "' не найдена";
            }
            return addr;
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
        operand = operand.trim();
        char prefix = Character.toUpperCase(operand.charAt(0));
        int start = operand.indexOf('\'');
        int end = operand.lastIndexOf('\'');

        if (start == -1 || end == -1) {
            start = operand.indexOf('"');
            end = operand.lastIndexOf('"');
        }
        if (start == -1 || end == -1 || end <= start) return null;

        String content = operand.substring(start + 1, end);

        if (prefix != 'C' && prefix != 'X') {
            prefix = content.matches("[0-9A-Fa-f]+") ? 'X' : 'C';
        }

        switch (prefix) {
            case 'C':
                StringBuilder sb = new StringBuilder();
                for (char c : content.toCharArray()) {
                    sb.append(String.format("%02X", (int) c));
                }
                return sb.toString();
            case 'X':
                if (content.length() % 2 != 0) content = "0" + content;
                if (!content.matches("[0-9A-Fa-f]+")) return null;
                return content.toUpperCase();
            default:
                return null;
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
        return str.matches("[0-9a-fA-F]+");
    }

    private boolean isRegister(String s) {
        return s != null && s.toUpperCase().matches("R([0-9]|1[0-5])");
    }

    private String getRegisterCode(String reg) {
        reg = reg.toUpperCase().substring(1);
        int num = Integer.parseInt(reg);
        return String.format("%02X", num);
    }

    public ArrayList<String> getObjCode() {
        return objCode;
    }

    public String getERROR() {
        return ERROR;
    }
}
