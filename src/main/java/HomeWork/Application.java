package HomeWork;
import javax.swing.*;
public class Application {
    public static void main(String[] args) {

        if (args.length > 0) {
            new CLI(args).run();
            return;
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Не удалось установить системный стиль: " + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> {
            new Controller();
        });
    }
}