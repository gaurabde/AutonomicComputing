/*
 * Author: Gaurab Dey
 * Details: This code is simulated version of Gossip and Push-Sum algorithm
 * 			also include the exception handling as the bonus section
 */

//package project2


import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}
import scala.util.Random
import scala.util.control.Breaks._

// case classes for immutable messages
case class GossipData(Id:Int,sum:Double,weight:Double)
case class DoneGossip(act:Int)
case class GossipMessage(data:Int)
case class Reset
case class Total


// actor definition
class GossipActor(num:Int, total:Int, topo:String, algo:String, counter:ArrayBuffer[Int]) extends Actor {
  var Count = 0
  var checker=0
  var threshold:Double=0
  var messageLoop=true
  var boss:Actor=null
  //Variable for Convergency
  var algoChecker=0
  var s:Double=num
  var w:Double=0
  // To avoid error with ID ZERO "0""
  if(s==0) s=1
  def trancateAt(n:Double, p:Int):Double={
      val s=math pow(10,p);(math floor n*s)/s
    }
    algo match{
      case "gossip"=> threshold=10
      case "push-sum"=> {
          threshold=trancateAt(1,8)
          algoChecker=1
        }
    }
    
    def act = {
    try{ 
      loop {
        react {
          case GossipData(n,sum,weight) => 
            Count+=1
            topo match{
  //Condition for FULL Topology with GOSSIP Alogrithm       
              case "full" => 
                //println("In Actor Line")
                if(algoChecker==0){
                  if(Count<threshold){
                   val newRandom=Random.nextInt(total)
                   sender ! GossipData(newRandom,1,1)
                  }else{
                    messageLoop=false
                   sender ! DoneGossip(num)
                  }
                }
//Condition for FULL Topology with PUSH-SUM Alogrithm                       
                else{
                    if(weight==0) w=w+4
                    else w=w+weight+4
                    if(s+sum>total) s=(s+sum)/2
                    else s=s+sum
                    
                    var ss=s/2
                    var ww=w/2
                    //var sw=math.round((ss/ww)*10000000)*0.000000001
                    var sw=trancateAt((s/w),8)
                   if((sw)>threshold){
                   //println(w)
                                       //println("Normal: "+sw+" Threshold:"+threshold+" "+((s/w)>threshold)+" ID: "+num)

                    val newRandom=Random.nextInt(total)
                    sender ! GossipData(newRandom,ss,ww)
                   }else{
                    messageLoop=false
                    //println("Threshold condition"+sw+" "+((sw)>threshold))
                    sender ! DoneGossip(num)
                  }
                }
  // Generic CASE for 2D Grid, Imperfect 2D Grid and Line Topology
              case _ => 
                if(algoChecker==0){ 
                  if(Count<20){
                    val newRandom=Random.nextInt(counter.length)
                    sender ! GossipData(counter(newRandom),1,1)
                   // println("Actor:"+num+" Random:"+counter(newRandom))
                  }else{
                   sender ! DoneGossip(num)
                  }
                }else{
                   if(weight==0) w=w+4
                    else w=w+weight+4
                    if(s+sum>total) s=(s+sum)/4
                    else s=s+sum
                   
                    var ss=s/2
                    var ww=w/2
                    //var sw=math.round((ss/ww)*10000000)*0.000000001
                    var sw=trancateAt((s/w),8)
                   if((sw)>threshold){
                      //println("Normal: "+sw+" Threshold:"+threshold+" "+((s/w)>threshold)+" ID: "+num)
                   val newRandom=Random.nextInt(counter.length)
                   sender ! GossipData(counter(newRandom),ss,ww)
                   }else{
                    //println("Threshold condition"+sw+" "+((sw)>threshold))
                    sender ! DoneGossip(num)
                  }
                }
            }
         }
      }
    }
    catch {
        case e:IllegalArgumentException => e.printStackTrace(); println("Enter Correct Paramerts")
      }
    }
  
}

class GossipSimulator(num:Int, topo:String, algo:String) extends Actor{
  private val gossipActorList=new ArrayBuffer[GossipActor]()
  private val gossipCount=new ArrayBuffer[Int]()
  private var actorCount=num
  private var breakPoint:Int=0
  private val random=new Random()
  private val b=System.currentTimeMillis
  var count=0
  var algoo=0
  var Counter=0
  val neighbor=new ArrayBuffer[Int]()
  
  
//building Actor count for termination condition
  algo match{
    case "gossip"=>algoo=1; println(" GOSSIP ALGORITHM selected")
    case "push-sum"=>algoo=0; println(" PUSH-SUM ALGORITHM selected")
    case _=> 
      println("Enter Correct Algorithm parameter (gossip,push-sum)")
      System.exit(1)
  }
  
//Gossip Node counter for termination analysis
  def GossipEndCounter(count:Int){
    for(i<-0 until count) gossipCount+=0
  }
  
  //println("Simulator: "+this)
//Squar Root Method
   def sqrt(a:Double) = {
    val acc = 1e-10
    def findroot( x:Double ):Double = {
    val nextx = (a/x + x)/2
    if( (x-nextx).abs < acc*x ) nextx else findroot(nextx)
    } 
    findroot( 1 )
  }
  
//GossipActor list created for FULL topology
  def GossipCreatorFull(){
     try{
       neighbor+=0 
      GossipEndCounter(num)
      for(i<-0 until num){
       val gosh=new GossipActor(i,num,topo,algo,neighbor)
       gosh.start
       gossipActorList+=gosh
       gossipCount(i)=0  
       
      }
     }
     catch {
        case e:IllegalArgumentException => e.printStackTrace(); 
      }
    }
  
  
   // Random GossipActor started for the Gossip Simulation
  def StartSimulate(){
   
      val r=Random.nextInt(num)
      gossipActorList(r)!GossipData(r,1,1)    
  }
  
// Line Topology Implementation
  def Line(){
    
    try{
      GossipEndCounter(actorCount)
      for(i<-0 until actorCount){
        var neighbor=new ArrayBuffer[Int]()
 //Neighbour selection according to Grid
            
         if(i-1>=0) neighbor+=i-1
         if(i+1<=actorCount-1) neighbor+=i+1

  //Gossip Actor Creation
        val gosh=new GossipActor(i,actorCount,topo,algo,neighbor)
        gosh.start
        gossipActorList+=gosh
        gossipCount(i)=0  
        
       }
        val r=Random.nextInt(actorCount)
        gossipActorList(r)!GossipData(r,1,1)
      }
      catch {
        case e:IllegalArgumentException => e.printStackTrace()
      }
  }
 
  def TwoDGrid(){
   
    try{
      if(sqrt(num)%1!=0){
        breakPoint=sqrt(num).toInt+1
        actorCount=breakPoint*breakPoint
      }else{
        breakPoint=sqrt(num).toInt
        actorCount=breakPoint*breakPoint
      }
      var Ulimit=0
      var Llimit=breakPoint-1
    
      GossipEndCounter(actorCount)
      for(i<-0 until actorCount){
        var neighbor=new ArrayBuffer[Int]()
    //Neighbour selection according to Grid
        
         if(i-1>=Ulimit) neighbor+=i-1
         if(i+1<=Llimit) neighbor+=i+1
         if(i-breakPoint>=0) neighbor+=(i-breakPoint) 
         if(i+breakPoint<actorCount) neighbor+=i+breakPoint
             
         
         //println("/n")
         if((i+1)%breakPoint==0&&i!=0){
             Ulimit+=breakPoint
             Llimit+=breakPoint
         }
    //Gossip Actor Creation
        val gosh=new GossipActor(i,actorCount,topo,algo,neighbor)
        gosh.start
        gossipActorList+=gosh
        gossipCount(i)=0  
        
      } 
      val r=Random.nextInt(actorCount)
      gossipActorList(r)!GossipData(r,1,1)
    }
      
    catch {
       case e:IllegalArgumentException => e.printStackTrace()
      }
  }

 
  def ImperfectTwoD(){
    try{
      if(sqrt(num)%1!=0){
        breakPoint=sqrt(num).toInt+1
        actorCount=breakPoint*breakPoint
      }else{
        breakPoint=sqrt(num).toInt
        actorCount=breakPoint*breakPoint
      }
      GossipEndCounter(actorCount)
      var Ulimit=0
      var Llimit=breakPoint-1
      
      for(i<-0 until actorCount){
        var neighbor=new ArrayBuffer[Int]()
  //Neighbour selection according to Grid
         if(i-1>=Ulimit) neighbor+=i-1
         if(i+1<=Llimit) neighbor+=i+1
         if(i-breakPoint>=0) neighbor+=(i-breakPoint) 
         if(i+breakPoint<actorCount) neighbor+=i+breakPoint
         //println("/n")
         if((i+1)%breakPoint==0&&i!=0){
             Ulimit+=breakPoint
             Llimit+=breakPoint
         }
     //One Random Actor selected as the 5th neighbor for Imperfect 2D Grid
         neighbor+=Random.nextInt(actorCount)

  //Gossip Actor Creation
        val gosh=new GossipActor(i,actorCount,topo,algo,neighbor)
        gosh.start
        gossipActorList+=gosh
        gossipCount(i)=0  
        for(i<-0 until neighbor.length){
           neighbor(i)=0
         }
      } 
      val r=Random.nextInt(actorCount)
      gossipActorList(r)!GossipData(r,1,1)
    }
      
    catch {
      case e:IllegalArgumentException => e.printStackTrace()
      }
      
  }
  
   def act()={
    loop{
      receive{
        case GossipData(i,sum, weight) => 
          //println("Random Actor Respose:"+i +" "+ this.mailboxSize)
          gossipActorList(i) ! GossipData(i,sum,weight)
          
        case DoneGossip(gossip) =>
          Counter+=1
          //println("Node No: "+ gossip +" reached threshold not transmitting message")
          gossipCount(gossip)=1
          var m=true
          if(algoo==1){
            if(Counter>=actorCount){
            for(i<-0 until actorCount){
              //println("Actor No:"+i+" Count Statu: "+gossipActorList(i).Count)
              if(gossipActorList(i).Count<20 && m){
                var random=Random.nextInt(actorCount)
                gossipActorList(random)!GossipData(random,1,1)
                //println("New Actor No:"+random +" selected Main")
                //println("-------------------------------------------------")
                m=false
              }
              if(gossipCount(i)==1) count+=1
             }
            }else{
              var random=Random.nextInt(actorCount)
              gossipActorList(random)!GossipData(random,1,1)
            }
            if(count>=actorCount){
              var hitCount=0
              for(i<-0 until actorCount) hitCount+=gossipActorList(i).Count
              println("-------------------------------------------------")    
              println("Average Hits on each Node: "+hitCount/actorCount) 
              println("-------------------------------------------------")
              println("Total time taken: "+(System.currentTimeMillis-b))
              System.exit(0)
            }else {
              count=0
             }
           }
  //Check Node status for PUSH-SUM Algorithm
          else{
             for(i<-0 until actorCount){
              //println("Actor No:"+i+" Count Statu: "+gossipActorList(i).Count)
              if(gossipCount(i)==0 && m) {
                var random=Random.nextInt(actorCount)
                gossipActorList(random)!GossipData(random,1,4)
                //println("New Actor No:"+random +" selected Main")
                //println("---------------------------------------")
                m=false
               }
               if(gossipCount(i)==1) count+=1
               //if(count>actorCount-10) println("No. of Node recieved data: "+count)
              }
              if(count>=actorCount){
                var hitCount=0
                for(i<-0 until actorCount) hitCount+=gossipActorList(i).Count
                println("-------------------------------------------------")   
                println("Average Hits on each Node: "+hitCount/actorCount)
                println("-------------------------------------------------")
                println("Total time taken: "+(System.currentTimeMillis-b))
                System.exit(0)
              }else {
                //println("Count: "+count)
                count=0
               }
               
            }
          
              
          
        case "full" =>
          println("FULL Network Topology selected")
          //println("-----------------------------")
          GossipCreatorFull
          StartSimulate
          //Thread.sleep(1000)
        case "2D" => 
          println("2D GRID Netowork Topology selected")
          //println("-----------------------------")
          TwoDGrid
          //Thread.sleep(1000)
        case "imp2D" => 
          println("IMPERFECT 2D GRID Network Topology selected")
          //println("-----------------------------")
          ImperfectTwoD
          //Thread.sleep(1000)
        case "line" => 
          println("LINE Network Topology selected")
          //println("-----------------------------")
          Line
          //Thread.sleep(1000)
        
        case _ => 
          println("Enter Correct Topology Parameter (full,line,2D,imp2D)")
          System.exit(1)
      }
      
    }
  }
  
  
}

object project01 {

  /**
   * @param args the command line arguments
   */
  def main(args: Array[String]): Unit = {
    try{
      println("Gossip Simulator Started")
      println("========================")
    
      var num=1000//args(0).toInt
      //println("valuse"+args(0).toInt)
      var topo="full"//args(1).toString
      var algo="gossip"//args(2).toString
      val g=new GossipSimulator(num,topo,algo)
      g.start
      g!topo
    }
    catch{
      case e=> 
        println("Enter Valid parametes :"+e.getStackTrace)
        System.exit(1)
    }
  }

}
