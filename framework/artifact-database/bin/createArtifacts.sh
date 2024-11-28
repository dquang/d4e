#!/bin/bash

mkdir -p artifactsdb

DIR=`dirname $0`
DIR=`readlink -f "$DIR"`

if [ $# != 1 ]; then
    SCHEMA="$DIR/../doc/schema-h2.sql"
else
    SCHEMA="$1"
fi

URL="jdbc:h2:`readlink -f artifactsdb`/artifacts"

mvn -e -Dexec.mainClass=org.h2.tools.RunScript exec:java \
    -Dexec.args="-url $URL -script $SCHEMA"
