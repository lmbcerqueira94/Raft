
package Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Log {
    
    File file;
    FileWriter writer;
    private int logIndex;

    
    
    public Log(String filename) throws IOException{
        this.file = new File(filename);
        this.file.setReadable(true);
        this.writer = new FileWriter(this.file, true); 
        //this.reader= new FileReader(file); 
        
        if(!this.file.exists()){
            this.file.createNewFile();
            this.logIndex = 1;
        }
        else{
            FileReader reader = new FileReader(this.file);
            BufferedReader input = new BufferedReader(reader);
         
            String last = null, line;
                
            while ((line = input.readLine()) != null) 
                last = line;
            
            if(last!=null){
                String parts[] = last.split("@");
                this.logIndex = Integer.parseInt(parts[0]) + 1;
            }
            else
                this.logIndex = 1;           
            
        }   
    }
    
    public void writeLog(int term, String command) throws IOException{
        
        String entry = Integer.toString(this.logIndex) + "@" + Integer.toString(term) + "@" + command + "\n";

        // Writes the content to the file
        this.writer.write(entry); 
        this.writer.flush();
        //this.writer.close();
        
        this.logIndex ++;
    }
    
    public int getTermfromLOG() throws IOException{
        
        FileReader reader = new FileReader(this.file);
        BufferedReader input = new BufferedReader(reader);
        
        String last = null, line;
        
        int term;
        
        
        while ((line = input.readLine()) != null) {
            last = line;
            //System.out.println("LOG:"+last);
        }
        if(last!=null){
            String parts[] = last.split("@");
            term= Integer.parseInt(parts[1]);
            //System.out.println("HERE");
            return term;
            
        }
        else
            return 1;      
        
        
     } 
    
       
}
    
    

/*
String source = args[0];
          String target = args[1];

          File sourceFile=new File(source);

          Scanner content=new Scanner(sourceFile);
          PrintWriter pwriter =new PrintWriter(target);

          while(content.hasNextLine())
          {
             String s=content.nextLine();
             StringBuffer buffer = new StringBuffer(s);
             buffer=buffer.reverse();
             String rs=buffer.toString();
             pwriter.println(rs);
          }
          content.close();    
          pwriter.close();
          System.out.println("File is copied successful!");
          }

          catch(Exception e){
              System.out.println("Something went wrong");
          }
       }


*/