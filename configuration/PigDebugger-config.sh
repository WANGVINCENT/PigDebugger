
#config node.js
echo "Configuring node.js"
sudo apt-get install python-software-properties python g++ make
sudo add-apt-repository ppa:chris-lea/node.js
sudo apt-get update
sudo apt-get install nodejs
echo "Installation of node.js succeeds!"

echo "Install socket.io module"
sudo npm install socket.io
echo "Install mysql module"
sudo npm install mysql

echo "Install mysql server"
sudo apt-get install mysql-server

echo "Install shell terminal"
#install shell terminal
cd ~/PigDebugger/shellinabox-2.14
./configure
sudo make
sudo make install

echo "Install multipart module"
#install multipart
sudo apt-get install git
cd ~/PigDebugger/node_modules
rm -r multipart
git clone https://github.com/isaacs/multipart-js.git
mv multipart-js multipart
