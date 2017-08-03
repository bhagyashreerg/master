######################################################################################################
# Clean a message for log and screen print
######################################################################################################
clean_msg () {
   echo $(echo $@ | sed "s@$(pwd)@.@g");
}
######################################################################################################

######################################################################################################
# Log a message into $log_file
######################################################################################################
log () {
	if [ ! -z $log_file ] && [ -w $log_file ]
	then
		echo -e "$(date +%Y.%m.%d_%H:%M:%S)> $log_label$@" >> $log_file;
	fi
}
######################################################################################################

######################################################################################################
# Perform some standard commands before to call a bash function. 
# The function will return the error code of the function executed or -1 for missing parameters
#
# Input:
# 1.   Function name
# 2... Function parameters
######################################################################################################
cf () {
	local result=0;
  
  if [ ! -z $log_label ]
  then
    cf_old_log_label=$log_label
  fi
	
	if [ $# -eq 0 ]
	then
		log "cf: Missing parameters";
		result=-1;
	else
		local function_name=$1
		
		log_label="cf>$function_name: ";
    
    shift;  
    
		log "$@ ($#)";

    local i=2;
    local command="";
    for a in "$@"
    do
      if [ "x$a" == "x" ]
      then
        continue;
      fi
      
      eval par$i=\"$a\";
      
      local par_name="par$i";
      command=$command\"${!par_name}\"" ";
    
      i=$(( i + 1 ));
    done   
    
    eval $function_name $command ;
    
		result=$?
	fi

  log_variable result;
  
  if [ ! -z $cf_old_log_label ]
  then
    log_label=$cf_old_log_label;
    unset cf_old_log_label;
  else
    unset log_label;
  fi
  
	return $result;
}
######################################################################################################

######################################################################################################
# Archive a file with the label or the current date
######################################################################################################
archive () {
	if [ ! -z $label ]
	then
		new_name=${1}_${label};
	else
		new_name=$1_`date +"%Y%m%d%H%M%S"`;
	fi
	
	execute_log cp -p $1 $new_name;
	execute_log gzip -f -9 $new_name;
	
	unset log_label;
}
######################################################################################################

######################################################################################################
# Create a directory if this doesn't exists
######################################################################################################
create_dir () {
	result=0;

	if [ ! -e "$1" ]
	then
		execute_log mkdir -p "$1";
	else
		result=2;
	fi

	return $result;
}
######################################################################################################

######################################################################################################
# Cpy file to destination dir with all necessary checks
######################################################################################################
copy_file () {
	if [ $# -lt 2 ]
	then
		ERR_MSG="Missing parameters";
		return -1;
	fi

	source_file=$1
	dest_file=$2;
	dest_dir=$(dirname "$2");
    
	if [ -z "$dest_dir" ]
	then
		ERR_MSG="Unable to read destination directory: $dest_dir";
		return 2;
	fi

	if [ ! -r $source_file ]
	then
		ERR_MSG="The source file is not readable: $source_file";
		return 3;
	fi
	
	if [ -e "$dest_dir" ] && [ ! -d "$dest_dir" ]
	then
		ERR_MSG="Destination dir is not a directory: $dest_dir";
		return 4;
	fi

	if [ -e "$dest_file" ] && [ ! -w "$dest_file" ]
	then
		ERR_MSG="Destination file already exists and is not writeble: $dest_file";
		return 5;
	fi

	if [ ! -e "$dest_dir" ]
	then
		cf create_dir "$dest_dir"
	fi

	execute_log cp "$1" "$2";
}
######################################################################################################

######################################################################################################
# Do an echo with red message on black background
######################################################################################################
print_error(){
  local message=$(clean_msg "$@");
	echo -e "\033[40;31;1m## $message\033[0m"
	log "## $@"
}
######################################################################################################

######################################################################################################
# Do an echo with red message on black background
######################################################################################################
print_hd_error(){
  local message=$(clean_msg "$@");
	echo -e "\033[40;35;1m## $message\033[0m"
	log "## $@"
}
######################################################################################################

######################################################################################################
# Do an echo with red message on black background
######################################################################################################
print_warning(){
  local message=$(clean_msg "$@");
	echo -e "\033[33;40;1m++ $message\033[0m"
	log "++ $@"
}
######################################################################################################

######################################################################################################
# Do an echo with red message on black background
######################################################################################################
print_title(){
  local message=$(clean_msg "$@");
	echo -e "\033[34;40;1m** $message **\033[0m"
	log "*** $@ ***"
}
######################################################################################################

######################################################################################################
# Do an echo with red message on black background
######################################################################################################
print_message(){
  local message=$(clean_msg "$@");
	echo -e "\033[36;40;1m>> $message\033[0m"
	log "> $@"
}
######################################################################################################

######################################################################################################
# Do an echo with red message on black background
######################################################################################################
print_installed(){
  local message=$(clean_msg "$@");
	echo -e "\033[37;40;1m-- $message\033[0m"
	log "-- $@"
}
######################################################################################################

######################################################################################################
# Log a variable content using its name
######################################################################################################
log_variable () {
  variable_name=$1
  
  if [ $(declare -p $variable_name 2>/dev/null | grep "^declare -a" | wc -l) -gt 0 ]
  then
    # the variable is an array
    local array_name=$1;
    local ref=$array_name[@];
    if [ ! -z $log_file ] && [ -w $log_file ]
    then
      echo -e "$(date +%Y.%m.%d_%H:%M:%S)> $log_label$array_name: >${!ref}<" >> $log_file;
    fi
  else
    log "$variable_name: >${!variable_name}<";
  fi
}
######################################################################################################

######################################################################################################
# Log and execute a command
######################################################################################################
execute_log () {
  if [ -z $log_file ]
  then 
    output=/dev/null;
  else
    output=$log_file;
  fi
  
	log "Executing: $@ ($#)";
	$@ >>$output 2>>$output; 
}
######################################################################################################

######################################################################################################
# Log, print and execute a command
######################################################################################################
execute_print () {
  if [ -z $log_file ]
  then 
    output=/dev/null;
  else
    output=$log_file;
  fi

	print_message "Executing: $@ ($#)";
	$@ >>$log_file 2>>$log_file; 
}
######################################################################################################

######################################################################################################
# Check if in a file log there is a word of "success"
# If yes move the file into a directory, else print a error message
# 1 - log file name to check
# 2 - file to install
# 3 - destination directory of the success files
# 4 - word of success
######################################################################################################
check_log_success () {
	if [ $# -ne 4 ]
	then
		log "Error: missing parameters";
		return 2;
	fi
	
	if [ $(grep $4 $1 | wc -l) -gt 0 ] 
	then
		execute_log mv $2 $3;
		return 0;
	else
		return 1;
	fi
}
######################################################################################################

######################################################################################################
# Empty file only if exists
######################################################################################################
empty_file () {
  if [ $# -ne 1 ]
  then
    print_error "Missing parameters";
    return 2;
  fi

	tmp_file_x=$1
  
	if [ -e $tmp_file_x ]
	then
    if [ ! -w $tmp_file_x ]
    then
      print_error "File is not writeble: $tmp_file_x";
      return 3;
    fi

    log "cat /dev/null > $tmp_file_x";
		cat "/dev/null" > $tmp_file_x;
	fi
}
######################################################################################################

######################################################################################################
# Get a value from a CONFIG FILE
# Input:
# 1. Config file name
# 2. Config parameter name
# 3. Default config value (when present no error will be raised for missing configuration)
######################################################################################################
get_config_value () {
  if [ $# -lt 2 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local config_file_name=$1
  local config_parameter_name=$2
  
  if [ ! -z $3 ]
  then
    local config_parameter_default_value=$3;
  fi
  
  if [ ! -r $config_file_name ]
  then
    if [ -z $config_parameter_default_value ]
    then
      print_error "Unable to read the config file: $config_file_name";
      return 3;
    else
      log_variable config_parameter_default_value;
      echo $config_parameter_default_value;
      return 0;
    fi
  fi

  log "Executing: grep $config_parameter_name $config_file_name | cut -f2 -d\"=\"";
  local config_parameter_value=$(grep ^$config_parameter_name $config_file_name | cut -f2 -d"=");
  
  if [[ -z $config_parameter_value ]] && [ -z $config_parameter_default_value ]
  then
    print_error "Unable to find the config: $config_parameter_name";
    return 4;
  fi

  if [[ -z $config_parameter_value ]]
  then
    log_variable config_parameter_default_value;
    echo $config_parameter_default_value;
  else
    log_variable config_parameter_value
    echo $config_parameter_value;
  fi
  
  return 0;
}
######################################################################################################

######################################################################################################
# Get a value from a CONFIG FILE
# Input:
# 1. Config file name
# 2. Config parameter name
# 3. Config parameter value
######################################################################################################
save_config_value () {
  if [ $# -ne 3 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local config_file_name=$1
  local config_parameter_name=$2
  local config_parameter_value=$3;
  
  if [ ! -w $config_file_name ]
  then
    print_error "Unable to access to the config file: $config_file_name";
    return 3;
  fi

  log "Executing: grep ^$config_parameter_name $config_file_name";
  local config_exists=$(grep ^$config_parameter_name $config_file_name);
  
  if [[ ! -z $config_exists ]]
  then
    local temp_config_file="$TMP_DIR/$(basename $config_file_name)";
    log "Executing: grep -v $config_parameter_name $config_file_name >>$temp_config_file 2>>$log_file";
    grep -v $config_parameter_name $config_file_name >$temp_config_file 2>>$log_file;
    execute_log cp $temp_config_file $config_file_name;
  fi

  for value in $config_parameter_value
  do
    echo "$config_parameter_name=$value" >> $config_file_name;
  done

  if [ $(grep "$config_parameter_name=$value" $config_file_name | wc -l) -eq 0 ]
  then
    print_error "Unable to save the configuration" && return 3;
  fi
    
  return 0;
}
######################################################################################################

######################################################################################################
# Print a message and ask for Y or N before to continue
#
# input:
# 1 - Message yes
# 2 - Message no
#
# Returns:
# 0 - N
# 1 - Y
# 2 - Unexpected behavior
######################################################################################################
ask_yn () {
	while [ : ]
	do
		print_warning "Please enter [Y] $1 or [N] $2.";
		read choice

		lower_choice=$(echo $choice | tr '[:upper:]' '[:lower:]')
		
		if [[ -z $lower_choice ]]
		then
			continue;
		fi
		
		if [ $lower_choice == "n" ]
		then
			return 1;
		elif [ $lower_choice == "y" ]
		then
			return 0;
		else
			continue;
		fi
	done
}
######################################################################################################

######################################################################################################
# Search and return the host configured on the input pin.conf
#
# Input:
# 1. pin.conf path
######################################################################################################
get_pin_conf_host () {
  if [ $# -ne 1 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local pin_conf_path=$1;

  if [ ! -r $pin_conf_path ]
  then
    print_error "Unable to read the pin.conf file: $pin_conf_path";
    return 3;
  fi
  
  log "Executing: grep \"nap cm_ptr ip\" $pin_conf_path | grep -v \"^#\" | cut -f5 -d\" \"";
  local pin_conf_host=$(grep "nap cm_ptr ip" $pin_conf_path | grep -v "^#" | cut -f5 -d" ");
  
  log_variable pin_conf_host;
  
  if [[ -z $pin_conf_host ]]
  then
    print_error "Unable to find the cm_port";
    return 3;
  fi
  
  echo $pin_conf_host;
}
######################################################################################################

######################################################################################################
# Search and return the port configured on the input pin.conf
#
# Input:
# 1. pin.conf path
######################################################################################################
get_pin_conf_port () {
  if [ $# -ne 1 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local pin_conf_path=$1;

  if [ ! -r $pin_conf_path ]
  then
    print_error "Unable to read the pin.conf file: $pin_conf_path";
    return 3;
  fi
  
  log "Executing: grep \"nap cm_ptr ip\" $pin_conf_path | grep -v \"^#\" | cut -f6 -d\" \"";
  local pin_conf_port=$(grep "nap cm_ptr ip" $pin_conf_path | grep -v "^#" | cut -f6 -d" ");
  
  log_variable pin_conf_port;
  
  if [[ -z $pin_conf_port ]]
  then
    print_error "Unable to find the cm_port";
    return 3;
  fi
  
  echo $pin_conf_port;
}
######################################################################################################

######################################################################################################
# Execute a testnap request
#
# Input:
# 1. Opcode name
# 2. Input flist
# 3. Output file
######################################################################################################
execute_testnap () {
  if [ $# -ne 3 ]
  then
    print_error "Missing parameter(s)";
    return 2;
  fi
  
  local opcode_name=$1;
  local input_flist=$2;
  local output_flist=$3;
  
  if [ ! -r "pin.conf" ]
  then
    print_error "Unable to access the pin.conf file";
    return 1;
  fi
  
  if [[ -f $input_flist ]] && [ -r $input_flist ]
  then
    local testnap_command="r << XXX 1
$(cat $input_flist)
XXX
xop $opcode_name 0 1"; 
  else
    local testnap_command="r << XXX 1
$input_flist
XXX
xop $opcode_name 0 1";
      fi

  tmp_testnap_input_file="$TMP_DIR/testnap_input_file.ifl";
  echo "$testnap_command" > $tmp_testnap_input_file;

  log_variable tmp_testnap_input_file;
  
  testnap $tmp_testnap_input_file >$output_flist 2>>$log_file;
  return $?;
}

######################################################################################################
# Check if a variable contains a number
#
# Input:
# 1. Variable
######################################################################################################
is_number () {
  if [ $# -ne 1 ]
  then
   print_error "Missing parameter(s)";
    return 2;
  fi

  [ $1 -gt -1 2>>/dev/null ]
  result=$?;
  if [ $result -ne 0 ]
  then 
    return 3;
  fi
}
######################################################################################################

######################################################################################################
# Check if an array contains a value
#
# Input:
# 1. Array name
# 2. Value
######################################################################################################
array_contains () {
  [[ -n "$1" && -n "$2" ]] || {
    print_error "Missing parameter(s)"
    return 2
  }

  local ref=$1[@];

  if [ $(echo "${!ref}" | grep "$2" | wc -l) -gt 0 ]
  then
    return 0;
  fi
  
  #eval 'local values=("${'$1'[@]}")';
  #
  #local element;
  #for element in "${values[@]}"; do
  #  [[ "$element" == "$2" ]] && return 0;
  #done
  
  log "$2 is not contained";
  
  return 1;
}
######################################################################################################

######################################################################################################
# Add a value into array
#
# Input:
# 1. Array name
# 2. Value
######################################################################################################
add_element_to_array () {
  eval 'values=("${'$1'[@]}")';
  
  if [ ${#values[@]} -gt 0 ]
  then
    eval $1'=('${values[@]}' "'"$2"'")';
  else
    eval $1'=("'"$2"'")';
  fi
  
  return $?;
}

######################################################################################################
# Check if an array contains a value, if not add the value into array
#
# Input:
# 1. Array name
# 2. Value
######################################################################################################
array_add_if_not_exists () {
  [[ -n "$1" && -n "$2" ]] || {
    print_error "Missing parameter(s)"
    return 2
  }

  array_contains "${1}" "${2}" 2>>$log_file || add_element_to_array "${1}" "${2}";
  #eval "${1}"+=("${2}") 2>>$log_file;
  return $?;
} 
