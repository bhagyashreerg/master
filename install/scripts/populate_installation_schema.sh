#!/bin/bash
#set -x

######################################################################################################
# Script for automatic copy of the PIN schema to the PIN_INSTALL schema (HotDeploy)
#
# Exit errors:
# 2. Error in parmeters
# 3: no files to be installed
# 4: missing configuration
# 5: user choice to quit
# 6. Error(s) during installation
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh
. $script_path/sql-utils.sh

######################################################################################################
# Parse the input parameters
######################################################################################################
restore="no";

result=$(getopt r "$@")

if [ $? -gt 0 ]
then
  print_error "Error in parameters"
  exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-r) restore="restore";;
    (--) shift; break;;
    (-*) print_error "Unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done
######################################################################################################

######################################################################################################
# Initialization
######################################################################################################
null=/dev/null
export NLS_LANG=ITALIAN_ITALY.WE8MSWIN1252

if [ -z $INSTALLATION_TOOL_HOME ]
then
	print_error "Error: INSTALLATION_TOOL_HOME not initialized";
	exit 4
elif [ ! -d $INSTALLATION_TOOL_HOME ]
then
	print_error "Error: INSTALLATION_TOOL_HOME not valid: $INSTALLATION_TOOL_HOME";
	exit 4
fi

curr_dir=$(pwd)

INSTALLATION_SCHEMA=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_SCHEMA")
INSTALLATION_PASS=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_PASS")
INSTALLATION_SID=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_SID")

PIN_SCHEMA=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "PIN_SCHEMA")
PIN_PASS=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "PIN_PASS")
PIN_SID=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "PIN_SID")

log_variable INSTALLATION_SCHEMA
log_variable INSTALLATION_PASS
log_variable INSTALLATION_SID
log_variable PIN_SCHEMA
log_variable PIN_PASS
log_variable PIN_SID

hot_deploy_tables_list="$CONFIG_DIR/hot_deploy_tables_list.config"

if [ ! -r $hot_deploy_tables_list ]
then
  print_error "Unable to read the table list file: $hot_deploy_tables_list";
  exit 4;
fi

log "Executing: cat $hot_deploy_tables_list";
for table_name in $(cat $hot_deploy_tables_list)
do
  log_variable table_name;

  get_query_result $INSTALLATION_SCHEMA $INSTALLATION_PASS $INSTALLATION_SID "
    TRUNCATE TABLE $INSTALLATION_SCHEMA.$table_name";
  
  query_result=$?;
  if [ $query_result -ne 0 ]
  then 
    log "Table doesn't exists, creating $INSTALLATION_SCHEMA.$table_name";

    get_query_result $INSTALLATION_SCHEMA $INSTALLATION_PASS $INSTALLATION_SID "CREATE TABLE $INSTALLATION_SCHEMA.$table_name AS (SELECT * FROM $PIN_SCHEMA.$table_name)";
    query_result=$?;
  else
    log "Table truncated, populating from $PIN_SCHEMA.$table_name";

    get_query_result $INSTALLATION_SCHEMA $INSTALLATION_PASS $INSTALLATION_SID "INSERT INTO $INSTALLATION_SCHEMA.$table_name (SELECT * FROM $PIN_SCHEMA.$table_name)";
      query_result=$?;
  fi

  if [ $query_result -ne 0 ]
  then 
    print_error "Error copying table $table_name";
    error="true";
  else
    print_installed "$table_name done";
  fi

done  
    
if [ -z $error ]
then
  print_message "PHASE 0 finished with success";
  exit 0;
else
  print_hd_error "PHASE 0 finished with errors. Please check and try again."
  exit 6;
fi
