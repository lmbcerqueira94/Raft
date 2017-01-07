package Node;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Leader {
    
    public final ComunicationUDP comModule;
    private final long timeout;
    private final DataProcessing dataProcessing;
    private final ConcurrentLinkedQueue<Pair> queue;
    private final Log log;
    public int prevLogIndex;
    public int prevLogTerm;
    
    public Leader(ConcurrentLinkedQueue<Pair> queue, Log log) throws IOException {
        this.comModule = new ComunicationUDP();
        this.timeout = this.getHeartBeat();
        this.queue = queue;
        this.dataProcessing = new DataProcessing(this.queue);
        this.log = log;
        
    }
    
    private final int getHeartBeat(){
                         
        int min_value = 1500;
        int max_value = 1600;
        
        return (min_value + (int)(Math.random() * ((max_value - min_value) + 1)))/100;
    }
    
    public int cycle(int term) throws IOException {
        
        int[] info = new int[2];
        info = this.log.getLogLastEntry();
        this.prevLogIndex = info[0];
        this.prevLogTerm = info[1];
        
        // creating timer task and schedule
        Timer timer = new Timer();
        timer.schedule(new sendHeartBeatTimer(term, this.log, this.prevLogIndex, this.prevLogTerm),100, 10000);  //heartbeatfreq >>>>>>> timeoutsfollowers
       
        int newTerm = checkIncomingLeaderMsg(term); //retorna qd tiver de mudar para FOLLOWER
        
        System.out.println("LEADER : Vou sair do Leadercycle");
        timer.cancel();
        return newTerm;
    }
    
    
    public int checkIncomingLeaderMsg(int term) throws IOException{
        
        int receivedTerm;
        String pair;
        String message;
        
        while(true){
            Pair tmp = this.queue.poll();
            
            if (tmp == null)
                continue;
            else{
                receivedTerm = tmp.getTerm();
                
                if(receivedTerm > term)
                    break;   
                
                InetAddress address = tmp.getInet();
                message = tmp.getMessage();
                
                if(message.contains("ERROR_LOG")){                    
                    //decrementar a variavel writtenIndex desse follower
                    String sourceMsgIP = tmp.getInet().getHostAddress();
                    int id = Integer.parseInt(sourceMsgIP.substring(sourceMsgIP.length() - 1));
                    States.writtenIndex[id-1] = States.writtenIndex[id-1] -1;
                    
                    //mandar entry do index anterior anterior
                    String log = this.log.getLogEntry(States.writtenIndex[id-1]);
                    String msgLog = "CHECK_PREVIOSENTRY:" + log + "@" + Integer.toString(term);
                    this.comModule.sendMessage(msgLog, address);
                    System.out.println("[LEADER] received error_log. message sent: " + msgLog );
                }
                
                else if (message.contains("ACK")){
                    //atualizar a variavel writtenIndex
                    String contents[] = message.split(":");
                    int lastIndexWritten = Integer.parseInt(contents[1]);
                    String sourceMsgIP = tmp.getInet().getHostAddress();
                    int id = Integer.parseInt(sourceMsgIP.substring(sourceMsgIP.length() - 1));
                    States.writtenIndex[id-1] = lastIndexWritten; //o valor do nó 1 fica na pos 0, do nó 2 na pos 1, ...
                    System.out.println("[LEADER] ACK recvd writtenIndex: " + States.writtenIndex[0] + ":" + 
                                                                            + States.writtenIndex[1] + ":" +   
                                                                            + States.writtenIndex[2] + ":" +
                                                                             + States.writtenIndex[3] + ":" +
                                                                              + States.writtenIndex[4]);    
                }

                else if (message.contains("COMMAND")){

                    int[] info = new int[2];
                    info = this.log.getLogLastEntry();
                    this.prevLogIndex = info[0];
                    this.prevLogTerm = info[1];
        
                    String new_parts[] = message.split(":");
                    String command = new_parts[1];
                    System.out.println("[LEADER] : Received command: " + command);
                    int[] termLog = new int[1]; 
                    termLog[0] = term;
                    String[] commandLog = new String[1];
                    commandLog[0] = command;
                    this.log.writeLog(termLog, commandLog);
                    
                    //send APPENDENTRY TO ALL FOLLOWERS                
                    String entry = "AppendEntry" + ":" + Integer.toString(term) //este term aqui parece useless mas faz sentido qd se manda varios comandos de termos dif
                            + ":" + command + "@" + Integer.toString(term) + "@" + Integer.toString(this.prevLogTerm) + "@" + Integer.toString(this.prevLogIndex) ;
                    System.out.println("Sent AppendEntry with prevLogIndex = " + this.prevLogIndex);
                    this.comModule.sendMessageBroadcast(entry);
                } 
                
                else if(message.contains("LEDNOTUPD")){
                    break;
                }
            }  
        }
        return receivedTerm;
    }   
    
    //TIMER
    class sendHeartBeatTimer extends TimerTask  {
        
        private int term;
        private Log log;
        private int prevLogIndex;
        private int prevLogTerm;
        
        sendHeartBeatTimer (int term, Log log,int prevLogIndex,int prevLogTerm){
            this.term = term;
            this.log = log;
            this.prevLogIndex=prevLogIndex;
            this.prevLogTerm=prevLogTerm;
            
        }

        @Override
        public void run() {
            
            
            
            String heartBeatString = "HELLO" + "@" + Integer.toString(this.term) + "@" + Integer.toString(this.prevLogTerm) + "@" + Integer.toString(this.prevLogIndex) ;
            //System.out.println("LEADER: send" + heartBeatString);
            try {
                Leader.this.comModule.sendMessageBroadcast(heartBeatString);
            } catch (IOException ex) {
                Logger.getLogger(Leader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
