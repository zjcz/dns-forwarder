#!/bin/bash
mvn clean install
mvn -q exec:exec -Dexec.executable=java -Dexec.args="-cp %classpath dev.jonclarke.dnsforwarder.Main $1"
