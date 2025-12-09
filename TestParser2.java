import com.codecrafters.shell.parser.CommandParser;
import java.util.List;
import java.io.*;

public class TestParser2 {
    public static void main(String[] args) throws Exception {
        CommandParser parser = new CommandParser();
        PrintWriter out = new PrintWriter(new FileWriter("test_output2.txt"));
        
        // Probar si el archivo se llama con backslash literal
        String test1 = "\"exe with \\'single quotes\\'\"";
        List<String> result1 = parser.parseArguments(test1);
        out.println("Con escape: '" + result1.get(0) + "'");
        out.println("Longitud: " + result1.get(0).length());
        
        // Probar con backslash literal (doble backslash)
        String test2 = "\"exe with \\\\'single quotes\\\\'\"";
        List<String> result2 = parser.parseArguments(test2);
        out.println("Con backslash literal: '" + result2.get(0) + "'");
        out.println("Longitud: " + result2.get(0).length());
        
        out.close();
        System.out.println("Done");
    }
}
