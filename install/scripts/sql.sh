#!/bin/bash
#set -x

######################################################################################################
# Script for automatic installation of SQL files
# mario.lagana and simone.tancioni
# 7.3.1 Upgrade - 2012-02-20
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
#
# Exit errors:
# 3: no files to be installed
# 5: missing file(s)
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh
. $script_path/sql-utils.sh

######################################################################################################
# Parse the input parameters
######################################################################################################
restore="no";

result=$(getopt rhc "$@")

if [ $? -gt 0 ]
then
  print_error "Error in parameters"
  exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-h) hot_deploy_mode="yes";;
    (-r) restore="restore";;
    (-c) ONLY_CHECK="TRUE";;
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

e2e_dir="retail_sql"
mho_dir="wholesale_sql"

sql_e2e_dir="$PACKAGE_DIR/$e2e_dir"
sql_mho_dir="$PACKAGE_DIR/$mho_dir"

done_e2e_dir="$sql_e2e_dir/done"
done_mho_dir="$sql_mho_dir/done"

if [ ! -e $sql_e2e_dir ] && [ ! -e $sql_mho_dir ]
then
  exit 3
fi

if [ $(ls $sql_e2e_dir 2>$null | grep -v "done" | wc -l) -eq 0 ] && [ $(ls $sql_mho_dir 2>$null | grep -v "done" | wc -l) -eq 0 ]
then
  exit 3
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  exit 0;
fi

create_dir $done_e2e_dir
create_dir $done_mho_dir

sql_manager="$LIB_DIR/sql_manager.jar"
db_file="$CONFIG_DIR/db_pass.txt"

backup_sequence_name="BACKUP_SCRIPT_TABLES_SEQ"
backup_sequence_owner="ACSCONFIG"

tmp_partial_db_table_list_prefix="$TMP_DIR/${script_name}_tables_spool_db_"
tmp_total_db_table_list="$TMP_DIR/${script_name}_tables_spool_db_total.tmp"
tmp_partial_file_table_list="$TMP_DIR/${script_name}_tables_spool_file_partial.tmp";
tmp_total_file_table_list="$TMP_DIR/${script_name}_tables_spool_file_total.tmp"
tmp_final_table_list="$TMP_DIR/${script_name}_final_table_list.tmp"
tmp_query="$TMP_DIR/${script_name}_query.tmp"
tmp_log_file="$TMP_DIR/${script_name}_log_last_file.tmp"
tmp_tables_list_for_backup="$TMP_DIR/${script_name}_tables_list_for_backup.tmp"

execute_log rm $TMP_DIR/${script_name}*.tmp;

######################################################################################################
# This file will contain all archived tables 
# and their orginal name. 
# This file will be used by restore script to
# restore the backups of the installation.
#
# 1. DB target
# 2. User name
# 3. Password
# 4. Table name
# 5. Backup table name
######################################################################################################
tmp_backup_history_file="${BACKUP_DIR}/sql_files_history"
######################################################################################################

print_title "Start install SQL files"

print_warning "Please check that $db_file is correct, then press [Enter] key to continue." 
read -p "";

execute_log dos2unix $db_file $db_file;

DB_MHO=$(grep -i "|PIN|" $db_file | grep DB_MHO | cut -f2 -d"|")

if [ -z $hot_deploy_mode ]
then
  DB_CATALOG=$(grep -i "|PIN|" $db_file | grep DB_CATALOG | cut -f2 -d"|")
  DB_USER=$(grep -i "|ACSCONFIG|" $db_file | grep DB_CATALOG | cut -f3 -d"|")
  DB_PWD=$(grep -i "|ACSCONFIG|" $db_file | grep DB_CATALOG | cut -f4 -d"|")
else
  log "Hot deploy mode; DB connection will be performed on INSTALLATION_SCHEMA";
  DB_CATALOG=$INSTALLATION_SID;
  DB_USER=$INSTALLATION_SCHEMA;
  DB_PWD=$INSTALLATION_PASS;
fi

if [ -z $DB_CATALOG ] || [ -z $DB_USER ] || [ -z $DB_PWD ]
then
  print_error "Rate plan installation: Unable to read DB configuration";
  exit 2;
fi

MAX_BACKUP_ROWS=$(grep -i "MAX_BACKUP_ROWS=" $db_file | cut -f2 -d"=")

log_variable DB_MHO;
log_variable DB_CATALOG;
log_variable DB_USER;
log_variable DB_PWD;

if [ -z $MAX_BACKUP_ROWS ]
then
  MAX_BACKUP_ROWS=20000;
fi

log_variable MAX_BACKUP_ROWS;

hostname=$(hostname)

print_warning "WARNING - Retail installation will be performed on $DB_CATALOG DB. Wholesale installation will be performed on $DB_MHO DB.";
cf ask_yn "if you want to continue" "if you want to quit from the sql files installation" || exit 8 ;
    
print_title "Parse involved table names for backup"

get_table_names_for_backup () {

  if [ $# -ne 2 ]
  then
    print_error "Missing parameters";
    return 2;
  fi

  db_type=$1;
  db_sid=$2;

  tmp_partial_db_table_list="${tmp_partial_db_table_list_prefix}${db_type}.tmp";
  touch $tmp_partial_db_table_list

  get_query_result $DB_USER $DB_PWD ${db_sid} "
    DECLARE
     flag   NUMBER;
    BEGIN
    SELECT COUNT (1)
      INTO flag
      FROM ALL_OBJECTS
    WHERE OBJECT_TYPE = 'SEQUENCE' AND OBJECT_NAME = '$backup_sequence_name' AND OWNER = '$backup_sequence_owner';

    IF (flag > 0)
    THEN
      EXECUTE IMMEDIATE 'DROP SEQUENCE $backup_sequence_owner.$backup_sequence_name';
    END IF;

    EXECUTE IMMEDIATE 'CREATE SEQUENCE $backup_sequence_owner.$backup_sequence_name
      START WITH 0
      MAXVALUE 10000
      MINVALUE 0
      NOCYCLE
      NOCACHE
      NOORDER';

    END;
    /

    GRANT ALL ON $backup_sequence_owner.$backup_sequence_name TO PUBLIC;
  "

  sql_result=$?
  if [ $sql_result -ne 0 ]
  then
    print_warning "Unable to create sequence $backup_sequence_owner.$backup_sequence_name. $db_type installation will not be performed. Please check $script_dir/$log_file";
    return $sql_result;
  fi

  spool_query_result $DB_USER $DB_PWD ${db_sid} $tmp_partial_db_table_list "
    select owner || ';' || table_name
    from all_tables
    where owner in ('PIN', 'ACSCONFIG', 'ACSRTRCONFIG', 'INTEGRATE_MHO', 'ACSPINSHADOW', 'INTEGRATE', 'ERP_DATAEXTRACTOR')
    order by length(TABLE_NAME) desc
  "

  sql_result=$?
  if [ $sql_result -ne 0 ]
  then
    print_warning "Unable to get tables list. $db_type installation will not be performed. Please check $script_dir/$log_file";
    return $sql_result;
  fi
}

cf get_table_names_for_backup "Retail" $DB_CATALOG;
cf get_table_names_for_backup "Wholesale" $DB_MHO;

log "Executing: cat ${tmp_partial_db_table_list_prefix}* | awk '{print length($1),$1}' | sort -nr | uniq | awk '{print $2}' > $tmp_total_db_table_list";
cat  ${tmp_partial_db_table_list_prefix}* | awk '{print length($1),$1}' | sort -nr | uniq | awk '{print $2}' > $tmp_total_db_table_list

######################################################################################################
# Backup the field separator
######################################################################################################
OLD_IFS=$IFS
IFS=$'\n'
######################################################################################################

######################################################################################################
# Get info from SQL file to install
# Parameter:
# 1. Input dir name
#
# File structure:
# 1. Schema
# 2. Table
# 3. File name
######################################################################################################
create_backup_info() {
  if [ $# -ne 1 ]
  then
    print_error "Missing parameters";
    return 2;
  fi

  if [ $(cat $tmp_total_db_table_list | wc -l) -eq 0 ]
  then
    return 3;
  fi

  cf empty_file $tmp_log_file
  cf empty_file $tmp_partial_file_table_list
  
  log "Executing: java -jar $sql_manager parse_tables $tmp_total_db_table_list $1 $tmp_partial_file_table_list 1>>$tmp_log_file 2>>$tmp_log_file 3>>$tmp_log_file";
  java -jar $sql_manager parse_tables $tmp_total_db_table_list $1 $tmp_partial_file_table_list 1>>$tmp_log_file 2>>$tmp_log_file 3>>$tmp_log_file
  parse_result=$?

  cat $tmp_log_file >> $log_file;
  cat $tmp_partial_file_table_list >> $tmp_total_file_table_list;
  
  if [ $parse_result -eq 1 ]
  then
    print_error "Error on getting schema names. Please check log $script_dir/$log_file";
  elif [ $parse_result -eq 4 ]
  then
    print_error "Error on getting table names list. Please check log $script_dir/$log_file";
    return 1;
  elif [ $parse_result -eq 7 ]
  then
    print_error "Error: one of the sql names is wrong. Please check log $script_dir/$log_file";
  fi
  
  return 0
}

cf empty_file $tmp_total_file_table_list;

cf create_backup_info "$sql_e2e_dir";
check_parse=$?

if [ $check_parse -ne 0 ]
then
  log "Unable to parse create backup info";
  exit 1
fi

cf create_backup_info "$sql_mho_dir";
check_parse=$?

if [ $check_parse -ne 0 ]
then
  log "Unable to parse create backup info";
  exit 1
fi
######################################################################################################

######################################################################################################
# Add db info into $tmp_final_table_list
# 1. Schema
# 2. Table
# 3. File name
# 4. DB SID
######################################################################################################
cf empty_file  $tmp_final_table_list

check_install=0
for i in $(cat $tmp_total_file_table_list)
do
  check_install=1
  if [ $(echo $i | grep -i $mho_dir | wc -l) -gt 0 ]
  then
    target=$DB_MHO
  else 
    target=$DB_CATALOG
  fi
  
  echo "$i|$target" >> $tmp_final_table_list
  log "extracted: $i|$target";
done

if [ $check_install -eq 0 ]
then
  print_title "No files to install"
  exit 3
fi
######################################################################################################

######################################################################################################
# Get unique table for backup
# 1. Schema
# 2. Table
# 3. DB SID
######################################################################################################
cat $tmp_final_table_list | cut -f1,2,4 -d"|" | sort | uniq > $tmp_tables_list_for_backup
######################################################################################################

######################################################################################################
# Execute backup
######################################################################################################
print_title "Start backup of involved tables"

tmp_date=`date +"%y%m%d"`

for i in $(cat $tmp_tables_list_for_backup)
do
  tmp_user=$(echo $i | cut -f1 -d"|")

  if [ $(echo $tmp_user | wc -m) -eq 1 ]
  then 
    log "Warning - unable to parse Schema name from filename: $i";
    continue;
  fi

  tmp_table=$(echo $i | cut -f2 -d"|" )

  if [ $(echo $tmp_table | wc -m) -eq 1 ]
  then 
    log "Warning - unable to parse table name from filename: $i";
    continue;
  fi

  if [ $tmp_table == "NO_TABLE" ]
  then 
    continue;
  fi

  tmp_table_bck=$(echo $i | cut -f2 -d"|" | cut -c1-22)
  tmp_target=$(echo $i | cut -f3 -d"|")
  tmp_pass=$(grep -i "|$tmp_user|" $db_file | grep -i $tmp_target | cut -f4 -d"|" | head -n1)

  log "backup of: $tmp_target $tmp_user $tmp_pass $tmp_table ${tmp_table_bck}_${tmp_date}";

  get_query_result $DB_USER $DB_PWD $tmp_target "SELECT to_char(NUM_ROWS) FROM ALL_TABLES WHERE OWNER = '$tmp_user' AND TABLE_NAME = '$tmp_table'";
  ROW_NUM=$QUERY_RESULT;
  log "ROW_NUM: $ROW_NUM";

  [[ $ROW_NUM =~ "^[0-9]+$" ]] || continue;

  if [ ! -z $ROW_NUM ] && [ $ROW_NUM -ge $MAX_BACKUP_ROWS ]
  then
    log "The table is too big for backup. Skipping.";
    continue;
  fi    
      
  cf empty_file $tmp_log_file;
  
  get_query_result $tmp_user $tmp_pass $tmp_target "
  DECLARE
     flag   NUMBER;
     tab_name VARCHAR2(30);
  BEGIN
    
    LOOP 
      SELECT '${tmp_table_bck}_${tmp_date}' || to_char(MOD($backup_sequence_owner.$backup_sequence_name.NEXTVAL, 9))
      INTO tab_name
      FROM DUAL;
        
      DBMS_OUTPUT.PUT_LINE('BACKUP_TABLE_NAME:'||tab_name);

      SELECT COUNT (1)
      INTO flag
      FROM user_tables UT
      WHERE UT.table_name = UPPER (tab_name);
        
      EXIT WHEN  (flag = 0);
    END LOOP;

	EXECUTE IMMEDIATE 'create table ' || tab_name || ' as select * from $tmp_table';
  END;
  /
  "
  result=$?;

  echo $QUERY_RESULT >> $log_file

  if [ $result -ne 0 ]
  then 
    print_error "Error performing backup of table $tmp_table";
    error="true";
  else
    tmp_table_bck=$(echo "$QUERY_RESULT" | grep BACKUP_TABLE_NAME | tail -1 | cut -f2 -d":")
    echo "$tmp_target|$tmp_user|$tmp_pass|$tmp_table|$tmp_table_bck" >> $tmp_backup_history_file
  fi
  
done
######################################################################################################

if [ ! -z $error ]
then
  print_warning "Error found during backup.";
  cf ask_yn "if you want to continue" "if you want to quit from the sql files installation" || exit 8 ;
else
  print_title "Backup completed with success";
fi

print_title "Installing files"

######################################################################################################
# Execute installation
######################################################################################################
error=0
check=0
for i in $(cat $tmp_final_table_list)
do
  tmp_user=$(echo $i | cut -f1 -d"|")
  
  tmp_file=$(echo $i | cut -f3 -d"|")
  tmp_target=$(echo $i | cut -f4 -d"|")
  tmp_pass=$(grep -i "|$tmp_user|" $db_file | grep -i $tmp_target | cut -f4 -d"|" | head -n1)
  
  if [ $(echo $tmp_user | wc -m) -eq 1 ]
  then 
    print_error "Error parsing schema from filename: $tmp_file"
    continue
  fi

  if [ $(echo $tmp_target | wc -m) -eq 1 ]
  then 
    print_error "Error get SID from $db_file for user: $tmp_user"
    continue
  fi
  
  if [ $(echo $tmp_pass | wc -m) -eq 1 ]
  then 
    print_error "Error get password from $db_file for user: $tmp_user and SID:$tmp_target"
    continue
  fi

  log "install of: $tmp_target $tmp_user $tmp_pass $tmp_file";
  
  ######################################################################################################
  # Create, manipulate and launch a temp file for current file
  ######################################################################################################
  echo -e "set echo on\n" > $tmp_query
  #cat $tmp_file |  perl -p -e 's/END;/END;\n\//' >> $tmp_query
  cat $tmp_file >> $tmp_query
  echo -e "\nCOMMIT;\nexit;" >> $tmp_query;
  
  log "sqlplus -s $tmp_user/$tmp_pass@$tmp_target @${tmp_query}";

  cf empty_file $tmp_log_file
  execute_log dos2unix -ascii "$tmp_query" "$tmp_query";
  
  sqlplus -s $tmp_user/$tmp_pass@$tmp_target @${tmp_query} >>$tmp_log_file 2>>$tmp_log_file 3>>$tmp_log_file;
  ######################################################################################################
    
  cat $tmp_log_file >>$log_file
  if [ $(grep "^ORA" $tmp_log_file | wc -l) -gt 0 ]
  then
    print_error "Error found in file $tmp_file. Please check.";
    error=1
  elif [ $(grep "^Warning" $tmp_log_file | wc -l) -gt 0 ]
  then
    print_warning "Warning found in file $tmp_file. Please check.";
    error=1
  elif [ $(grep "^SP" $tmp_log_file | wc -l) -gt 0 ]
  then
    print_warning "Error found in file $tmp_file. Please check.";
    error=1
  else
    check=1
    print_installed "$tmp_file installed with no errors";

    if [ $(echo $tmp_file | grep -i $mho_dir | wc -l) -gt 0 ]
    then
      mv $tmp_file $done_mho_dir;
    else
      mv $tmp_file $done_e2e_dir;
    fi
  fi
done
######################################################################################################

######################################################################################################
# Print restart messages
######################################################################################################
if [ $(cat $tmp_final_table_list | grep ACSCONFIG | grep retail | wc -l) -gt 0 ]
then
  print_warning "ATTENTION: A refresh cache into retail and inquiry adaptrer is needed";
fi

if [ $(cat $tmp_final_table_list | grep ACSCONFIG | grep wholesale | wc -l) -gt 0 ]
then
  print_warning "ATTENTION: A refresh cache into wholesale adaptrer is needed";
fi

if [ $(cat $tmp_final_table_list | grep ACSRTRCONFIG | grep retail | wc -l) -gt 0 ]
then
  print_warning "ATTENTION: A refresh cache into RTR adaptrer is needed";
fi

if [ $(cat $tmp_final_table_list | grep INTEGRATE | wc -l) -gt 0 ]
then
  print_warning "ATTENTION: Restart of pipeline is needed";
fi
######################################################################################################

######################################################################################################
# Restore the field separator
######################################################################################################
IFS=$OLD_IFS
######################################################################################################

if [ $error -eq 1 ]
then
  print_error "Issue(s) found in installation, please check log $script_dir/$log_file";
  exit 6;
fi

if [ $check -eq 0 ] 
then
  print_warning "Warning - One or more files are not installed";
  exit 6;
fi

print_title "End of SQL installation"
