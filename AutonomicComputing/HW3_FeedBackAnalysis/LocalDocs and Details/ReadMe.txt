Pre-requisit for allowing the job to run for longer period:
-----------------------------------------------------------
Please provide atleast 100MB of text data 


Command to execute the MapReduce application:
----------------------------------------------
bin/hadoop jar WordCountPair.jar usr.code.WordCountPair <input_HDFS> <ouput_Location>


Commant to execute bash-script to change Mapper and Reducer max. values in fair-sheduler.xml
---------------------------------------------------------------------------------------------
to start changing: ./MaxMapperReducerChanger.sh

to reset max. values: ./MaxMapperReducerChanger.sh 1



Cluster Size used for evaluation:
-----------------------------------
1 Master
3 Slaves

Reason: Issue with started larger size cluster


Application Running time:
-----------------------------------
Approax : 5mins


Collected Data Points
------------------------
#u(k) considered as percentage of job completed by Mapper
 divided by 10
uWave(k) = {0.8, 2.2, 4.2, 6.5, 8.7, 10.0}

#p(k) considerd as Job Execution Rate from the values 

pWave(k) = (uWave(k)/(delta k) )*6

pWave(k) = {0.1200, 0.3300, 0.6300, 0.9750, 1.3050, 1.5000}

