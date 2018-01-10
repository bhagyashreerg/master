package ContractStampDuty;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;

public class DBInterface {
	private static final int RECCONNECT_TRY = 3;

	private DBManager dbManager;

	public DBInterface() throws FileNotFoundException, ClassNotFoundException, SQLException, IOException {
		dbManager = new DBManager();
	}

	public DBManager getDbManager() {
		return dbManager;
	}

	

	public void reconnect(
			LoggerManager logger
	)  
	{
		logger.log("Reestablishing DB connection ", "", "");

		int i = 0;
		boolean check = true;

		while (i < RECCONNECT_TRY && check )
		{
			try {
				Thread.sleep(50); 
				dbManager = new DBManager();
				check = false;
			} catch (Exception e){
				i++;
				if (ContractStampDutyProcessor.debugMode)
					e.printStackTrace();
				logger.log("DB connection reestablishment failed, attempt number: ", "" + i, "");
			}
		}
	}

	public Statement createStatement() throws SQLException {
		return dbManager.dbConn.createStatement();
	}

	public boolean commitConnection(LoggerManager logger) {
		boolean proceed = true;
		try {
			dbManager.commitConnection();
			if (ContractStampDutyProcessor.debugMode)
			{
				System.out.println("dbInf.getDbManager().commitConnection()");
			}
		} catch(SQLException e) {
			proceed = false;
			logger.log("", "", "Issue with DB commit "
					+ e.fillInStackTrace());
			if (ContractStampDutyProcessor.debugMode)
			{
				System.out.println("Issue with DB commit " + AcsLabels.INACTIVE);
				e.printStackTrace();
			}
		}
		return proceed;
	}
	
	public void closeConnection() throws SQLException {
		dbManager.closeConnection();
	}

	public void rollbackConnection(LoggerManager logger) {
		try {
			dbManager.rollbackConnection();
			if (ContractStampDutyProcessor.debugMode)
			{
				System.out.println("rollbackConnection()");
			}
		} catch (Exception e) {
			logger.log("","","Issue with DB rollback "+e.fillInStackTrace());
			if (ContractStampDutyProcessor.debugMode)
			{
				System.out.println("Issue with DB rollback ");
				e.printStackTrace();
			}
		}
	}

		
}
