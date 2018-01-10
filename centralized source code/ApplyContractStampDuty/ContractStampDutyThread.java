package ContractStampDuty;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ContractStampDutyThread extends ThreadManager {
	private String nomeThread;

	public ContractStampDutyThread(
			ArrayList<ContractStampDutyPojo> processPojoArray,					
			LoggerManager logger, 			
			ContractStampDutyPojo contractStampDutyPojo,
			ConcurrentLinkedQueue<ThreadManager> activeThreads, 
			AcsTaxTreatmentConfigLines acsTaxTreatmentConfigLines,
			AcsTaxOnContractConfigLines acsTaxOnContractConfigLines,
			AcsCodConversionConfigLines acsCodConversionConfigLines
			) 
	throws IOException {

		super(
				processPojoArray,				
				logger,
				contractStampDutyPojo,
				activeThreads,
				acsTaxTreatmentConfigLines,
				acsTaxOnContractConfigLines,
				acsCodConversionConfigLines
				);
		nomeThread = AcsLabels.Process_Name + getThreadId();
	}

	public void run() {

		try {	
			/***contractStampDuty***/
			ContractStampDutyManagement.contractStampDuty(
					dbInf,
					portal,
					context, 
					processPojoArray,	
					contractStampDutyPojo,
					acsTaxTreatmentConfigLines, 
					acsTaxOnContractConfigLines,
					acsCodConversionConfigLines,
					logger
					);
		}catch (Exception e) {
			logger.log("", nomeThread + ": Error Occured while launch "+AcsLabels.Process_Name+" ", e.toString() + " " + e.getMessage());
			if (ContractStampDutyProcessor.debugMode)
			{
				e.printStackTrace();
				System.err.println(nomeThread + ": Error Occured while launch "+AcsLabels.Process_Name+" ");
			}
		}

		//******************************************************************
		// Close Portal Context
		try {
			context.close(true);
			logger.log("Portal Connection Closed ", "", "");
			
		} catch (Exception e) {
			logger.log("", nomeThread + ": Error Occured while trying to close context ", e.toString() + " " + e.getMessage());
			if (ContractStampDutyProcessor.debugMode)
			{
				e.printStackTrace();
				System.err.println(nomeThread + ": Error Occured while trying to close context");
			}
		} 
		//******************************************************************

		//******************************************************************
		//	Close the connection with DB
		try
		{
			dbInf.getDbManager().closeConnection();
			if (ContractStampDutyProcessor.debugMode)
				System.out.println(nomeThread + ": Connection Closed");
			logger.log("Database Connection Closed\n\n", "", "");
		}
		catch(SQLException e2)
		{
			logger.log("", nomeThread + ": Error Occured while trying to close DBConnection ", e2.toString() + " " + e2.getMessage());
			if (ContractStampDutyProcessor.debugMode)
			{
				e2.printStackTrace();
				System.err.println(nomeThread + ": Error Occured while trying to close DBConnection");
			}			
		}
		//******************************************************************

		logger.log("", "", nomeThread + ": Removing Thread from Active List");
		remove(this);
		
		logger.closeLogFile();
	}
}