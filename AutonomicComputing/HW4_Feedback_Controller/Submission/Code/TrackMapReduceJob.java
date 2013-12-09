package usr.code;

import java.io.File;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
 

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.JobStatus;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapred.TaskReport;
import org.apache.hadoop.mapreduce.TaskID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;






public class TrackMapReduceJob {
	
	//default path for the xml file
	public static String xmlFilePath="/usr/local/hadoop/conf/fair-scheduler.xml";
	
	//XML editor for modifying the maxMaps values in fair-scheduler.xml
	public static boolean XMLEditor(int maxValue, String type){
		try{
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(xmlFilePath);
			
			//Node creation from XML data
			Node pool = document.getElementsByTagName("pool").item(0);
			NodeList nodes = pool.getChildNodes();

			//Search for maxMaps value can be used to MaxReducer also in input values
			for (int i = 0; i < nodes.getLength(); i++) {
				Node element = nodes.item(i);
				if (type.equals(element.getNodeName())) {
					element.setTextContent(Integer.toString(maxValue));
					System.out.println(element.getTextContent());
				}
			}

			// write the DOM object to the file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(document);

			StreamResult streamResult = new StreamResult(new File(xmlFilePath));
			transformer.transform(domSource, streamResult);

			System.out.println("The XML File was updated with maxMaps: "+maxValue);
			return true;

		}
		//Exceptions for the XML editor
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			return false;
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
			return false;
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		} catch (SAXException sae) {
			sae.printStackTrace();
			return false;
		}
	}
	public static void main(String[] args) throws Exception {
	    
		//Input value check
		if(args.length<3){
			System.out.println("Please provide inputs. Usage: TrackMapReduceJob <jobId> <referencePercentage> <noOfSlaveNodes>");
			System.out.println("=================================================================================");
			System.out.println("jobId: ID of the MapRecude Job started for WordCountPair.java \n or any other MapReduce Job");
			System.out.println("referencePercentage: input to maintain the mapper performance close to reference value \n by increase or decrease the number of mappers");
			System.out.println("noOfSlaveNodes: number of slaves nodes to avoid increase no. of mappers above threshold \n (2 mapper per nodes)");
			System.out.println("=================================================================================\n exit program");
			System.exit(0);
		}
		
		//Initialization of variables
	    String[] inputData={args[0].toString(),args[1].toString(),args[2].toString()};
	    int thresEff=Integer.parseInt(inputData[1]); //max threshold efficiency from the reference Percentage 
	    int maxMapperValue=Integer.parseInt(inputData[2])*2; //max possible mapper values expected from the np. of slave nodes
	    int currentMaxMapper=1;
	    int timmerCheck=0;
	    
	    //configuration initialization of Tracking job variables
	    Configuration conf = new Configuration();
	    JobClient jobClient= new JobClient(new JobConf(conf));
	    JobID jobId=JobID.forName(inputData[0]);
	    RunningJob runningJob=jobClient.getJob(jobId);
	
	    TaskReport[] taskReport=jobClient.getMapTaskReports(jobId);
	    String taskID=runningJob.getTaskCompletionEvents(0)[1].getTaskId();
	    TaskID firstCompletedTaskID = runningJob.getTaskCompletionEvents(0)[1].getTaskAttemptId().getTaskID();
		 
	    //TaskReport initialization 
	    for(TaskReport tr:taskReport){
	    	tr.getTaskID().equals(taskID);
	    	System.out.println(taskID+"--"+tr.getTaskID());
	    }
	    
	    //Start time of the MapReduce Job
	    long startTime=taskReport[0].getStartTime();
	    long curTime=System.currentTimeMillis();
	    Date now=new Date();
	    String curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
	    
	    //Start of controller section
	    try{
		    FileWriter f=new FileWriter("/usr/code/perf.txt");
			BufferedWriter bw=new BufferedWriter(f);
			float prog;
			float timeLapsed, efficiency;
			
			//Initial jumps for the MapReduce Task changing no. of maxMaps
			XMLEditor(maxMapperValue/2, "maxMaps");
			currentMaxMapper=maxMapperValue/2;
			
			System.out.println("Initial change impact analysis\n============================");
			System.out.println("Start Time: "+startTime);
			
			//Analysis of initial jump in maxMaps
			while(timmerCheck<10){
				prog=(float)runningJob.mapProgress();//data.getMapProgress();
				curTime=System.currentTimeMillis();
				timeLapsed=(float)(curTime-startTime);
				efficiency= ((prog)/(timeLapsed)*10000000);
				now=new Date();
				curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
				System.out.println(curTimeString+" == Mapper %completed: "+prog+" efficiency (%): "+efficiency +" timelappsed: "+timeLapsed +" current: "+curTime);
				timmerCheck++;
				Thread.sleep(1000);
			}
			timmerCheck=0;
			System.out.println("===============================End of Initial impact analysis\n");
			
			
			System.out.println("Starting Controller Design with limiting efficieny to: "+thresEff+" % \n==================================\n ");
			while(!runningJob.isComplete()){//data.isJobComplete()){
				prog=(float)runningJob.mapProgress();//data.getMapProgress();
				curTime=System.currentTimeMillis();
				timeLapsed=(float)(curTime-startTime);
				//Efficiency calculation of MapReduce Job
				efficiency= ((prog)/(timeLapsed)*10000000);
				bw.write(Float.toString(prog));
				
				//Comparison of efficiency with max threshold value
				if(efficiency<thresEff-2){
					//increasing maxMaps if efficiency is less
					if(currentMaxMapper<maxMapperValue){
						currentMaxMapper++;
						//if change is successful in XML monitor the impact of change
						if(XMLEditor(currentMaxMapper, "maxMaps")){
							now=new Date();
							curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
							System.out.println(curTimeString+" ==Max Mapper updated to: "+currentMaxMapper+"\n waiting for effect of MaxMapper change."+"WaitPeriod Status:\n ================\n");
							while(timmerCheck<10){
								System.out.print("..");
								timmerCheck++;
								
								prog=(float)runningJob.mapProgress();
								curTime=System.currentTimeMillis();
								timeLapsed=(float)(curTime-startTime);
								efficiency= ((prog)/(timeLapsed)*10000000);
								now=new Date();
								curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
								System.out.println(curTimeString+" ==Mapper %completed: "+prog+" efficiency (%): "+efficiency +" timelappsed: "+timeLapsed +" current: "+curTime);
								
								Thread.sleep(1000);
							}
							System.out.println("================= End Wait");
							timmerCheck=0;
						}else{
							System.out.println("Error in updating the max value of mapper");
						}
					}
				}
				//If efficiency is greater then threshold value reduce no. of maxMaps
				else{
					if(efficiency>thresEff+2){
						if(currentMaxMapper>1 && currentMaxMapper<=maxMapperValue){
							currentMaxMapper--;
							//if change is successful in XML monitor the impact of change
							if(XMLEditor(currentMaxMapper, "maxMaps")){
								now=new Date();
								curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
								System.out.println(curTimeString+" ==Max Mapper updated to: "+currentMaxMapper+"\n waiting for effect of MaxMapper change."+"WaitPeriod Status:\n ================\n");
								while(timmerCheck<10){
									System.out.print("..");
									timmerCheck++;
									
									prog=(float)runningJob.mapProgress();
									curTime=System.currentTimeMillis();
									timeLapsed=(float)(curTime-startTime);
									efficiency= ((prog)/(timeLapsed)*10000000);
									now=new Date();
									curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
									System.out.println(curTimeString+" ==Mapper %completed: "+prog+" efficiency (%): "+efficiency +" timelappsed: "+timeLapsed +" current: "+curTime);
									
									Thread.sleep(1000);
								}
								System.out.println();
								timmerCheck=0;
							}else{
								System.out.println("Error in updating the max value of mapper");
							}
						}
					}
				}
				//Monitoring normal section when the efficieny is withing the limits of threshold
				now=new Date();
				curTimeString= new SimpleDateFormat("HH:mm:ss:SSSZ").format(now);
				System.out.println(curTimeString+" ==Mapper %completed: "+prog+" efficiency (%): "+efficiency +" timelappsed: "+timeLapsed +" current: "+curTime);
		    	Thread.sleep(1000);
			}
			
			//Restoring values of maxMaps to 1
			if(XMLEditor(1, "maxMaps")){
				System.out.println("=================================================================================");
				System.out.println("MaxMaps value restored to : 1");
				System.out.println("=================================================================================");
			}else{
				System.out.println("=================================================================================");
				System.out.println("MaxMaps value restored ERROR: manual restore needed");
				System.out.println("location: /usr/local/hadoop/conf/fair-scheduler.xml");
				System.out.println("=================================================================================");
			}
		    bw.close();
	    }catch(IOException e){
				e.printStackTrace();
		}

	   
	  }
}
