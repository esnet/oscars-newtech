#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars PSS"

# Find the OSCARS pss.jar file
JARFILE=""
# Artifact location in target directory, for running in-tree
LOCALJAR=`echo target/pss-*.jar`
if [ -e $LOCALJAR ]; then
    JARFILE=$LOCALJAR
fi
# Make sure we can find it
if [ "x$JARFILE" = "x" ]; then
    echo "Unable to locate OSCARS pss.jar file"
    exit 1
fi

java -Xmx512m -jar ${JARFILE}
