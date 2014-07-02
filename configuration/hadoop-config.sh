#!/usr/bin/env sh

# The following script sets up Hadoop 2.2.0 and Pig 0.12.0 on a single node.
# NOTES: The resources of the node should be of sufficient performance. For example, if implementation is on AWS, Hadoop will not realiably operate on the smallest instance. 

cd ~ 
sudo apt-get update

#### HADOOP INSTALLATION ###

# Download java jdk
sudo apt-get install openjdk-7-jdk
cd /usr/lib/jvm
sudo ln -s java-7-openjdk-amd64 jdk

# Uncommment to install ssh 
sudo apt-get install openssh-server

# Generate keys
sudo -u guest ssh-keygen -t rsa -P ''
sudo sh -c 'cat /home/guest/.ssh/id_rsa.pub >> /home/guest/.ssh/authorized_keys'
sudo sh -c 'echo "# disable ipv6 \nnet.ipv6.conf.all.disable_ipv6 = 1 \nnet.ipv6.conf.default.disable_ipv6 = 1 \nnet.ipv6.conf.lo.disable_ipv6 = 1" >> /etc/sysctl.conf'

# Add hadoop user
sudo addgroup hadoop
sudo adduser --ingroup hadoop hduser
sudo adduser hduser sudo

# Generate keys
sudo -u hduser ssh-keygen -t rsa -P ''
sudo sh -c 'cat /home/hduser/.ssh/id_rsa.pub >> /home/hduser/.ssh/authorized_keys'
#ssh localhost

# Install Hadoop and set permissons
cd ~
if [ ! -f hadoop-1.2.1.tar.gz ]; then
	wget http://apache.mirrors.timporter.net/hadoop/common/hadoop-1.2.1/hadoop-1.2.1.tar.gz
fi
sudo tar vxzf hadoop-1.2.1.tar.gz -C /usr/local
cd /usr/local
sudo mv hadoop-1.2.1 hadoop
sudo chown -R hduser:hadoop hadoop

# Hadoop variableis
sudo sh -c 'echo export JAVA_HOME=/usr/lib/jvm/jdk/ >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_HOME=/usr/local/hadoop >> /home/hduser/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_HOME/bin >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOPDIR=$HADOOP_HOME/conf >> /home/hduser/.bashrc'
#sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/sbin >> /home/hduser/.bashrc'
#sudo sh -c 'echo export HADOOP_MAPRED_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
#sudo sh -c 'echo export HADOOP_COMMON_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
#sudo sh -c 'echo export HADOOP_HDFS_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
#sudo sh -c 'echo export YARN_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
#sudo sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_INSTALL\}/lib/native >> /home/hduser/.bashrc'
#sudo sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\$HADOOP_INSTALL/lib\" >> /home/hduser/.bashrc'

# Modify JAVA_HOME 
#cd /usr/local/hadoop/conf

sudo -u hduser sh -c 'echo export JAVA_HOME=/usr/lib/jvm/jdk >> /usr/local/hadoop/conf/hadoop-env.sh'
#sudo -u hduser sh -c 'echo export HADOOP_OPTS=-Djava.net.preferIPv4Stack=true >> /usr/local/hadoop/conf/hadoop-env.sh'
#sudo -u hduser sed -i.bak s=\${JAVA_HOME}=/usr/lib/jvm/jdk/=g hadoop-env.sh
pwd

# Check that Hadoop is installed
/usr/local/hadoop/bin/hadoop version

cd /usr/local/hadoop/conf/

# configuration of *site.xml
sudo mkdir -p /app/hadoop/tmp
sudo chown hduser:hadoop /app/hadoop/tmp
sudo chmod 750 /app/hadoop/tmp

# Edit configuration files
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>hadoop\.tmp\.dir\</name>\<value>/app/hadoop/tmp</value>\</property>\<property>\<name>fs\.default\.name\</name>\<value>hdfs://146.169.45.191:54310\</value>\</property>=g' core-site.xml 

sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>dfs\.replication\</name>\<value>3</value>\</property>=g' hdfs-site.xml

sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>mapred\.job\.tracker\</name>\<value>146.169.45.191:54311\</value>\</property>\<property>\<name>mapred\.child\.java\.opts\</name>\<value>-Xmx1024m\</value>\</property>\<property>\<name>mapred\.tasktracker\.map\.tasks\.maximum\</name>\<value>4\</value>\</property>\<property>\<name>mapred\.tasktracker\.reduce\.tasks\.maximum\</name>\<value>2\</value>\</property>=g' mapred-site.xml

#### PIG INSTALLATION ####

# Prepare for Pig installation
cd ~
sudo apt-get install ant
sudo apt-get install junit
sudo apt-get install subversion
if [ ! -f pig-0.12.1.tar.gz ]; then
    wget http://mirror.catn.com/pub/apache/pig/stable/pig-0.12.1.tar.gz 
        #svn co http://svn.apache.org/repos/asf/pig/trunk   #Using svn would alternatively download pig-0.13.0.SNAPSHOT
fi
sudo tar vxzf pig-0.12.1.tar.gz -C /usr/local
cd /usr/local
sudo mv pig-0.12.1 pig
sudo chown -R hduser:hadoop pig

# Recompile Pig 0.12.0 for Hadoop 2.2.0
cd /usr/local/pig
#sudo ant clean jar-withouthadoop -Dhadoopversion=23

# Set paths
sudo sh -c 'echo export PIG_HOME=/usr/local/pig >> /home/hduser/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$PIG_HOME/bin >> /home/hduser/.bashrc'
#sudo sh -c 'echo export PIG_CLASSPATH=\$HADOOP_HOME/conf >> /home/hduser/.bashrc'

# Check pig version
JAVA_HOME=/usr/lib/jvm/jdk/ /usr/local/pig/bin/pig --version

sudo -u hduser /usr/local/hadoop/bin/stop-all.sh
#name format
sudo -u hduser /usr/local/hadoop/bin/hadoop namenode -format


### Testing Hadoop and Pig

## Run the following commands as hduser to start and test hadoop
#sudo su hduser
# Format Namenode
#hdfs namenode -format
# Start Hadoop Service
#start-dfs.sh
#start-yarn.sh
# Check status
#hduser jps
# Example
#cd /usr/local/hadoop
#hadoop jar ./share/hadoop/mapreduce/hadoop-mapreduce-examples-2.2.0.jar pi 2 5

## Run the following commands as hduser to start and test pig (see http://pig/apache.org/docs/r0.12.0/start.html)
#sudo su hduser
#cp /etc/passwd ~/passwd 
#hdfs dfs -copyFromLocal passwd 
#pig
# Within pig
#A = load 'passwd' using PigStorage(':'); 
#B = foreach A generate $0 as id;
#dump B;

# Visit hdfs online 
# http://146.169.44.224:50070


