
import scala.actors.{OutputChannel, AbstractActor, Actor}, Actor._
import scala.collection.mutable.ArrayBuffer
import scala.collection.{immutable, mutable}

case class Start
case class StartNode(key:String)
case class Stop(key:String)
case class Error(id:String)
case class DeadNode(id:String)
case class StartMessage(msg:String)
case class BossMessage(key:String,id:Int,nextId:Int,msg:String)
case class NodeMessage(Key:String,sourceID:Int,msg:String)
case class Choice
case class PrintLog(id:Int,key:String)
case class Data(node:ArrayBuffer[Actor],ref:Actor)
case class BossLog(key:String,id:Int,nextId:Int,msg:String,ref:Actor)
case class NodeLog(status:Int,key:String,id:Int,nextId:Int,msg:String,ref:Actor)
case class BossError(key:String,id:Int,nextId:Int,msg:String,ref:Actor)
case class NodeError(key:String,id:Int,nextId:Int,msg:String,ref:Actor)
case class PrintBossLog(status:Int,key:String)
case class PrintNodeLog(status:Int,key:String,id:Int,ref:Actor)