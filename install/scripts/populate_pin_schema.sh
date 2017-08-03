#!/bin/bash
#set -x

######################################################################################################
# Script for automatic copy of the PIN schema to the PIN_INSTALL schema (HotDeploy)
#
# Exit errors:
# 2. Error in parmeters
# 3. no files to be installed
# 4. missing configuration
# 5. user choice to quit
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
history_file="${BACKUP_DIR}/${script_name}_history"
history_table="INSTALL_TOOL_BACKUP_HISTORY"

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

backup_sequence_name="BACKUP_SCRIPT_TABLES_SEQ"
######################################################################################################

######################################################################################################
# Create the history table
######################################################################################################
get_query_result $PIN_SCHEMA $PIN_PASS $PIN_SID "
  DECLARE
   flag   NUMBER;
  BEGIN
  SELECT COUNT (1)
    INTO flag
    FROM ALL_TABLES
  WHERE TABLE_NAME = '$history_table' AND OWNER = '$PIN_SCHEMA';

  IF (flag > 0)
  THEN
    EXECUTE IMMEDIATE 'DROP TABLE $PIN_SCHEMA.$history_table';
  END IF;

  EXECUTE IMMEDIATE  'CREATE TABLE $PIN_SCHEMA.$history_table
                      (
                         script_name          VARCHAR2 (255),
                         installation_label   VARCHAR2 (255),
                         object_name          VARCHAR2 (255),
                         backup_name          VARCHAR2 (255),
                         installation_date    DATE
                      )';

  EXECUTE IMMEDIATE  'GRANT ALL ON $PIN_SCHEMA.$history_table TO PUBLIC';

  END;
  /
"

sql_result=$?
if [ $sql_result -ne 0 ]
then
  print_warning "Unable to create history backup table $PIN_SCHEMA.$history_table";
  print_hd_error "PHASE $CURRENT_PHASE finished with errors. Please check and try again."
  exit 6;
fi
######################################################################################################

######################################################################################################
# Create the sequence for the backup-table name
######################################################################################################
get_query_result $PIN_SCHEMA $PIN_PASS $PIN_SID "
  DECLARE
   flag   NUMBER;
  BEGIN
  SELECT COUNT (1)
    INTO flag
    FROM ALL_OBJECTS
  WHERE OBJECT_TYPE = 'SEQUENCE' AND OBJECT_NAME = '$backup_sequence_name' AND OWNER = '$PIN_SCHEMA';

  IF (flag > 0)
  THEN
    EXECUTE IMMEDIATE 'DROP SEQUENCE $PIN_SCHEMA.$backup_sequence_name';
  END IF;

  EXECUTE IMMEDIATE 'CREATE SEQUENCE $PIN_SCHEMA.$backup_sequence_name
    START WITH 0
    MAXVALUE 10000
    MINVALUE 0
    NOCYCLE
    NOCACHE
    NOORDER';

  END;
  /

  GRANT ALL ON $PIN_SCHEMA.$backup_sequence_name TO PUBLIC;
"

sql_result=$?
if [ $sql_result -ne 0 ]
then
  print_warning "Unable to create sequence $PIN_SCHEMA.$backup_sequence_name";
  print_hd_error "PHASE $CURRENT_PHASE finished with errors. Please check and try again."
  exit 6;
fi
######################################################################################################

######################################################################################################
# Perform the backup of the input table
#
# Input
# 1. Source table name
######################################################################################################
perform_table_backup () {
  if [ $# -ne 1 ]
  then
    print_error "Missing parameters";
    return 2;
  fi
  
  local source_table_name=$1;
  local backup_table_name_prefix=$(echo $source_table_name | cut -c1-22)

  tmp_date=`date +"%y%m%d"`

  get_query_result $PIN_SCHEMA $PIN_PASS $PIN_SID "
  DECLARE
     flag   NUMBER;
     tab_name VARCHAR2(30);
  BEGIN
    
    LOOP 
      SELECT '${backup_table_name_prefix}_${tmp_date}' || to_char(MOD($PIN_SCHEMA.$backup_sequence_name.NEXTVAL, 9))
      INTO tab_name
      FROM DUAL;
        
      DBMS_OUTPUT.PUT_LINE('BACKUP_TABLE_NAME:'||tab_name);

      SELECT COUNT (1)
      INTO flag
      FROM user_tables UT
      WHERE UT.table_name = UPPER (tab_name);
        
      EXIT WHEN  (flag = 0);
    END LOOP;

    INSERT INTO $PIN_SCHEMA.$history_table (SCRIPT_NAME, INSTALLATION_LABEL, OBJECT_NAME, BACKUP_NAME, INSTALLATION_DATE) VALUES ('$script_name', '$label', '$PIN_SCHEMA.$source_table_name', tab_name, sysdate);
    COMMIT;
    EXECUTE IMMEDIATE 'create table ' || tab_name || ' as select * from $PIN_SCHEMA.$source_table_name';
  END;
  /
  "
  sql_result=$?
  if [ $sql_result -ne 0 ]
  then
    return $sql_result;
  fi
}
######################################################################################################

log "Executing: cat $hot_deploy_tables_list";
for table_name in $(cat $hot_deploy_tables_list)
do
  log_variable table_name;

  ######################################################################################################
  # Perform the backup of the table on PIN schema before to overwrite
  ######################################################################################################
  perform_table_backup $table_name;
  result=$?;
  if [ $result -ne 0 ]
  then 
    get_query_result $PIN_SCHEMA $PIN_PASS $PIN_SID "
      CREATE TABLE $PIN_SCHEMA.$table_name AS
        SELECT * FROM $INSTALLATION_SCHEMA.$table_name";
    result=$?;
    if [ $result -ne 0 ]
    then 
      print_error "Unable to copy table $INSTALLATION_SCHEMA.$table_name";
      error="true";
      continue;
    fi
  fi
  ######################################################################################################
  
  ######################################################################################################
  # Copy the table from installation schema into PIN schema
  ######################################################################################################
  get_query_result $PIN_SCHEMA $PIN_PASS $PIN_SID "
    TRUNCATE TABLE $PIN_SCHEMA.$table_name;
    INSERT INTO $PIN_SCHEMA.$table_name (SELECT * FROM $INSTALLATION_SCHEMA.$table_name)";
  
  result=$?;
  if [ $result -ne 0 ]
  then 
    print_error "Error copying table $table_name";
    error="true";
  else
    print_installed "$table_name done";
  fi
  ######################################################################################################
done  
    
if [ -z $error ]
then
  print_message "PHASE $CURRENT_PHASE finished with success";
  exit 0;
else
  print_hd_error "PHASE $CURRENT_PHASE finished with errors. Please check and try again."
  exit 6;
fi
