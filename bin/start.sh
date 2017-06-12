#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars"

orig_dir=`pwd`

top_dir="$(dirname "$0")/.."
cd "$top_dir"
top_dir=`pwd`

cd "$top_dir/core"

# set a trap on SIGINT to kill the first background task (the DS process) then exit
trap 'kill %1; kill %2 echo -e "\nExiting.."; exit' SIGINT
echo "Starting core.."
java -jar target/core-1.0.0-beta.jar &

echo "Starting PSS"
cd "$top_dir/pss"
java -jar target/pss-1.0.0-beta.jar

kill %1; kill %2

cd ${orig_dir}

