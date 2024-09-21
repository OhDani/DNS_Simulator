import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * Class monitorQuit listens to the input stream and waits for a user to send the
 * exit signal by typing "exit" (not case sensitive).
 */
class monitorQuit extends Thread {
    @Override
    public void run() {
        //Read each new line of input.
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(System.in));
        String st = null;
        while(true){
            try{
                st = inFromClient.readLine();
            } catch (IOException e) {
            }
            if(st.equalsIgnoreCase("exit")){
                //Close Input stream and cleanly exit the Server Program.
                System.out.println("Exiting the server");
                System.exit(0);
            }
        }
    }
}