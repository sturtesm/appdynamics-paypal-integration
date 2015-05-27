#!/bin/sh

TOP=`dirname $0`

${TOP}/bin/shutdown.sh
${TOP}/bin/startup.sh

tail -f ${TOP}/logs/catalina.out
