package HomeWork;

import java.util.*;

public class Core {
    private boolean can_be_editable = true;
    private List<String[]> nam_tab = new ArrayList<>();
    private List<String[]> def_tab = new ArrayList<>();
    private List<String[]> var_tab = new ArrayList<>();
    private List<String> res_tab = new ArrayList<>();
    private String[] source_code;
    private int line_id = 0;
    private int def_line_id = -1;
    private String ERROR = "";
    private boolean end_flag = false;
    private boolean macro_record = false;
    private String current_macro = "";
    private static class IfState {
        boolean conditionTrue;
        boolean elseUsed;
    }
    private static class WhileState {
        int executionLine;
        String[] condition;
        int iterations;
    }
    private final List<IfState> ifStack = new ArrayList<>();
    private final List<WhileState> whileStack = new ArrayList<>();
    private Map<String, String[]> macroParams = new HashMap<>();
    private int unique_label_index = 0;
    private int endFlag = -1;
    private Map<String, String> localLabelMap = new HashMap<>();
    private List<String> labels = new ArrayList<>();


    public void one_step(String[] source_code){
        if (this.source_code == null || this.source_code.length == 0) {
            this.setSourceCode(source_code);
        }
        if (!ERROR.isEmpty()) {
            return;
        }
        if (line_id >= this.source_code.length) {
            can_be_editable = true;
            reset();
            return;
        }
        can_be_editable = false;
        String line = getCurrentLine();
        step(line);
        if (!ERROR.isEmpty()) {
            ERROR = (line_id + 1) + " -- " + ERROR;
            if (!current_macro.isEmpty() && def_line_id != -1) {
                ERROR = ERROR + " Возможная строка с ошибкой - (" + String.join(" ", def_tab.get(def_line_id)).trim() + "). номер строки в таблице макроопредений - " + (def_line_id + 1);
            }
            can_be_editable = true;
        }
        if ((endFlag == 1 || line_id == this.source_code.length - 1) && macro_record) {
            ERROR = "Ошибка: Ожидалось MEND. Незавершенное макроопределение";
        }
        nextLine();
    }

    public void full_pass(String[] source_code){
        reset();
        setSourceCode(source_code);
        while (line_id < source_code.length && ERROR.isEmpty()) {
            one_step(source_code);
        }
    }

    private boolean step(String line) {
        if (endFlag == 1) return true;
        if (line.isEmpty()){
            return true;
        }
        List<String> tokens = split_line(line);
        if (tokens.isEmpty() || (tokens.size() == 1 && tokens.getFirst().isEmpty())) {
            res_tab.add("");
            return true;
        }
        int idx = 0;
        String label = "";
        String macroName = "";
        String mnemonic = "";
        String[] body = {};
        if (isLabel(tokens.get(idx))) {
            label = tokens.get(idx);
            if (labels.contains(label.toUpperCase()) && current_macro.isEmpty()) {
                ERROR = "Ошибка: Повторное определение метки";
                return false;
            }
            labels.add(label.toUpperCase());
            idx++;
        } else if (can_be_macro_name(tokens.get(idx))) {
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).equalsIgnoreCase("MACRO")) {
                    macroName = tokens.get(idx);
                    idx++;
                    break;
                }
            }
        }


        if (idx < tokens.size()) {
            mnemonic = tokens.get(idx);
            idx++;
        }
        if (idx < tokens.size()) {
            body = tokens.subList(idx, tokens.size()).toArray(new String[0]);
        }
        if (isAssemblyMnemonic(mnemonic)) {
            if (mnemonic.equalsIgnoreCase("END")) {
                endFlag = 1;
            }
            if (!macro_record) {
                if (!label.isEmpty()) {
                    label = label.substring(0, label.length() - 1) + "_" + unique_label_index + ":";
                    line = String.join(" ", label, mnemonic, String.join(" ", body) );
                }
                line = substituteVariables(line);
                res_tab.add(line);
            } else {
                def_tab.add(new String[] {"", line});
            }
            return true;
        } else if (isMacroMnemonic(mnemonic)) {
            String labelOrMacroName;
            if (!macroName.isEmpty()) labelOrMacroName = macroName;
            else labelOrMacroName = label;
            if (macroMnemonics(labelOrMacroName, mnemonic, body, line)) {
                return true;
            }
            return false;
        } else if (isMacroName(mnemonic)) {
            if (macro_record) {
                ERROR = "Ошибка: Макровызов внутри макроса недопустим";
                return false;
            }
            if (macroGeneration(macroName, mnemonic, body, line)) {
                return true;
            }
            return false;
        } else if (mnemonic.isEmpty() && body.length == 0 && !label.isEmpty()) {
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            } else {
                res_tab.add(line);
                return true;
            }
        } else {
            ERROR = "Ошибка: нераспознанная команда макроязыка";
        }
        return true;
    }
    private boolean macroMnemonics(String label, String mnemonic, String[] body, String line) {
        if (mnemonic.equalsIgnoreCase("MACRO")) {
            if (macro_record) {
                ERROR = "Ошибка: вложенное макроопределение недопустимо.";
                return false;
            }
            if (!can_be_macro_name(label)) {
                ERROR = "Ошибка: некорректное имя макроса";
                return false;
            }
            macro_record = true;
            String paramLine = String.join(" ", body);
            nam_tab.add(new String[] {label, String.valueOf(def_tab.size()), "0"});
            def_tab.add(new String[] {label, paramLine});
            return true;
        }
        if (mnemonic.equalsIgnoreCase("MEND")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву MEND";
                return false;
            }
            if (!macro_record) {
                ERROR = "Ошибка: использование директивы MEND без предшествующей директивы MACRO";
            }
            if (!ifStack.isEmpty()) {
                ERROR = "Ошибка: не все IF закрыты ENDIF.";
                return false;
            }
            if (!whileStack.isEmpty()) {
                ERROR = "Ошибка: не все WHILE закрыты ENDW.";
                return false;
            }
            def_tab.add(new String[] {"", "MEND"});
            macro_record = false;
            String[] macro = nam_tab.get(nam_tab.size() - 1);
            int start = Integer.parseInt(macro[1]);
            int end = def_tab.size();
            macro[2] = String.valueOf(end - start);
            return true;
        }
        if (mnemonic.equalsIgnoreCase("VAR")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву VAR";
                return false;
            }
            if (body.length == 0) {
                ERROR = "Ошибка: отсутствует имя переменной для объявления";
                return false;
            }
            if (body.length > 2) {
                ERROR = "Ошибка: присутствуют лишние данные. Ожидалось - VAR <имя переменной> [значение]. Проверьте наличие кавычки.";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            }
            if (!can_be_var_name(body[0])) {
                ERROR = "Ошибка: некорректное имя переменной.";
                return false;
            }
            String visible = current_macro.isEmpty() ? "global" : current_macro;
            if (findVarIndex(body[0]) != -1) {
                ERROR = "Ошибка: переменная с данным именем уже объявлена в данной области видимости - " + var_tab.get(findVarIndex(body[0]))[3] + ".";
                return false;
            }
            if (body.length == 1) {
                var_tab.add(new String[] {body[0], "", "", visible});
                return true;
            }
            String init = body[1];
            String type;
            String value;

            if (body[1].startsWith("&")) {
                init = init.substring(1);
            }

            if (isInteger(init)) {
                type = "INT";
                value = init;
            } else if (isStringLiteral(init)) {
                type = "STRING";
                value = init;
            } else if (can_be_var_name(init)) {
                if (body[1].startsWith("&")) {
                    init = "&" + init;
                }
                int srcIndex = findVarIndex(init);
                if (srcIndex == -1) {
                    ERROR = "Ошибка: переменной не существует - " + init +  ".";
                    return false;
                }
                String[] src = var_tab.get(srcIndex);
                type = src[1];
                value = src[2];
            } else {

                ERROR = "Ошибка: некорректная инициализация переменной - " + body[0] + " значением - " + init + ".";
                return false;
            }
            var_tab.add(new String[] {body[0], type, value, visible});
            return true;
        }
        if (mnemonic.equalsIgnoreCase("SET")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву SET";
                return false;
            }
            if (body.length != 2) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - SET <имя переменной> <значение>";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            }
            int varIndex = findVarIndex(body[0]);
            if (varIndex == -1) {
                ERROR = "Ошибка: переменной с таким именем не существует.";
                return false;
            }
            String[] target = var_tab.get(varIndex);
            String targetType = target[1];
            String init = body[1];
            String newType;
            String newValue;
            if (isStringLiteral(init)) {
                newType = "STRING";
                newValue = init;
            }
            else if (isInteger(init)) {
                newType = "INT";
                newValue = init;
            }
            else {
                int srcIndex = findVarIndex(init);
                if (srcIndex == -1) {
                    ERROR = "Ошибка: переменной не существует - " + init + ".";
                    return false;
                }
                String[] src = var_tab.get(srcIndex);
                newType = src[1];
                newValue = src[2];
            }
            if (!targetType.isEmpty() && !targetType.equals(newType)) {
                ERROR = "Ошибка: несовместимые типы. Ожидался " + targetType + ", получен " + newType +  ".";
                return false;
            }
            target[1] = newType;
            target[2] = newValue;
            return true;
        }
        if (mnemonic.equalsIgnoreCase("INC")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву INC";
                return false;
            }
            if (body.length != 1) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - INC <имя переменной>";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            }
            int varIndex = findVarIndex(body[0]);
            if (varIndex == -1) {
                ERROR = "Ошибка: переменной с таким именем не существует.";
                return false;
            }
            String[] var = var_tab.get(varIndex);
            if (!"INT".equals(var[1])) {
                ERROR = "Ошибка: INC применим только к переменным типа INT.";
                return false;
            }
            if (var[2] == null || var[2].isEmpty()) {
                ERROR = "Ошибка: переменная не инициализирована.";
                return false;
            }
            int value;
            try {
                value = Integer.parseInt(var[2]);
            } catch (Exception e) {
                ERROR = "Ошибка: значение переменной повреждено (ожидалось INT).";
                return false;
            }
            var[2] = String.valueOf(value + 1);
            return true;
        }
        if (mnemonic.equalsIgnoreCase("DEC")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву DEC";
                return false;
            }
            if (body.length != 1) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - DEC <имя переменной>";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            }
            int varIndex = findVarIndex(body[0]);
            if (varIndex == -1) {
                ERROR = "Ошибка: переменной с таким именем не существует.";
                return false;
            }
            String[] var = var_tab.get(varIndex);
            if (!"INT".equals(var[1])) {
                ERROR = "Ошибка: DEC применим только к переменным типа INT.";
                return false;
            }
            if (var[2] == null || var[2].isEmpty()) {
                ERROR = "Ошибка: переменная не инициализирована.";
                return false;
            }
            int value;
            try {
                value = Integer.parseInt(var[2]);
            } catch (Exception e) {
                ERROR = "Ошибка: значение переменной повреждено (ожидалось INT).";
                return false;
            }
            var[2] = String.valueOf(value - 1);
            return true;
        }
        if (mnemonic.equalsIgnoreCase("IF")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву IF";
                return false;
            }
            if (body.length != 3) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - IF <параметр1> < '<' | '>' | '==' | '<=' | '>=' | '!=' > <параметр2>";
                return false;
            }
            Set<String> operators = Set.of("<", ">", "==", "<=", ">=", "!=");
            if (!operators.contains(body[1])) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - IF <параметр1> < '<' | '>' | '==' | '<=' | '>=' | '!=' > <параметр2>";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            }
            Object v1 = resolveValue(body[0]);
            Object v2 = resolveValue(body[2]);
            if (!ERROR.isEmpty()) return false;
            boolean result = compare(v1, v2, body[1]);
            IfState st = new IfState();
            st.conditionTrue = result;
            st.elseUsed = false;
            ifStack.add(st);
            if (!result) {
                skipToElseOrEndif();
            }
            return true;
        }
        if (mnemonic.equalsIgnoreCase("ELSE")){
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву ELSE";
                return false;
            }
            if (body.length != 0) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - ELSE";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[]{"", line});
                return true;
            }
            if (ifStack.isEmpty()) {
                ERROR = "Ошибка: ELSE без предшествующей директивы IF.";
                return false;
            }
            IfState st= ifStack.get(ifStack.size() - 1);
            if (st.elseUsed) {
                ERROR = "Ошибка: повторный ELSE.";
                return false;
            }
            st.elseUsed = true;
            if (st.conditionTrue) {
                skipToEndif();
            }
            return true;
        }
        if (mnemonic.equalsIgnoreCase("ENDIF")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву ENDIF";
                return false;
            }
            if (body.length != 0) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - ENDIF";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[]{"", line});
                return true;
            }
            if (ifStack.isEmpty()) {
                ERROR = "Ошибка: ENDIF без предшествующей директивы IF.";
                return false;
            }
            ifStack.remove(ifStack.size() - 1);
            return true;
        }
        if (mnemonic.equalsIgnoreCase("WHILE")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву WHILE";
                return false;
            }
            if (body.length != 3) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - WHILE <параметр1> < '<' | '>' | '==' | '<=' | '>=' | '!=' > <параметр2>";
                return false;
            }
            Set<String> operators = Set.of("<", ">", "==", "<=", ">=", "!=");
            if (!operators.contains(body[1])) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - WHILE <параметр1> < '<' | '>' | '==' | '<=' | '>=' | '!=' > <параметр2>";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[]{"", line});
                return true;
            }
            Object v1 = resolveValue(body[0]);
            Object v2 = resolveValue(body[2]);
            if (!ERROR.isEmpty()) return false;
            boolean result = compare(v1, v2, body[1]);
            WhileState st = new WhileState();
            st.executionLine = isMacroExpansion() ? def_line_id: line_id;
            st.condition = body;
            st.iterations = 0;
            whileStack.add(st);
            if (!result) {
                whileStack.remove(whileStack.size() - 1);
                skipToEndw();
            }
            return true;
        }
        if (mnemonic.equalsIgnoreCase("ENDW")) {
            if (!label.isEmpty()) {
                ERROR = "Ошибка: метка на директиву ENDW";
                return false;
            }
            if (body.length != 0) {
                ERROR = "Ошибка: неверный формат строки. Ожидалось - ENDW";
                return false;
            }
            if (macro_record) {
                def_tab.add(new String[] {"", line});
                return true;
            }
            if (whileStack.isEmpty()) {
                ERROR = "Ошибка: ENDW без предшествующей директивы WHILE.";
                return false;
            }
            WhileState st = whileStack.get(whileStack.size() - 1);
            Object v1 = resolveValue(st.condition[0]);
            Object v2 = resolveValue(st.condition[2]);
            if (!ERROR.isEmpty()) return false;
            boolean result = compare(v1, v2, st.condition[1]);
            if (result) {
                if (isMacroExpansion()) {
                    def_line_id = st.executionLine;
                } else {
                    line_id = st.executionLine;
                }
                unique_label_index++;

                localLabelMap.clear();
                collectLocalLabels();

            } else {
                whileStack.remove(whileStack.size() - 1);
            }
            if (result) {
                st.iterations++;
                if (st.iterations > 1000) {
                    ERROR = "Ошибка: возможный бесконечный цикл WHILE (прошло 1000 итераций) , текущее условие - "
                            + resolveValue(st.condition[0]) + " "
                            + st.condition[1] + " "
                            + resolveValue(st.condition[2]) + ". ";
                    return false;
                }
            }
            return true;
        }
        return true;
    }
    private void skipToElseOrEndif() {
        int depth = 0;
        while (true) {
            nextLine();
            String line = getCurrentLine();
            List<String> tokens = split_line(line);
            if (tokens.isEmpty()) continue;
            String cmd = tokens.get(tokens.size() > 1 && isLabel(tokens.get(0)) ? 1 : 0).toUpperCase();
            if (cmd.equals("IF")) depth++;
            else if (cmd.equals("ENDIF")) {
                if (depth == 0) return;
                depth--;
            }
            else if (cmd.equals("ELSE") && depth == 0) {
                return;
            }
        }
    }
    private void skipToEndif() {
        int depth = 0;
        while (true) {
            nextLine();
            String line = getCurrentLine();
            List<String> tokens = split_line(line);
            if (tokens.isEmpty()) continue;
            String cmd = tokens.get(tokens.size() > 1 && isLabel(tokens.get(0)) ? 1 : 0).toUpperCase();
            if (cmd.equals("IF")) depth++;
            else if (cmd.equals("ENDIF")) {
                if (depth == 0) return;
                depth--;
            }
        }
    }
    private void skipToEndw() {
        int depth = 0;
        while (true) {
            nextLine();
            String line = getCurrentLine();
            List<String> tokens = split_line(line);
            if (tokens.isEmpty()) continue;
            String cmd = tokens.get(tokens.size() > 1 && isLabel(tokens.get(0)) ? 1 : 0).toUpperCase();
            if (cmd.equals("WHILE")) depth++;
            else if (cmd.equals("ENDW")) {
                if (depth == 0) return;
                depth--;
            }
        }
    }
    private boolean isMacroExpansion() {
        return !macro_record && def_line_id > 0 && def_line_id < def_tab.size();
    }
    private String getCurrentLine() {
        if (isMacroExpansion()) {
            return def_tab.get(def_line_id)[1];
        }
        return source_code[line_id];
    }
    private void nextLine() {
        if (isMacroExpansion()) {
            def_line_id++;
        } else {
            line_id++;
        }
    }
    private Object resolveValue(String token) {
        int idx = findVarIndex(token);
        if (idx != -1) {
            String[] var = var_tab.get(idx);
            if ("INT".equals(var[1])) {
                return Integer.parseInt(var[2]);
            }
            if ("STRING".equals(var[1])) {
                return var[2];
            }
            ERROR = "Ошибка: переменная не инициализирована - " + token +  ".";
            return null;
        }
        if (isStringLiteral(token)) {
            return token.substring(1, token.length() - 1);
        }
        if (isInteger(token)) {
            return Integer.parseInt(token);
        }
        if (token.startsWith("&")) {
            ERROR = "Ошибка: параметр макроса не найден в области видимости - " + token + ".";
            return null;
        }
        ERROR = "Ошибка: переменной не существует - " + token +  ".";
        return null;
    }
    private boolean compare(Object a, Object b, String op) {
        if (a instanceof Integer && b instanceof String) {
            a = a.toString();
        }
        if (a instanceof  String && b instanceof Integer) {
            b = b.toString();
        }
        if (a instanceof Integer && b instanceof Integer) {
            int x = (Integer) a;
            int y = (Integer) b;
            return switch (op) {
                case "<" -> x < y;
                case ">" -> x > y;
                case "==" -> x == y;
                case "<=" -> x <= y;
                case ">=" -> x >= y;
                case "!=" -> x != y;
                default -> false;
            };
        }
        String x = a.toString();
        String y = b.toString();
        return switch (op) {
            case "==" -> x.equals(y);
            case "!=" -> !x.equals(y);
            default -> false;
        };
    }
    private boolean isStringLiteral(String s) {
        return (s.startsWith("\"") && s.endsWith("\"")) ||
                (s.startsWith("'") && s.endsWith("'"));
    }
    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private int findVarIndex(String varName) {
        int global = -1;
        for (int i = 0; i < var_tab.size(); i++) {
            String[] row = var_tab.get(i);
            if (!row[0].equalsIgnoreCase(varName)) continue;
            if (row[3].equals(current_macro)) {
                return i;
            }
            if (row[3].equals("global")) {
                global = i;
            }
        }
        return global;
    }
    private int findMacro(String macro) {
        int i = 0;
        for (i = 0; i < nam_tab.size(); i++ ){
            if (macro.equalsIgnoreCase(nam_tab.get(i)[0])) {
                return i;
            }
        }
        return -1;
    }
    private boolean macroGeneration(String label, String macroName, String[] args, String line) {
        localLabelMap.clear();
        unique_label_index++;
        int macroIndex = findMacro(macroName);
        if (macroIndex == -1) {
            ERROR = "Ошибка: макрос не найден - " + macroName;
            return false;
        }
        current_macro = macroName;
        String[] macroEntry = nam_tab.get(macroIndex);
        int defStart = Integer.parseInt(macroEntry[1]);
        int defLen = Integer.parseInt(macroEntry[2]);
        String macroHeader = def_tab.get(defStart)[1];
        Map<String, String> defaultParams = new LinkedHashMap<>();
        List<String> headerParams = parseMacroParams(macroHeader);
        collectLocalLabels();
        if (headerParams.isEmpty()) {
            if (args.length > 0) {
                ERROR = "Ошибка: макрос " + macroName + " не принимает параметров";
                return false;
            }
        }
        for (String p : headerParams) {
            if (p.isEmpty()) continue;
            if (!p.startsWith("&")) {
                ERROR = "Ошибка: параметр макроса должен начинаться с &";
                return false;
            }
            String param = p.substring(1);
            if (param.contains("=")) {
                String[] kv = param.split("=", 2);
                defaultParams.put(kv[0], kv[1]);
            } else {
                defaultParams.put(param, null);
            }
        }
        Map<String, String> callParams = new HashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("&") || !arg.contains("=")) {
                ERROR = "Ошибка: параметры макроса должны задаваться как &name=value";
                return false;
            }
            String[] kv = arg.substring(1).split("=", 2);
            callParams.put(kv[0], kv[1]);
        }
        macroParams.clear();
        for (Map.Entry<String, String> e : defaultParams.entrySet()) {
            String name = e.getKey();
            String raw  = callParams.getOrDefault(name, e.getValue());
            if (raw == null) {
                ERROR = "Ошибка: не задан обязательный параметр макроса - &" + name;
                return false;
            }
            String type;
            String value;
            if (isStringLiteral(raw)) {
                type = "STRING";
                value = raw;
            }
            else if (isInteger(raw)) {
                type = "INT";
                value = raw;
            }
            else {
                int idx = findVarIndex(raw);
                if (idx == -1) {
                    ERROR = "Ошибка: переменной не существует - " + raw;
                    return false;
                }
                String[] v = var_tab.get(idx);
                type = v[1];
                value = v[2];
            }
            macroParams.put(name, new String[]{type, value});
        }
        for (String p : callParams.keySet()) {
            if (!macroParams.containsKey(p)) {
                ERROR = "Ошибка: неизвестный параметр макроса - &" + p;
                return false;
            }
        }
        def_line_id = defStart + 1;
        int defEnd = defStart + defLen - 1;
        addMacroParamsToVarTab();
        while (def_line_id < defEnd) {
            String srcLine = def_tab.get(def_line_id)[1];
            if (!step(srcLine)) {
                return false;
            }
            def_line_id++;
        }
        removeMacroParamsFromVarTab();
        macroParams.clear();
        def_line_id = -1;
        current_macro = "";
        localLabelMap.clear();
        return true;
    }
    private String substituteVariables(String line) {
        if (line == null || line.isEmpty()) return line;

        List<String> tokens = split_line(line);

        for (int i = 0; i < tokens.size(); i++) {
            String t = tokens.get(i);
            if (isStringLiteral(t)) continue;
            if (isLabel(t)) {
                String base = t.substring(0, t.length() - 1);
                if (localLabelMap.containsKey(base)) {
                    tokens.set(i, localLabelMap.get(base) + ":");
                }
                continue;
            }
            if (localLabelMap.containsKey(t)) {
                tokens.set(i, localLabelMap.get(t));
                continue;
            }
            int varIdx = findVarIndex(t);
            if (varIdx != -1) {
                String[] var = var_tab.get(varIdx);
                if (var[2] != null && !var[2].isEmpty()) {
                    tokens.set(i, var[2]);
                }
            }
        }
        return String.join(" ", tokens);
    }
    private void addMacroParamsToVarTab() {
        for (Map.Entry<String, String[]> entry : macroParams.entrySet()) {
            String name = '&' + entry.getKey();
            String type = entry.getValue()[0];
            String value = entry.getValue()[1];
            var_tab.add(new String[]{name, type, value, current_macro});
        }
    }
    private void removeMacroParamsFromVarTab() {
        var_tab.removeIf(v -> v[3].equals(current_macro));
    }
    private List<String> parseMacroParams(String line) {
        List<String> params = new ArrayList<>();
        if (line == null) return params;
        line = line.trim();
        if (line.isEmpty()) return params;
        String[] tokens = line.trim().split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("MACRO")) continue;
            params.add(tokens[i]);
        }
        return params;
    }
    private boolean isMacroName(String name) {
        for (String[] row : nam_tab) {
            if (name.equalsIgnoreCase(row[0])) return true;
        }
        return false;
    }
    private List<String> split_line(String line){
        List<String> tokens = new ArrayList<>();
        if (line == null) return tokens;
        int i = 0, n = line.length();
        StringBuilder sb = new StringBuilder();
        while (i < n) {
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
    public int get_line_id() {
        return line_id;
    }
    public void set_line_id(int number) {
        line_id = number;
    }
    public String[][] getMacroNameTable() {
        return nam_tab.toArray(new String[0][]);
    }
    public String[][] getMacroDefinitionTable() {
        return def_tab.toArray(new String[0][]);
    }
    public String[][] getVariableTable() {
        return var_tab.toArray(new String[0][]);
    }
    public String[] getResultTable() {
        return res_tab.toArray(new String[0]);
    }
    public String getResultCodeAsString() {
        return String.join(System.lineSeparator(), res_tab);
    }
    public boolean isCan_be_editable() {
        return can_be_editable;
    }
    public String getERROR(){
        return ERROR;
    }
    public void setSourceCode(String[] code) {
        reset();
        source_code = code;
        line_id = 0;
        current_macro = "";
        ERROR = "";
        end_flag = false;
        macro_record = false;
    }
    public void reset() {
        nam_tab.clear();
        def_tab.clear();
        var_tab.clear();
        res_tab.clear();
        source_code = null;
        line_id = 0;
        def_line_id = 0;
        ERROR = "";
        end_flag = false;
        macro_record = false;
        current_macro = "";
        ifStack.clear();
        whileStack.clear();
        can_be_editable = true;
        unique_label_index = 0;
        endFlag = 0;
        labels.clear();
    }
    public boolean is_letter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
    public boolean is_special_symbol(char c) {
        return c == '?' || c == '@' || c == '.' || c == '_' || c == '$';
    }
    private boolean is_valid_char(char c) {
        return is_letter(c) || is_digit(c) || is_special_symbol(c);
    }
    private boolean is_digit(char c) {
        return c >= '0' && c <= '9';
    }
    public boolean can_be_macro_name(String s) {
        if (s.isEmpty()) return false;
        if (s.length() > 33) return false;
        char first = s.charAt(0);
        if (!(is_letter(first) || is_special_symbol(first))) return false;
        for (char c : s.toCharArray()) {
            if (!is_valid_char(c)) return false;
        }
        if (is_reserved_word(s)) return false;
        if (isAssemblyMnemonic(s)) return false;
        if (isMacroMnemonic(s)) return false;
        return true;
    }

    public boolean can_be_var_name(String s) {
        if (s.isEmpty()) return false;
        if (s.length() > 33) return false;
        if (s.startsWith("&")) return false;
        char first = s.charAt(0);
        if (!(is_letter(first) || is_special_symbol(first))) return false;
        for (char c : s.toCharArray()) {
            if (!is_valid_char(c)) return false;
        }
        if (is_reserved_word(s)) return false;
        return true;
    }
    public boolean is_reserved_word(String s){
        return RESERVED_WORDS.contains(s.toUpperCase());
    }
    private static final Set<String> RESERVED_WORDS = Set.of(
            "START", "END", "MACRO", "MEND",
            "IF", "ELSE", "ENDIF", "WHILE",
            "ENDW", "VAR", "SET", "INC",
            "DEC", "BYTE", "WORD", "RESB",
            "RESW", "CSECT", "EXTREF", "EXTDEF", "ADD"
    );
    public boolean isLabel(String label) {
        if (label == null || label.isEmpty()) return false;
        if (label.length() > 33 || label.length() < 2) return false;
        if (label.charAt(label.length() - 1) != ':') return false;
        String name = label.substring(0, label.length() - 1);
        char first = name.charAt(0);
        if (!(is_letter(first) || is_special_symbol(first))) return false;
        for (char c : name.toCharArray()) {
            if (!is_valid_char(c)) return false;
        }
        if (is_reserved_word(name)) return false;
        return true;
    }
    private static final Set<String> assembly_commands = Set.of(
            "JMP", "ADD", "LOADR1", "LOADR2", "SAVER1", "NOP", "INT", "SUB"
    );
    private static final Set<String> assembly_directives = Set.of(
            "START", "END", "EXTREF", "EXTDEF", "CSECT"
    );
    private static final Set<String> macro_directives = Set.of(
            "MACRO", "MEND", "IF", "ELSE", "ENDIF",
            "WHILE", "ENDW", "VAR", "SET", "INC", "DEC"
    );
    public boolean isAssemblyMnemonic(String mnemonic) {
        if (assembly_directives.contains(mnemonic.toUpperCase())) return true;
        if (assembly_commands.contains(mnemonic.toUpperCase())) return true;
        return false;
    }
    public boolean isMacroMnemonic(String mnemonic) {
        return macro_directives.contains(mnemonic.toUpperCase());
    }
    private void collectLocalLabels() {
        int macroIndex = findMacro(current_macro);
        String[] macroEntry = nam_tab.get(macroIndex);
        int from = Integer.parseInt(macroEntry[1]) + 1;
        int to = from + Integer.parseInt(macroEntry[2]) - 1;
        for (int i = from; i < to; i++) {
            String line = def_tab.get(i)[1];
            List<String> tokens = split_line(line);
            if (tokens.isEmpty()) continue;

            String first = tokens.get(0);
            if (isLabel(first)) {
                String base = first.substring(0, first.length() - 1);
                localLabelMap.put(
                        base,
                        base + "_" + unique_label_index
                );
            }
        }
    }
}