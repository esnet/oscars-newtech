#!/bin/bash
echo "OSCARS database installation script."
echo ""
echo "Note: the Postgres server should be running, and you will need an account"
echo "on it that can create new roles and databases."

while true; do
    read -p "This will clear previous data! Ready? Y/n: " yn
    case ${yn} in
        [Y]* ) break;;
        [Nn]* ) exit;;
        * ) echo "Y/n:";;
    esac
done

psql -lqt | cut -d \| -f 1 | grep -qw oscars_db
DB_FOUND_CODE=$?
# need to drop the DB before the role
if [ ${DB_FOUND_CODE} -eq 0 ]; then
    read -p "Found existing OSCARS backend db, press enter to drop it..."
    dropdb oscars_db
else
    echo "OSCARS backend DB not found"
fi


psql -lqt | cut -d \| -f 1 | grep -qw oscars_backend
DB_FOUND_CODE=$?
# need to drop the DB before the role
if [ ${DB_FOUND_CODE} -eq 0 ]; then
    read -p "Found existing OSCARS core db, press enter to drop it..."
    dropdb oscars_db
else
    echo "OSCARS core DB not found"
fi


psql postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='oscars'" | grep -q 1
USER_FOUND_CODE=$?

if [ ${USER_FOUND_CODE} -eq 0 ]; then
    read -p "Found old OSCARS role, press enter to drop it..."
    dropuser oscars
else
    echo "OSCARS role not found"
fi

echo "Now, please provide a password for the OSCARS role in the Postgres database."
while true; do
    read -s -p "Password: " password
    echo
    read -s -p "Password (again): " password2
    echo
    [ "${password}" = "${password2}" ] && break
    echo "Password mismatch!"
done
psql template1 -c "CREATE ROLE oscars WITH LOGIN CREATEDB PASSWORD '${password}';"

echo "creating new database"
createdb -O oscars oscars_db
createdb -O oscars oscars_backend

echo "Configured Postgres. Please, edit backend and core application.properties and set"
echo "the password in the 'spring.datasource.password' line, if you haven't already."
read -p " Press enter to create OSCARS tables.. "

cd core

java -jar target/core-1.0.0-beta.jar \
    --spring.jpa.hibernate.ddl-auto=update \
    --startup.exit=true spring.datasource.password=${password}

cd ../backend

java -jar target/backend-1.0.0-beta.jar \
    --spring.jpa.hibernate.ddl-auto=update \
    --startup.exit=true spring.datasource.password=${password}

