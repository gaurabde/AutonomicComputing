/**
 *
 */
/**
 * @author gaurab
 *
 */
import scala.actors.Actor
import scala.actors.Actor._
import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}
import scala.util.Random
import scala.util.control.Breaks._
import java.io._
import scala.xml._
import scala.sys.process._
import java.util._

case class P2PData(Id:Int,sum:Double,weight:Double)
case class NeighborList(list:Array[P2PActor])
case class NeighborListMsg(list:Array[P2PActor],in:P2PActor)
case class NeighborUpdateMsg(list:Array[P2PActor],in:Int)
case class TransComplete(act:Int)
case class P2PMessage(data:Int)
case class ReCalDistance
case class Restart
case class Start
case class NextCycle
//case class Total


//Generic Node Structure 
class P2PActor(num:Int,bossID:Actor,xx:Double,yy:Double,neighCount:Int) extends Actor{

  val id=num
  var x:Double=xx
  var y:Double=yy
  var neighborList= new Array[P2PActor](neighCount)
  var distanceList=new Array[Double](neighCount)
  private var couter=5
  private var rand= new Random
  private val msgFlag=false
  private var counter=0
  
  //Sending Message to Neighbor Nodes
  def SendMessage(){
	//for(i<-0 until neighCount){
    	var r=rand.nextInt(neighCount)
    	neighborList(r)!NeighborListMsg(neighborList,this)
    //}
        
  }
  
  //Comparison of Neighbor list after receiving update from Neighbor Nodes
  def ListComparision(newList:Array[P2PActor]){
	  breakable 
	  {for(i<-0 until neighCount){
	    var newDis=Math.sqrt((Math.pow((x-newList(i).x), 2.0))+(Math.pow((y-newList(i).y), 2.0)))
	    for(j<-0 until neighCount){
	      if(distanceList(j)>newDis){
	        neighborList(j)=newList(j)
	        distanceList(j)=newDis
	     //   i=i+1
	        break
	      }
	    }
	  }
	  //println("ListUpdated Node: "+id)
	  }
  }
  
  //Distance Table creation for easy comparison
  def DistanceListCal(){
    for(i<-0 until neighCount){
      try{
        distanceList(i)=Math.sqrt((Math.pow((x-neighborList(i).x), 2.0))+(Math.pow((y-neighborList(i).y), 2.0)))
      }catch{
        case e=>println("Exception in Distance Cal. ID: "+id +"\t"+ e.printStackTrace())
      }
    }
    println("Neighbor DisCal done- ID: "+id)
  }
  
  def act()={
	    loop{
	      receive{
	        case Start=>
	          SendMessage()
	          //println("Triggered Node: "+id)
          
	        case NeighborListMsg(list:Array[P2PActor],senderRef:P2PActor)=>
	          ListComparision(list)
	          Thread.sleep(400)
	          senderRef!NeighborUpdateMsg(neighborList,id)
	        
	        case NeighborUpdateMsg(updateList:Array[P2PActor],sendID:Int)=>
	           ListComparision(updateList) 
	           bossID!TransComplete(id)
	          
	        case NeighborList(list:Array[P2PActor])=>
	      	  neighborList=list
	          //Thread.sleep(600)
	          //println("NeighborListRecieved: "+id)
	          DistanceListCal
	          //Thread.sleep(600)
	          
	        case ReCalDistance=>
	          DistanceListCal
	        
	        case NextCycle=>
	          counter=0
	          SendMessage()
	        case _=>
	    }
	    }
	}
  
	
	  
}

//Main Boss Controller nodes managing the Nodes
class PeerToPeerSimulator(taskType:Int,num:Int,noOfNegih:Int,iteration:Int,radius:Array[Int]) extends Actor{
  
   var rand=new util.Random
   private val p2pActorList = new ArrayBuffer[P2PActor]()
   private var checker=0
   private val cycle=20
   private var cycleChecker=0
   //Use--> var r=(rand.nextInt(pastryNodes.length))
  
   //P2PActor Creator
   def P2PActorCreator(){
     var x:Double=0
	 var y:Double=0
	 var angle:Double=0.0
	 var angle_stepSize:Double=6.0/num.toDouble
	 
     for(i<-0 until num){
       x=(radius(0).toDouble*Math.cos(angle))//.toInt
	   y=(radius(0).toDouble*Math.sin(angle))//.toInt
	   //println("Round :"+check+"\t x:"+x+"\t y:"+y)
	   angle =angle+ angle_stepSize;
       val p2p=new P2PActor(i,this,x,y,noOfNegih)
       p2p.start
       p2pActorList+=p2p
       println("X: "+x+" Y: "+y)
      }
     
      NeighborAllocation(noOfNegih)
   }
   
   //Random Neighbor Allocation for each node
   def NeighborAllocation(n:Int){
     
     for(i<-0 until num){
       var neighborList=new Array[P2PActor](n)
       for(j<-0 until n){
         neighborList(j)=p2pActorList(rand.nextInt(num))
       }
       p2pActorList(i)!NeighborList(neighborList)
     }
     println("Neighbor Allocation Complete (5 each)")
     //var r=rand.nextInt(num)
     //Thread.sleep(4000)
     if(taskType==1){
       TriggerNodes()
     }else{
       StatusUpdate
       for(k<-1 until radius.length){ 
    	   NewCoordinateAllocation(k)
    	   StatusUpdate
       }
       
     }
   }
   
   //Starting each node to start sharing once the Neighbor list is updated in all nodes
   def TriggerNodes(){
     for(i<-0 until num){
       p2pActorList(i)!Start
     }
   }
   
   //NextCyle configuration and changes in Node
   def NextCycleTrigger(){
     for(i<-0 until num){
       p2pActorList(i)!NextCycle
     }
   }
   
   //Status upate of each node (Graph Image generation)
   def StatusUpdate(){
     if(taskType==1){
	     val writer = new FileWriter(new File("./DotFiles/"+this+".dot" ),true)
	     writeToFile("./DotFiles/"+this+".dot","digraph Connection{ ");//size=\""+radius(cycleChecker)+","+radius(cycleChecker)+"\";\n")
	     for(i<-0 until num){
	       println("======================")
	       var m=i.toString+"->"
	       println("Status of Node: "+p2pActorList(i).id+" connected Nodes: ")
	       for(j<-0 until 5){
	         print(p2pActorList(i).neighborList(j).id+" , ")
	         appendToFile("./DotFiles/"+this+".dot", m+p2pActorList(i).neighborList(j).id.toString+";")
	       }
	       println("\n=======================")
	       
	     }
	     var s=taskType.toString()+"_N"+num.toString+"_k"+noOfNegih.toString+"_"+cycleChecker.toString+".png"
	     appendToFile("./DotFiles/"+this+".dot", "}")
	     //"+num.toString+"_k"+noOfNegih.toString+"
	    
	     println( Seq("circo", "-Tpng", "./DotFiles/"+this.toString()+".dot", "-o "+s.toString()).!! )
	     Thread.sleep(5000)
	     //println(Seq("xdg-open", s.toString()).!! )
	     
	     NeighborLog(cycle)
	     cycleChecker=cycleChecker+5
	     if(cycleChecker>cycle){
	       DistanceLog(cycle)
	       System.exit(0)
	     }else{
	       
	       this!NextCycle
	     }
     }else{
       
     } 
   }
   
   //Distance log creation of each cycle
   def DistanceLog(cycleID:Int){
     var s=taskType.toString()+"_N"+num.toString+"_k"+noOfNegih.toString+".txt"
     val writer = new FileWriter(new File("./NeighborListFiles/"+s.toString()),true)
	 writeToFile("./NeighborListFiles/"+s.toString(),"Details of Sum-Distance of each Node (Cycle: "+(cycleID-5)+")\n============================")
	 for(i<-0 until num){
	   var distance:Double=0.0
	   for(j<-0 until p2pActorList(i).distanceList.length){
	     distance=distance+p2pActorList(i).distanceList(j)
	   }
	   appendToFile("./NeighborListFiles/"+s.toString(),"\n Node ID: "+p2pActorList(i).id+"\t Sum-Distance: "+distance.toString)
	 }  
     writer.close()
   }
   
   //Neighbor details log creation in each cycle
   def NeighborLog(cycleID:Int){
     var s=taskType.toString()+"_N"+num.toString+"_k"+noOfNegih.toString+"_"+(cycleID)+".txt"
     val writer = new FileWriter(new File("./NeighborListFiles/"+s.toString()),true)
	 writeToFile("./NeighborListFiles/"+s.toString(),"Details of Neighbors of each Node \n============================")
	 for(i<-0 until num){
	   var distance:String=""
	   for(j<-0 until p2pActorList(i).neighborList.length){
	     distance=distance+p2pActorList(i).neighborList(j).id+" , "
	   }
	   appendToFile("./NeighborListFiles/"+s.toString(),"\n Node ID: "+p2pActorList(i).id+"\t Neighbors: "+distance.toString)
	 }
     writer.close()
   }
   
   def NewCoordinateAllocation(n:Int){
     var newX:Double=0
	 var newY:Double=0
	 var angle:Double=0.0
	 var angle_stepSize:Double=6.0/num.toDouble
	 
     for(i<-0 until num){
       newX=(radius(n).toDouble*Math.cos(angle))//.toInt
	   newY=(radius(n).toDouble*Math.sin(angle))//.toInt
	   //println("Round :"+check+"\t x:"+x+"\t y:"+y)
	   angle =angle+ angle_stepSize;
       p2pActorList(i).x=newX
       p2pActorList(i).y=newY
       println("NEWX: "+newX+" NEWY: "+newY)
      }
     for(j<-0 until num){
       p2pActorList(j)!ReCalDistance
     }
    
   }
   
   
   
   def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B =
	try { f(param) } finally { param.close() }
	
   def writeToFile(fileName:String, data:String) =
	  using (new FileWriter(fileName)) {
		fileWriter => fileWriter.write(data)
	  }
   def appendToFile(fileName:String, textData:String) =
	  using (new FileWriter(fileName, true)){
		fileWriter => using (new PrintWriter(fileWriter)) {
		  printWriter => printWriter.println(textData)
		}
	  }
   
   def act()={
    loop{
      receive{
        case Start=>
          println("Reached Main Class")
          P2PActorCreator()
        
        case TransComplete(act:Int)=>
          if(checker<num-1){
            checker=checker+1
            println("checker counter : "+checker)
          }else{
            StatusUpdate
          }
        case NextCycle=>
          NextCycleTrigger
        
        case _ => 
          println("Provide correct paramerts: P2PSimulator")
          System.exit(1)
      }
      
    }
  }
  
  
}

object hw1 {
def main(args: Array[String]): Unit = {
    try{
      clearDir("./DotFiles/")
      
      println("Gossip Simulator Started")
      println("========================")
    
      
      var task=args(0).toInt
      var num=args(1).toInt
      var neighbor=args(2).toInt
      var iteration=args(3).toInt
      var radiusList=new Array[Int](iteration)
      var checkPoint=4
      for(i<-0 until iteration){
        radiusList(i)=args(checkPoint).toInt
        checkPoint=checkPoint+1
      }
      val g=new PeerToPeerSimulator(task,num,neighbor,iteration,radiusList)
      g.start
      g!Start
           
      println("Main Triggered")
    } catch{
      case e=> 
        println("Enter Valid parametes :"+e.getStackTrace)
        System.exit(1)
    }
    
    def clearDir(dfile : String) : Unit = { 
			  for {
				  files <- Option(new File(dfile).listFiles)
				  file <- files if file.getName.endsWith(".dot")
			  } file.delete()
	} 
   
  }

}