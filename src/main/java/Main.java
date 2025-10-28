
import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        
        String [] commands = {"echo", "type", "exit"};
        while (true) { 
            System.out.print("$ ");
            
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String Detector [] = input.split(" ");

            if (input.equals("exit 0")) {
                System.exit(0);
            }
            if (Detector[0].equals("echo")) {
                String echoOutput = input.substring(5);
                System.out.println(echoOutput);
            }
            if (Detector[0].equals("type")) {
                for (String command : commands) {
                    if (Detector[1].equals(command)) {
                        System.out.println(Detector[1] + " is a shell builtin");
                    }else{
                        System.out.println(Detector[1] + ": command not found");
                    }
                }
            }else{
                System.out.println(input + ": command not found");
            }
            

           
        }
        
    }
}
