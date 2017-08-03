#!/bin/bash
#set -x

######################################################################################################
# Script for automatic copy of files by ssh connection
#
# Input Parameters:
# -l LABEL
#  LABEL string will be used to mark the backup files.
#
# -d ORIGIN_DIR
#  Origin file directory
#
# -t FILE_TYPE
#  Type of the files to install (ifw, libraries)
#
# -h DESTINATION_HOSTS_LABEL
#  The label of the hosts to search in the hot_deploy config file
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
#
# -f
# FINAL installation: when this flag is present the files will be moved on done dir after installation.
#
# Exit errors:
# 2: Parameter(s) error
# 3: no files to be installed
# 4: path.config file is missing
# 5: error in ssh.config file
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh

######################################################################################################
# Execute an SSH command to the remote machine
# 1 - ssh user
# 2 - ssh host
# 3 - Command to execute
# 4 - Print error (0 = no errors showed, 1 = all errors showed
#
# Error codes:
# 1 - Undefined
# 2 - File Skipped
#
# The output of the SSH command will be copied on SSH_COMMAND_OUTPUT variable
######################################################################################################
ssh_command () {
	if [ $# -lt 3 ]
	then
		print_error="Missing parameter(s)";
    return 2;
	fi
	
  local ssh_user=$1;
  local ssh_host=$2;
  local ssh_command=$3;

	if [ ! -z $4 ]
	then
		show_errors=$4;
  else
		show_errors=0;
	fi
	
	log "Executing: ssh $ssh_user@$ssh_host $1";

	SSH_COMMAND_OUTPUT=$(ssh $ssh_user@$ssh_host "$ssh_command" 2>>$log_file);
	ssh_result=$?;

	log_variable ssh_result;
	log_variable SSH_COMMAND_OUTPUT;
		
	if [ $ssh_result -ne 0 ] && [ $show_errors -ne 0 ]
	then
    print_error "Error executing command: ssh $ssh_user@$ssh_host $ssh_command. Please check log $log_file";
	fi;
	
	return $ssh_result;
}
######################################################################################################

######################################################################################################
# Execute an SCP command to the remote machine
# 1 - ssh user
# 2 - ssh host
# 3 - origin file with path
# 4 - destination file with path
#
# Error codes:
# 1 - Undefined
# 2 - File Skipped
######################################################################################################
ssh_copy () {
	if [ $# -lt 4 ]
	then
		print_error="Missing parameter(s)";
    return 2;
	fi
	
  local ssh_user=$1;
  local ssh_host=$2;
  local scp_origin_file=$3;
  local scp_dest_file=$4;
	
	command="scp -p $scp_origin_file $ssh_user@$ssh_host:$scp_dest_file";

	log "Executing: $command";
	execute_log scp -p $scp_origin_file $ssh_user@$ssh_host:$scp_dest_file;
	scp_result=$?;

	log "scp_result: $scp_result";

	if [ $scp_result -ne 0 ]
	then
		print_error "Error executing command: $command. Please check log $log_file";
	fi;
	
	return $scp_result;
}
######################################################################################################

######################################################################################################
# Archive a file with the current date
######################################################################################################
ssh_archive () {
  if [ -z $label ]
  then
  	new_name=$1_`date +"%Y%m%d%H%M%S"`
  else
    new_name=${1}"_"${label};
  fi
	
	dirname_new_name=$(dirname $new_name)
	basename_new_name=$(basename $new_name)
	basename_original_name=$(basename $1)
	
	cf ssh_command  $tmp_ssh_user $destination_host "cd $dirname_new_name; tar -cf - $basename_original_name | gzip -c > $basename_new_name.tar.gz;";
  
  echo "$1;$destination_host;$basename_new_name.tar.gz" >> $tmp_backup_history;
}
######################################################################################################

######################################################################################################
# Parse the input parameters
######################################################################################################
restore="no";

result=$(getopt d:h:t:cf "$@")

if [ $? -gt 0 ]
then
	print_error "Error in parameters";
	exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-d) origin_directory="$2"; shift;;
    (-h) destination_hosts_label="$2"; shift;;
    (-t) file_type="$2"; shift;;
    (-c) ONLY_CHECK="TRUE";;
    (-f) FINAL_INSTALL="TRUE";;
    (--) shift; break;;
    (-*) print_error "Unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done

if [ -z $origin_directory ]
then
	print_error "Origin directory parameter -d is mandatory.";
	exit 2;
fi

if [ -z $file_type ]
then
	print_error "File type -t is mandatory.";
	exit 2;
fi

log_variable origin_directory;
log_variable file_type;
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

curr_dir=$(pwd);

script_path="${INSTALLATION_TOOL_HOME}/${script_name}"
origin_path="$PACKAGE_DIR/$origin_directory"
done_dir="$origin_path/done"

path_config_file="$CONFIG_DIR/${file_type}_path.config"
db_conf_file="$CONFIG_DIR/db_pass.txt"

if [ ! -e $origin_path ]
then
  log "origin_path doesn't exists: $origin_path";
	exit 3
fi

log "Executing: ls $origin_path | grep -v \"done\" | grep -v \"\.fict\" | grep -v \"$(basename $path_config_file)\" | wc -l";
if [ $(ls $origin_path | grep -v "done" | grep -v "\.fict" | grep -v "$(basename $path_config_file)" | wc -l) -eq 0 ]
then
  log "No files to install";
	exit 3
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  exit 0;
fi

if [ -z $destination_hosts_label ]
then
	print_error "Destination hosts label -h is mandatory.";
	exit 2;
fi

destination_hosts=(`grep ^$destination_hosts_label $HOT_DEPLOY_CONFIG_FILE | cut -f2 -d"="`);

if [ -z $destination_hosts ] || [ ${#destination_hosts[@]} -eq 0 ]
then
  print_error "Destination hosts array is empty.";
  exit 2;
fi

log_variable destination_hosts;

tmp_log_file=${TMP_DIR}/tmp_fict_log_file.log

tmp_backup_history="${BACKUP_DIR}/${file_type}_history"
ssh_conf="$CONFIG_DIR/${file_type}_ssh.config";

if [ ! -e $ssh_conf ]
then
  print_error "The SSH configuration file is missing ($ssh_conf)";
  exit 5;
fi

create_dir $done_dir
execute_log dos2unix -ascii $ssh_conf $ssh_conf;
######################################################################################################

######################################################################################################
# Archive the old configuration files and copy the new one
######################################################################################################
if [ -e $origin_path/$(basename $path_config_file) ]
then
	execute_log cp -r $origin_path/$(basename $path_config_file) $path_config_file;
	execute_log mv $origin_path/$(basename $path_config_file) $done_dir;
	
	print_installed "$(basename $path_config_file) installed";
fi

if [ ! -e $path_config_file ]
then
	print_error "Error - $(basename $path_config_file) is missing. Please check."
	exit 4;
fi

execute_log dos2unix -ascii $path_config_file $path_config_file;
######################################################################################################

print_title "Start install $file_type"

######################################################################################################
# Prepare the package files
######################################################################################################
execute_log cd $origin_path;

for i in $(ls *.tar.gz 2>$null)
do
	log "Executing: gunzip -c $i 2>$null | tar -xvf -";
	gunzip -c $i 2>$null | tar -xvf - 1>>$log_file 2>>$log_file
	
	if [ $? -eq 0 ]
	then
		execute_log rm $i;
	else	
		print_warning "Unable to unzip $i";
	fi
done

execute_log find . -type f -exec chmod 666 {} \; ;
execute_log find . -type d -exec chmod 777 {} \; ;

execute_log cd $curr_dir;
######################################################################################################

######################################################################################################
# Print a warning in case some files is not in the other_path.config file
######################################################################################################
log "Executing: grep \";\" $path_config_file | grep -v \"^#\"";
grep_command_result=$(grep ";" $path_config_file | grep -v "^#")

printed=0;
log "Executing: ls $origin_path | grep -v \"done\" | grep -v \"\.fict\" | grep -v \"$(basename $path_config_file)\"";
for i in $(ls $origin_path | grep -v "done" | grep -v "\.fict" | grep -v "$(basename $path_config_file)")
do
  log "Executing: echo $grep_command_result | grep $i | wc -l";
	if [ $(echo $grep_command_result | grep $i | wc -l) -eq 0 ]
	then
		if [ $printed -eq 0 ]
		then
			print_warning "Warning - These files will be not installed because they are not present in file $path_config_file";
			printed=1;
		fi
		
		echo "  * "$i;
	else
    add_element_to_array "files_to_install" "${i}";
  fi
done

log_variable files_to_install;

if [ $printed -eq 1 ]
then
	echo "";
fi
######################################################################################################

######################################################################################################
# Install the files
######################################################################################################
for file_to_install in "${files_to_install[@]}"
do
  unset installed;
  log_variable file_to_install;
  
  path_config_line=$(grep ";" $path_config_file | grep -v "^#" | grep $file_to_install);
    
	tmp_file_name=$(echo $path_config_line | cut -f1 -d";")
	tmp_path=$(echo $path_config_line | cut -f2 -d";")
	tmp_dest_file_name=$(echo $path_config_line | cut -f3 -d";")
  tmp_file_class=$(echo $path_config_line | cut -f4 -d";");
	
  log_variable tmp_file_name;
  log_variable tmp_path;
  log_variable tmp_dest_file_name;
  log_variable tmp_file_class;

	if [ -z $tmp_file_name ] 
	then 
		print_error "Error in $path_config_file";
		continue;
	fi	
	
	if [ -z $tmp_path ] || [ -z $tmp_dest_file_name ]
	then 
		print_error "Error in $path_config_file about $tmp_file_name";
    continue;
	fi
  
  if [ "x$tmp_dest_file_name" == "x-" ]
  then
    tmp_dest_file_name=$tmp_file_name
    log "\"-\" found-> Using the source file name as destination file name";
  fi
  
	if [ ! -e $origin_path/$tmp_file_name ]
	then
    log "Origin file not found: $origin_path/$tmp_file_name";
		continue;
	fi

  for destination_host in "${destination_hosts[@]}"
  do
    log_variable destination_host;
    
    # Check if into the destination_host should be installed this tmp_file_class
    log "Executing: grep $tmp_file_class $ssh_conf | grep $destination_host | grep -v \"^#\" | head -1 ";
    ssh_config_line=$(grep $tmp_file_class $ssh_conf | grep $destination_host | grep -v "^#" | head -1);
    if [[ -z $ssh_config_line ]]
    then
      log "Skipping file $tmp_file_name: the class $tmp_file_class should not be installed on the destination host $destination_host";
      continue;
    fi
    
    tmp_ssh_user=$(echo $ssh_config_line | cut -f3 -d";")
    tmp_ssh_home=$(echo $ssh_config_line | cut -f4 -d";")
    
    if [ -z $tmp_ssh_home ]
    then
      print_error "Please configure the home path on file $ssh_conf";
      continue;
    fi
    
    cf ssh_command $tmp_ssh_user $destination_host "ls $tmp_ssh_home";

    if [ $? -ne 0 ]
    then
      print_error "Unable to find the remote home path: $tmp_ssh_home";
      continue;
    fi

    tmp_remote_path=$(echo $tmp_path | sed s@\$IFW_HOME@$tmp_ssh_home@g | sed s@\$PIN_HOME@$tmp_ssh_home@g)
    
    if [ $tmp_file_name == "create_dir" ]
    then
      cf ssh_command $tmp_ssh_user $destination_host "mkdir $tmp_ssh_home/$tmp_dest_file_name";
      installed="true";
      continue;
    fi
    ##################################################################################################

    ##################################################################################################
    # Check the presence of the destination file. 
    # If exist, it will check if the file still exists (identical to file to install) 
    #	and then it will do a backup of the existend file
    ##################################################################################################
    cf ssh_command $tmp_ssh_user $destination_host "ls $tmp_remote_path/$tmp_dest_file_name";
    ssh_command_result=$?;
    
    if [ $ssh_command_result -eq 0 ]
    then
      ##############################################################################################
      # Check for identical files
      ##############################################################################################
      md5_tmp_file=$(digest -a md5 $origin_path/$tmp_file_name)
      
      cf ssh_command $tmp_ssh_user $destination_host "digest -a md5 $tmp_remote_path/$tmp_dest_file_name";
      md5_tmp_dest_file=$SSH_COMMAND_OUTPUT;
      
      if [ ! -z $md5_tmp_dest_file ] 
      then
        if [ $md5_tmp_file == $md5_tmp_dest_file ]
        then
          print_installed "Skipped: $tmp_dest_file_name > $destination_host (identical)";
          installed="true";
          continue;
        else
          log "-- Backup of $tmp_remote_path/$tmp_dest_file_name"
          cf ssh_archive $tmp_remote_path/$tmp_dest_file_name;
        fi
      fi
      ##############################################################################################
    else
      echo "$tmp_file_name;$destination_host;new" >> $tmp_backup_history;
    fi
    
    if [ $(file $origin_path/$tmp_file_name | egrep "text|script" | wc -l) -gt 0 ]
    then
      execute_log dos2unix -ascii $origin_path/$tmp_file_name $origin_path/$tmp_file_name;
    fi
    ##################################################################################################

    ##################################################################################################
    # Finally install the new file
    ##################################################################################################
    cf ssh_copy $tmp_ssh_user $destination_host "$origin_path/$tmp_file_name" "$tmp_remote_path/$tmp_dest_file_name";
    
    copy_result=$?

    if [ $copy_result -eq 0 ]		#No errors
    then
      print_installed "Installed: $tmp_file_name > $destination_host";
      installed="true";
    elif [ $copy_result -eq 2 ]		#File skipped by the user
    then
      print_installed "Skipped: $tmp_file_name > $destination_host (user choice)";
      installed="true";
      continue;
    else							#Other error(s)
      print_error "Error: $tmp_file_name > $destination_host (unable to copy)";
      error="true";
      continue;
    fi
    ##################################################################################################
  done
  
  if [ ! -z $installed ] && [ ! -z $FINAL_INSTALL ]
  then 
    execute_log mv -f $origin_path/$tmp_file_name $done_dir;
  fi
done
######################################################################################################

######################################################################################################
# Tar all the installed files
######################################################################################################
cd $done_dir
for i in $(find * -prune 2>$null | grep -v tar 2>$null)
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
cd $curr_dir
######################################################################################################

if [ -z $installed ]
then
	print_warning "No files installed"
	exit 4
fi

if [ -z $error ]
then
  print_title "End install $file_type"
else
  print_warning "WARNING - Storable classes installation is not completed.";
  exit 6;
fi