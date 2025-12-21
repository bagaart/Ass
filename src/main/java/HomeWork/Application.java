package HomeWork;
import javax.swing.*;
public class Application {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Не удалось установить системный стиль: " + e.getMessage());}
        SwingUtilities.invokeLater(() -> {
            new Controller();});}}
