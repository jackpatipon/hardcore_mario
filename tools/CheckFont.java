package tools;
import java.awt.*;
public class CheckFont {
    public static void main(String[] args) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        System.out.println("Checking available fonts that can display Thai ('ภ'):");
        int count = 0;
        for (String name : ge.getAvailableFontFamilyNames()) {
            Font f = new Font(name, Font.PLAIN, 12);
            if (f.canDisplay('ภ')) {
                System.out.println("  - " + name);
                count++;
            }
        }
        System.out.println("Total Thai fonts found: " + count);
        Font sans = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        System.out.println("Font.SANS_SERIF canDisplay('ภ'): " + sans.canDisplay('ภ'));
        Font dialog = new Font(Font.DIALOG, Font.PLAIN, 12);
        System.out.println("Font.DIALOG canDisplay('ภ'): " + dialog.canDisplay('ภ'));
        Font tahoma = new Font("Tahoma", Font.PLAIN, 12);
        System.out.println("Tahoma canDisplay('ภ'): " + tahoma.canDisplay('ภ'));
    }
}
