
package Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThreadLog extends Thread{
    
    private final ConcurrentLinkedQueue<Pair> queueLOG;    
    private final Log log;
    public final ComunicationUDP comModule;
    
    public ThreadLog (ConcurrentLinkedQueue<Pair> queueLog, Log log, ComunicationUDP comModule){
        this.queueLOG = queueLog;
        this.log = log;
        this.comModule = comModule;
    }
    
    public void run() {
        
        boolean checkIsOk = true;
        
        while(true){
            
            Pair tmp = this.queueLOG.poll();
            
            if(tmp == null)
                continue;
            
            else{
                
                int[] info = new int[2];
                
                try {
                    //get LastLogEntry 
                    info = this.log.getLogLastEntry();
                    int prevLogIndex = info[0];
                    int prevLogTerm = info[1];
                    
                    //get MsgContents
                    //Message Formate-> AppendEntry:term1:command1:term2:command2:term3:command3:....
                    String message = tmp.getMessage(); 
                    String newEntries[] = message.split(":"); 
                    int nNewEntries = (newEntries.length-1)/2; //newEntries.length-1 tem de dar sempre um n.o par
                    int[] newEntryTerms = new int[nNewEntries];
                    String[] newEntryCommands = new String[nNewEntries];
                    
                    int i, j=0; //começa em 1 para ignorar o newEntris[0]=AppendEntry
                    for(i=1; i<newEntries.length-1; i+=2){
                        newEntryTerms[j] = Integer.parseInt(newEntries[i]);
                        newEntryCommands[j] = newEntries[i+1];
                        j++;
                        System.out.println("new entry to be written: command- " + newEntryCommands[j-1] + "; term: " + newEntryTerms[j-1] );
                    }
                    
                    //Message PrevLogIndex and msgPrevLogTerm
                    int leadPrevLogIndex = tmp.getPrevLogIndex();
                    int leadPrevLogTerm = tmp.getPrevLogTerm();
                    int leadTerm = tmp.getTerm();
                    System.out.println("leadPrevLogTerm: " + leadPrevLogTerm + "leadPrevLogIndex: " + leadPrevLogIndex);
                    
                    //test conditions before writting on the log
                    
                    if (leadTerm < States.term){
                        //reply false 
                        InetAddress inet = tmp.getInet();
                        String msgToSend = "LEADNOTUPD@" + Integer.toString(States.term);
                        this.comModule.sendMessage(msgToSend, inet); //msg vai ser processada pelo lider juntamente com as msg "normais"
                        checkIsOk = false;
                    }
                    
                    //se o ficheiro não estiver vazio
                    BufferedReader br = new BufferedReader(new FileReader(this.log.file));     
                    if (br.readLine() != null) {
                        if(this.log.lookForTerm(leadPrevLogIndex) != leadPrevLogTerm){
                            System.out.println("logTerm: " + this.log.lookForTerm(leadPrevLogIndex) + "leadPrevLogTerm: " + leadPrevLogTerm);
                            //reply false
                            InetAddress inet = tmp.getInet();
                            String msgToSend = "ERROR_LOG@" + Integer.toString(States.term);
                            this.comModule.sendMessage(msgToSend, inet);  
                            System.out.println("Log Matching Property failed");
                            checkIsOk = false;
                        }

                        //test if a new entry conflicts with an existing one. Conflict = same index but different term
                        int termWithConflict = this.log.checkForConflicts(newEntryTerms, leadTerm); // verificar a partir do inicio do log, a melhorar
                        if(termWithConflict != 0){
                            //if yes, delete the existing entry and all the ones that follow it
                            this.log.deleteEntries(termWithConflict);
                            InetAddress inet = tmp.getInet();
                            String msgToSend = "ERROR_LOG@" + Integer.toString(States.term);
                            this.comModule.sendMessage(msgToSend, inet);  
                            System.out.println("Conflicts detected");
                            checkIsOk = false;
                        }                        
                    }
                    
                    //if no problem, write new entries on the log                   
                    if(checkIsOk){
                        this.log.writeLog(newEntryTerms,newEntryCommands);
                        System.out.println("wrire log");
                    }

                    //if leader commit > commitIndex , set commitIndex = min(kleaderCommit, index of last new entry
                    
                    
                } catch (IOException ex) {
                    Logger.getLogger(ThreadLog.class.getName()).log(Level.SEVERE, null, ex);
                }
                   
            }
        }
    }
}