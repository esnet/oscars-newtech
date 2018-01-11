#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars PSS"

java -Xmx512m -jar target/pss-1.0.0-beta9.jar
