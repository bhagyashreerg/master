#!/bin/bash
#set -x

######################################################################################################
# Script for automatic installation of the zonemaps
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
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

result=$(getopt rc "$@")

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

INSTALLATION_SCHEMA=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_SCHEMA")
INSTALLATION_PASS=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_PASS")
INSTALLATION_SID=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_SID")

log_variable INSTALLATION_SCHEMA
log_variable INSTALLATION_PASS
log_variable INSTALLATION_SID

files_type="zone_maps"
origin_path="$PACKAGE_DIR/$files_type";
done_dir="$origin_path/done"

zone_map_backup_dir="$BACKUP_DIR/$files_type";

if [ ! -e $origin_path ]
then
	exit 3
fi

if [ $(ls ${origin_path}/*.ifl* | grep -v "done" | wc -l) -eq 0 ]
then
	exit 3
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  exit 0;
fi

cf create_dir $done_dir
cf create_dir $zone_map_backup_dir

tmp_flist_output=${TMP_DIR}/${script_name}_flist_output.ofl
tmp_flist_input=${TMP_DIR}/${script_name}_flist_input.ifl
######################################################################################################

print_title "Start install zone maps"

######################################################################################################
# Decompress the zone maps files to install
######################################################################################################
execute_log cd $origin_path;

for i in $(ls *.tar.gz 2>$null)
do
	log "Executing: gunzip -c $i 2>>$log_file | tar -xvf - 1>>$log_file 2>>$log_file";
	gunzip -c $i 2>>$log_file | tar -xvf - 1>>$log_file 2>>$log_file
	
	if [ $? -eq 0 ]
	then
		execute_log rm $i;
	else	
		print_warning "Unable to unzip $i";
	fi
done

execute_log cd $TMP_DIR;
execute_log cp $PIN_CONF_PATH $TMP_DIR;
log "Executing: ls ${origin_path}/*.ifl";
######################################################################################################

zone_maps_curr_dir=$(pwd);
cd $TMP_DIR;
execute_log cp -p $PIN_CONF_PATH .;

for zone_map_path in $(ls ${origin_path}/*.ifl)
do
  zone_map_file=$(basename $zone_map_path);
  zone_map_name=${zone_map_file%.*};
  
  log_variable zone_map_path;
  log_variable zone_map_file;
  log_variable zone_map_name;
  
  ######################################################################################################
  # Get the zone map poid
  ######################################################################################################
  get_query_result $INSTALLATION_SCHEMA $INSTALLATION_PASS $INSTALLATION_SID "SELECT TO_CHAR(POID_ID0) FROM PIN.ZONEMAP_T WHERE NAME = '$zone_map_name'";

  result=$?;
  if [ $result -ne 0 ]
  then 
    print_error "Unable to get zone map poid for $zone_map_name";
    error="true";
    continue;
  fi
  
  zone_map_poid=$QUERY_RESULT;
  
  cf is_number $zone_map_poid || ( print_error "Invalid zone map poid ($zone_map_poid) from the query, please check the log $log_file" && continue );
  ######################################################################################################
  
  ######################################################################################################
  # Get the zone map backup
  ######################################################################################################
  cf execute_testnap "PCM_OP_ZONEMAP_GET_ZONEMAP" "0 PIN_FLD_POID                      POID [0] 0.0.0.1 /zonemap $zone_map_poid 1" $tmp_flist_output;
  zone_map_result=$?;
  if [ $zone_map_result -ne 0 ]
  then 
    log "Unable to backup the zone map $zone_map_name. New zone map?";
    zone_map_poid="-1";
  else
    cp $tmp_flist_output "${zone_map_backup_dir}/${zone_map_name}.ifl";
  fi
  ######################################################################################################
  
  ######################################################################################################
  # Prepare the zone map file
  ######################################################################################################
  flist_poid_lines=($(grep -n PIN_FLD_POID $zone_map_path | cut -f1 -d":"));
  
  log_variable flist_poid_lines;
  
  if [ ${#flist_poid_lines[@]} -ne 2 ]
  then
    print_error "Errors in zone map file: $zone_map_path";
    error="true";
    continue;
  fi
  
  poid_line_1="0 PIN_FLD_POID                      POID [0] 0.0.0.1 /zonemap $zone_map_poid 1";
  poid_line_2="1     PIN_FLD_POID                  POID [0] 0.0.0.1 /zonemap $zone_map_poid 1";
  
  log "Executing: sed \"${flist_poid_lines[0]}s@.*@$poid_line_1@\" $zone_map_path | sed \"${flist_poid_lines[1]}s@.*@$poid_line_2@\" > $tmp_flist_input";
  sed "${flist_poid_lines[0]}s@.*@$poid_line_1@" $zone_map_path 2>>$log_file | sed "${flist_poid_lines[1]}s@.*@$poid_line_2@" > $tmp_flist_input 2>>$log_file;
  ######################################################################################################
  
  ######################################################################################################
  # Finally install the zone map
  ######################################################################################################
  cf execute_testnap "PCM_OP_ZONEMAP_COMMIT_ZONEMAP" $tmp_flist_input $tmp_flist_output;
  zone_map_result=$?;
  if [ $zone_map_result -ne 0 ]
  then 
    print_error "Unable to install the zone map $zone_map_name";
    error="true";
  else
		execute_log mv -f $zone_map_path $done_dir;
    print_installed "$zone_map_name installed";
  fi
  ######################################################################################################
done

cd $zone_maps_curr_dir;

if [ -z $error ]
then
  print_title "End install zone maps"
else
  print_warning "WARNING - Zone map installation is not completed. Press [Enter] key to continue.";
  exit 6;
fi