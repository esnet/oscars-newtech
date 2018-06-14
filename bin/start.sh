#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars"

version="1.0.7"

orig_dir=`pwd`

top_dir="$(dirname "$0")/.."
cd "$top_dir"
top_dir=`pwd`

# set a trap on SIGINT to kill the first background task (the core process) then exit
trap 'kill %1; kill %2; echo -e "\n\n######   Exiting all OSCARS tasks. ######\n\n"; exit' SIGINT

echo "Starting backend"
cd "$top_dir/backend"

java -jar "target/backend-${version}-exec.jar" &

echo "Starting PSS"
cd "$top_dir/pss"
java -jar "target/pss-${version}.jar"

kill %1; kill %2

cd ${orig_dir}

