#!/bin/sh


cd sonar-application/target/
if ! ls sonarqube-*/bin/sonarqube*.jar &> /dev/null; then
  unzip sonarqube-*.zip
fi

cd sonarqube-*
java -jar ./lib/sonarqube*.jar -Dsonar.web.javaAdditionalOpts=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
