//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//
//class monitorQuit extends Thread {
//    @Override
//    public void run() {
//        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(System.in));
//        String st = null;
//        while(true){
//            try{
//                st = inFromClient.readLine();
//            } catch (IOException e) {
//            }
//            if(st.equalsIgnoreCase("exit")){
//                System.out.println("Exiting the server");
//                System.exit(0);
//            }
//        }
//    }
//}