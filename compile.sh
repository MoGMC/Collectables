#!/bin/sh
mvn clean
mvn package 
rm ../../test-server-1.8/plugins/CollectablesPlugin-2.1.jar 
cp ./target/CollectablesPlugin-2.1.jar ../../test-server-1.8/plugins/
