/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Node;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author joaqu
 */
class DataProcessing {
    private final long timeOutFollower;
    private final long timeOutCandidate;
    private final ConcurrentLinkedQueue<Pair> queue;

    public DataProcessing(long timeOutFollower, long timeOutCandidate, ConcurrentLinkedQueue<Pair> queue) {
        this.timeOutFollower = timeOutFollower;
        this.timeOutCandidate = timeOutCandidate;
        this.queue = queue;
    }

    
  
    
    public boolean checkHeartBeats(long timeStart){
        String message="HELLO";
        
        while(this.queue.isEmpty()||this.queue.peek().getTime()<timeStart){
            long x=System.currentTimeMillis()-timeStart;
            if(x>timeOutFollower)
                return false;
            System.out.println("FIFO VAZIA, timeout="+timeOutFollower+"  cuurentTime-timestart="+x);
        }
        
        do{
            while(this.queue.peek().getTime()<timeStart)
                this.queue.poll();
            System.out.println("FIFO COM VALORES ERRADOS");
        }while(!this.queue.isEmpty());
        
        
        
        
        while(!this.queue.isEmpty()){
           if(this.queue.poll().getMessage().contains(message)){
               System.out.println("TINHA UM HELLO");
               return true; 
            }
        }
        return false;
        
        
//        if(!this.queue.isEmpty()){
//            while(this.queue.peek().getTime()<timeStart){
//
//                this.queue.poll();
//                
//                if(this.queue.isEmpty())
//                    break;
//            }
//        }
//        
//        //{
//            do{
//                if(this.queue.isEmpty()){
//                    if(System.currentTimeMillis()-timeStart>timeOutFollower)
//                        return false;
//                    else
//                        continue;
//                }
//                else if(this.queue.poll().getMessage().contains(message)){
//                    
//                    return true;
//                }
//            }while(this.queue.peek().getTime()-timeStart<timeOutFollower);
        //}

    }
    
        }
    

