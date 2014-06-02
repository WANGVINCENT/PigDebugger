Pig Latin Debugger
==================

Currently, the relationship between sections of Pig Scripts, and Map or Reduce tasks running on a Hadoop Cluster is difficult to establish.

Pig Latin Debugger is a web-based user interface enabling users to keep track of the execution of Pig Latin scripts in Hadoop HDFS or local file system. It provides precise details to users about which parts of data flows are executing at any one time on different nodes across the hadoop cluster.

Configuration
=============
  
To configure, please run the script in the configuration folder :
    
    sh PigDebugger-config.sh 
    
Launch
======
To launch the server, run the script in the root project folder :
    
    node main.js

