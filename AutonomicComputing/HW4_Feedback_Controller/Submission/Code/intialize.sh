#!/bin/bash

#create folders and compile the code
function startChanger() {
	echo "Creating Folders for WordCountPair and TrackMapReduceJob"
	mkdir class
	
	mkdir tclass
	echo "Folder created"
	echo "compiling codes"
	javac -classpath ../local/hadoop/bin/hadoop-core-1.0.3.jar -d class/ WordCountPair.java
	jar -cvf WordCountPair.jar -C class/ .
	echo "WordCountPair.jar created"
	javac -classpath ../local/hadoop/bin/hadoop-core-1.0.3.jar -d tclass/ TrackMapReduceJob.java
	jar -cvf TrackMapReduceJob.jar -C tclass/ .
	echo "TrackMapReduceJob.jar created"
	echo "Start the MapReduce Job and input the jobId to TrackMapReduceJob"

}

function restore() {
rm -Rf tclass/
rm -Rf class/
nano WordCountPair.java
rm Track*
nano TrackMapReduceJob.java
exit
}

if [ $# -eq 1 ] 
then 
	restore $1
else
	startChanger
fi




