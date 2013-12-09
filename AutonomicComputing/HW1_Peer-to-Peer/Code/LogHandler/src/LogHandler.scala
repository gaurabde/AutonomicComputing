import scala.actors.{OutputChannel, AbstractActor, Actor}, Actor._
import java.util.HashMap
import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}
import scala.util.Random
import java.io.IOException
import java.io._
import scala.xml._

class LogHandler extends Actor{
  
	var nodeRef = new mutable.ArrayBuffer[Actor]()
	var boss:Actor=null
	
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
	
	
	def prepBossLog(state:Int,key:String,cId:Int,nId:Int,desp:String):Elem={
		 if(state==0){
		 var s:Elem= <Flow Id={key}><PD><ID>{cId}</ID><Status>{nodeRef(cId).getState}</Status></PD><DD><ID>{nId}</ID><Status>{nodeRef(nId).getState}</Status></DD><Desp>{desp}</Desp></Flow>  
		 return s
		 }else{
		     var s:Elem= <Error Id={key}><Sid>{cId}</Sid><Did>{nId}</Did><Desp>{desp}</Desp></Error>
		     return s
		 }     
	}
	
	def prepNodeLog(state:Int,key:String,cId:Int,nId:Int,desp:String):Elem={
		 if(state==0){
		 var s:Elem= <Flow Id={key}><Sid>{cId}</Sid><Did>{nId}</Did><Desp>{desp}</Desp></Flow>  
		 return s
		 }else{
		   if(state==1){
			   var s:Elem= <Start Id={key}><Sid>{cId}</Sid><Did>{nId}</Did><Desp>{desp}</Desp></Start>
			   return s
		   }else{
		     var s:Elem= <Error Id={key}><Sid>{cId}</Sid><Did>{nId}</Did><Desp>{desp}</Desp></Error>
		     return s
		   }
		 }     
	}
	
	var count=0
	def printBossLog(status:Int,key:String){
	  var doc = scala.xml.XML.loadFile("./BossLog/"+boss+".xml")
	  if(status==1){
	    doc=scala.xml.XML.loadFile("./BossLog/"+boss+"_error.xml")
		  println("Error Log for Boss \n------")
		  (doc \"Error").foreach { err=>
	    	if((err \ "@Id").toString()==key){
	    		println("Process Key: "+(err \ "@Id").text)
			  	println("Source  Process ID: "+(err \\ "Sid").text)
				println("Next Process ID: "+(err \\ "Did").text)
				println((err \\ "Desp").text+"\n-----------")
	    	}		
		  }
	  }else{
		  println("Boss Log Directory \n===============")
		  println("Process Flow details with Key: "+key)
		  (doc \"Flow").foreach { flow =>
		    	if((flow \ "@Id").toString()==key){
				  //println("Process Key: "+(flow \ "@Id").text + "\n")
		    	  if(count==0){
		    	    println("Root Process ID:"+(flow \\ "PD"\"ID").text)
		    	    println("Message Passed: "+(flow \\ "Desp").text+"\n-----------")
		    	    count+=1
		    	  }else{
		    	    println("Source Process ID: "+(flow \\ "PD"\"ID").text)
					println("Destination Process ID: "+(flow \\ "DD"\"ID").text)
					println("Message Passed: "+(flow \\ "Desp").text+"\n-----------")
		    	  }
						
				 }
			} 
	  }
	  
	  count=0
	    
	}
	
	def printNodeLog(status:Int,key:String,id:Int,ref:Actor){
	  var doc=scala.xml.XML.loadFile("./Logs/"+ref.toString()+".xml")
	  if(status==1){
		  doc=scala.xml.XML.loadFile("./Logs/"+ref.toString()+"_error.xml")
		  println("Error Log for Node: "+id+" Ref: "+ref)
		  (doc \"Error").foreach { err=>
	    	if((err \ "@Id").toString()==key){
	    		println("Process Key: "+(err \ "@Id").text)
			  	println("Source  Process ID: "+(err \\ "Sid").text)
				println("Next Process ID: "+(err \\ "Did").text)
				println((err \\ "Desp").text+"\n-----------")
	    	}		
		  }
	  }else{
		  println("Node Logs of ID: "+id+" Ref: "+ref+" \n===============")
		  //println("Change details with Key: "+key)
		  (doc \"Flow").foreach { flow =>
	    		println("Process Key: "+(flow \ "@Id").text)
			  	println("Source  Process ID: "+(flow \\ "Sid").text)
				println("Next Process ID: "+(flow \\ "Did").text)
				println((flow \\ "Desp").text+"\n-----------")
	    			
		  }
		}
	 }
	
	
	def addToLog(newNode: Node,root:Node):Node = root match {
       //match all the node from your model
       // and make a new one, appending old nodes and the new one
        case <Log>{oldNodes@_*}</Log> => 
          var log = <Log>{oldNodes}{newNode}</Log>
          return log
    }
	def addToError(newNode: Node,root:Node):Node = root match {
       //match all the node from your model
       // and make a new one, appending old nodes and the new one
        case <Issue>{oldNodes@_*}</Issue> => 
          var log = <Issue>{oldNodes}{newNode}</Issue>
          return log
    }
	
	def act={
	  loop{
	    react{
	      case Data(node:ArrayBuffer[Actor],ref:Actor)=>
	        nodeRef=node
	        boss=ref
	        //println("Data Section")
	      case BossLog(key:String,id:Int,nextId:Int,msg:String,ref:Actor)=>
	        //println("Reached Boss LOgger")
	        	var s=prepBossLog(0,key,id,nextId,id.toString())
		        var log=addToLog(s,scala.xml.XML.loadFile("./BossLog/"+boss.toString()+".xml"))
		        writeToFile("./BossLog/"+boss.toString()+".xml",log.toString())
	      case BossError(key:String,id:Int,nextId:Int,msg:String,ref:Actor)=>
	        //println("Reached Boss Error Logger")	
	        
	        var s=prepBossLog(1,key,id,nextId,msg)
		        var log=addToError(s,scala.xml.XML.loadFile("./BossLog/"+boss.toString()+"_error.xml"))
		        writeToFile("./BossLog/"+boss.toString()+"_error.xml",log.toString())
	      
	      case PrintBossLog(id:Int,key:String)=>
	        	printBossLog(id,key)
	      
	      case NodeLog(status:Int,key:String,id:Int,nextId:Int,msg:String,ref:Actor)=>
	        //println("Node Logger")
	        var s=prepNodeLog(status,key,id,nextId,msg)
	        var log=addToLog(s,scala.xml.XML.loadFile("./Logs/"+ref.toString()+".xml"))
	        writeToFile("./Logs/"+ref.toString()+".xml",log.toString())
	      
	      case NodeError(key:String,id:Int,nextId:Int,msg:String,ref:Actor)=>
	       // println("Node Error Logger")
	        var s=prepNodeLog(2,key,id,nextId,msg)
	        var log=addToError(s,scala.xml.XML.loadFile("./Logs/"+ref.toString()+"_error.xml"))
	        writeToFile("./Logs/"+ref.toString()+"_error.xml",log.toString())
	        printNodeLog(1,key,id,ref)
	        println("Message History\n------------")
	        printNodeLog(0,key,id,ref)
	      case PrintNodeLog(status:Int,key:String,id:Int,ref:Actor)=>
	        printNodeLog(status,key,id,ref)
	      case _=>println("Logger Default case")
	    }
	  }
	}
	
}
