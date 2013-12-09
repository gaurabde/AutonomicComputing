object TestCode {
	def main(args: Array[String]): Unit = {
    try{
      var rand=new util.Random
      for(i<-0 until 1000){
        print(rand.nextInt(1000)+" , ")
        if(i/10==0.00){
          println()
        }
      }
      
    }catch{
      case e=>println(e.printStackTrace())
      }
    }
}