package HomeWork;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class CLI {
    private final Scanner scanner = new Scanner(System.in);
    private final Core core = new Core();

    private String[] sourceCode;
    private String inputPath;
    private String outputPath;

    public CLI(String[] args) {
        parseArgs(args);
    }

    public void run() {
        if (inputPath == null) {
            help();
            requestInputFile();
        } else {
            if (!loadSourceWithRetry(inputPath)) {
                requestInputFile();
            }
        }
        menuLoop();
    }

    private void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("-help") || arg.equals("--help")) {
                help();
                System.exit(0);
            }
            if (arg.startsWith("-input_file=")) {
                inputPath = stripQuotes(arg.substring(12));
            }
            if (arg.startsWith("-output_file=")) {
                outputPath = stripQuotes(arg.substring(13));
            }
        }
    }

    private String stripQuotes(String s) {
        if ((s.startsWith("\"") && s.endsWith("\"")) ||
                (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private void menuLoop() {
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("> ");
            String cmd = scanner.nextLine().trim();

            switch (cmd) {
                case "1" -> step();
                case "2" -> fullPass();
                case "3" -> printSource();
                case "4" -> printResult();
                case "5" -> printVarTable();
                case "6" -> printMacroNames();
                case "7" -> printMacroDefs();
                case "8" -> saveResult();
                case "9" -> restart();
                case "10" -> resetAll();
                case "11" -> running = false;
                default -> System.out.println("Неизвестная команда");
            }
        }
    }

    private void printMenu() {
        System.out.println("""
                ==================== МЕНЮ ====================
                1  - Выполнить один шаг
                2  - Выполнить полный проход
                3  - Показать исходный код
                4  - Показать результирующий код
                5  - Показать таблицу глобальных переменных
                6  - Показать таблицу имён макросов
                7  - Показать таблицу макроопределений
                8  - Сохранить результирующий код
                9  - Начать выполнение заново
                10 - Полный сброс
                11 - Выход
                ==============================================
                """);
    }


    private void step() {
        if (core.get_line_id() == sourceCode.length) {
            core.reset();
        }
        if (core.get_line_id() == 0) {
            core.one_step(sourceCode);
        } else {
            core.one_step(null);
        }
        printStatus();
    }


    private void fullPass() {
        core.reset();
        core.full_pass(sourceCode);
        printStatus();
    }


    private void restart() {
        core.reset();
        System.out.println("Выполнение начато заново.");
    }

    private void resetAll() {
        core.reset();
        requestInputFile();
    }

    private void printSource() {
        printCodeBlock("Исходный код", sourceCode);
    }

    private void printResult() {
        printCodeBlock(
                "Результирующий код",
                core.getResultCodeAsString().split(System.lineSeparator())
        );
    }


    private void printVarTable() {
        printTable(
                new String[]{"Имя", "Тип", "Значение", "Область"},
                core.getVariableTable()
        );
    }


    private void printMacroNames() {
        printTable(
                new String[]{"Имя", "Начало", "Длина"},
                core.getMacroNameTable()
        );
    }

    private void printMacroDefs() {
        printTable(
                new String[]{"Метка", "Строка"},
                core.getMacroDefinitionTable()
        );
    }

    private void printStatus() {
        if (!core.getERROR().isEmpty()) {
            System.out.println("ОШИБКА: " + core.getERROR());
        } else {
            System.out.println("Выполнена строка: " + core.get_line_id());
        }
    }

    private void saveResult() {
        if (outputPath == null) {
            System.out.print("Введите путь для сохранения: ");
            outputPath = scanner.nextLine();
        }

        Path path = Path.of(outputPath);

        if (Files.exists(path)) {
            writeResult(path);
            return;
        }

        System.out.println("""
                Файл не существует:
                1 - Создать файл
                2 - Ввести другой путь
                0 - Отмена
                """);

        System.out.print("> ");
        switch (scanner.nextLine()) {
            case "1" -> writeResult(path);
            case "2" -> {
                outputPath = null;
                saveResult();
            }
            default -> System.out.println("Сохранение отменено.");
        }
    }

    private void writeResult(Path path) {
        try {
            Files.writeString(path, core.getResultCodeAsString());
            System.out.println("Результат сохранён: " + path);
        } catch (IOException e) {
            System.out.println("Ошибка записи файла: " + e.getMessage());
        }
    }

    private void requestInputFile() {
        while (true) {
            System.out.print("Введите путь к исходному файлу (0 - выход): ");
            String path = scanner.nextLine().trim();

            if (path.equals("0")) {
                System.out.println("Выход из программы.");
                System.exit(0);
            }

            if (loadSourceWithRetry(path)) {
                return;
            }
        }
    }

    private boolean loadSourceWithRetry(String path) {
        try {
            sourceCode = Files.readAllLines(Path.of(path)).toArray(new String[0]);
            inputPath = path;
            core.reset();
            System.out.println("Файл успешно загружен.");
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка: файл не найден или недоступен.");
            return false;
        }
    }

    private void printCodeBlock(String title, String[] lines) {
        System.out.println("==== " + title + " ====");
        for (int i = 0; i < lines.length; i++) {
            System.out.printf("%4d | %s%n", i + 1, lines[i]);
        }
    }
    private void printTable(String[] headers, String[][] data) {
        int cols = headers.length;
        int[] widths = new int[cols];

        for (int i = 0; i < cols; i++) {
            widths[i] = headers[i].length();
        }

        for (String[] row : data) {
            for (int i = 0; i < cols; i++) {
                if (row[i] != null)
                    widths[i] = Math.max(widths[i], row[i].length());
            }
        }

        printSeparator(widths);
        printRow(headers, widths);
        printSeparator(widths);

        for (String[] row : data) {
            printRow(row, widths);
        }

        printSeparator(widths);
    }

    private void printSeparator(int[] widths) {
        for (int w : widths) {
            System.out.print("+-" + "-".repeat(w) + "-");
        }
        System.out.println("+");
    }

    private void printRow(String[] row, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            String cell = (row[i] == null) ? "" : row[i];
            System.out.printf("| %-" + widths[i] + "s ", cell);
        }
        System.out.println("|");
    }

    public void help() {
        System.out.println(
            """
            ============================================================
            Макропроцессор ассемблерного языка
            ============================================================
            
            Назначение:
            Программа выполняет обработку макроязыка ассемблера,
            поддерживает макроопределения, макровызовы, переменные,
            условную и циклическую макрогенерацию.
            
            ------------------------------------------------------------
            Ключи командной строки:
            ------------------------------------------------------------
            
            -input_file=<путь>   Путь к файлу с исходным кодом.
                                 Если файл не найден, программа предложит
                                 ввести корректный путь вручную.
            
            Примеры:
              -input_file=text.txt
              -input_file="code text.txt"
            
            -output_file=<путь>  Путь к файлу для сохранения результирующего
                                 ассемблерного кода.
                                 Если файл не существует, программа предложит
                                 создать его или указать другой путь.
            
            Примеры:
              -output_file=out.txt
              -output_file="result code.txt"
            
            -help                Вывод данной справки.
            
            ------------------------------------------------------------
            После загрузки файла доступно интерактивное меню CLI.
            ============================================================
            """
        );
    }
}

