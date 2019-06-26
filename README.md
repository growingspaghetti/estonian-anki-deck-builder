# Example of Docker, Gitea+Jenkins - webhook & pipeline, Selenium

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

Settings > Webhooks > Add Webhooks > Gitea

![add-webhook.png](./imgs/add-webhook.png)

![gitea-gogs-webhook.png](./imgs/gitea-gogs-webhook.png)

### build (shell)

for example,

![jenkins-shell.png](./imgs/jenkins-shell.png)

![jenkins-webhook-shell-output.png](./imgs/jenkins-webhook-shell-output.png)

### build (maven)

for example,

Jenkins > Global Tool Configuration > Maven

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

# setup 3 (pipeline)

## Gitea webhook

```
http://dockerhost:8090/gogs-webhook/?job=eesti-anki-pipeline
```

## Jenkins

Jenkins > Global Tool Configuration > JDK

![jdk8.png](./imgs/jdk8.png)

http://localhost:8090/descriptorByName/hudson.tools.JDKInstaller/enterCredential

![oracle.png](./imgs/oracle.png)

![jenkins-pipeline1.png](./imgs/jenkins-pipeline1.png)

![jenkins-pipeline2.png](./imgs/jenkins-pipeline2.png)

### Selenium

![selenium-pipeline.png](./imgs/selenium-pipeline.png)

```
package com.github.growingspaghetti.eesti;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.core.StringContains;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class EestiLoecsenSeleniumTest {
    @Test
    public void testSelenium() throws Exception {
        WebDriver driver = new RemoteWebDriver(
            new URL("http://dockerhost:4444/wd/hub"),
            DesiredCapabilities.chrome());

        List<String> pages = new ArrayList<String>();
        try {
            driver.get("https://www.loecsen.com/en/learn-estonian");
            WebElement   itemdiv = driver.findElement(By.id("list_t"));
            List<String> targets
                = itemdiv
                      .findElements(By.xpath("//div[@id='list_t']//li"))
                      .stream()
                      .map(WebElement::getText)
                      .map(String::trim)
                      .collect(Collectors.toList());

            for (String target : targets) {
                System.out.println("page: " + target);
                By         by = By.xpath("//div[@id='list_t']//li//h3[contains(text(), '" + target + "')]");
                WebElement we = itemdiv.findElement(by);
                // https://stackoverflow.com/questions/37879010/selenium-debugging-element-is-not-clickable-at-point-x-y
                // we.click();
                ((JavascriptExecutor)driver).executeScript("arguments[0].click();", we);

                WebDriverWait wait       = new WebDriverWait(driver, 10);
                WebElement    playButton = driver.findElement(By.id("btn_autoplay"));
                wait.until(ExpectedConditions.elementToBeClickable(playButton));

                WebElement js   = driver.findElement(By.xpath("//div[@id='container']//script"));
                String     code = (String)((JavascriptExecutor)driver)
                                  .executeScript("return jQuery(arguments[0]).text();", js);

                System.out.println(code);
                Assert.assertThat(code, StringContains.containsString("https://www.loecsen.com/OrizonFlash_V2"));
                pages.add(code);
            }
        } finally {
            Optional.ofNullable(driver).ifPresent(WebDriver::quit); // not close
        }
    }
}

```
