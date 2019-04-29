#!/bin/bash
# https://github.com/jenkinsci/docker/issues/177
FILE=local_jenkins_home
if [ ! -d "$FILE" ]; then
    mkdir local_jenkins_home
    chown 1000 local_jenkins_home
    chgrp 1000 local_jenkins_home
fi
# https://stackoverflow.com/questions/24319662/from-inside-of-a-docker-container-how-do-i-connect-to-the-localhost-of-the-mach
export DOCKERHOST=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | awk '{ print $2 }' | cut -f2 -d: | head -n1)
docker-compose -f docker-compose.yml up
