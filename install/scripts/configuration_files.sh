#!/bin/bash
#set -x

##########################################################################
# Script for automatic installation of configuration files
#
# Input Parameters:
# -l LABEL
#  LABEL string will be used to mark the backup files.
#
# -r
#  This is an internal parameter that indicate the script is launched by the restore script
#
# -t TYPE
#  File type to install <wholesale|retail|all>
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
#
# Exit errors:
# 2: Parameter(s) missing
# 3: no files to be installed
##########################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh

##########################################################################
# Parse the input parameters
##########################################################################
restore="no";

result=$(getopt rt:c "$@")

if [ $? -gt 0 ]
then
	print_error "$script_name. Error in parameters: $@"
	exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-r) restore="restore";;
    (-t) installation_parameter="$2"; shift;;
    (-c) ONLY_CHECK="TRUE";;
    (--) shift; break;;
    (-*) print_error "$0: error - unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done

if [ -z $installation_parameter ]
then
	print_error "$script_name. File type parameter -t is mandatory: $@";
	exit 2;
elif [ ! $installation_parameter == "wholesale" ] && [ ! $ifw_file_type == "retail" ] && [ ! $ifw_file_type == "all" ]
then
	print_error "$script_name. File type parameter not recognized <wholesale|retail|all>: $@";
	exit 2;
fi
##########################################################################

##########################################################################
# Initialization
##########################################################################
null=/dev/null

if [ -z $INSTALLATION_TOOL_HOME ]
then
	print_error "Error: INSTALLATION_TOOL_HOME not initialized";
	exit 4;
elif [ ! -d $INSTALLATION_TOOL_HOME ] 
then
	print_error "Error: INSTALLATION_TOOL_HOME not valid: $INSTALLATION_TOOL_HOME";
	exit 4;
fi

files_dir="$PACKAGE_DIR/configuration_files"
done_dir="$files_dir/done"
curr_dir=`pwd`
log_dir="$INSTALLATION_TOOL_HOME/logs"

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

TMP_DIR="$INSTALLATION_TOOL_HOME/tmp"

cf create_dir $done_dir;
cf create_dir $TMP_DIR;

tmp_file_log="$TMP_DIR/${script_name}_log.tmp"

execute_log cp $PIN_CONF_PATH $TMP_DIR;

##########################################################################
# This file will contain all archived files 
# and their orginal name. 
# This file will be used by restore script to
# restore all backups of the installation.
##########################################################################
files_type="configuration_files"
tmp_backup_history="$BACKUP_DIR/${files_type}_history"
touch $tmp_backup_history;
##########################################################################

##########################################################################
# Archive a file with the current date
# and store the info in the backup history file
# 1. Original file name
# 2. Bakcup file name
##########################################################################
archive_targz () {
	new_name=$1_`date +"%Y%m%d%H%M%S"`
	
	dirname_new_name=$(dirname $new_name)
	basename_new_name=$(basename $new_name)
	basename_original_name=$(basename $1)
	
	execute_log cd $dirname_new_name;
	log "Executing: tar -cf - $basename_original_name 2>$null | gzip -c > $basename_new_name.tar.gz 2>$null";
	tar -cf - $basename_original_name 2>$null | gzip -c > $basename_new_name.tar.gz 2>$null;
	execute_log cd $curr_dir;
	
	if [ $restore == "no" ]
	then
		echo "$1;$new_name.tar.gz" >> $tmp_backup_history;
	fi
}
##########################################################################

log_variable tmp_file_log;
execute_log touch $tmp_file_log;

##########################################################################
# Retail configuration
##########################################################################
pin_glid_dir="${TMP_DIR}"
pin_glid="pin_glid"
pin_impact_category_dir="${TMP_DIR}"
pin_impact_category="pin_impact_category_POSTE"
pin_beid_dir="$pin_glid_dir"
pin_beid="pin_beid_POSTE_REL_3_4"
pin_event_map_dir="$pin_glid_dir"
pin_event_map="pin_event_map_POSTE"
pin_reasons_dir="${TMP_DIR}"
pin_reasons="reasons.en_US"
pin_rum_dir="$pin_glid_dir"
pin_rum_dir="$pin_glid_dir"
pin_rum="pin_rum_POSTE"
pin_rum="pin_rum_POSTE"
config_item_dir="$pin_glid_dir"
config_item="config_item_tags.xml"
config_item_types_dir="$pin_glid_dir"
config_item_types="config_item_types.xml"
pin_bill_suppression_dir="$pin_impact_category_dir"
pin_bill_suppression="Poste_pin_bill_suppression.xml"
##########################################################################

##########################################################################
# MHO configuration
##########################################################################
mho_config_dir="${PIN_HOME}/apps/load_config_mho"
pin_glid_mho="pin_glid_mho"
pin_impact_category_mho="pin_impact_category_POSTE_mho"
##########################################################################

##########################################################################
# Install a pin configuration file, if exists
# Input:
# 1. configuration file name
# 2. directory of the configuration file
# 3. pin utiliry name
# 4. word to recognize a successful install
##########################################################################
pin_file_install(){
  log "$@ ($#)";

	if [ $# -ne 4 ]
	then
		print_error "Missing parameter(s)";
		return 2;
	fi

	file_to_install=$1
	dir_to_install=$2
	pin_utility_name=$3
	word_of_success=$4

	if [ ! -e $files_dir/$file_to_install ]
	then
		return 0;
	fi
  
  if [ -z $start_title_printed ]
  then
    print_title "Start install $installation_parameter configuration files";
    start_title_printed="true";
  fi
	
	if [ ! -e $dir_to_install ]
	then
		print_error "No $dir_to_install directory, unable to install $file_to_install file";
		return 1;
	fi

	log "-- Installing $file_to_install";
	cf archive_targz $dir_to_install/$file_to_install;
	
	execute_log cp $files_dir/$file_to_install $dir_to_install;
	execute_log cd $dir_to_install;
	
	cf empty_file $tmp_file_log;
  
  log "Executing: $pin_utility_name $file_to_install >>$tmp_file_log 2>>$tmp_file_log";
	$pin_utility_name $file_to_install >>$tmp_file_log 2>>$tmp_file_log
  cat $tmp_file_log >> $log_file;
  
	execute_log cd $curr_dir;
	
	cf check_log_success $tmp_file_log $files_dir/$file_to_install $done_dir "$word_of_success" && print_installed "$file_to_install installed whithout errors" || print_error "Error in $file_to_install - Please check log file $log_file";
	cf empty_file $tmp_file_log
}
##########################################################################

##########################################################################
# Untar files
##########################################################################
execute_log cd $files_dir

for i in $(ls *.tar.gz 2>$null)
do
	log "Executing: gunzip -c $i 2>$null | tar -xvf - 1>$null 2>$null";
	gunzip -c $i 2>$null | tar -xvf - 1>$null 2>$null
	
	if [ $? -eq 0 ]
	then
		rm $i
	else	
		print_warning "Unable to unzip $i";
	fi
done

execute_log cd $curr_dir
##########################################################################
if [ $installation_parameter == "wholesale" ] || [ $installation_parameter == "all" ]
then
	cf pin_file_install $pin_glid_mho $mho_config_dir "load_pin_glid -v" "committed";
	cf pin_file_install $pin_impact_category_mho $mho_config_dir "load_pin_impact_category" "successfully";
fi

if [ $installation_parameter == "retail" ] || [ $installation_parameter == "all" ]
then
	cf pin_file_install $pin_glid $pin_glid_dir "load_pin_glid -v" "committed";
	cf pin_file_install $pin_impact_category $pin_impact_category_dir "load_pin_impact_category" "successfully";
	cf pin_file_install $pin_reasons $pin_rum_dir "load_localized_strings" "successfully";
	cf pin_file_install $pin_event_map $pin_event_map_dir "load_event_map -v" "commit";
	cf pin_file_install $pin_beid $pin_beid_dir "load_pin_beid -v" "committed";
	cf pin_file_install $pin_rum $pin_rum_dir "load_pin_rum -v" "commit";
	cf pin_file_install $config_item $config_item_dir "load_config_item_tags -v" "Successfully";
	cf pin_file_install $config_item_types $config_item_types_dir "load_config_item_types -v" "Successfully";
	cf pin_file_install $pin_bill_suppression $pin_bill_suppression_dir "load_pin_bill_suppression" "Success";
fi

##########################################################################
# Tar installed files
##########################################################################
execute_log cd $done_dir;
for i in $(ls * 2>$null | grep -v tar 2>$null)
do	
	log "Executing: tar -cf - $i | gzip -c > $i.tar.gz";
	tar -cf - $i | gzip -c > $i.tar.gz
	
	if [ $? -eq 0 ]
	then
		execute_log rm -r $i;
	else	
		print_warning "Unable to compress $i in tar.gz package";
	fi
done
execute_log cd $curr_dir;
##########################################################################

if [ ! -z $start_title_printed ]
then
  print_title "End install configuration files"
fi
