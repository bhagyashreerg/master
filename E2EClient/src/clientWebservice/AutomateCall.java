package clientWebservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.portal.pcm.EBufException;

public class AutomateCall {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws DBException 
	 */
	
	
	
	public static void main(String[] args) throws IOException, ParseException {
	
			UserInput ui = new UserInput();
			Date pinVirtualTime = null;
			PortalInterface portal = null;
			DBConnection db = null;
			AcsLabels al = null;
		
		//---------------  GET BASIC DETAILS FROM USER. ---------------// 
			init(ui, al);
			
		//---------------  CREATE DB CONNECTION. ---------------// 
			try {
				
				db = new DBConnection();
			} catch (EBufException e) {
				System.out.println("Error creating DB connection");
				e.printStackTrace();
				System.exit(0);
			} catch (PropertiesException e) {
				System.out.println("Error creating DB connection");
				e.printStackTrace();
				System.exit(0);
			} catch (DBException e) {
				System.out.println("Error creating DB connection");
				e.printStackTrace();
				System.exit(0);
			}	
			
		//--------------- PREPARE JOB ROUTING ---------------// 
			JobRouter jr = new JobRouter(ui, db);
			
		//--------------- GET PORTAL CONNECTION ---------------// 
			try {
				portal = new PortalInterface();
				pinVirtualTime = portal.getPinVirtualTime();
				AcsLabels.WEB_SERVICE_URL  = portal.url;
				//System.out.println(al.WEB_SERVICE_URL);
				
				jr.md.setPVT_CODE((new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")).format(pinVirtualTime));
				/*System.out.println(jr.md.getPVT_CODE());*/
				System.out.println("And Pin virtual time in selected enviornment: " + pinVirtualTime);				
				System.out.println("");
				
			} catch (FileNotFoundException e) {
				System.out.println("Error during creating portal context");
				e.printStackTrace();
				System.exit(0);
			} catch (EBufException e) {
				System.out.println("Error during creating portal context");
				e.printStackTrace();
				System.exit(0);
			} catch (IOException e) {
				System.out.println("Error during creating portal context");
				e.printStackTrace();
				System.exit(0);
			}
		
	
		//--------------- CALL CORRESPONDING JOB ROUTING ---------------// 	
			try {
				jr.workerSelect();
			} catch (DBException e) {
				e.printStackTrace();
			} finally{
				try {
					db.closeConnection();
				} catch (SQLException e) {
					System.out.println("Error closing DB connection");
					e.printStackTrace();
				}
			}

			System.out.println("");
			System.out.println("********** Process Ended !!! ********** ");
			
	}
	
	private static void init(UserInput ui, AcsLabels al){
		
		ui.setEnvType();
		/*Refresh Cache*/
		ui.setRefCache();
		ui.setAccountCreateType();
		ui.setActionType();

		al = new AcsLabels();
		
		AcsLabels.PROP_DATA_URL = al.hm_DBEnv.get(ui.getEnvType());
		AcsLabels.PROP_CONTEXT_PATH = al.hm_PortalEnv.get(ui.getEnvType());

		if(AcsLabels.PROP_DATA_URL == "" || AcsLabels.PROP_DATA_URL == null || AcsLabels.PROP_CONTEXT_PATH == "" || AcsLabels.PROP_CONTEXT_PATH == null ){
			System.out.println("");
			System.out.println("Sorry no matching entry for enviornment type...");
			System.exit(0);
		}
	}

}
