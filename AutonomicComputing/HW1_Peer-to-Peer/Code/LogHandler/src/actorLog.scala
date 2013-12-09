/*Author: Gaurab Dey
 * Desc: Contain the details of the independent Node Actors simulating sharing data for any
 * 		specific process flow. 
 * 
 */
import scala.actors.{OutputChannel, AbstractActor, Actor}, Actor._
import scala.actors.remote.RemoteActor, RemoteActor._
import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}
import scala.util.Random
import java.io.IOException
import java.io._
import scala.xml._


class actorLog(id:Int,bossRef:Actor,size:Int) extends Actor {
  val boss=bossRef
  var rand=new util.Random
  val writer = new FileWriter(new File("./Logs/"+this+".xml" ),true)
  writeToFile("./Logs/"+this+".xml","<Log></Log>")
  val errorLog=new FileWriter(new File("./Logs/"+this.toString()+"_error.xml" ),true)
  writeToFile("./Logs/"+this+"_error.xml","<Issue></Issue>")
  var changeMsg=id.toString()
  
  var logger=new LogHandler()
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
        case StartNode(key:String)=>
          println("Starting: "+this)
          var nextId=rand.nextInt(size)
          while(nextId==id){
            nextId=rand.nextInt(size)
          }
          
          logger!NodeLog(1,key,id,nextId,"Start Message: "+changeMsg,this)
          
          boss!BossMessage(key,id,nextId,changeMsg)
          
        case NodeMessage(key:String,sourceID:Int,msg:String)=> 
         println("Message Changed in Node: "+id)
          
          var nextId=rand.nextInt(size)
          while(nextId==id){
            nextId=rand.nextInt(size)
          }
         
         logger!NodeLog(0,key,id,nextId,"Message Changes-- old: "+changeMsg+" new: "+msg,this)
       
          changeMsg=msg
          
          boss!BossMessage(key,id,nextId,changeMsg)
        case PrintLog(status:Int,key:String)=>
          //printLog(status,key)
          logger!PrintNodeLog(status,key,id,this)
        
        case Stop(key:String)=>
          logger!NodeError(key,id,0,"Current Message Status-- "+changeMsg,this)
          exit
          
        case _=> println("Default Case in Nodes.scala")
      }
    }
  }

}