#!/bin/bash

declare -i countold
countold=0
declare -i count
count=1

function startChanger() {
	echo "Starting Changes in Max of Mapper & Reducer"
	while [ $count -lt 8 ]; do       
	    xmlstarlet ed -L --update "/allocations/pool/maxMaps" -v $count fair-scheduler.xml
	    xmlstarlet ed -L --update "/allocations/pool/maxReduces" -v $count fair-scheduler.xml
	    countold=$count
	    count=$count+1
	    echo "Map & Reduce max changed to: " $count
	    sleep 5s           
	done

}

function restore() {
rm -Rf tclass/
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

!q





