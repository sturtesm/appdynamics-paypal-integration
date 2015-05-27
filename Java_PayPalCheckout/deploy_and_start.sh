#!/bin/sh


TOP=`dirname $0`
SOURCE=${TOP}/target/PayPalLoginWebTier-0.0.1-SNAPSHOT.war
TOMCAT=${TOP}/../apache-tomee-jaxrs-1.6.0

#re-package the app
rm -rf ${TOP}/target
rm -rf ${SOURCE} ${TOMCAT}/webapps/*

mvn package

cp ${SOURCE} ${TOMCAT}/webapps/paypal-online-store.war

TC_PIDS=`pgrep -f apache-tomee-jaxrs-1.6.0`

if [ "x${TC_PIDS}" = "x" ];
then
    echo ------------------------------------------------------------------------------
    echo
    echo "Tomcat not running on system, starting ${TOMCAT}/bin/catalina.sh now..."
    echo
    echo ------------------------------------------------------------------------------
    ${TOMCAT}/bin/catalina.sh start&
fi

echo ------------------------------------------------
echo Tailing Tomcat Log Files: ${TOMCAT}/logs/catalina.out
echo ------------------------------------------------
tail -f ${TOMCAT}/logs/catalina.out

