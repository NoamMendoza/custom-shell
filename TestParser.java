import com.codecrafters.shell.parser.CommandParser;
import java.util.List;
import java.io.*;

public class TestParser {
    public static void main(String[] args) throws Exception {
        CommandParser parser = new CommandParser();
        PrintWriter out = new PrintWriter(new FileWriter("test_output.txt"));
        
        // Test 3: comillas dobles con comillas simples escapadas
        String test3 = "\"exe with \\'single quotes\\'\" /tmp/dog/f3";
        List<String> result3 = parser.parseArguments(test3);
        out.println("Test 3: " + test3);
        out.println("Resultado: " + result3);
        out.println("Primer argumento: '" + result3.get(0) + "'");
        out.println("Longitud: " + result3.get(0).length());
        
        out.close();
        System.out.println("Output written to test_output.txt");
    }
}
