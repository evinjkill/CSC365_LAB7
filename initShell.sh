#!/bin/bash

echo Exporting variables for sql auth
export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16:wq-bin.jar:.
export `HP_JDBC_URL=jdbc:mysql://db.labthreesixfive.com/ekillian?autoReconnect=true\&useSSL=false
export HP_JDBC_USER=ekillian
export HP_JDBC_PW=S19_CSC-365-011798367
