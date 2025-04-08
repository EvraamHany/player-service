#!/bin/bash


echo "Installing Docker on Debian-based system..."
sudo apt-get update
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
    $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
sudo systemctl start docker
sudo systemctl enable docker

if ! sudo systemctl is-active --quiet docker; then
    echo "Starting Docker..."
    sudo systemctl start docker
    sleep 5
    if ! sudo systemctl is-active --quiet docker; then
        echo "Failed to start Docker. Please start Docker manually and try again."
        exit 1
    fi
fi

MAVEN_VERSION=3.8.5
wget https://www-us.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz
tar -xvzf apache-maven-$MAVEN_VERSION-bin.tar.gz
sudo mv apache-maven-$MAVEN_VERSION /opt/maven
echo "export M2_HOME=/opt/maven" >> ~/.bashrc
echo "export PATH=\$PATH:\$M2_HOME/bin" >> ~/.bashrc
source ~/.bashrc


docker pull postgres:13
docker tag postgres:13 mypostgres:13
docker-compose up -d
sleep 10

if ! docker ps | grep -q mypostgres; then
    exit 1
fi
mvn clean install

mvn spring-boot:run
