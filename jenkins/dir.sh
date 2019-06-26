#!/bin/bash
FILE=local_jenkins_home
if [ ! -d "$FILE" ]; then
    mkdir local_jenkins_home
    chown 1000 local_jenkins_home
    chgrp 1000 local_jenkins_home
fi