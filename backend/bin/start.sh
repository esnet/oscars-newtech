#!/bin/bash
function reset_tabname {
    printf '\e]0;\a'
}
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars backend"

# Find the OSCARS backend.jar file
JARFILE=""
# Artifact location in target directory, for running in-tree
LOCALJAR=`echo target/backend-*-exec.jar`
if [ -e $LOCALJAR ]; then
    JARFILE=$LOCALJAR
fi
# Make sure we can find it
if [ "x$JARFILE" = "x" ]; then
    echo "Unable to locate OSCARS backend.jar file"
    exit 1
fi

java -Xmx512m -jar ${JARFILE} $1 $2 $3 $4 $5
reset_tabname