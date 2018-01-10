package ContractStampDuty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.portal.pcm.PortalContext;

public class ContractStampDutyProcessor
{
	public static boolean debugMode = false;	
	
	public static void main(String args[]) throws IOException	
	{
		DBInterface dbInf = null;		
		LoggerManager logger = null;		
		ContractStampDutyPojo[] processPojoArray = null;
		ContractStampDutyPojo contractStampDutyPojo = new ContractStampDutyPojo();
		AcsTaxTreatmentConfigLines acsTaxLinesConfigInst = null;
		AcsTaxOnContractConfigLines acsTaxOnContractLinesConfigInst = null;
		AcsCodConversionConfigLines acsCodConversionLinesConfigInst = null;
		boolean error = false;
		Date inputDate = null;
//		******************************************************************
		//		Input arguments passed
		
		String accountNo=null;
		String creditClass=null;
		int noOfThreads=0;
		String inputDateStr = null;

		try{   
			noOfThreads = Integer.parseInt(args[0]);  
			if (noOfThreads <= 0)
			{
				logger.log("","No of Threads executing is invalid","");
				System.err.println("No of Threads executing is invalid");
				System.exit(1);
			}
		} catch(NumberFormatException nfe) {
			logger.log("","No of Threads executing is invalid","");
			System.err.println("No of Threads executing is invalid");
			System.exit(1);
		}	
		
		
		inputDateStr = args[1];
		SimpleDateFormat sdf = new SimpleDateFormat(AcsLabels.MMDDYYYY);

		try {
			inputDate = sdf.parse(inputDateStr.substring(0,8));
		} catch (ParseException e) {
			System.err.println("Error while parsing input date.");
			e.printStackTrace();
			System.exit(1);
		}
		
		
		if(args.length==3){
			accountNo=args[2];
		}			
		
		if(args.length==4){
			accountNo=args[2];
			creditClass=args[3];			
		}
		//******************************************************************
		
								
//		******************************************************************
		//  Open Main logger
		try
		{
			logger = new LoggerManager("0");
		}
		catch(FileNotFoundException e)
		{
			System.err.println("Problem while Opening LogFile: " + e);
			e.printStackTrace();
			System.exit(1);
		}
		catch(IOException e)
		{
			System.err.println("Problem while Opening LogFile: " + e);
			e.printStackTrace();
			System.exit(1);
		}
		//******************************************************************
		
		//******************************************************************
		//	   Open Portal Context
		PortalContext context = null;
		PortalInterface portal = null;
		try
		{
			portal = new PortalInterface();
			context = portal.openPortalContext();
			logger.log("Portal Connection Established ", "", "");
			if (debugMode)
				System.out.println("Portal Connection Established ");
		}
		catch(Exception e)
		{
			logger.log("", "Error Occured while Initializing Portal Interface ", e.toString() + " " + e.getMessage());
			if (debugMode)
			{
				e.printStackTrace();
				System.err.println("Error Occured while Initializing Portal Interface");
			}
			System.exit(1);
		}
		//******************************************************************
		
//		******************************************************************
		
		//	   Control pin virtual time
		Date pinVirtualTime = null;
		try {
			pinVirtualTime = portal.getPinVirtualTime(context,logger);
		} catch (Exception e) {
			logger.log("", "Error Occured while trying to get pin virtual time ", e.toString() + " " + e.getMessage());
			if (debugMode)
			{
				e.printStackTrace();
				System.err.println("Error Occured while trying to get pin virtual time ");
			}
			System.exit(1);
		} 

		if (pinVirtualTime == null)
		{
			logger.log("", "pin virtual time is not available ","");
			System.err.println("pin virtual time is not available " );
			System.exit(1);
		}
		//		Set CutOffDate
		contractStampDutyPojo.setCurrentDate(pinVirtualTime);
		contractStampDutyPojo.setCutOffDate(inputDate);
		//******************************************************************

		
		//******************************************************************
		// Connection to database
		try
		{
			if (debugMode)
				System.out.println("Connection to database");
			dbInf = new DBInterface();
			if (debugMode)
				System.out.println("DataBase Connection Established ");
			logger.log("DataBase Connection Established ", "", "");
		}
		catch(Exception e)
		{
			logger.log("", "Problem in Initializing DBManager Class ", e.toString() + " " + e.getMessage());
			if (debugMode)
			{
				e.printStackTrace();
				System.out.println("Problem in Initializing DBManager Class ");
			}
			System.exit(1);
		}
		//******************************************************************

		try {
			acsTaxLinesConfigInst = dbInf.getDbManager().readCacheTax(logger);
			logger.log("", "Read records from ACS_TREATMENT_IVA_CONFIG Table - CacheTax ", "");
		} catch(SQLException e) {
			System.err.println("Problem in Getting the records from ACS_TREATMENT_IVA_CONFIG Table "+(new StringBuilder()).append(e.toString()).append(" ").append(e.getMessage()).toString());
			logger.log("", "Problem in Getting the records from ACS_TREATMENT_IVA_CONFIG Table ", (new StringBuilder()).append(e.toString()).append(" ").append(e.getMessage()).toString());
			System.exit(1);
		}
		
		try {
			acsTaxOnContractLinesConfigInst = dbInf.getDbManager().readCacheTaxOnContract(logger);
			logger.log("", "Read records from ACS_TAX_ON_CONTRACT_CONFIG Table - CacheContract ", "");
		} catch(SQLException e) {
			System.err.println("Problem in Getting the records from ACS_TAX_ON_CONTRACT_CONFIG Table "+(new StringBuilder()).append(e.toString()).append(" ").append(e.getMessage()).toString());
			logger.log("", "Problem in Getting the records from ACS_TAX_ON_CONTRACT_CONFIG Table ", (new StringBuilder()).append(e.toString()).append(" ").append(e.getMessage()).toString());
			System.exit(1);
		}
		
		try {
			acsCodConversionLinesConfigInst = dbInf.getDbManager().readCacheCodConversion(logger);
			logger.log("", "Read records from ACS_COD_CONVERSION Table - CacheConversion ", "");
		} catch(SQLException e) {
			System.err.println("Problem in Getting the records from ACS_COD_CONVERSION Table "+(new StringBuilder()).append(e.toString()).append(" ").append(e.getMessage()).toString());
			logger.log("", "Problem in Getting the records from ACS_COD_CONVERSION Table ", (new StringBuilder()).append(e.toString()).append(" ").append(e.getMessage()).toString());
			System.exit(1);
		}
		
		//******************************************************************
		//	   Getting the list of accounts
		boolean threadStart;	
				
		try{
			
		processPojoArray = dbInf.getDbManager().getContractStampDutyList(accountNo, creditClass, contractStampDutyPojo.getCurrentDate(), contractStampDutyPojo.getCutOffDate(),logger);
		logger.log("", "Read records for "+AcsLabels.Process_Name+" ", ":" + processPojoArray.length);
		if (debugMode)
			System.out.println("Read records for "+AcsLabels.Process_Name+" :" + processPojoArray.length);
		
		threadStart =true;		
		
		}
		catch(SQLException e)
		{
			if (debugMode)
			{
				System.out.println("Problem in getting the Promos for "+AcsLabels.Process_Name+" ");
				e.printStackTrace();
			}

			logger.log("", "Problem in getting the Promos for  "+AcsLabels.Process_Name+" ", "");
			logger.log("", "","SQLException: "+e.fillInStackTrace());
			threadStart =false;
		} catch(Exception e)
		{
			if (debugMode)
			{
				System.out.println("Problem in getting the Promos for "+AcsLabels.Process_Name+" ");
				e.printStackTrace();
			}

			logger.log("", "Problem in getting the Promos for  "+AcsLabels.Process_Name+" ", "");
			logger.log("", "","SQLException: "+e.fillInStackTrace());
			threadStart =false;
		}
		//******************************************************************
		
		
//		******************************************************************
		// Close Portal Context
		try {
			context.close(true);
			if (debugMode)
				System.out.println("Portal Connection Closed");
			logger.log("Portal Connection Closed ", "", "");
		} catch (Exception e) {
			logger.log("", "Error Occured while trying to close context ", e.toString() + " " + e.getMessage());
			if (debugMode)
			{
				e.printStackTrace();
				System.err.println("Error Occured while trying to close context");
			}
		} 
		//******************************************************************
		

		
		//******************************************************************
		//	Close the connection with DB
		try
		{
			dbInf.getDbManager().closeConnection();
			if (debugMode)
				System.out.println("Database Connection Closed");
			logger.log("Database Connection Closed After Query\n\n", "", "");
		}
		catch(SQLException e2)
		{
			e2.printStackTrace();
			logger.log("Problem closing connection DB after query","","");
		}
		//******************************************************************

		//******************************************************************
//		Multithreading for Applying Contract Stamp Duty

		if(threadStart==false){
			logger.log("","Problem in continuing the process","");
			System.err.println("Problem in continuing the process");
			System.exit(1);
		}
		
		if (debugMode)
			System.out.println("Starting Multithreading");
		
		startMyThread(
				ContractStampDutyThread.class,
				processPojoArray,
				error,
				noOfThreads,
				acsTaxLinesConfigInst,
				acsTaxOnContractLinesConfigInst,
				acsCodConversionLinesConfigInst,
				contractStampDutyPojo
		);
		//******************************************************************

		
		if (debugMode)
			System.out.println("End...");
		
		logger.closeLogFile();
	}

	private static void startMyThread(
			Class<?> threadType,
			ContractStampDutyPojo[] processPojoArray,
			boolean error,
			int noOfThreads, 
			AcsTaxTreatmentConfigLines acsTaxLinesInst, 
			AcsTaxOnContractConfigLines acsTaxOnContractLinesConfigInst,
			AcsCodConversionConfigLines acsCodConversionLinesConfigInst,
			ContractStampDutyPojo contractStampDutyPojo
	) 
	{
		if(processPojoArray != null && error == false)
		{
			int nT = noOfThreads;
			ConcurrentLinkedQueue<ThreadManager> activeThreads = new ConcurrentLinkedQueue<ThreadManager>();				

			int threadCount = 1;
			int startRow = 0; 		//Each thread process starts with row
			int endRow = 0; 		//Each thread process ends with row

			if(processPojoArray.length<nT)
			{
				nT = processPojoArray.length;
			}

			while (threadCount <= nT) 
			{

				if (activeThreads.size() < nT) 
				{

					startRow = endRow;

					if (threadCount == nT)
					{
						endRow = processPojoArray.length;
					}
					else
					{
						endRow = (processPojoArray.length/nT)*threadCount;
					}					    	
					//Threads get activated and method is get called with arguments
					
					ArrayList<ContractStampDutyPojo> acsPR = new ArrayList<ContractStampDutyPojo>();
					for (int y=0; y<(endRow-startRow); y++ )
					{
						acsPR.add(processPojoArray[(startRow+y)]);
					}

					//******************************************************************
					//  Open Thread logger
					LoggerManager logger = null;
					//String nomeThread = threadType.getName() + threadCount++;
					String nomeThread = AcsLabels.Thread_Process_Name + threadCount++;
					try
					{
						logger = new LoggerManager (nomeThread);
					}
					catch(FileNotFoundException e)
					{
						System.err.println("Problem while Opening LogFile: " + e);
						e.printStackTrace();
						System.exit(1);
					}
					catch(IOException e)
					{
						System.err.println("Problem while Opening LogFile: " + e);
						e.printStackTrace();
						System.exit(1);
					}
					//******************************************************************

					//******************************************************************
					//  Start the thread
					try {
						Class cls = Class.forName(threadType.getName());
						Class partypes[] = new Class[7];
						partypes[0] = acsPR.getClass();						
						partypes[1] = LoggerManager.class;
						partypes[2] = contractStampDutyPojo.getClass();						
						partypes[3] = activeThreads.getClass();		
						partypes[4] = acsTaxLinesInst.getClass();
						partypes[5] = acsTaxOnContractLinesConfigInst.getClass();
						partypes[6] = acsCodConversionLinesConfigInst.getClass();
						Constructor ct = cls.getConstructor(partypes);						
						Object arglist[] = new Object[7];
						arglist[0] = acsPR;					
						arglist[1] = logger;
						arglist[2] = contractStampDutyPojo;						
						arglist[3] = activeThreads;				
						arglist[4] = acsTaxLinesInst;	
						arglist[5] = acsTaxOnContractLinesConfigInst;
						arglist[6] = acsCodConversionLinesConfigInst;						
						Thread myThread = (Thread) ct.newInstance(arglist);
						myThread.start();						
					} catch (Exception e) {
						logger.log("", nomeThread + ": Error Occured while trying to start the thread", e.toString() + " " + e.getMessage());
						if (ContractStampDutyProcessor.debugMode)
						{
							e.printStackTrace();
							System.err.println(threadType.getName() + ": Error Occured while trying to start the thread");
						}
					}
					//******************************************************************
				}

				try {				    	 
					Thread.sleep(100); 
				} catch(Exception e) {} //briefly pause as to not use all the CPU
			}

			//To check thread alive, Until all threads deactivated the process stays within this loop
			waitForThreads(activeThreads);
		}
	}

	
	@SuppressWarnings("unchecked")
	private static void waitForThreads(Object array) {
		ConcurrentLinkedQueue<Thread> activeThreads = (ConcurrentLinkedQueue<Thread>) array;
		int running = 0;
		do 
		{
			//briefly pause as to not use all the CPU
			try {				    	 
				Thread.sleep(50); 
			} catch(Exception e) {} 

			running = 0;
			for (Thread thread : activeThreads) 
				if (thread.isAlive()) 
					running++;

		} while (running > 0);		
		//Now no thread is active				
	}
	
	
}