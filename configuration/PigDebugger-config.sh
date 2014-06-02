
#config node.js
echo "Configuring nodejs"
sudo apt-get install python-software-properties python g++ make
sudo add-apt-repository ppa:chris-lea/node.js
sudo apt-get update
sudo apt-get install nodejs

sudo npm install socket.io
sudo npm install mysql

sudo apt-get install mysql-server

#install shell terminal
cd /home/hduser/PigDebugger/shellinabox-2.14
./configure
sudo make
sudo make install

