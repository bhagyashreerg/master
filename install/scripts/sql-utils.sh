######################################################################################################
# Perform a query and get the result.
# Input
# 1 - DB_USER
# 2 - DB_PASS
# 3 - DB_SID
# 4 - Query
######################################################################################################
get_query_result () {
	if [ $# -ne 4 ]
	then
		log "Error: missing parameters";
		return 2;
	fi

  if [ -z $log_label ]
  then
    log_label="get_query_result> ";
  fi
  
	DB_USER=$1
	DB_PASS=$2
	DB_SID=$3	
  
  log_variable DB_USER
	log_variable DB_PASS
	log_variable DB_SID

	shift;
	shift;
	shift;
		
	SQL_PLUS_HEAD="set time off
		set timing off
		set echo off
		set head off
		set scan off
		set pagesize 0
		set serveroutput off
		set term off";

  SQL_QUERY="
    ${SQL_PLUS_HEAD}
      
    $@;
    
    exit
  "

	log "Executing query: 
		$SQL_QUERY";
		
	QUERY_RESULT=`sqlplus -S $DB_USER/$DB_PASS@$DB_SID <<EOF 2>&1
		${SQL_QUERY}
EOF`

  result=$?;
  
  unset log_label;

  if [ $result -ne 0 ]
  then
    log_variable QUERY_RESULT;
    return 4;
  fi
  
  if [ $(echo -e "$QUERY_RESULT" | egrep "(^ORA-|^Warning|^SP2-)" | wc -l) -gt 0 ]
  then
    log_variable QUERY_RESULT;
    return 3;
  fi

  return 0;
}
######################################################################################################

######################################################################################################
# Perform a query and spool the result into a file.
# Input
# 1 - DB_USER
# 2 - DB_PASS
# 3 - DB_SID
# 4 - Spool file
# 5 - Query
######################################################################################################
spool_query_result () {
	if [ $# -ne 5 ]
	then
		log "Error: missing parameters";
		return 2;
	fi
  
  if [ -z $log_label ]
  then
    log_label="spool_query_result> ";
  fi

	DB_USER=$1
	DB_PASS=$2
	DB_SID=$3
	SPOOL_FILE=$4

  log_variable DB_USER
	log_variable DB_PASS
	log_variable DB_SID
	log_variable SPOOL_FILE
  
	shift;
	shift;
	shift;
	shift;
		
	SQL_PLUS_HEAD="set timing off
				set serveroutput on
				set heading off
				set feedback off
				set tab off
				set pagesize 0
				set linesize 3000
				set trimspool on";
        
  SQL_QUERY="
    ${SQL_PLUS_HEAD}
      
    spool $SPOOL_FILE

    $@;
    
    spool off

    exit
  "

	log "Executing query: 
		$SQL_QUERY";
		
	log_variable SPOOL_FILE;

	QUERY_RESULT=`sqlplus -S $DB_USER/$DB_PASS@$DB_SID <<EOF 2>&1
		$SQL_QUERY
EOF`

  unset log_label;

  if [ $(echo -e "$QUERY_RESULT" | egrep "(^ORA-|^Warning|^SP2-)" | wc -l) -gt 0 ]
  then
    log_variable QUERY_RESULT >> $log_file;
    return 3;
  fi
  
  #if [ $(cat $SPOOL_FILE | egrep "(^ORA-|^Warning|^SP-)" | wc -l) -gt 0 ]
  #then
  #  log "Executing: cat $SPOOL_FILE >> $log_file";
  #  log_variable QUERY_RESULT >> $log_file;
  #  return 3;
  #fi
  
  return 0;
}
######################################################################################################