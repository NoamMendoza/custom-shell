
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        
        while (true) { 
            System.out.print("$ ");
            
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String echoDetector [] = input.split(" ");

            if (input.equals("exit 0")) {
                System.exit(0);
            }
            if (echoDetector[0].equals("echo")) {
                String echoOutput = input.substring(5);
                System.out.println(echoOutput);
            }else{
                System.out.println(input + ": command not found");
            }
            

           
        }
        
    }
}
