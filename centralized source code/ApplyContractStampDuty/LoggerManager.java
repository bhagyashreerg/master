package ContractStampDuty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class LoggerManager 
{
	Logger logger;
	private String logLevel;
	private String strLogFile;
	private String logFileName;
	
	private BufferedWriter buff;
	/**
	 * Constructor Method: This method will create and add the FileAppender Instance to the logger .
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	LoggerManager(String name) throws IOException 
	{
		Properties props = new Properties();
		props.load(new FileInputStream( new File(AcsLabels.PROP_CONTEXT_PATH))) ;

		strLogFile = props.getProperty (AcsLabels.LOG_FILE);
		logLevel = props.getProperty(AcsLabels.LOG_LEVEL);
		logFileName = new SimpleDateFormat(AcsLabels.LOG_FILE_FORMAT).format(new Date());

		
		String logFilepath;

		(new File("logs")).mkdir();

		if(name.equals("0")){
			logFilepath=strLogFile+"_"+logFileName+".log";			
		}
		else{
			logFilepath=strLogFile+"_"+logFileName+"_T"+name+".log";
		}

		FileAppender appender = null;	
		SimpleLayout layout = new SimpleLayout();		

		buff =  new BufferedWriter(new FileWriter(logFilepath, true));
		logger = Logger.getLogger(logFilepath);

		appender = new FileAppender(layout,logFilepath,true);
		logger.addAppender(appender);
	}

	/**
	 * This method will get the TimeStamp from Calendar. 
	 * @return
	 */
	private String getTimeStamp()
	{
		Calendar date = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
		String strDate = sdf.format(date.getTime());
		return strDate;
	}

	public void log(
			String strNormMsg, 
			String strErrMsg, 
			String strDetailErrMsg
			)
	{			
		String strDate = getTimeStamp();
		String log = null;
		
		//logger.error(strErrorMsg);
		if(logLevel.equals("0")&& !strNormMsg.equals(""))
		{
			log = strDate + ": " + strNormMsg;
//			logger.debug(strDate+" :"+strNormMsg);
		}
		else if (logLevel.equals("1") && !(strNormMsg.equals("") && strErrMsg.equals("")))
		{
			log = strDate+": " + strNormMsg + " " + strErrMsg;
//			logger.debug(strDate+": " + strNormMsg + " " + strErrMsg);
		}
		else if(logLevel.equals("2"))
		{
			log = strDate+": " + strNormMsg + " " + strErrMsg;
			// All the Messaged including the Normal Messages + Error Messages and the detailed Error messages.
//			if(strNormMsg==""){
//				logger.debug(strDate+" :"+strNormMsg+""+strErrMsg+""+strDetailErrMsg);
//			} else {
//				logger.debug(strDate+" :[Thread"+strNormMsg+"] "+strErrMsg+""+strDetailErrMsg);
//			}
		}
		else if(logLevel.equals("3"))
		{
			 log = strDate + ": " + strNormMsg + " " + strErrMsg + " " + strDetailErrMsg;
		}
		
		try {
			buff.write(log);
			buff.newLine();
			buff.flush();
		} catch (FileNotFoundException e1) {
			System.err.println("Log file error: file not found");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Log file error: IOException");
			e1.printStackTrace();
		}
	}

	public void closeLogFile() 
	{
		try {
			if (buff != null)
				buff.close();
		} catch (FileNotFoundException e1) {
			System.err.println("Log file error: file not found");
			e1.printStackTrace();
		} catch (IOException e1) {
			System.err.println("Log file error: IOException");
			e1.printStackTrace();
		}
	}



}