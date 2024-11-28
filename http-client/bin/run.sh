#!/bin/bash

DIR=`dirname $0`/..
DIR=`readlink -f "$DIR"`

CLASSPATH=
for l in `find "$DIR/target" -name \*.jar -print`; do
   CLASSPATH=$CLASSPATH:$l
done

export CLASSPATH

exec java -Xmx256m \
     -Djava.io.tmpdir="$DIR/cache" \
     -Dconfig.dir="$DIR/conf" \
     -Dconfig.file="demo-config.conf" \
     org.dive4elements.artifacts.httpclient.ConsoleClient \
     2>&1 > /dev/null
