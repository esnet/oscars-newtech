#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars backend"

java -Xmx512m -jar target/backend-1.0.0-beta6.jar \
    --spring.jpa.hibernate.ddl-auto=create-drop \
     $1 $2 $3 $4 $5
