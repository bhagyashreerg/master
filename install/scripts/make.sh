#!/bin/bash
#set -x

######################################################################################################
# Script for automatic installation of Opcodes
#
# Input Parameters:
# -l LABEL
#  LABEL string will be used to mark the backup files.
#
# -r
#  This is an internal parameter that indicate the script is launched by the restore script
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
#
# Exit errors:
# 2: Parameter error
# 3: No files to be installed
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh

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
# Firsts checks
######################################################################################################
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
current_host_name=$(hostname)

source_path="$PIN_HOME/source/sys";

header_file_type="header";
opcode_file_type="opcode";
header_files_history="${BACKUP_DIR}/${header_file_type}_history"
opcode_files_history="${BACKUP_DIR}/${opcode_file_type}_history"

make_config_file="$CONFIG_DIR/make.config"

if [ ! -r $make_config_file ]
then
  print_error "Unable to read the file $make_config_file";
  exit 4;
fi

if [ ! -r $header_files_history ] && [ ! -r $opcode_files_history ]
then
  log "No header or opcode installed in this package: $header_files_history and opcode_files_history do not exists or they are not readable";
  exit 3;
fi

if [ $(cat $header_files_history | wc -l) -eq 0 ] && [ $(cat $opcode_files_history | wc -l) -eq 0 ]
then
  log "No header or opcode installed in this package: $header_files_history and opcode_files_history are empty";
  exit 3;
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  exit 0;
fi

library_output_path="$PACKAGE_DIR/lib_files";
create_dir $library_output_path;

tmp_file_log="$TMP_DIR/${script_name}_log.tmp"
######################################################################################################

######################################################################################################
# Collect the libraries to compile
# Input
# 1. file type
# 2. history file
######################################################################################################
collect_libs () {
  if [ $# -ne 2 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local file_type=${1}"_files";
  local history_file=$2;
  
  for history_file_line in $(cat $history_file)
  do
    local file_name=$(echo $history_file_line | cut -f1 -d";");

    if [ -z $file_name ]
    then
      error="true";
      print_error "Unable to find the header file name into file $history_file";
      continue;
    fi

    log "grep \"^$file_type\" $make_config_file 2>>$log_file | grep $file_name | cut -f\"3\" -d\";\" 2>>$log_file";
    local library_name=$(grep "^$file_type" $make_config_file 2>>$log_file | grep $(basename $file_name) | cut -f"3" -d";" 2>>$log_file)

    if [[ -z $library_name ]]
    then
      error="true";
      print_error "Unable to find configuration for $file_name into file $make_config_file";
      continue;
    fi
    
    log_variable file_name;
    log_variable library_name;

    for i in $library_name
    do
      cf array_add_if_not_exists libs_to_compile "${library_name}";
    done
    
    continue;
  done
}
######################################################################################################

print_title "Start portal library compilation"

collect_libs $header_file_type $header_files_history;
collect_libs $opcode_file_type $opcode_files_history;

print_message "The following libraries will be compiled: ${libs_to_compile[@]}";
log_variable libs_to_compile;

curr_dir=$(pwd);

for lib_name in ${libs_to_compile[@]}
do
  log_variable lib_name;
  lib_path=$source_path/$lib_name;
  
  if [ ! -d $lib_path ]
  then
    print_error "Unable to find lib path: $lib_path";
    error="true";
    continue;
  fi
  
  lib_file_path="${lib_path}/${lib_name}.so";
  
  log_variable lib_name;
  log_variable lib_path;
  log_variable lib_file_path;
  
  cf archive "${lib_file_path}";
  
  make_command=$(grep -i $current_host_name $make_config_file | grep "make_command" | head -1 | cut -f3 -d";");
  
  if [ -z $make_command ]
  then
    print_error "Unable to find the make command for this host ($hostname) into file $make_config_file";
    error="true";
    continue
  fi
    
  log_variable make_command;
  
  cf cd $lib_path;
  
  cf empty_file $tmp_file_log;
  
  execute_log $make_command clean && execute_log $make_command;
  make_result=$?;
  if [ $make_result -ne 0 ]
  then
    print_error "Unable to make the library $lib_name:"
    cat $tmp_file_log;
    error="true";
    continue;
  fi
  
  execute_log cp -p $lib_file_path $library_output_path;
  cp_result=$?;
  if [ $cp_result -ne 0 ]
  then
    print_error "Unable to copy the library into package dir"
    error="true";
    continue;
  fi
  
  print_installed "Successfully compiled: $lib_name";
done

cf cd $curr_dir;

if [ -z $error ]
then
  print_title "End portal library compilation"
else
  print_warning "WARNING - Library compilation is not completed.";
  exit 6;
fi
