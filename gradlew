#!/usr/bin/env sh

##############################################################################
##
##  Gradle start up script for UN*X based systems.
##
##############################################################################

DIR="$(cd "$(dirname "$0")" && pwd)"

if [ -z "$JAVA_HOME" ] ; then
    JAVA_CMD="java"
else
    JAVA_CMD="$JAVA_HOME/bin/java"
fi

CLASSPATH=$DIR/gradle/wrapper/gradle-wrapper.jar

exec "$JAVA_CMD" -Xmx64m -Xms64m -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
