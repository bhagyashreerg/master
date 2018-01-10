package ContractStampDuty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.portal.pcm.PortalContext;

public abstract class ThreadManager extends Thread {
	private final Object lock = new Object();

	private ConcurrentLinkedQueue<ThreadManager> activeThreads;

	private static int threadCount = 1;
	private final int threadId;

	ArrayList<ContractStampDutyPojo> processPojoArray;	
	ContractStampDutyPojo contractStampDutyPojo;
	AcsTaxTreatmentConfigLines acsTaxTreatmentConfigLines;
	AcsTaxOnContractConfigLines acsTaxOnContractConfigLines;
	AcsCodConversionConfigLines acsCodConversionConfigLines;

	DBInterface dbInf;
	PortalInterface portal;
	LoggerManager logger;
	PortalContext context;

	public ThreadManager(
			ArrayList<ContractStampDutyPojo> processLine,			
			LoggerManager logger, 
			ContractStampDutyPojo contractStampDutyPojo,
			ConcurrentLinkedQueue<ThreadManager> activeThreads,
			AcsTaxTreatmentConfigLines acsTaxTreatmentConfigLines,
			AcsTaxOnContractConfigLines acsTaxOnContractConfigLines,
			AcsCodConversionConfigLines acsCodConversionConfigLines
			) 
	throws IOException {

		synchronized(lock) { //make sure to protect shared data
			threadId = threadCount++;
		}

		this.processPojoArray = processLine;
		this.logger = logger;
		this.activeThreads = activeThreads;		
		this.contractStampDutyPojo = contractStampDutyPojo;	
		this.acsTaxTreatmentConfigLines = acsTaxTreatmentConfigLines;
		this.acsTaxOnContractConfigLines = acsTaxOnContractConfigLines;
		this.acsCodConversionConfigLines = acsCodConversionConfigLines;
		
		try
		{
			dbInf = new DBInterface();
			if (ContractStampDutyProcessor.debugMode)
				System.out.println("DataBase Connection Established ");
			logger.log("DataBase Connection Established ", "", "");
		}
		catch(Exception e)
		{
			if (ContractStampDutyProcessor.debugMode)
				e.printStackTrace();
			logger.log("", "Error Occured while Initializing Portal Interface ", e.toString() + e.getMessage());
			System.exit(1);
		}

		try
		{
			portal = new PortalInterface();
			context = portal.openPortalContext();
			if (ContractStampDutyProcessor.debugMode)
				System.out.println("Portal Connection Established ");
			logger.log("Portal Connection Established ", "", "");
		}
		catch(Exception e)
		{
			if (ContractStampDutyProcessor.debugMode)
				e.printStackTrace();
			logger.log("", "Error Occured while Initializing Portal Interface ", e.toString() + e.getMessage());
			System.exit(1);
		}
	}

	protected void remove(Thread thread) 
	{
		activeThreads.remove(thread);
	}

	public void start() {		
		logger.log("","", "Adding Thread to Active List");
		activeThreads.add(this);
		super.start();
	}

	public int getThreadId() {
		return threadId;
	}
}