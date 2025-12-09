import com.codecrafters.shell.parser.CommandParser;
import java.util.List;

public class QuickTest {
    public static void main(String[] args) {
        CommandParser parser = new CommandParser();
        String input = "\"exe with \\'single quotes\\'\"";
        List<String> result = parser.parseArguments(input);
        
        String cmd = result.get(0);
        System.out.println("Input: " + input);
        System.out.println("Output: '" + cmd + "'");
        System.out.println("Length: " + cmd.length());
        System.out.println("Expected with backslashes: 'exe with \\'single quotes\\''");
        System.out.println("Expected without backslashes: 'exe with 'single quotes''");
        
        // Mostrar cada carácter
        System.out.println("\nCaracteres:");
        for (int i = 0; i < cmd.length(); i++) {
            char c = cmd.charAt(i);
            System.out.println(i + ": '" + c + "' (ASCII: " + (int)c + ")");
        }
    }
}
