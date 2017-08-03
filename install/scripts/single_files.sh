#!/bin/bash
#set -x

######################################################################################################
# Script for automatic installation of script anc JAR files
# mario.lagana and simone.tancioni
# 7.3.1 Upgrade - 2012-02-22
#
# Input Parameters:
# -l LABEL
#  LABEL string will be used to mark the backup files.
#
# -r
#  This is an internal parameter that indicate the script is launched by the restore script
#
# -t FILE_TYPE
#  File type to install (tool|ifw|others|opcodes)
#
# -d ORIGIN_DIR
#  Origin file directory
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
#
# Input parameters:
# 1. Origin file directory
# 2. File type to install (tool|ifw|others)
# 3. Restore parameter (optional). This will be used during restore phase.
#
# Exit errors:
# 2: Parameter(s) error
# 3: no files to be installed
# 4: path.config file is missing
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh

######################################################################################################
# Archive a file with the current date
######################################################################################################
archive () {
	new_name=$1_`date +"%Y%m%d%H%M%S"`
	
	dirname_new_name=$(dirname $new_name)
	basename_new_name=$(basename $new_name)
	basename_original_name=$(basename $1)
	
	cd $dirname_new_name
	tar -cf - $basename_original_name | gzip -c > $basename_new_name.tar.gz
	cd $curr_dir
	
	if [ $restore == "no" ]
	then
		echo "$1;$new_name.tar.gz" >> $tmp_backup_history;
	fi
}
######################################################################################################

######################################################################################################
# This will execute the installation tool script after the tool patches
######################################################################################################
execute_parent_script(){
	if [ $file_type == "tool" ]
	then
		cd $INSTALLATION_TOOL_HOME;
		execute_log dos2unix -ascii $parent_sctipt $parent_sctipt;
		execute_log chmod +x $parent_sctipt;
		
		log "Executing: exec $parent_sctipt -p -l $label";
		exec $parent_sctipt -p -l $label;
	fi
}
######################################################################################################

######################################################################################################
# Parse the input parameters
######################################################################################################
restore="no";

result=$(getopt l:rt:d:i:c "$@")

if [ $? -gt 0 ]
then
	print_error "$script_name. Error in parameters"
	exit 2;
fi

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-l) label="$2"; shift;;
    (-r) restore="restore";;
    (-t) file_type="$2"; shift;;
    (-d) origin_directory="$2"; shift;;
    (-c) ONLY_CHECK="TRUE";;
    (--) shift; break;;
    (-*) print_error "$0: error - unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done

if [ -z $file_type ]
then
	print_error "File type parameter -t is mandatory.";
	exit 2;
fi

if [ -z $label ] && [ "x$file_type" == "xtool" ]
then
	print_error "Label parameter -l is mandatory for tool file type.";
	exit 2;
fi

if [ -z $origin_directory ]
then
	print_error "Origin directory parameter -d is mandatory.";
	exit 2;
fi
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

curr_dir=$(pwd)
parent_sctipt="install.sh"
origin_path="$PACKAGE_DIR/$origin_directory"
done_dir="$origin_path/done"

if [ ! -e $origin_path ]
then
	exit 3
fi

path_config_file="$CONFIG_DIR/${file_type}_path.config"
new_path_config_file="$origin_path/${file_type}_path.config"
db_conf_file="$CONFIG_DIR/db_pass.txt"

if [ $(ls $origin_path | grep -v "done" | grep -v "$(basename $path_config_file)" | wc -l) -eq 0 ]
then
	execute_parent_script;
	exit 3
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  exit 0;
fi

create_dir $done_dir

tmp_log_file=${TMP_DIR}/tmp_fict_log_file.log
tmp_backup_history="${BACKUP_DIR}/${file_type}_history"
######################################################################################################

######################################################################################################
# Archive the old files and copy the new one
######################################################################################################
if [ -e $new_path_config_file ]
then
	execute_log cp -r $new_path_config_file $path_config_file;
  cp_result=$?;
  if [ $cp_result -ne 0 ]
  then
    print_error "Unable to install $new_path_config_file";
    exit 6;
  fi

  print_installed "$path_config_file installed";
  execute_log mv $new_path_config_file $done_dir;
fi

execute_log dos2unix -ascii $path_config_file $path_config_file;

if [ ! -e $path_config_file ]
then
	print_error "Error - $path_config_file is missing. Please check."
	exit 4;
fi
######################################################################################################

grep_command_result=$(grep ";" $path_config_file | grep -v "^#")

if [ $file_type == "tool" ]
then
	restore="no";
fi
######################################################################################################

######################################################################################################
# run the fict files for tool purpose
######################################################################################################
run_fict_files(){
	if [ ! -e $db_conf_file ]
	then
		log "Unable to run fict file, the $db_conf_file is missing";
		return;
	fi

	SID=$(grep -i "|PIN|" $db_conf_file | grep DB_CATALOG | cut -f2 -d"|");

	USER=$(grep -i "|ACSCONFIG|" $db_conf_file | grep DB_CATALOG | cut -f3 -d"|");
	PASS=$(grep -i "|ACSCONFIG|" $db_conf_file | grep DB_CATALOG | cut -f4 -d"|");
	
	log_variable SID;
	log_variable USER;
	log_variable PASS;

	for i in $(ls -1 $origin_path/*.fict 2>$null)
	do
		cf empty_file $tmp_log_file;
		
		temp_fict_file=$i"_cleaned";
		
		log "Executing: cat $i | sed 's/&//g' > $temp_fict_file 2>$tmp_log_file";
		cat $i | sed 's/&//g' > $temp_fict_file 2>$tmp_log_file;
		
		if [ $? -ne 0 ]
		then
			log "Errors in clean fict file";
			cat $tmp_log_file >> $log_file;
		fi

		sqlplus $USER/$PASS@$SID @$temp_fict_file 1>$tmp_log_file 2>$tmp_log_file
		
		if [ $(cat $tmp_log_file | grep ORA | wc -l) -gt 0 ]
		then
			log "Errors in fict file";
			cat $tmp_log_file >> $log_file;
		fi
		
		execute_log mv -f $i $done_dir;
		execute_log rm $temp_fict_file;
	done
}
######################################################################################################
if [ "$file_type" != "tool" ]
then
	print_title "Start install $file_type files"
else
	run_fict_files;
fi

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

execute_log cd $curr_dir;

check=0

######################################################################################################
# Print a warning in case some files is not in the other_path.config file
######################################################################################################
printed=0;
for i in $(ls $origin_path | grep -v "done" | grep -v "\.fict" | grep -v "$(basename $path_config_file)")
do
	if [ $(echo $grep_command_result | grep $i | wc -l) -eq 0 ]
	then
		if [ $printed -eq 0 ]
		then
			print_warning "Warning - These files will be not installed because they are not present in file $path_config_file";
			printed=1;
		fi
		
		echo "  * "$i;
	fi
done

if [ $printed -eq 1 ]
then
	echo "";
fi
######################################################################################################

######################################################################################################
# Install the files
######################################################################################################
for i in $grep_command_result
do
	tmp_file_name=$(echo $i | cut -f1 -d";")
	tmp_path=$(echo $i | cut -f2 -d";" | sed s@\$PIN_HOME@$PIN_HOME@g | sed s@\$IFW_HOME@$IFW_HOME@g)
	tmp_dest_file_name=$(echo $i | cut -f3 -d";" | sed s@\$PIN_HOME@$PIN_HOME@g | sed s@\$IFW_HOME@$IFW_HOME@g)
	
  log_variable tmp_file_name
  log_variable tmp_path
  log_variable tmp_dest_file_name
  log_variable origin_path
  log_variable tmp_file_name
  
	if [ -z $tmp_file_name ] 
	then 
		continue;
	fi	
	
	if [ -z $tmp_path ] || [ -z $tmp_dest_file_name ]
	then 
		print_error "Error in $path_config_file about $tmp_file_name";
	fi
	
	if [ $(echo "$tmp_path" | grep "^/" | wc -l) -eq 0 ]
	then
		print_error "Error in $path_config_file about destination path of $tmp_file_name";
	fi
  
  if [ "x$tmp_dest_file_name" == "x-" ]
  then
    tmp_dest_file_name=$tmp_file_name
    log "\"-\" found-> Using the source file name as destination file name";
  fi
	
	if [ "x$tmp_file_name" == "xcreate_dir" ]
	then
		cf create_dir $tmp_path/$tmp_dest_file_name
		
		if [ $? -eq 0 ]
		then
			print_installed "Directory $tmp_path/$tmp_dest_file_name created.";
		else
			log "Directory $tmp_path/$tmp_dest_file_name already exists";
		fi
		
		continue;
	fi
	
	if [ ! -e $origin_path/$tmp_file_name ]
	then
    log "Skipping because the source file doen't exists.";
		continue;
	fi

	if [ $tmp_dest_file_name == $script_path ]
	then
		print_warning "Warning - You are trying to replace this script while is executing. Skipping";
		continue;
	fi
	
	check=1
		
	##################################################################################################
	# Check the presence of the detination dir. 
	# If doesn't exist, the script will create the directory
	##################################################################################################
	if [ ! -e $tmp_path ]
	then
		create_dir $tmp_path;
		if [ $? -eq 0 ]
		then
			log "Directory $tmp_path/$tmp_dest_file_name created.";
		else
			log "Error creating dir $tmp_path/$tmp_dest_file_name already exists";
		fi
	fi
	##################################################################################################
	
	##################################################################################################
	# Check the presence of the destination file. 
	# If exist, it will check if the file still exists (identical to file to install) 
	#	and then it will do a backup of the existend file
	##################################################################################################
	if [ -e $tmp_path/$tmp_dest_file_name ]
	then
		##############################################################################################
		# Check for identical files
		##############################################################################################
		md5_tmp_file=$(digest -a md5 $origin_path/$tmp_file_name)
		md5_tmp_dest_file=$(digest -a md5 $tmp_path/$tmp_dest_file_name)
		
		if [ $md5_tmp_file == $md5_tmp_dest_file ]
		then
			if [ $tmp_dest_file_name == $parent_sctipt ]
			then
				execute_log mv $origin_path/$tmp_file_name $done_dir;
				continue;
			fi
			
			print_installed "Skipping $tmp_dest_file_name (identical)";
			execute_log mv -f $origin_path/$tmp_file_name $done_dir;
			continue;
		else
			log "-- Backup of $tmp_path/$tmp_dest_file_name"
			cf archive $tmp_path/$tmp_dest_file_name;
		fi
		##############################################################################################
	else
  	if [ $restore == "no" ]
    then
      echo "$tmp_file_name;new" >> $tmp_backup_history;
    fi
  fi
  
	##################################################################################################
	# Managing the release of directory (not recommended but supported)
	##################################################################################################
	if [ -d $tmp_path/$tmp_dest_file_name ]
	then
		execute_log mv -f $tmp_path/$tmp_dest_file_name $tmp_path/${tmp_dest_file_name}_to_remove;
		print_warning "Directory $tmp_dest_file_name is backed up and also moved in $tmp_path/${tmp_dest_file_name}_to_remove directory";
	##################################################################################################
	# Convert scripts with dos2unix command
	##################################################################################################
	elif [ $(file $origin_path/$tmp_file_name | egrep "text|script" | wc -l) -gt 0 ]
	then
		execute_log dos2unix -ascii $origin_path/$tmp_file_name $origin_path/$tmp_file_name;
	fi
	##################################################################################################

	##################################################################################################
	# Finally install the new file
	##################################################################################################
	execute_log cp -r $origin_path/$tmp_file_name $tmp_path/$tmp_dest_file_name;
	
	##################################################################################################
	# Make scripts executable
	##################################################################################################
  if [ $(echo $tmp_file_name | grep "sh$" | wc -l) -gt 0 ]
	then
		execute_log chmod +x $tmp_path/$tmp_dest_file_name;
	fi
	##################################################################################################
  
	copy_result=$?

	if [ $copy_result -eq 0 ]
	then
		execute_log mv -f $origin_path/$tmp_file_name $done_dir;

		print_installed "$tmp_file_name installed";
	else
		print_error "Error copying $tmp_file_name";
    error="true";
	fi
	##################################################################################################
done
######################################################################################################

if [ $check -eq 0 ]
then
	log "No files to install"
	cf execute_parent_script;
	exit 3
fi

if [ ! $file_type == "tool" ]
then
  if [ -z $error ]
  then
  	print_title "End install $file_type files"
  else
    print_warning "WARNING - $file_type installation is not completed.";
    exit 6;
  fi
fi

cf execute_parent_script;
