#!/bin/bash
#set -x

######################################################################################################
# AUTOMATIC INSTALLATION TOOL SCRIPT
# This script will install all the compatible file into package folder.
#
# Input Parameters:
# -l LABEL
#  LABEL string will be used to mark the backup files.
#
# -r
#  This is an internal parameter that indicate the script is launched by the restore script
#
# -p
#  This is an internal parameter that indicate the script is launched after the pathces installation
#
# Exit errors:
# 0. no errors
# 1. unexpected error
# 2. unable to run some script(s)
# 3. error on permanence configuration file
# 4. lock file presence
# 5. inconsistence of the hot deploy flow
# 6. Error incountered during installation
# 7: missing configuration
# 8: user choice to quit
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

export INSTALLATION_TOOL_HOME=$(echo $(cd $script_path; pwd));
export INSTALLATION_TOOL_PARENT=$(echo $(cd $script_path/..; pwd));
export PACKAGE_DIR="$INSTALLATION_TOOL_PARENT/package";
export SCRIPTS_DIR="$INSTALLATION_TOOL_HOME/scripts"
export CONFIG_DIR="$INSTALLATION_TOOL_HOME/config"
export LIB_DIR="$INSTALLATION_TOOL_HOME/lib"
export TMP_DIR="$INSTALLATION_TOOL_HOME/tmp"
export CURRENT_PHASE=""
export PERMANENCE_CONFIG="$CONFIG_DIR/permanence.config"
export HOT_DEPLOY_CONFIG_FILE="$CONFIG_DIR/hot_deploy.config"
export PIN_CONF_PATH="$CONFIG_DIR/pin.conf";
export INFRANET_CONF_PATH="$CONFIG_DIR/Infranet.properties";

. $SCRIPTS_DIR/utils.sh

lock_file="${script_name}-running.lock"

if [ -e $lock_file ] && [ -z $ignore_lock_file ]
then
	print_warning "The script is used by someone else. Please waiting";
	print_warning "If you are sure there is not other instance of this script, please remove $lock_file file and run the script again";
	exit 4;
fi

######################################################################################################
# Do the needed command befor to run a script
######################################################################################################
prepare_script(){
	prepare_script_error=0;

  execute_log dos2unix $1 $1;
	
	if [ $? -ne 0 ]
	then
		prepare_script_error=1;
	fi
	
	execute_log chmod 777 $1;

	if [ $? -ne 0 ]
	then
		prepare_script_error=1;
	fi
  
  if [ $? -ne 0 ] || [ $prepare_script_error -ne 0 ]
	then
    quit 2 "Unable to run the script $1. Please check writing permissions in the package directory";
	fi
}
######################################################################################################

######################################################################################################
# Launch an install script
# 1 - Script filename
# 2 - Restart message
# 3-9 - Script parameters if needed
######################################################################################################
run_script(){
  log "$@ ($#)";
  
  if [ $# -lt 2 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local script_to_execute="$1";
  local restart_message="$2";
  
  shift;
  shift;

	cf prepare_script $script_to_execute
  
  if [ $restore == "restore" ]
	then
		restore_parameter="-r"
	fi
	
  log "Executing: eval /bin/bash $script_to_execute $restore_parameter $@";
	eval /bin/bash $script_to_execute $restore_parameter $@ 2>> $log_file;
  result=$?;
  
  log_variable result;

	if [ $result -ne 3 ]
	then
		installed=1
    
    if [ $result -ne 0 ]
    then
      run_script_error="true";
    else
      if [ ! "x$restart_message" == "xNONE" ]
      then
        log_variable RESTART_MESSAGES;
        log_variable restart_message;
        cf array_add_if_not_exists RESTART_MESSAGES "${restart_message}";
      fi  
    fi
    
    return $result;
	fi
  
  return 0;
}
######################################################################################################


######################################################################################################
# Check if the script will install some file
#
# Input:
# 1    - Script to check
# 2... - Script parameters
#
# Exit codes:
# 0 - There are at least one files for the script
# 1 - There are not files to be installed with the script
######################################################################################################
check_script_files () {
  if [ $# -lt 1 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local script_to_check="$1";
  
  shift;

	cf prepare_script $script_to_check
  
  log "Executing: eval /bin/bash $script_to_check -c $@";
	eval /bin/bash $script_to_check -c $@ 2>> $log_file;
  result=$?;
  
  log "Script result: $result";

  #The result code for "no files to install" is always 3.
  #The script will install some files only when result code is 0.
  
  if [ $result -eq 3 ]
  then
    return 1;
  elif [ $result -eq 0 ]
  then
    return 0;
  else
    quit 1 "Unexpected result on script check. Please check file log $log_file";
  fi
}

######################################################################################################
# Backup and clean the package directory
######################################################################################################
package_backup(){
  if [ ! -d $PACKAGE_DIR ]
  then
    return 2;
  fi

  if [ $(find $PACKAGE_DIR -type f | wc -l) -eq 0 ]
  then
    return 3;
  fi
  
  package_backup_curr_dir=$(pwd);
  
	execute_log cd $PACKAGE_DIR;
  log "Executing: tar -cf - * 2>>$log_file | gzip -c > $package_backup_name\".tar.gz\" 2>>$log_file";
	tar -cf - * 2>>$log_file | gzip -c > $package_backup_name".tar.gz" 2>>$log_file;
  package_backup_tar_result=$?;
  if [ $package_backup_tar_result -eq 0 ]
  then
    execute_log cd $PACKAGE_DIR && execute_log rm -r *;
  fi

  #execute_log mv $PACKAGE_DIR $package_backup_name;
  #execute_log mv $package_backup_name".tar.gz" $package_backup_dir;
  #cf create_dir $PACKAGE_DIR;

	execute_log cd $curr_dir;

	print_warning "The package directory is backed up on ${package_backup_name}.tar.gz";
}
######################################################################################################

get_current_phase () {
  local current_phase=$(cf get_config_value $PERMANENCE_CONFIG CURRENT_PHASE 0);
  log_variable current_phase;

  cf is_number $current_phase || ( quit 3 "Invalid CURRENT_PHASE in the permanence config file: $PERMANENCE_CONFIG");
  
  echo $current_phase;
}

get_current_installation_type () {
  installation_type=$(cf get_config_value $PERMANENCE_CONFIG INSTALLATION_TYPE "COMPLETE");

  log_variable installation_type;
  
  if [ -z $CURRENT_PHASE ] || [ $CURRENT_PHASE -eq 0 ] || [ "x$installation_type" == "x" ]
  then
    check_installation
    if [ $? -ne 0 ]
    then
      print_message "Installation completed";
      quit 0;
    fi
    
    #If no files will be installed on phase 1 (configuration_files, storable_classes, rateplan_files (retail), zone_maps, header_files, opcode_files, make_files) then the PIN_INSTALL schema is not used. 
    #In this case the steps 0-1-5 will not be performed
    cf check_light_installation || installation_type="LIGHT";
    
    if [ "x$installation_type" == "xLIGHT" ]
    then
      print_message "No files will be installed on PIN_INSTALL schema: light installation will be performed."
      cf ask_yn "if you want to continue" "if you want to force a complete installation" || installation_type="COMPLETE";
    fi
    
    if [ "x$installation_type" == "xLIGHT" ]
    then
      print_message "Light installation start."
    else
      print_message "Complete installation start."
    fi
  fi

  cf save_config_value $PERMANENCE_CONFIG "INSTALLATION_TYPE" "$installation_type";

  log_variable installation_type;
}

######################################################################################################
# Remove the lock file, print the error message (optional) and exit from the script.
#
# Input:
# 1. result code
# 2. error message (optional)
######################################################################################################
quit () {
  if [ $# -lt 1 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi

  if [ $# -ge 2 ]
  then
    print_error $2;
  fi
  
  execute_log rm $lock_file; 
  exit $1;
}
######################################################################################################

######################################################################################################
# Check if a light installation must be performed.
#
# If no files will be installed on phase 1 than the PIN_INSTALL schema is not used. 
# In this case the steps 0-1-5 will not be performed
#
# Exit codes:
# 0 - Complete installation
# 1 - Light installation
######################################################################################################
check_light_installation () {
    cf check_script_files $configuration_files_script -t retail && return 0;
    cf check_script_files $storable_classes_script && return 0;
    cf check_script_files $zone_map_script && return 0;
    cf check_script_files $rate_plan_files_script -t retail && return 0;
    
    return 1;
}
######################################################################################################

######################################################################################################
# Check if there is some file to install
#
# Exit codes:
# 0 - At least one file will be installed
# 1 - No file to install
######################################################################################################
check_installation () {
    cf check_light_installation && return 0;
    
    cf check_script_files $configuration_files_script -t retail && return 0;
    cf check_script_files $storable_classes_script && return 0;
    cf check_script_files $zone_map_script && return 0;
    cf check_script_files $rate_plan_files_script -t retail && return 0;
    cf check_script_files $configuration_files_script -t wholesale && return 0;
    cf check_script_files $rate_plan_files_script -t wholesale && return 0;
    cf check_script_files $other_files_script $other_files_parameters && return 0;
    cf check_script_files $header_files_script $header_files_parameters && return 0;
    cf check_script_files $opcode_files_script $opcode_files_parameters && return 0;
    cf check_script_files $lib_script -d lib_files -t lib && return 0;
    cf check_script_files $sql_script && return 0;
    cf check_script_files $ifw_files_script -d ifw_files -t ifw && return 0;
    
    return 1;
}
######################################################################################################
######################################################################################################
# Perform the steps of the Hot Deploy - phase 1
######################################################################################################
execute_phase_1 () {
  unset error;
  unset installed;
  
  print_warning "The installation will be performed into the schema $INSTALLATION_SCHEMA of $INSTALLATION_SID DB using CM on port $CM_PORT of host $CM_HOST."
  cf ask_yn "if you want to continue" "if you want to quit" || quit 8 ;

  cf run_script $configuration_files_script "Please restart CM and DM" -t retail || error="true";
  cf run_script $storable_classes_script "Please perform the steps in DN to install the custom fields" || error="true";
  cf run_script $zone_map_script "Please restart CM and DM" || error="true";
  
  if [ ! -z $installed ]
  then
    print_warning "Please restart the CM on port $CM_PORT of host $CM_HOST before to continue. Press [Enter] when ready.";
    read -p "";
  fi
  
  cf run_script $rate_plan_files_script "Please restart CM and DM" -t retail -h || error="true";
  cf run_script $header_files_script "NONE" $header_files_parameters || error="true";
  cf run_script $opcode_files_script "NONE" $opcode_files_parameters || error="true";
  cf run_script $make_script "Please restart CM and DM" || error="true";

  if [ -z $error ]
  then
    if [ -z $installed ]
    then
      print_message "No file in the package for this phase. PHASE $CURRENT_PHASE finished with success";
    else
      print_message "PHASE $CURRENT_PHASE finished with success";
    fi
  else
    print_hd_error "PHASE $CURRENT_PHASE finished with errors. Please check and try again."
    return 1;
  fi    
}
######################################################################################################

######################################################################################################
# Perform the steps of the Hot Deploy - phase 3
######################################################################################################
execute_phase_3 () {
  unset error;
  unset installed;

  print_warning "The installation will be performed into the schema $INSTALLATION_SCHEMA of $INSTALLATION_SID DB and into GROUP A environments: ${GROUP_A_HOSTS[@]}."
  print_warning "Wholesale installation will be performed too (if required)."
  cf ask_yn "if you want to continue" "if you want to quit" || quit 8 ;
  
  cf run_script $ifw_files_script "Please restart pipelines and AAA registries" -d ifw_files -t ifw -h "GROUP_A" || error="true";
  cf run_script $lib_script "Please restart CM and DM" -d lib_files -t lib -h "GROUP_A" || error="true";
  
  cf run_script $other_files_script "NONE" $other_files_parameters || error="true";
  cf run_script $sql_script "NONE" || error="true";
  cf run_script $configuration_files_script "Please restart CM and DM" -t wholesale || error="true";
  cf run_script $rate_plan_files_script "Please restart CM and DM" -t wholesale || error="true";

  if [ -z $error ]
  then
    if [ -z $installed ]
    then
      print_message "No file in the package for this phase. PHASE $CURRENT_PHASE finished with success";
    else
      print_message "PHASE $CURRENT_PHASE finished with success";
    fi
  else
    print_hd_error "PHASE $CURRENT_PHASE finished with errors. Please check and try again."
    return 1;
  fi    
}
######################################################################################################

######################################################################################################
# Perform the steps of the Hot Deploy - phase 6
######################################################################################################
execute_phase_6 () {
  unset error;
  unset installed;

  print_warning "The installation will be performed into GROUP B environments: ${GROUP_B_HOSTS[@]}."
  cf ask_yn "if you want to continue" "if you want to quit" || quit 8 ;
  
  cf run_script $ifw_files_script "Please restart pipelines and AAA registries" -d ifw_files -t ifw -h "GROUP_B" -f || error="true";
  cf run_script $lib_script "Please restart CM and DM" -d lib_files -t lib -h "GROUP_B" -f || error="true";

  if [ -z $error ]
  then
    if [ -z $installed ]
    then
      print_message "No file in the package for this phase. PHASE $CURRENT_PHASE finished with success";
    else
      print_message "PHASE $CURRENT_PHASE finished with success";
    fi
  else
    print_hd_error "PHASE $CURRENT_PHASE finished with errors. Please check and try again."
    return 1;
  fi    
}
######################################################################################################

no_action_message () {
  print_message "Starting tool with PHASE $1. No action will be performed by the tool."
  cf ask_yn "$2" "if you want to quit" || quit 8 ;

  cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" $(( $1 + 1 ));
}

######################################################################################################
# Parse the input parameters
######################################################################################################
installation_parameter="patches";
restore="no";

null=/dev/null
new_single_files_dir="$PACKAGE_DIR/installation_tool_files"

if [ $(ls $new_single_files_dir/*.fict 2>$null | wc -l 2>$null) -eq 1 ]
then
	fict_file=$(ls $new_single_files_dir/*.fict)
	fict_base_filename=$(basename $fict_file)
	fict_base_filename_no_ext=$(echo $fict_base_filename | sed -e "s/.fict//g")
	label=${fict_base_filename_no_ext}_`date +"%Y%m%d%H%M%S"`;
fi

result=$(getopt prl: "$@")

if [ $? -gt 0 ]
then
	print_error "$script_name. Error in parameters"
	exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-p) installation_parameter="no_patches";;
    (-r) restore="restore";;
    (-l) export label="$2"; shift;;
    (--) shift; break;;
    (-*) print_error "$script_name. $0: error - unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done

if [ -z $label ]
then
  print_error "Missing mandatory parameter: -l LABEL. Please use a label for this installation (ex. 2014_X11)";
  exit 2;
fi
######################################################################################################

######################################################################################################
# Initialization
######################################################################################################
cd ..;
chmod -R 777 . 1>$null 2>&1;
cd - 1>$null 2>&1;

curr_dir=$(pwd)
current_host_name=$(hostname)
user=$(id | cut -f2 -d"(" | cut -f1 -d")")

export BACKUP_DIR="$INSTALLATION_TOOL_PARENT/backups/backup_${label}"

log_variable installation_parameter

if [ -x $BACKUP_DIR ] && [ ! "x$installation_parameter" == "xno_patches" ]
then
  print_warning "Label \"${label}\" was already used in the past.";
  cf ask_yn "if you are continuing a previous installation" "if you want to quit and choice another label" || quit 2;
fi

log_dir="$INSTALLATION_TOOL_HOME/logs"
cf create_dir $log_dir;
export log_file=${log_dir}/${label}_install.log
touch $log_file;

log_variable SCRIPTS_DIR;
log_variable CONFIG_DIR;
log_variable LIB_DIR;
log_variable TMP_DIR;
log_variable INSTALLATION_TOOL_HOME;
log_variable PACKAGE_DIR;
log_variable BACKUP_DIR;
log_variable log_dir;

export CURRENT_PHASE=$(cf get_current_phase);

if [ ! -r $HOT_DEPLOY_CONFIG_FILE ] && [ ! $installation_parameter == "no_patches" ]
then
  print_error "Unable to read the hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  exit 5;
fi

touch $lock_file;

hosts_config_file="$CONFIG_DIR/hosts.config"

execute_log dos2unix $hosts_config_file $hosts_config_file;
execute_log dos2unix $HOT_DEPLOY_CONFIG_FILE $HOT_DEPLOY_CONFIG_FILE;

cf create_dir $BACKUP_DIR;
cf create_dir $TMP_DIR;

pin_install=0
aaa_install=0
pipeline_install=0

old_single_files_script="$SCRIPTS_DIR/single_files.sh"
new_single_files_script="$new_single_files_dir/single_files.sh"
new_single_files_script_done_dir="$new_single_files_dir/done"

configuration_files_script="$SCRIPTS_DIR/configuration_files.sh"
rate_plan_files_script="$SCRIPTS_DIR/rate_plan_files.sh"
sql_script="$SCRIPTS_DIR/sql.sh"
make_script="$SCRIPTS_DIR/make.sh"
storable_classes_script="$SCRIPTS_DIR/storable_classes.sh"

other_files_parameters="-d other_files -t other"
other_files_script="$SCRIPTS_DIR/single_files.sh"
header_files_parameters="-d header_files -t header"
header_files_script="$SCRIPTS_DIR/single_files.sh"
opcode_files_parameters="-d opcode_files -t opcode"
opcode_files_script="$SCRIPTS_DIR/single_files.sh"

installation_tool_files_parameters="-d installation_tool_files -t tool"
installation_tool_files_script="$SCRIPTS_DIR/single_files.sh"

populate_installation_schema_script="$SCRIPTS_DIR/populate_installation_schema.sh"
populate_pin_schema_script="$SCRIPTS_DIR/populate_pin_schema.sh"
zone_map_script="$SCRIPTS_DIR/zone_maps.sh"
######################################################################################################

execute_log cd ..;

log "Executing: find . -name \".copyarea.db\" | xargs rm -f";
find . -name ".copyarea.db" | xargs rm -f;

execute_log cd $curr_dir;

log "Installation label for backups is: $label";

if [ ! -d $PACKAGE_DIR ]
then
  quit 1 "Unable to find the package dir: $PACKAGE_DIR";
fi

package_backup_dir="$INSTALLATION_TOOL_PARENT/old_packages";
cf create_dir $package_backup_dir;

package_backup_name=$package_backup_dir/"package_"$label;

installed=0
patches=0

log_variable installation_parameter;
log_variable new_single_files_dir;

######################################################################################################
# Update all the released script of the installation tool
######################################################################################################
if [ ! $installation_parameter == "no_patches" ] && [ -e $new_single_files_dir ]
then
	######################################################################################################
	# Updating single_files script if exists a new one
	######################################################################################################
	if [ -e $new_single_files_script ]	|| [ $(ls $new_single_files_dir | grep -v "done" | wc -l) -gt 0 ]
  then
    print_message "Installing installation tool patches.";
  fi
  
	if [ -e $new_single_files_script ]
	then
		patches=1
		
		if [ -e $old_single_files_script ]
		then
			new_name=$old_single_files_script"_"$label
			execute_log cp -p $old_single_files_script $new_name;
			tar -cf - $new_name | gzip -c > $new_name.tar.gz
		fi
		
		execute_log cp -p $new_single_files_script $SCRIPTS_DIR || quit 6 "Unable to install $old_single_files_script";
    print_installed "$old_single_files_script installed";
		
		cf create_dir $new_single_files_script_done_dir
		
		execute_log mv $new_single_files_script $new_single_files_script_done_dir;
	fi
	######################################################################################################

	if [ $(ls $new_single_files_dir | grep -v "done" | wc -l) -gt 0 ]
	then
	
		if [ -z $ignore_lock_file ]
		then
			execute_log rm $lock_file;
		fi

		execute_log cd $installation_tool_files_dir;
		
		cf prepare_script $installation_tool_files_script;
    log "Executing: exec /bin/bash $installation_tool_files_script -l $label $installation_tool_files_parameters";
		exec /bin/bash $installation_tool_files_script -l $label $installation_tool_files_parameters;
	fi
fi
######################################################################################################

######################################################################################################
# Read the hosts.config file to decide what install in the running environment
######################################################################################################
ifw_install=0;

if [ $(cat $hosts_config_file | grep -v "^#" | grep -i $current_host_name | grep -i $user | grep -i PIN | wc -l) -gt 0 ]
then
	pin_install=1
fi

if [ $(cat $hosts_config_file | grep -v "^#" | grep -i $current_host_name | grep -i $user | grep -i AAA | wc -l) -gt 0 ]
then
	ifw_install=1
fi

if [ $(cat $hosts_config_file | grep -v "^#" | grep -i $current_host_name | grep -i $user | grep -i PIPELINE | wc -l) -gt 0 ]
then
	ifw_install=1
fi

if [ $(cat $hosts_config_file | grep -v "^#" | grep -i $current_host_name | grep -i $user | grep -i PROD | wc -l) -gt 0 ]
then
	env_type="PROD"
fi

if [ $(cat $hosts_config_file | grep -v "^#" | grep -i $current_host_name | grep -i $user | grep -i DEV | wc -l) -gt 0 ]
then
	env_type="DEV"
fi

if [ ! -z $env_type ] && [ $env_type == "PROD" ]
then
	ifw_files_script="$SCRIPTS_DIR/single_files_ssh.sh"
  lib_script="$SCRIPTS_DIR/single_files_ssh.sh"
else
	ifw_files_script="$SCRIPTS_DIR/single_files.sh"
	lib_script="$SCRIPTS_DIR/single_files.sh"
fi

log_variable env_type;
######################################################################################################

if [ "x$env_type" == "xPROD" ]
then
  ######################################################################################################
  # HOT DEPLOY START
  ######################################################################################################

  export PIN_CONF_PATH="$CONFIG_DIR/pin.conf";
  export INSTALLATION_SCHEMA=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_SCHEMA") || quit 7 "Unable to find the INSTALLATION_SCHEMA into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  export INSTALLATION_PASS=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_PASS") || quit 7 "Unable to find the INSTALLATION_PASS into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  export INSTALLATION_SID=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "INSTALLATION_SID") || quit 7 "Unable to find the INSTALLATION_SID into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  export PIN_SCHEMA=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "PIN_SCHEMA") || quit 7 "Unable to find the PIN_SCHEMA into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  export PIN_PASS=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "PIN_PASS") || quit 7 "Unable to find the PIN_PASS into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  export PIN_SID=$(cf get_config_value $HOT_DEPLOY_CONFIG_FILE "PIN_SID") || quit 7 "Unable to find the PIN_SID into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  export CM_HOST=$(cf get_pin_conf_host $PIN_CONF_PATH) || quit 7 "Unable to find the cm host from pin.conf file: $PIN_CONF_PATH";
  export CM_PORT=$(cf get_pin_conf_port $PIN_CONF_PATH) || quit 7 "Unable to find the cm port from pin.conf file: $PIN_CONF_PATH";
  
  #WARNING - Array are not exported in this unix bash
  GROUP_A_HOSTS=(`grep ^GROUP_A $HOT_DEPLOY_CONFIG_FILE | cut -f2 -d"="`) || quit 7 "Unable to find the GROUP A HOSTS into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";
  GROUP_B_HOSTS=(`grep ^GROUP_B $HOT_DEPLOY_CONFIG_FILE | cut -f2 -d"="`) || quit 7 "Unable to find the GROUP B HOSTS into hot deploy config file: $HOT_DEPLOY_CONFIG_FILE";

  log_variable PIN_CONF_PATH;
  log_variable INSTALLATION_SCHEMA;
  log_variable INSTALLATION_PASS;
  log_variable INSTALLATION_SID;
  log_variable PIN_SCHEMA;
  log_variable PIN_PASS;
  log_variable PIN_SID;
  log_variable CM_HOST;
  log_variable CM_PORT;
  log_variable GROUP_A_HOSTS;
  log_variable GROUP_B_HOSTS;
  
  cf get_current_installation_type;
  
  while :
  do
    CURRENT_PHASE=$(cf get_current_phase);

    case "$CURRENT_PHASE" in
      (0) 
          if [ "x$installation_type" == "xCOMPLETE"  ]
          then
            print_message "Starting tool with PHASE $CURRENT_PHASE. The $PIN_SCHEMA schema on $PIN_SID DB will be copied into $INSTALLATION_SCHEMA schema on $INSTALLATION_SID DB."
            cf ask_yn "if you want to continue" "if you want to quit" || quit 8;

            cf run_script $populate_installation_schema_script "NONE" || continue;
          fi

          cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" $(( $CURRENT_PHASE + 1 ));
          continue;
          ;;
      (1) 
          if [ "x$installation_type" == "xCOMPLETE" ]
          then
            print_message "Starting tool with PHASE $CURRENT_PHASE. The following items will be installed: configuration_files, storable_classes, rateplan_files (retail), zone_maps, header_files, opcode_files, make_files."
            
            cf execute_phase_1 || continue;
          fi
            
          cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" $(( $CURRENT_PHASE + 1 ));
          continue;
          ;;
      (2) 
          no_action_message "$CURRENT_PHASE" "when GROUP A environments are stopped";
          continue;
          ;;
      (3) 
          print_message "Starting tool with PHASE $CURRENT_PHASE. The following items will be installed: ifw_files, other_files, retail_sql, wholesale_sql, configuration_files_wholesale, rateplan_files_wholesale, libraries (.so)."
    
          cf execute_phase_3 || continue;
          
          cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" $(( $CURRENT_PHASE + 1 ));
          continue;
          ;;
      (4)
          no_action_message "$CURRENT_PHASE" "when GROUP A environments are started on the $INSTALLATION_SCHEMA schema and when GROUP B environments are stopped";
          continue;
          ;;
      (5)
          if [ "x$installation_type" == "xCOMPLETE" ]
          then
            print_message "Starting tool with PHASE $CURRENT_PHASE. The $INSTALLATION_SCHEMA schema on $INSTALLATION_SID DB will be copied into $PIN_SCHEMA schema on $PIN_SID DB."
            cf ask_yn "if you want to continue" "if you want to quit" || quit 8;

            cf run_script $populate_pin_schema_script "NONE" || continue;
          fi

          cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" $(( $CURRENT_PHASE + 1 ));
          continue;
          ;;
      (6)
          print_message "Starting tool with PHASE $CURRENT_PHASE. The following items will be installed: ifw_files, libraries (.so)."
          
          cf execute_phase_6 || continue;
          
          cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" $(( $CURRENT_PHASE + 1 ));
          continue;
          ;;
      (7) 
          no_action_message "$CURRENT_PHASE" "when GROUP B machines are started";
          print_message "The installation is completed";
          
          cf save_config_value $PERMANENCE_CONFIG "CURRENT_PHASE" 0;
          ;;
      (*) print_error "Unrecognized phase $CURRENT_PHASE"; quit 3;;
    esac

    break;

  done
else
  ######################################################################################################
  # Normal installation starts
  ######################################################################################################
  if [ $pin_install -eq 1 ]
  then
    cf run_script $configuration_files_script "Please restart CM and DM" -t retail || error="true";
    cf run_script $storable_classes_script "Please perform the steps in DN to install the custom fields" || error="true";
    cf run_script $zone_map_script "Please restart CM and DM" || error="true";
    cf run_script $rate_plan_files_script "Please restart CM and DM" -t retail || error="true";

    cf run_script $configuration_files_script "Please restart CM and DM" -t wholesale || error="true";
    cf run_script $rate_plan_files_script "Please restart CM and DM" -t wholesale || error="true";

    cf run_script $other_files_script "NONE" $other_files_parameters || error="true";
    cf run_script $header_files_script "NONE" $header_files_parameters || error="true";
    cf run_script $opcode_files_script "NONE" $opcode_files_parameters || error="true";
    cf run_script $make_script "Please restart CM and DM" || error="true";
    cf run_script $lib_script "Please restart CM and DM" -d lib_files -t lib || error="true";
    
    if [ ! "x$restore" == "xrestore" ]
    then
      cf run_script $sql_script "NONE" || error="true";
    fi
  fi

  if [ $ifw_install -eq 1 ]
  then
    cf run_script $ifw_files_script "restart pipelines and AAA registry and " -d ifw_files -t ifw || error="true";
  fi

  if [ $installed -eq 0 ]
  then
    print_installed "No files to be installed found into package";
  fi

fi

for message in "${RESTART_MESSAGES[@]}"
do
  print_warning $message;
done

if [ -z $run_script_error ]
then
  cf package_backup;
else
  print_warning "The installation is not completed due to some error(s). Please execute the install script again";
fi

execute_log mv *.log $log_dir;

if [ -z $ignore_lock_file ]
then
	execute_log rm $lock_file;
fi
######################################################################################################
