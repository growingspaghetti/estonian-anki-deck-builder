# Example of Docker, Gitea+Jenkins, Selenium

It is important to bind docker services running in localhost with DNS name `dockerhost` by the means of shell and the docker proxy.

## docker user group
```
sudo groupadd docker
sudo gpasswd -a $USER docker
```

## dockerhost
every docker-run.sh contains the following lines for dockerhost which is forwarded to docker-compose.yml. 
```
# https://stackoverflow.com/questions/24319662/from-inside-of-a-docker-container-how-do-i-connect-to-the-localhost-of-the-mach
export DOCKERHOST=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }' | cut -f2 -d: | head -n1)
docker-compose -f docker-compose.yml up
```
# setup 1

## ssh

```
ssh-keygen -f gitea -t rsa -b 4096
# name it gitea
```

```
vim ~/.ssh/config
Host gitea
   Hostname localhost
   User git
   Port 10022
   PreferredAuthentications publickey
   IdentityFile ~/.ssh/gitea
```

## jenkins

```
cd jenkins
./docker-run.sh
# note: sh contains `chown 1000 local_jenkins_home`
```

```
version: '3'
services:
  jenkins:
    image: jenkins/jenkins:lts
    volumes:
      - ./local_jenkins_home:/var/jenkins_home
    ports:
      - "8090:8080"
      - "50000:50000"
    restart: always
    extra_hosts:
      - "dockerhost:$DOCKERHOST"
```

open localhost:8090

## gitea

```
cd gitea
./docker-run.sh
```
open localhost:3000

```
version: '3'
services:
  web:
    image: gitea/gitea:1.8
    volumes:
      - ./local-gitea-data:/data
    ports:
      - "3000:3000"
      - "10022:22"
    environment:
      - TZ=Japan
      - SSH_PORT=10022
    restart: always
    extra_hosts:
      - "dockerhost:$DOCKERHOST"
```

## selenium

![selenium.png](./imgs/selenium.png)

```
cd selenium
./docker-run.sh
```

open localhost:4444

```
version: "3.3"

services:
  selenium-hub:
    image: selenium/hub:3.141.0
    ports:
      - "4444:4444"
    networks: [selenium-grid]
    environment:
      - GRID_BROWSER_TIMEOUT=3400
      - GRID_TIMEOUT=3600
    restart: always
    extra_hosts:
      - "dockerhost:$DOCKERHOST"
  chrome-node:
    image: selenium/node-chrome-debug:3.141.0
    ports:
      - "5900:5900"
    environment:
      HUB_PORT_4444_TCP_ADDR: selenium-hub
      HUB_PORT_4444_TCP_PORT: 4444
    volumes:
      - ./hub:/dev/random
    networks: [selenium-grid]
    links:
      - selenium-hub:selenium-hub
    depends_on:
      - selenium-hub


networks:
  selenium-grid:
    driver: bridge
    external: false
```

# setup 2

## gitea

![gitea_settings.png](./imgs/gitea_settings.png)

![gitea_account_key.png](./imgs/gitea_account_key.png)

## jenkins

### install gog plugin
https://plugins.jenkins.io/gogs-webhook

![jenkins-gogs.png](./imgs/jenkins-gogs.png)

### git ssh credentials

Jenkins > Credentials > System

![jenkins-git-credentials.png](./imgs/jenkins-git-credentials.png)

### git host
 * use `dockerhost` as the DNS name.
 * Credentials:`git`

![jenkins-git-docker-host.png](./imgs/jenkins-git-docker-host.png)

### attach webhook
![gitea-gogs-webhook.png](./imgs/gitea-gogs-webhook.png)

### build (shell)

for example,

![jenkins-shell.png](./imgs/jenkins-shell.png)

![jenkins-webhook-shell-output.png](./imgs/jenkins-webhook-shell-output.png)

### build (maven)

for example,

![jenkins-mvn.png](./imgs/jenkins-mvn.png)

![jenkins-mvn2.png](./imgs/jenkins-mvn2.png)

![junit4.png](./imgs/junit4.png)

## selenium
![vnc.png](./imgs/vnc.png)

```
sudo snap install remmina
snap run remmina
localhost:5900 (VNC)
User password: secret
```

Eclipse > run EestiLoecsenSelenium.java

In dockered Jenkins, this address must be `dockerhost` instead of `127.0.0.1`

![sel-run.png](./imgs/sel-run.png)

![vnc-run.png](./imgs/vnc-run.png)
