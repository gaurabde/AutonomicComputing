
import scala.actors.{OutputChannel, AbstractActor, Actor}, Actor._
import java.util.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}
import scala.util.Random
import java.io.IOException
import java.io._
import scala.xml._


object example {
  
	  
	def main(args :Array[String]) {
     try{
       
       clearDir("./Logs/")
       clearDir("./BossLog/")
       val node=new NodeManager(this,50)
       node.start()
       node!Start
     }catch{
       case e:Exception=>println("Error in Main"+e.printStackTrace())
       }
     }
	
	def clearDir(dfile : String) : Unit = { 
			  for {
				  files <- Option(new File(dfile).listFiles)
				  file <- files if file.getName.endsWith(".xml")
			  } file.delete()
	} 
     
}

class NodeManager(ref:Any,no:Int) extends Actor{
  var nodeRef = new mutable.ArrayBuffer[Actor]()
  var nodeChange=new mutable.ArrayBuffer[Int]()
  var keyTracker=new mutable.ArrayBuffer[String]()
  var rand=new util.Random()
  val writer = new FileWriter(new File("./BossLog/"+this.toString()+".xml" ),true)
  writeToFile("./BossLog/"+this+".xml","<Log></Log>")
  val errorLog=new FileWriter(new File("./BossLog/"+this.toString()+"_error.xml" ),true)
  writeToFile("./BossLog/"+this+"_error.xml","<Issue></Issue>")
 // println("Boss Id: "+this)
  private var keyList:HashMap[String,Int] = new HashMap[String,Int]()
  var checker=0
  
  val logger=new LogHandler()
  logger.start()
  
  def using[A <: {def close(): Unit}, B](param: A)(f: A => B): B =
	try { f(param) } finally { param.close() }
 
	def writeToFile(fileName:String, data:String) =
	  using (new FileWriter(fileName)) {
		fileWriter => fileWriter.write(data)
	  }
 
	
	def act={
	  loop{
	    react{
	      case BossMessage(key:String,id:Int,nextId:Int,msg:String)=> 
	        //println("Reached Boss Message Case")
	        nodeChange+=nextId
	        keyTracker+=key
	        logger!BossLog(key,id,nextId,msg,this)
	        
	        keyList.put(key,keyList.get(key)+1)
	        if(keyList.get(key)==5){
	          checker+=1
	          println("Process flow completed (Key): "+key)
	          
	        }else{
	          nodeRef(nextId)!NodeMessage(key,id,msg)
	        }
	        if(checker==4){
	          this!Choice
	        }
	        
	      case Start=>
	        writeToFile("./BossLog/"+this+".xml","<Log> </Log>")
	        logger!Data(nodeRef,this)
	        //println("Reached NodeManger Start")
	        for(i<-0 until no){
	          var node=new actorLog(i,this,no)
	          node.start
	          //println("Node Created: "+node)
	          nodeRef+=node
	        }
	        for(i<-0 until 5){
		        var randNode=rand.nextInt(no)
		        nodeRef(randNode)!StartNode(nodeRef(randNode).toString())
		        keyList.put(nodeRef(randNode).toString(),0)
		        println("Message send to Node : "+nodeRef(randNode))
		        
		        logger!BossLog(nodeRef(randNode).toString(),randNode,randNode,randNode.toString(),this)
		        
	        
	        }
	        println("List of Nodes undergoing changes use if for reference \n---------------------------")
	        
	      case Choice=>
	      	while(true){
			   println("Distributed Debugging Simulation")
		       println("================================")
		       println("1. Process Flow log details ")
		       println("2. Error Simulation and log details")
		       println("3. Log of any Node")
		       println("4. Exit")
		       println("Enter your choice: ")
		       var m=readLine().toString()
		      m match{
		        case "1"=>
		          var cond=true
		          println("Process Key List:")
		          var keys = (keyList.keySet()).toArray()
		          for(i<-0 until keys.length) println((i+1)+". "+keys(i))
		          var m:Int=0
		          while(cond){
		        	  println("Enter Process Key ID (1-5): ")
		        	  m=readLine().toInt
		        	  if(m>0 && m<=5){
		        	    cond=false
		        	  }else println("Enter Corrent ID (1-5)")
		          }
		          println("========================")
		          println("Process Flow log details")
		          println("========================")
		          //printLog(1,keys(m-1).toString())
		          logger!PrintBossLog(0,keys(m-1).toString())
		          Thread.sleep(1000)
		          	
		        case "2"=>
		          println("========================")
		          println("Error Simulation and log details")
		          println("========================")
		          var randNode=rand.nextInt(nodeChange.length)
		          println("Termination Random Node ID: "+nodeChange(randNode)+" "+keyTracker(randNode))
		          println("Accessing Error Logs.")
		          for(i<-0 until 4){ print("."); Thread.sleep(1000);}
		          nodeRef(nodeChange(randNode))!Stop(keyTracker(randNode))
		          	logger!BossError(keyTracker(randNode),nodeChange(randNode),0,"Simulated Error",this)
		          	Thread.sleep(2000)
		          println("Status of the Process: "+nodeRef(nodeChange(randNode)).getState+"\n-------------")
		          	Thread.sleep(1000)
		          	
		        case "3"=>
		          var cond=true
		          var m:Int=0
		          while(cond){
		        	  println("Please Enter Id from the above changed Message List for better log details")
		        	  println("Enter Process ID (1-50): ")
		        	  m=readLine().toInt
		        	  if(m>0 && m<=50){
		        	    cond=false
		        	  }else println("Enter Corrent ID (1-5)")
		          }
		          println("Accessing Process ID Logs.")
		          for(i<-0 until 4){ print("."); Thread.sleep(1000);}
		          println("========================")
		          nodeRef(m)!PrintLog(0,"")
		          Thread.sleep(3000)
		          		          	
		        case "4"=>
		          println("Exit Debugger Simulation")
		          System.exit(0)
		        case _=>
		          println("Please enter proper Simulation code")
		      }
	    	}
	      case _=> println("Default case in NodeManger")
	    }
	  }
	}
  
}

