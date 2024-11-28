#!/bin/bash

# Copyright (c) 2010 by Intevation GmbH
#
# This program is free software under the LGPL (>=v2.1)
# Read the file LGPL.txt coming with the software for details
# or visit http://www.gnu.org/licenses/ if it does not exist.

RESTLET_CORE=`find -L ~/.m2 -name org\.restlet-\*SNAP\*.jar`
RESTLET_XML=`find -L ~/.m2 -name org\.restlet.ext.xml-\*SNAP\*.jar`
H2=`find -L ~/.m2 -name h2-\*.jar`
LOG4J=`find -L ~/.m2 -name log4j-1.2.13\*.jar`
DBCP=`find -L ~/.m2 -name commons-dbcp-\*.jar`
POOL=`find -L ~/.m2 -name commons-pool-*.jar | head -1`
POSTGRES=`find -L ~/.m2 -name postgresql-8.3\*.jar`
DIR=`dirname $0`/..
CLASSPATH=$DIR/artifact-database/target/classes
CLASSPATH=$CLASSPATH:$DIR/artifacts/target/classes
CLASSPATH=$CLASSPATH:$RESTLET_CORE:$RESTLET_XML
CLASSPATH=$CLASSPATH:$LOG4J:$H2:$DBCP:$POOL:$POSTGRES
export CLASSPATH
java "$@"
