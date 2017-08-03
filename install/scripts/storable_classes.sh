#!/bin/bash
#set -x

######################################################################################################
# Script for automatic installation of Storable classes
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
# 3: no files to be installed
#
# Created:			mario.lagana	7.3.1 Upgrade		2012-03-22
# Last Update: 		mario.lagana	X6					2012-05-02
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh

######################################################################################################
# Parse the input parameters
######################################################################################################
result=$(getopt rc "$@")

if [ $? -gt 0 ]
then
	print_error "$script_name. Error in parameters"
	exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-r) restore="true";;
    (-c) ONLY_CHECK="TRUE";;
    (--) shift; break;;
    (-*) print_error "$script_name. $0: error - unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done
######################################################################################################

######################################################################################################
# Initialization
######################################################################################################
null=/dev/null

if [ -z $INSTALLATION_TOOL_HOME ]
then
	print_error "Error: INSTALLATION_TOOL_HOME not initialized";
	exit 4
elif [ ! -d $INSTALLATION_TOOL_HOME ]
then
	print_error "Error: INSTALLATION_TOOL_HOME not valid: $INSTALLATION_TOOL_HOME";
	exit 4
fi

files_type="storable_classes"
files_dir="$PACKAGE_DIR/${files_type}"
done_dir="${files_dir}/done"
curr_dir=`pwd`

backup_dir="${BACKUP_DIR}/${files_type}"
backup_history="${backup_dir}/${files_type}_history"
tmp_file_log="$TMP_DIR/file_log.tmp"

if [ ! -e $files_dir ]
then
	exit 3
fi

if [ $(ls $files_dir | grep -v "done" | wc -l) -eq 0 ]
then
	exit 3
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  exit 0;
fi

touch  $tmp_file_log
create_dir $backup_dir
create_dir $done_dir

execute_log cp $PIN_CONF_PATH $TMP_DIR;
unset storable_class_error;
######################################################################################################

print_title "Start install storable classes"

######################################################################################################
# Print a message to set the Write Flag on DM_ORACLE pin.conf before continue
######################################################################################################
print_set_flag_message(){
	print_error "ERROR - Write flag is not set on DM_ORACLE pin.conf - Please check";
	print_error "  In $PIN_HOME/sys/dm_oracle directory open the pin.conf file";
	print_error "  Set the flags:";
	print_error "    dm dd_write_enable_fields 1";
	print_error "    dm dd_write_enable_objects 1";
	print_error "    dm dd_write_enable_portal_objects 1";
	print_error "  Stop and restart the Connection Manager (CM) and DM on both servers (both cm and cm_noTimos)";
	print_error "    using 1) stop_cm and start_cm";
	print_error "          2) stop_dm_oracle and start_dm_oracle";
	print_error "";
  print_warning "Please press [Enter] key to try again.";
	read -p "";
}
######################################################################################################

######################################################################################################
# Install a storable class file
#
# Input parameters:
# 1. File name
#
# Exit errors:
# 0. No errors (go to next file)
# 1. Unexpected error (go to next file)
# 2. Write flag not set (retry current file)
######################################################################################################
install_file ()
{
  cd $TMP_DIR;
  
	class=$(grep "STORABLE CLASS" $1 | cut -f3 -d" " | head -1)
	file_name=$(basename $1)
	backup="${backup_dir}/${file_name}"

	log "-- Backup of $class"
	
	cf empty_file $tmp_file_log
  
  log "Executing: pin_deploy class -mnscp $class >$backup 2>$tmp_file_log";
	pin_deploy class -mnscp $class >$backup 2>$tmp_file_log
	cat $tmp_file_log >> $log_file
	
	if [ $(grep "Class not found" $tmp_file_log | wc -l) -gt 0 ]
	then
		log "Class not found during backup of $class - this is a new class."
	elif [ $(grep "Error" $tmp_file_log | wc -l) -gt 0 ]
	then
		print_error "ERROR - Error found during backup of $class - Please check log $script_dir/$log_file"
		return 1;
	else
		echo ${file_name}";"$(basename $backup) >> $backup_history
	fi
		
	cf empty_file $tmp_file_log
  log "Executing: pin_deploy verify $1 >$tmp_file_log 2>$tmp_file_log";
	pin_deploy verify $1 >$tmp_file_log 2>$tmp_file_log
	cat $tmp_file_log >> $log_file

	if [ $(grep "write flags not set" $tmp_file_log | wc -l) -gt 0 ]
	then
		cf print_set_flag_message;
		return 2;
	fi
	
	command="error"
	if [ $(grep "No Conflicts detected" $tmp_file_log | wc -l) -gt 0 ]
	then
		log "-- Verification success, continue with install"
		command="create"
	fi
	
	if [ $(grep "already exists" $tmp_file_log | wc -l) -gt 0 ]
	then
		log "-- Class $class exists, this will be replaced"
		command="replace"
	fi
	
	if [ $command == "error" ]
	then
		print_error "ERROR - Error found during install of $class - Please check log $script_dir/$log_file"
		return 1;
	fi

	cf empty_file $tmp_file_log
  log "Executing: pin_deploy $command $1 >>$tmp_file_log 2>>$tmp_file_log";
	pin_deploy $command $1 >$tmp_file_log 2>$tmp_file_log
	cat $tmp_file_log >> $log_file
		
	if [ $(grep "successfully" $tmp_file_log | wc -l) -gt 0 ]
	then
		base_file_name=$(basename $1)
		print_installed "$base_file_name correctly installed"
		execute_log mv $1 $done_dir;
	else
		print_error "Error found during install of $class - Please check log $script_dir/$log_file";
    storable_class_error="true";
		return 1;
	fi
  
  cd $curr_dir
	
	return 0;
}
######################################################################################################

######################################################################################################
# Installation
######################################################################################################
for i in $(ls $files_dir/*.podl 2>$null)
do
	install_result=-1;
	
	while [ $install_result -ne 0 ]
	do
		install_file $i;
		install_result=$?;
		if [ $install_result -eq 1 ]
		then
			break;
		elif [ $install_result -eq 2 ]
		then
			continue;
		fi
	done
done
######################################################################################################

if [ -z $storable_class_error ]
then
  print_title "End install storable classes"
else
  print_warning "WARNING - Storable classes installation is not completed.";
  exit 6;
fi