#!/bin/bash
#set -x

######################################################################################################
# Script for automatic installation of Pricelist files
#
# Input Parameters:
# -l LABEL
#  LABEL string will be used to mark the backup files.
#
# -c
# "Only check mode" - The script will just check if some file will be installed, without perform any other action
#
# -r
#  This is an internal parameter that indicate the script is launched by the restore script
#
# -t TYPE
#  File type to install <wholesale|retail|all>
#
# -b
#  This flag activate the bakup mode: the backup of the calague. the TYPE parameter is used here too.
#
# -h
#  This flag activate the hot deploy mode: the DB connection will be performed on $INSTALLATION_SCHEMA
#
# Exit errors:
# 2: Parameter error
# 3: No files to be installed
######################################################################################################

script_name=$(basename $0)
script_path=$(dirname $0)

. $script_path/utils.sh
. $script_path/sql-utils.sh

export NLS_LANG=AMERICAN_AMERICA.WE8MSWIN1252
merge_products_number=25;

######################################################################################################
# Parse the input parameters
######################################################################################################
restore="no";
backup_mode="no";

result=$(getopt hrt:bc "$@")

if [ $? -gt 0 ]
then
	print_error "Error in parameters"
	exit 2;
fi

log_variable ONLY_CHECK;

set -- $result
while [ $# -gt 0 ]
do
    case "$1" in
    (-r) restore="restore";;
    (-b) backup_mode="yes";;
    (-t) installation_parameter="$2"; shift;;
    (-h) hot_deploy_mode="yes";;
    (-c) ONLY_CHECK="TRUE";;
    (--) shift; break;;
    (-*) print_error "Unrecognized option $1"; exit 2;;
    (*)  break;;
    esac
    shift
done

log_variable ONLY_CHECK;

if [ -z $installation_parameter ]
then
	print_error "$script_name. File type parameter -t is mandatory: <wholesale|retail|all>";
	exit 2;
elif [ ! $installation_parameter == "wholesale" ] && [ ! $ifw_file_type == "retail" ] && [ ! $ifw_file_type == "all" ]
then
	print_error "$script_name. File type parameter not recognized <wholesale|retail|all>.";
	exit 2;
fi

load_pricelist_classpath_retail="${PIN_HOME}/jars/loadpricelist.jar:${PIN_HOME}/jars/xerces.jar:${PIN_HOME}/jars/pcm.jar:${PIN_HOME}/jars/pcmext.jar:$CONFIG_DIR";
log_variable load_pricelist_classpath_retail
######################################################################################################

######################################################################################################
# Check if in a file log there is a word of "success"
# If yes move the file into a directory, else print a error message
# 1 - log file name to check
# 2 - file installed
# 3 - directory to move the success files
# 4 - word of success to be searched
#
# Exit errors:
# 0. No errors
# 1. Unexpected error
######################################################################################################
check () {
	if [ ! $(grep $4 $1 | wc -l) -gt 0 ] 
	then
		return 1;
	fi
	
	filename=$(basename $2)
	filename_no_ext=$(echo $filename | cut -f1 -d".")
	execute_log mv $2 $3;
	
	execute_log cd $3;
	execute_log zip "$filename_no_ext.zip" "$filename";
	
	if [ $? -eq 0 ]
	then
		execute_log rm "$filename";
	else	
		print_warning "Unable to compress $filename";
	fi
	
	execute_log cd $curr_dir;
}
######################################################################################################

paging () {
	##################################################################################################
	# Backup the field separator
	##################################################################################################
	OLD_IFS=$IFS
	IFS=$'\n'
	##################################################################################################

	tmp_paging_file="$TMP_DIR/paging.tmp"
	log "Executing: > $tmp_paging_file";
	> $tmp_paging_file
	max_chars=0
	
	for i in $(cat $1)
	do
		prod_name=$(echo $i | cut -f2 -d"|")
		curr_chars=$(echo $prod_name | wc -m)
		
		if [ $curr_chars -gt $max_chars ]
		then
			max_chars=$curr_chars
		fi
	done

	for i in $(cat $1)
	do
		mod_date=$(echo $i | cut -f1 -d"|")
		prod_name=$(echo $i | cut -f2 -d"|")
		prod_file=$(echo $i | cut -f3 -d"|")
		
		echo -n "$mod_date    $prod_name" >> $tmp_paging_file

		difference=$(($max_chars - $(echo $prod_name | wc -m)))
		
		while [ $difference -gt 0 ]
		do
		   echo -n " " >> $tmp_paging_file
		   difference=`expr $difference - 1`
		done
		
		log_variable difference;
		
		echo -n -e $prod_file"\n" >> $tmp_paging_file
	done
	
	log "Executing: cat $tmp_paging_file > $1";
	cat $tmp_paging_file > $1
	
	##################################################################################################
	# Restore the field separator
	##################################################################################################
	IFS=$OLD_IFS
	##################################################################################################
}

######################################################################################################
# Unzip dir recursively
######################################################################################################
unzip_dir() {
	execute_log cd $1;
	
	log "Executing: find * -type d -prune 2>>$log_file | grep -v done 2>>$log_file";
	for i in $(find * -type d -prune 2>>$log_file | grep -v done 2>>$log_file)
	do
		cf unzip_dir $i;
	done

	for i in $(ls *.zip 2>>$log_file)
	do
		execute_log /usr/bin/unzip -jo $i;
		if [ $? -eq 0 ]
		then
			execute_log rm -f $i;
		else
			log "## Unable to unzip $i";
		fi
	done
	
	execute_log cd ..;
}
######################################################################################################

######################################################################################################
# Load a pricelist with entire catalogue
# 1. Backup catalogue type (retail|wholesale)
######################################################################################################
backup_catalogue(){
  if [ $# -ne 1 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
	print_installed "Retrieving catalogue for backup - This will take some minutes, if no error showed the script is still running.";
  backup_log="backup_log.tmp"

  if [ "x$1" == "xretail" ]
  then
    local backup_filename=$backup_catalogue_e2e_file;
		execute_log cd $pin_conf_e2e_dir;
		
		log "Executing: > $backup_log";
		> $backup_log;
		
		log "Executing: java -cp \"$load_pricelist_classpath_retail\" com.portal.loadpricelist.LoadPriceList -rf \"$backup_filename\"";
    java -cp "$load_pricelist_classpath_retail" com.portal.loadpricelist.LoadPriceList -rf "$backup_filename" 1>>$backup_log 2>>$backup_log 3>>$backup_log;
    loadpricelist_resultcode=$?;
  elif [ "x$1" == "xwholesale" ]
  then
    local backup_filename=$backup_catalogue_mho_file;
		execute_log cd $mho_dir;
		
		cf empty_file $backup_log;
		
		log "Executing: loadpricelistMHO -rf \"$backup_filename\" 1>>$backup_log 2>>$backup_log 3>>$backup_log";
		loadpricelistMHO -rf "$backup_filename" 1>>$backup_log 2>>$backup_log 3>>$backup_log
    loadpricelist_resultcode=$?;
  else
    print_error "Backup catalogue type is not valid: $2";
    return 2;
  fi
	
	if [ $loadpricelist_resultcode -eq 130 ]
	then
		print_warning "Backup interrupted";
	else
		execute_log mv "$backup_log" "$TMP_DIR";
		
		log "Executing: cat $TMP_DIR/$backup_log >> $log_file";
		cat $TMP_DIR/$backup_log >> $log_file;
		
		if [ $(cat $TMP_DIR/$backup_log | wc -l) -gt 0 ]
		then
			print_error "Errors during backup of the catalogue $2";
		fi
	fi

  execute_log cd $curr_dir;
}
######################################################################################################

######################################################################################################
# Use the Java script to extract the object backup from catalogue
#
# 1. File name to backup
# 2. Object type (product|deal|plan)
# 3. Catalogue type (retail|wholesale)
######################################################################################################
backup_object(){
	if [ $3 == "retail" ]
	then
		local backup_catalogue_file=$backup_catalogue_e2e_file
		local backup_object_dir=$backup_dir_e2e
	elif [ $3 == "wholesale" ]
	then
		local backup_catalogue_file=$backup_catalogue_mho_file
		local backup_object_dir=$backup_dir_mho
	else
		print_error "Parameter not recognized: $3";
		exit 3;
	fi
	
	output_base_filename=$(basename $1)

	log "Backup of $output_base_filename from the catalogue file";

	log "Executing: > $tmp_file_log"; 
	empty_file $tmp_file_log;
	
	output_filename=$backup_object_dir/$2s/$output_base_filename
	
	#/**
	# * This Java script will extract an xml object from a multiple object file to a single object file.
	# * Will be used to copy a deal, product or plan to from a pricelist to another.
	# * 
	# * Input parameters:
	# * 1. Input file name
	# * 2. Object type (eg: product, deal or plan)
	# * 3. Object file name
	# * 4. Output file name (this file will contain the object choosed)
	# * 
	# * Error exit code:
	# * 1: Unexpected error
	# * 2: Missing parameter(s)
	# * 3: Object not found
	# * 4: Unable to read object file
	# * 5: Unable to create the output file
	# * 6: Unable to create log file
	# * 7: Unable to read input file
	# * @param args
	# */
	log "Executing: java -jar $price_list_manager backup $backup_catalogue_file $2 \"$1\" \"$output_filename\" 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
	java -jar $price_list_manager backup $backup_catalogue_file $2 "$1" "$output_filename" 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log
		
	result=$?;
	
	if [ $result -eq 0 ]
	then
		execute_log cd $BACKUP_DIR/$3/$2s/;
		output_basefilename=$(basename $output_filename);
		output_basefilename_no_ext=$(echo $output_basefilename | cut -f1 -d".")

		execute_log zip  "$output_basefilename_no_ext.zip" "$output_basefilename";
		
		if [ $? -eq 0 ]
		then
			execute_log rm $output_basefilename;
		else	
			print_warning "Unable to compress $output_basefilename";
		fi

		execute_log cd $curr_dir;
	elif [ $result -eq 3 ]
	then
		log "Product is new $1. No backup needed";
	else
		log "## Errors during backup of $1. Please check log $script_dir/$log_file";
	fi

	log "Executing: cat $tmp_file_log >> $log_file";
	cat $tmp_file_log >> $log_file;
}

######################################################################################################
# loadpricelist
# 1. Files directory
# 2. Files type (product|deal|plan)
# 3. DB type (retail|wholesale)
# 4. Done files directory
######################################################################################################
merge_install_pricelists() {
	if [ $(ls $1/*.xml 2>>$null | wc -l ) -eq 0 ]
	then
		return 3;
	fi

	message="$3 $2s";
	
	execute_log cp -p $xsd_path $1;
	print_title "Installing $message. This will take time";
	
	installed=0
	
	log "Merging directory $1";
	
	merged_done_dir=$4"/merged";
	merge_out_dir=${1}"_merged";
	merge_out_file_name=${2}"_merge_file"
	
	if [ -d $merge_out_dir ]
	then
		execute_log rm -r $merge_out_dir;
		execute_log mkdir $merge_out_dir;
	else
		execute_log mkdir $merge_out_dir;
	fi
	
	execute_log mkdir $merged_done_dir;
	
	execute_log cp -p $xsd_path $merge_out_dir;

	log "Executing: > $tmp_file_log";
	cf empty_file $tmp_file_log;
	
	#/**
	# * This java script will create a "big" rate plan file with inside more xml with
	# * the same object type (product|deal|plan) 
	# * 
	# * Input parameters:
	# * 1. Object type (product|deal|plan) 
	# * 2. Input directory (the files will be picked up alphabetically)
	# * 3. Output directory
	# * 4. Output file name for the output file (output_name_xx.xml - xx will be a crescent number)
	# * 5. Number of the objects for file
	# * 
	# * Error exit code:
	# * 1: Unexpected error
	# * 2: Missing parameter(s)
	# * 3: Input directory not found
	# * 4: No files into input directory
	# * 5: Unable to read input file
	# * 6: Unable to create output file
	# * 7: An input file contains the wrong object
	# * @author mario.lagana
	# */
	log "java -jar $price_list_manager merge_pricelist $2 $1 $merge_out_dir ${merge_out_file_name} $merge_products_number 1>$tmp_file_log 2>$tmp_file_log 3>$tmp_file_log";
	java -jar $price_list_manager merge_pricelist $2 $1 $merge_out_dir ${merge_out_file_name} $merge_products_number 1>$tmp_file_log 2>$tmp_file_log 3>$tmp_file_log
	
	merge_result=$?;

	log "Executing: cat $tmp_file_log >> $log_file";
	cat $tmp_file_log >> $log_file;
	
	for i in $(ls $merge_out_dir/*.xml 2>>$null)
	do
		installed=1
		merged_basename_file=$(basename $i)
		log "-- Installing: $merged_basename_file";

		current_filename_no_ext=$(echo $i | sed -e "s/.xml//g")

		for single_pricelist in $(cat ${current_filename_no_ext}.info | cut -f1 -d";")
		do
			backup_basename_file=$(basename $single_pricelist)
			
			log "-- Backup of: $backup_basename_file";
			installed=1
			execute_log dos2unix $single_pricelist $single_pricelist;
			
			#backup_object "$single_pricelist" "$2" "$3";
		done
		
		log "Executing: > $tmp_file_log";
		> $tmp_file_log;
		
		if [ $2 == "deal" ] || [ $2 == "plan" ]
		then
			log "-- Cleaning transitions: $merged_basename_file";
			
			clean_input_file=$i;
			clean_output_file=$TMP_DIR/$merged_basename_file;

			#/**
			# * This java script will clean all transitions from a deal or plan xml.
			# * 
			# * Input parameters:
			# * 1. Input xml file name
			# * 1. Output xml file name
			# * 
			# * Error exit code:
			# * 1: Unexpected error
			# * 2: Missing parameter(s)
			# * 3: No transitions
			# * 4: Unable to read input file
			# * 5: Unable to create output file
			# * @author mario.lagana
			# *
			# */
			log "java -jar $price_list_manager transitions_clean $clean_input_file $clean_output_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
			java -jar $price_list_manager transitions_clean $clean_input_file $clean_output_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log
		
			execute_log cat $tmp_file_log >> $log_file;
			
			file_to_install=$clean_output_file
			
			transitions_install="yes";
		else
			file_to_install=$i
		fi
		
		log "Executing: > $tmp_file_log";
		> $tmp_file_log;

		log "-- Sending to system: $merged_basename_file";
		
		if [ $3 == "retail" ]
		then
      log "Executing: java -cp \"$load_pricelist_classpath_retail\" com.portal.loadpricelist.LoadPriceList -cf \"$file_to_install\"";
      java -cp "$load_pricelist_classpath_retail" com.portal.loadpricelist.LoadPriceList -cf "$file_to_install" 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log;
		elif [ $3 == "wholesale" ]
		then
			log "Executing: loadpricelistMHO -cf $file_to_install 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
			loadpricelistMHO -cf $file_to_install 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log;
		else
			print_error "Error - Wrong parameter: $3.";
			continue;
		fi
		
		log "Executing: cat $tmp_file_log >> $log_file";
		cat $tmp_file_log >> $log_file;
		cf check "$tmp_file_log" "$i" "$merged_done_dir" "successfully";
		
		check_result=$?;
		
		execute_log zip "$merged_done_dir/$(basename $i).zip" "$merged_done_dir/$(basename $i)";
		
		if [ $check_result -eq 0 ]
		then
			for single_pricelist in $(cat ${current_filename_no_ext}.info | cut -f1 -d";")
			do
				installed_basefilename=$(basename $single_pricelist)
				filename_no_ext=$(echo $installed_basefilename | cut -f1 -d".")
				execute_log mv $single_pricelist $4;
				
				execute_log cd $4;
				execute_log zip "$filename_no_ext.zip" "$installed_basefilename";
				
				if [ $? -eq 0 ]
				then
					execute_log rm "$installed_basefilename";
				else	
					log "WARNING - Unable to compress $installed_basefilename";
				fi

				execute_log cd $curr_dir;
				
				print_installed "$(basename $single_pricelist) installed with no errors";
			done
			
			execute_log mv ${current_filename_no_ext}.info $merged_done_dir;
		elif [ $check_result -eq 1 ]
		then
			log "## File $i is not installed. The single files will be reprocessed at the end. ";
		fi
		
		execute_log rm $file_to_install;
	done
	
	execute_log rm -r $merge_out_dir;

	if [ $(ls $1/*.xml 2>>$null | wc -l ) -gt 0 ]
	then 
		return 0;
	else
		if [ ! $installed -eq 1 ]
		then
			log "No merged $message to install";
			print_installed "No merged $message to install";
		fi
	fi

	return 0;
}
######################################################################################################

######################################################################################################
# loadpricelist
# 1. Files directory
# 2. Files type (product|deal|plan)
# 3. DB type (retail|wholesale)
# 4. Done files directory
######################################################################################################
install_pricelists() {
  if [ $# -ne 4 ]
  then
    print_error "Missing paramters";
    return 2;
  fi

	if [ $(ls $1/*.xml 2>>$log_file | wc -l ) -eq 0 ]
	then
		return 3;
	fi

	message="$3 $2s";
	
	execute_log cp -p $xsd_path $1;
	
	if [ $installed -eq 0 ]
	then
		print_title "Installing $message. This will take time";
	fi
	
	installed=0

	for i in $(ls $1/*.xml 2>>$null)
	do
		basename_file=$(basename $i)
		
		installed=1
		log "-- Installing: $basename_file";
		execute_log dos2unix $i $i;
		
		backup_object "$i" "$2" "$3";

		log "Executing: > $tmp_file_log";
		> $tmp_file_log;
		
		if [ $2 == "deal" ] || [ $2 == "plan" ]
		then
			log "-- Cleaning transitions: $basename_file";
			
			clean_input_file=$i;
			clean_output_file=$TMP_DIR/$basename_file;

			log "Executing: java -jar $price_list_manager transitions_clean $clean_input_file $clean_output_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
			java -jar $price_list_manager transitions_clean $clean_input_file $clean_output_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log
		
			log "Executing: cat $tmp_file_log >> $log_file"
			cat $tmp_file_log >> $log_file;
			
			file_to_install=$clean_output_file
			transitions_install="yes";
		else
			file_to_install=$i
		fi
		
		empty_file $tmp_file_log;

		log "-- Sending to system: $basename_file";
		
		if [ $3 == "retail" ]
		then
      log "Executing: java -cp \"$load_pricelist_classpath_retail\" com.portal.loadpricelist.LoadPriceList -cf \"$file_to_install\"";
      java -cp "$load_pricelist_classpath_retail" com.portal.loadpricelist.LoadPriceList -cf "$file_to_install" 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log;
		elif [ $3 == "wholesale" ]
		then
			log "Executing: loadpricelistMHO -cf $file_to_install 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
			loadpricelistMHO -cf $file_to_install 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log;
		else
			print_error "Error - Wrong parameter: $3.";
			continue;
		fi
		
		log "Executing: cat $tmp_file_log >> $log_file";
		cat $tmp_file_log >> $log_file;
		cf check "$tmp_file_log" "$i" "$4" "successfully";
		
		check_result=$?;
		
		execute_log zip  "$4/$(basename $i).zip" "$4/$(basename $i)";
		
		if [ $check_result -eq 0 ]
		then
			print_installed "$basename_file installed with no errors";
			execute_log rm $file_to_install;
		else
      error="true";
			print_error "Error in $basename_file - Please check log $script_dir/$log_file";
		fi
	done
  
  if [ ! -z $error ]
  then
    print_warning "Error found during installation.";
    cf ask_yn "if you want to continue" "if you want to quit from the rate plan installation" || exit 8 ;
  fi
	
	return 0;
}
######################################################################################################

######################################################################################################
# Create the transitions
# 1. Plans type (retail|wholesale)
#
# Error codes:
# 0. no errors
# 1. unexpected error
# 2. parameter error
######################################################################################################
create_transitions(){
	print_title "Installing $1 transitions";

	if [ $1 == "retail" ]
	then
		SID=$SID_CATALOG
		USER=$USER_CATALOG
		PASS=$PASS_CATALOG
	elif [ $1 == "wholesale" ]
	then
		SID=$SID_MHO
		USER=$USER_MHO
		PASS=$PASS_MHO
	else
    print_error "Error in parameters";
		return 2;
	fi
	
	tmp_plan_list="${TMP_DIR}/plans_spool_${1}.tmp"
	
	log "Creating transition file";

	spool_query_result $USER $PASS ${SID} $tmp_plan_list "
	WITH PLANS
        AS (SELECT P.NAME AS PLAN_NAME, PS.SERVICE_OBJ_TYPE as SERVICE_OBJ_TYPE, D.NAME AS DEAL_NAME
              FROM ${USER}.PLAN_T p, ${USER}.PLAN_SERVICES_T PS, ${USER}.DEAL_T D
             WHERE     P.POID_ID0 = PS.OBJ_ID0
                   AND PS.DEAL_OBJ_ID0 = D.POID_ID0
                   AND PS.SERVICE_OBJ_TYPE IN ('/service/telco/SIM', '/service/telco/Postpaid')),
     DEALS AS (SELECT PERMITTED as SERVICE_OBJ_TYPE, DEAL_T.NAME AS DEAL_NAME
                 FROM ${USER}.DEAL_T
                WHERE DEAL_T.PERMITTED IN ('/service/telco/SIM', '/service/telco/Postpaid')
                      AND (DEAL_T.NAME LIKE '%Traffico Personal%' OR DEAL_T.NAME LIKE '%Open Roaming%' OR DEAL_T.NAME LIKE '%Connect Card%'))
	SELECT PLAN_NAME || ';' || SERVICE_OBJ_TYPE || ';' ||DEAL_NAME AS LINE FROM PLANS
	UNION
	SELECT ';' || SERVICE_OBJ_TYPE || ';' || DEAL_NAME AS LINE FROM DEALS";

  result=$?;
  if [ $result -ne 0 ]
  then 
    print_error "Unable to create transition file";
    return 4;
  fi
  

	#/**
	# * This java script will create all transitions for all plans contained in the input file.
	# * 
	# * Input parameters:
	# * 1. Input file name of the file with the plans list
	# * 2. Output file name for transitions.xml
	# * 
	# * Error exit code:
	# * 1: Unexpected error
	# * 2: Missing parameter(s)
	# * 3: 
	# * 4: Unable to read input file
	# * 5: Unable to create output file
	# * @author mario.lagana
	# *
	# */
	log "Executing: java -jar $price_list_manager transitions_create $tmp_plan_list $transitions_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
	java -jar $price_list_manager transitions_create $tmp_plan_list $transitions_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log
	log "Executing: cat $tmp_file_log >> $log_file";
	cat $tmp_file_log >> $log_file;

	cf empty_file $tmp_file_log;
	
	log "Installing transition file";

	if [ $1 == "retail" ]
	then
    log "Executing: java -cp \"$load_pricelist_classpath_retail\" com.portal.loadpricelist.LoadPriceList -rf \"$transitions_file\"";
    java -cp "$load_pricelist_classpath_retail" com.portal.loadpricelist.LoadPriceList -cf "$transitions_file" 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log;
	elif [ $1 == "wholesale" ]
	then
		log "Executing: loadpricelistMHO -cf $transitions_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log";
		loadpricelistMHO -cf $transitions_file 1>>$tmp_file_log 2>>$tmp_file_log 3>>$tmp_file_log;
	else
		print_error "## Error - Wrong parameter: $1.";
		return 2;
	fi

	log "Executing: cat $tmp_file_log >> $log_file";
	cat $tmp_file_log >> $log_file;
	execute_log check "$tmp_file_log" "$transitions_file" "$TMP_DIR" "successfully";

	check_result=$?;

	if [ $check_result -eq 0 ]
	then
		print_installed "Transitions installed with no errors";
    return 0;
  else
    print_error "Error installing transitions";
    error="true":
    return 4;
	fi
}
######################################################################################################

###############################################################
# Initialization
###############################################################
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

xsd_path="$CONFIG_DIR/price_list.xsd"
db_conf_file="$CONFIG_DIR/db_pass.txt"

price_list_manager="$LIB_DIR/pricelist_manager.jar"

example_dir="${PIN_HOME}/sys/data/pricing/example"
mho_dir="${PIN_HOME}/apps/load_price_list_mho"

files_type="rate_plan_files"

backup_dir="${BACKUP_DIR}/${files_type}"
backup_dir_e2e="$backup_dir/retail"
backup_dir_mho="$backup_dir/wholesale"
backup_catalogue_e2e_file="$backup_dir_e2e/backup_e2e_catalogue.xml"
backup_catalogue_mho_file="$backup_dir_mho/backup_mho_catalogue.xml"

pricelist_e2e_dir="$PACKAGE_DIR/${files_type}/retail"
pricelist_mho_dir="$PACKAGE_DIR/${files_type}/wholesale"

discount_e2e_dir="$pricelist_e2e_dir/discount"
discount_mho_dir="$pricelist_mho_dir/discount"
charge_sharing_e2e_dir="$pricelist_e2e_dir/charge_sharing"
charge_sharing_mho_dir="$pricelist_mho_dir/charge_sharing"
products_e2e_dir="$pricelist_e2e_dir/products"
products_mho_dir="$pricelist_mho_dir/products"
deals_e2e_dir="$pricelist_e2e_dir/deals"
deals_mho_dir="$pricelist_mho_dir/deals"
plans_e2e_dir="$pricelist_e2e_dir/plans"
plans_mho_dir="$pricelist_mho_dir/plans"

done_discount_e2e_dir="$discount_e2e_dir/done"
done_discount_mho_dir="$discount_mho_dir/done"
done_charge_sharing_e2e_dir="$charge_sharing_e2e_dir/done"
done_charge_sharing_mho_dir="$charge_sharing_mho_dir/done"
done_products_e2e_dir="$products_e2e_dir/done"
done_products_mho_dir="$products_mho_dir/done"
done_deals_e2e_dir="$deals_e2e_dir/done"
done_deals_mho_dir="$deals_mho_dir/done"
done_plans_e2e_dir="$plans_e2e_dir/done"
done_plans_mho_dir="$plans_mho_dir/done"

transitions_file="$TMP_DIR/transitions.xml"

tmp_file_log="$TMP_DIR/${script_name}_log.tmp"
######################################################################################################
# Backup mode
######################################################################################################
if [ $backup_mode == "yes" ]
then
	print_title "Start backup of $installation_parameter catalogue"

	if [ $installation_parameter == "wholesale" ] || [ $installation_parameter == "all" ]
	then
		cf backup_catalogue $installation_parameter;
		
		print_installed "Backup of wholesale catalogue is finished";
	fi

	if [ $installation_parameter == "retail" ] || [ $installation_parameter == "all" ]
	then
		cf backup_catalogue $installation_parameter;
		
		print_installed "Backup of retail catalogue is finished";
	fi
	
	print_title "End backup of $installation_parameter catalogue"
	
	exit 0;
fi
######################################################################################################

if [ ! -e $pricelist_e2e_dir ] && [ ! -e $pricelist_mho_dir ]
then
  log "E2E dir and MHO dir don't exist both ($pricelist_e2e_dir, $pricelist_mho_dir)"
	exit 3
fi

if [ $installation_parameter == "retail" ] || [ $installation_parameter == "all" ]
then
	if [ $(find "$pricelist_e2e_dir" -type f | grep -v done | grep -v xsd | wc -l) -eq 0 ]
	then
    log "No retail file to install in $pricelist_e2e_dir (find \"$pricelist_e2e_dir\" -type f | grep -v done | grep -v xsd | wc -l)"
		exit 3
	fi
fi

if [ $installation_parameter == "wholesale" ] || [ $installation_parameter == "all" ]
then
	if [ $(find "$pricelist_mho_dir" -type f | grep -v done | grep -v xsd | wc -l) -eq 0 ]
	then
    log "No wholesale file to install in $pricelist_mho_dir (find \"$pricelist_mho_dir\" -type f | grep -v done | grep -v xsd | wc -l)"
		exit 3
	fi
fi

# If ONLY_CHECK is populated (the only possible value is true) then we exit without install the files
if [ ! -z $ONLY_CHECK ]
then
  log "Check finished ($ONLY_CHECK)"
  exit 0;
fi

create_dir $backup_dir;
create_dir $backup_dir_e2e;
create_dir $backup_dir_mho;

create_dir $pricelist_e2e_dir;
create_dir $pricelist_mho_dir;
create_dir $discount_e2e_dir;
create_dir $discount_mho_dir;
create_dir $charge_sharing_e2e_dir;
create_dir $charge_sharing_mho_dir;
create_dir $products_e2e_dir;
create_dir $products_mho_dir;
create_dir $deals_e2e_dir;
create_dir $deals_mho_dir;
create_dir $plans_e2e_dir;
create_dir $plans_mho_dir;
create_dir $done_discount_e2e_dir;
create_dir $done_discount_mho_dir;
create_dir $done_charge_sharing_e2e_dir;
create_dir $done_charge_sharing_mho_dir;
create_dir $done_products_e2e_dir;
create_dir $done_products_mho_dir;
create_dir $done_deals_e2e_dir;
create_dir $done_deals_mho_dir;
create_dir $done_plans_e2e_dir;
create_dir $done_plans_mho_dir;

execute_log cp $PIN_CONF_PATH $TMP_DIR;
execute_log cp $INFRANET_CONF_PATH $TMP_DIR;

pin_conf_e2e_dir="${TMP_DIR}"
pin_conf_mho_dir="$PIN_HOME/apps/load_price_list_mho"

if [ -z $hot_deploy_mode ]
then
  SID_CATALOG=$(grep -i "DB_CATALOG|" $db_conf_file | cut -f2 -d"|" | head -1)
  USER_CATALOG=$(grep -i "DB_CATALOG|" $db_conf_file | grep -i "|PIN|" | cut -f3 -d"|" | head -1)
  PASS_CATALOG=$(grep -i "DB_CATALOG|" $db_conf_file | grep -i "|PIN|" | cut -f4 -d"|" | head -1)
else
  log "Hot deploy mode; DB connection will be performed on INSTALLATION_SCHEMA";
  SID_CATALOG=$INSTALLATION_SID;
  USER_CATALOG=$INSTALLATION_SCHEMA;
  PASS_CATALOG=$INSTALLATION_PASS;
fi
  
SID_MHO=$(grep -i "DB_MHO|" $db_conf_file | cut -f2 -d"|" | head -1)
USER_MHO=$(grep -i "DB_MHO" $db_conf_file | grep -i "|PIN|" | cut -f3 -d"|" | head -1)
PASS_MHO=$(grep -i "DB_MHO" $db_conf_file | grep -i "|PIN|" | cut -f4 -d"|" | head -1)

log_variable SID_CATALOG;
log_variable USER_CATALOG;
log_variable PASS_CATALOG;

log_variable SID_MHO;
log_variable USER_MHO;
log_variable PASS_MHO;
######################################################################################################

print_title "Start install $installation_parameter pricelists"

if [ -z $SID_CATALOG ] || [ -z $USER_CATALOG ] || [ -z $PASS_CATALOG ]
then
  print_error "Rate plan installation: Unable to read DB configuration";
  exit 2;
fi

######################################################################################################
# Check for custom price_list.xsd
######################################################################################################
if [ ! -e $xsd_path ]
then
	execute_log cp -p $example_dir/$(basename $xsd_path) .;
fi

if [ ! -e $TMP_DIR/$(basename $xsd_path) ]
then
	execute_log cp -p $xsd_path $TMP_DIR;
fi
######################################################################################################

######################################################################################################
# Installation
######################################################################################################
if [ $installation_parameter == "wholesale" ] || [ $installation_parameter == "all" ]
then
	cf unzip_dir $pricelist_mho_dir
	execute_log cd $curr_dir;
	
	cf merge_install_pricelists $discount_mho_dir discount wholesale $done_discount_mho_dir;
	cf install_pricelists $discount_mho_dir discount wholesale $done_discount_mho_dir;
	
	cf merge_install_pricelists $charge_sharing_mho_dir charge_sharing wholesale $done_charge_sharing_mho_dir;
	cf install_pricelists $charge_sharing_mho_dir charge_sharing wholesale $done_charge_sharing_mho_dir;
	
	cf merge_install_pricelists $products_mho_dir product wholesale $done_products_mho_dir;
	cf install_pricelists $products_mho_dir product wholesale $done_products_mho_dir;
	
	cf merge_install_pricelists $deals_mho_dir deal wholesale $done_deals_mho_dir;
	cf install_pricelists $deals_mho_dir deal wholesale $done_deals_mho_dir;

	cf merge_install_pricelists $plans_mho_dir plan wholesale $done_plans_mho_dir;
	cf install_pricelists $plans_mho_dir plan wholesale $done_plans_mho_dir;
	
	if [ ! -z $transitions_install ]
	then
		cf create_transitions wholesale || error="true";
	fi
fi

if [ $installation_parameter == "retail" ] || [ $installation_parameter == "all" ]
then
	cf unzip_dir $pricelist_e2e_dir
	execute_log cd $curr_dir;

	cf merge_install_pricelists $discount_e2e_dir discount retail $done_discount_e2e_dir;
	cf install_pricelists $discount_e2e_dir discount retail $done_discount_e2e_dir;
	
	cf merge_install_pricelists $charge_sharing_e2e_dir charge_sharing retail $done_charge_sharing_e2e_dir;
	cf install_pricelists $charge_sharing_e2e_dir charge_sharing retail $done_charge_sharing_e2e_dir;
	
	cf merge_install_pricelists $products_e2e_dir product retail $done_products_e2e_dir;
	cf install_pricelists $products_e2e_dir product retail $done_products_e2e_dir;
	
	cf merge_install_pricelists $deals_e2e_dir deal retail $done_deals_e2e_dir;
	cf install_pricelists $deals_e2e_dir deal retail $done_deals_e2e_dir;

	cf merge_install_pricelists $plans_e2e_dir plan retail $done_plans_e2e_dir;
	cf install_pricelists $plans_e2e_dir plan retail $done_plans_e2e_dir;
	
	if [ ! -z $transitions_install ]
	then
		cf create_transitions retail || error="true";
	fi
fi
###########################