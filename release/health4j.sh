#!/usr/bin/env bash

# PARAMS
NAME=health4j
USER=root
PID_PATH=/var/run
HEALTH4J_HOME=/usr/local/health4j
COMMON_CONFIG_FILE=$HEALTH4J_HOME/conf/common.properties
JAR_FILE=$HEALTH4J_HOME/start.jar
LOG_PROPERTY_PATH=$HEALTH4J_HOME/conf/log4j.properties
EXCEPTION="Usage: health4j {start|check|status}"
RETVAL=0
HEALTH4J_PID=$PID_PATH/$NAME.pid

usage()
{
    echo $EXCEPTION
    exit 1
}

[ $# -gt 0 ] || usage

running()
{
  if [ -f "$1" ]
  then
    local PID=$(cat "$1" 2>/dev/null) || return 1
    kill -0 "$PID" 2>/dev/null
    return
  fi
  rm -f "$1"
  return 1
}

# DEFINE STARTUP & ENDUP
start() {
      echo -n "Starting health4j ..."

      if running $HEALTH4J_PID
      then
        echo "Already running $(cat $HEALTH4J_PID)"
        exit 1
      fi

      start-stop-daemon -S -p$HEALTH4J_PID -c$USER -b -m -a /usr/bin/java -- -jar $JAR_FILE $COMMON_CONFIG_FILE
      RETVAL=$?
      echo "started."
}

# COMMANDS
case "$1" in
    start)
      start
  ;;
    check|status)
      echo "Checking arguments to health4j: "
      echo "HEALTH4J_HOME          =  $HEALTH4J_HOME"
      echo "COMMON_CONFIG_FILE     =  $COMMON_CONFIG_FILE"
      echo "JAR_PATH               =  $JAR_FILE"
      echo "HEALTH4J_PID           =  $HEALTH4J_PID"
      echo "HEALTH4J_LOG_CONFIG    =  $LOG_PROPERTY_PATH"

      if running "$HEALTH4J_PID"
      then
        echo "health4j running pid  =  $(< "$HEALTH4J_PID")"
        exit 0
      fi
      exit 1
    ;;
    *)
      echo $EXCEPTION
      exit 1
  ;;
esac

exit $RETVAL