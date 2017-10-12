package clientWebservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import com.portal.pcm.EBufException;


public class DBConnection {

	private static Connection dbConn = null;

	/*private Properties prop ;*/
	
	public DBConnection() throws FileNotFoundException, IOException, EBufException, PropertiesException, DBException {
		getConnection();
	}
	
	private void getConnection() throws PropertiesException, DBException {
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(new File(AcsLabels.PROP_DATA_PATH)));
		} catch (IOException e) {
			throw new PropertiesException("Unable to open properties file", e);
		} 
		String strDriverClassName = props.getProperty(AcsLabels.PROP_DATE_DRIVER);
		String strUrl = props.getProperty(AcsLabels.PROP_DATA_URL);
		String strUserName = props.getProperty(AcsLabels.PROP_DATA_UNAME);
		String strPassword = props.getProperty(AcsLabels.PROP_DATA_PWD);
		try {
			Class.forName(strDriverClassName);
			dbConn = DriverManager.getConnection(strUrl, strUserName, strPassword);
			dbConn.setAutoCommit(false);
		} catch (ClassNotFoundException e) {
			throw new DBException("Unable to find class driver for DB connection", e);
		} catch (SQLException e) {
			throw new DBException("Unable to open DB connection", e);
		}
	}	

	public void closeConnection() throws SQLException {
		dbConn.close();
	}
	
	public ArrayList<InputPojoAttributes> getAttributeData(String product_id, String tariff_id) throws DBException{
		System.out.println("{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{{E2E");
		product_id = product_id.trim();
		tariff_id = tariff_id.trim();
		ArrayList<InputPojoAttributes> attributeData = new ArrayList<InputPojoAttributes>();
		
		String query = "(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG1    as NAME," +
						"       E2E.SBL_BILL_PARAMETER1        as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG1'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"            MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"        AND MAST.SBL_BILL_PARAMETER_TAG1 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG2 as NAME," +
						"       E2E.SBL_BILL_PARAMETER2 as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG2'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG2 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG3 as NAME," +
						"       E2E.SBL_BILL_PARAMETER3 as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG3'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG3 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG4 as NAME," +
						"       E2E.SBL_BILL_PARAMETER4 as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG4'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG4 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST. SBL_ERA_TAG as NAME," +
						"       '' as VALUE," +
						"       'SBL_ERA_TAG'    as COLUM" +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING        E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_ERA_TAG <> 'NONE' " +
						")"  ;
		
		
		//System.out.println(query);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			while (rs.next()) {
				InputPojoAttributes ip = new InputPojoAttributes();
				
				ip.setname(rs.getString(1));
				ip.setvalue(rs.getString(2));
				ip.setcolum(rs.getString(3));
				attributeData.add(ip);
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
				
			
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}
		return attributeData;
	}
	
	//added for M2M
public ArrayList<InputPojoAttributes> getAttributeData1(String product_id, String tariff_id) throws DBException{
System.out.println("}}}}}}}}}}}}}}}}}}}}}}}}M2M");
		product_id = product_id.trim();
		tariff_id = tariff_id.trim();
		ArrayList<InputPojoAttributes> attributeData = new ArrayList<InputPojoAttributes>();
		
		String query = "(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG1    as NAME," +
						"       E2E.SBL_BILL_PARAMETER1        as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG1'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"            MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"        AND MAST.SBL_BILL_PARAMETER_TAG1 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG2 as NAME," +
						"       E2E.SBL_BILL_PARAMETER2 as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG2'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG2 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG3 as NAME," +
						"       E2E.SBL_BILL_PARAMETER3 as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG3'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG3 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG4 as NAME," +
						"       E2E.SBL_BILL_PARAMETER4 as VALUE," +
						"       'SBL_BILL_PARAMETER_TAG4'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG4 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST. SBL_ERA_TAG as NAME," +
						"       '' as VALUE," +
						"       'SBL_ERA_TAG'    as COLUM" +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING        E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND E2E.SBL_tariff_ID = '" + tariff_id + "'" +
						"         AND MAST.SBL_ERA_TAG <> 'NONE' " +
						")"  ;
		
		
		System.out.println(query);
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			while (rs.next()) {
				InputPojoAttributes ip = new InputPojoAttributes();
				
				ip.setname(rs.getString(1));
				ip.setvalue(rs.getString(2));
				ip.setcolum(rs.getString(3));
				attributeData.add(ip);
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}
		return attributeData;
	}

    public ArrayList<String> getOverrideData(String product_id, String tariff_id) throws DBException{
		
		product_id = product_id.trim();
		tariff_id = tariff_id.trim();
		ArrayList<String> overrideData = new ArrayList<String>();
		
		String query = "SELECT " +
						"       DISTINCT " +
						"       SBL_OVERRIDE_JURISDICTION" +
						"  FROM " +
						"        ACS_PF_PRODUCT_OVERRIDE " +
						" WHERE " +
						"         SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND SBL_tariff_TAG = '" + tariff_id + "'" ;
		
		
		//System.out.println("QUERY BOSS" + query);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			//System.out.println("INSIDE 1");
			
			while (rs.next()) {
				//System.out.println("query data" + rs.getString(1));
				overrideData.add(rs.getString(1));
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}
		return overrideData;
	}

public String queryValidMsisdn(String msisdn) throws DBException {
	
	Long v0 = Long.parseLong(msisdn);
	Long v1 = Long.parseLong(msisdn) +1;
	Long v2 = Long.parseLong(msisdn) +2;
	Long v3 = Long.parseLong(msisdn) +3;
	Long v4 = Long.parseLong(msisdn) +4;
	Long v5 = Long.parseLong(msisdn) +5;
	Long v6 = Long.parseLong(msisdn) +6;
	Long v7 = Long.parseLong(msisdn) +7;
	Long v8 = Long.parseLong(msisdn) +8;
	Long v9 = Long.parseLong(msisdn) +9;
	Long v10 = Long.parseLong(msisdn) +10;
	Long v11 = Long.parseLong(msisdn) +11;
	Long v12 = Long.parseLong(msisdn) +12;

	msisdn = msisdn.trim();	
	
	//System.out.println("MSISDN VA" + msisdn);
	String query = "select column_value as name " +
			"from table(sys.dbms_debug_vc2coll('" +
			v0 + "','" +
			v1 + "','" +
			v2 + "','" +			
			v3 + "','" +
			v4 + "','" +
			v5 + "','" +
			v6 + "','" +
			v7 + "','" +
			v8 + "','" +
			v9 + "','" +
			v10 + "','" +
			v11 + "','" +
			v12 + "'))" +
			" WHERE " +
			"         column_value  not in (" +
			"         		select SA.name from PIN.SERVICE_ALIAS_LIST_T SA) order by column_value";
	
		//System.out.println("QueryMS: " + query);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			//System.out.println("INSIDE 1");
			
			while (rs.next()) {
				//System.out.println("query data" + rs.getString(1));
				return rs.getString(1);
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}
		return "";
	}


	public void getOptionDetailsForDelUpdate(String root_inst_id, String product_id, MasterData md) throws DBException{
		
		String query = "SELECT " +
				"       DISTINCT " +
				"       AP.ACS_CUSTOMER," +
				"       AP.PRODUCT_INT_ID" +
			//	"       AP.ROOT_INT_ID" +
				"  FROM " +
				"        ACSCONFIG.ACS_E2E_PRODUCT_MAPPING   E, " +
				"        ACSCONFIG.ACS_PROD_INST  AP " +
				" WHERE " +
				"         E.SBL_PRODUCT_ID = '" + product_id + "'" +
				"        AND AP.ROOT_INT_ID = '" + root_inst_id + "'" +
				"        AND AP.PARENT_INT_ID is not null " +
				"		 AND SUBSTR (AP.deal_id,(INSTR (AP.deal_id, ' ', 1, 2) + 1),((INSTR (AP.deal_id, ' ', 1, 3))-(INSTR (AP.deal_id, ' ', 1, 2) + 1 )))  =  SUBSTR (E.deal_id,(INSTR (E.deal_id, ' ', 1, 2) + 1),((INSTR (E.deal_id, ' ', 1, 3))-(INSTR (E.deal_id, ' ', 1, 2) + 1 ))) ";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			//System.out.println("INSIDE 1");
			/*if(!rs.next()){
				
				System.out.println("No such deal exists....");
			}else{
			*/
			while (rs.next()) {
			//	System.out.println("query data" + rs.getString(1));
				md.setCUSTOMER_CODE(rs.getString(1));
			//	System.out.println(md.getCUSTOMER_CODE());
				md.setPROD_INST_CODE(rs.getString(2));
			//	System.out.println(md.getPROD_INST_CODE());
			//	md.setROOT_INST_CODE(rs.getString(3));
			//	System.out.println(md.getROOT_INST_CODE());
				
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}
		
	}
	
	//added for M2M
public void getOptionDetailsForDelUpdate1(String root_inst_id, String product_id, MasterData md) throws DBException{
	System.out.println("eeeeeeeeeeeeeM2M");
		String query = "SELECT " +
				"       DISTINCT " +
				"       AP.ACS_CUSTOMER," +
				"       AP.PRODUCT_INT_ID" +
			//	"       AP.ROOT_INT_ID" +
				"  FROM " +
				"        ACSCONFIG.ACS_M2M_PRODUCT_MAPPING   E, " +
				"        ACSCONFIG.ACS_PROD_INST  AP " +
				" WHERE " +
				"         E.SBL_PRODUCT_ID = '" + product_id + "'" +
				"        AND AP.ROOT_INT_ID = '" + root_inst_id + "'" +
				"        AND AP.PARENT_INT_ID is not null " +
				"		 AND SUBSTR (AP.deal_id,(INSTR (AP.deal_id, ' ', 1, 2) + 1),((INSTR (AP.deal_id, ' ', 1, 3))-(INSTR (AP.deal_id, ' ', 1, 2) + 1 )))  =  SUBSTR (E.deal_id,(INSTR (E.deal_id, ' ', 1, 2) + 1),((INSTR (E.deal_id, ' ', 1, 3))-(INSTR (E.deal_id, ' ', 1, 2) + 1 ))) ";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			//System.out.println("INSIDE 1");
			/*if(!rs.next()){
				
				System.out.println("No such deal exists....");
			}else{
			*/
			while (rs.next()) {
			//	System.out.println("query data" + rs.getString(1));
				md.setCUSTOMER_CODE(rs.getString(1));
			//	System.out.println(md.getCUSTOMER_CODE());
				md.setPROD_INST_CODE(rs.getString(2));
			//	System.out.println(md.getPROD_INST_CODE());
			//	md.setROOT_INST_CODE(rs.getString(3));
			//	System.out.println(md.getROOT_INST_CODE());
				
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}
		
	}
	
	public ArrayList<InputPojoAttributes> getAttributeDataforDel(String product_id) throws DBException{
		
		product_id = product_id.trim();
		ArrayList<InputPojoAttributes> attributeData = new ArrayList<InputPojoAttributes>();
		
		String query = "(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG1    as NAME," +
						"       'SBL_BILL_PARAMETER_TAG1'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"            MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND MAST.SBL_BILL_PARAMETER_TAG1 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG2 as NAME," +
						"       'SBL_BILL_PARAMETER_TAG2'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG2 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG3 as NAME," +
						"       'SBL_BILL_PARAMETER_TAG3'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG3 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG4 as NAME," +
						"       'SBL_BILL_PARAMETER_TAG4'    as COLUM    " +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG4 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST. SBL_ERA_TAG as NAME," +
						"       'SBL_ERA_TAG'    as COLUM" +
						"  FROM " +
						"        ACS_E2E_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_E2E_PRODUCT_MAPPING        E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_ERA_TAG <> 'NONE' " +
						")"  ;
		
		
		//System.out.println(query);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			while (rs.next()) {
				InputPojoAttributes ip = new InputPojoAttributes();
			//	System.out.println(rs.getString(1)  + rs.getString(2));
				
				ip.setname(rs.getString(1));
				ip.setvalue("Default");
				ip.setcolum(rs.getString(2));
				
				attributeData.add(ip);
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}
		return attributeData;
	}
	
	
	//added for M2M
public ArrayList<InputPojoAttributes> getAttributeDataforDel1(String product_id) throws DBException{
	System.out.println("qqqqqqqqqqqqqqM2M");
		product_id = product_id.trim();
		ArrayList<InputPojoAttributes> attributeData = new ArrayList<InputPojoAttributes>();
		
		String query = "(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG1    as NAME," +
						"       'SBL_BILL_PARAMETER_TAG1'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"            MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND MAST.SBL_BILL_PARAMETER_TAG1 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG2 as NAME," +
						"       'SBL_BILL_PARAMETER_TAG2'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG2 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG3 as NAME," +
						"       'SBL_BILL_PARAMETER_TAG3'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG3 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST.SBL_BILL_PARAMETER_TAG4 as NAME," +
						"       'SBL_BILL_PARAMETER_TAG4'    as COLUM    " +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING             E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_BILL_PARAMETER_TAG4 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       MAST. SBL_ERA_TAG as NAME," +
						"       'SBL_ERA_TAG'    as COLUM" +
						"  FROM " +
						"        ACS_M2M_PRODUCT_CONFIGURATION  MAST," +
						"        ACS_M2M_PRODUCT_MAPPING        E2E" +
						" WHERE " +
						"         MAST.SBL_PRODUCT_ID = E2E.SBL_PRODUCT_ID" +
						"         AND E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"         AND MAST.SBL_ERA_TAG <> 'NONE' " +
						")"  ;
		
		
		//System.out.println(query);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			while (rs.next()) {
				InputPojoAttributes ip = new InputPojoAttributes();
			//	System.out.println(rs.getString(1)  + rs.getString(2));
				
				ip.setname(rs.getString(1));
				ip.setvalue("Default");
				ip.setcolum(rs.getString(2));
				
				attributeData.add(ip);
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}
		return attributeData;
	}
	
	
	public ArrayList<InputOptionUpgrade> getAccDetailsForUpgr(String cust_code) throws DBException{
		
		ArrayList<InputOptionUpgrade> attributeData = new ArrayList<InputOptionUpgrade>();
		
		String query = "SELECT " +
				"       DISTINCT " +
				"       AP.ACS_BILLING_ACCOUNT" +
				"  FROM " +
				"        PIN.SERVICE_ALIAS_LIST_T SA1, " +
				"        ACSCONFIG.ACS_PROD_INST  AP " +
				" WHERE " +
				"         AP.acs_customer = '" + cust_code + "'" +
				"        AND (SA1.name not like 'TX%' and SA1.name like '39%') " +
				"        AND AP.PARENT_INT_ID is null " +
				"		 AND SA1.obj_id0 = SUBSTR (OWNER_POID,(INSTR (OWNER_POID, ' ', 1, 2) + 1),((INSTR (OWNER_POID, ' ', 1, 3))-(INSTR (OWNER_POID, ' ', 1, 2) + 1 )))  ";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			
			//System.out.println("INSIDE 1");
			
			while (rs.next()) {
				InputOptionUpgrade io = new InputOptionUpgrade();
	
				io.setAccount_code(rs.getString(1));
				
				attributeData.add(io);
				
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}
		
		return attributeData;
	}
	
	public ArrayList<InputOptionUpgrade> getOptionDetailsForUpgr(String acct_code, MasterData md) throws DBException{
		
		ArrayList<InputOptionUpgrade> attributeData = new ArrayList<InputOptionUpgrade>();
		
		String query = "SELECT " +
				"       DISTINCT " +
				"       SA1.name," +
				"       AP.ROOT_INT_ID," +
				"       AP.ACS_BILLING_ACCOUNT," +
				"       AP.ACS_CUSTOMER" +
				"  FROM " +
				"        PIN.SERVICE_ALIAS_LIST_T SA1, " +
				"        ACSCONFIG.ACS_PROD_INST  AP " +
				" WHERE " +
				"         AP.acs_billing_account = '" + acct_code + "'" +
				"        AND (SA1.name not like 'TX%' and SA1.name like '39%') " +
				"        AND AP.PARENT_INT_ID is null " +
				"		 AND SA1.obj_id0 = SUBSTR (OWNER_POID,(INSTR (OWNER_POID, ' ', 1, 2) + 1),((INSTR (OWNER_POID, ' ', 1, 3))-(INSTR (OWNER_POID, ' ', 1, 2) + 1 )))  ";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();	
			//System.out.println("INSIDE 1");
			
			while (rs.next()) {
				InputOptionUpgrade io = new InputOptionUpgrade();
				
				io.setMsisdn(rs.getString(1));
				io.setRoot_inst_id(rs.getString(2));
				io.setAccount_code(rs.getString(3));
				//System.out.println("%%%%%"+io.getAccount_code());
				
				attributeData.add(io);
			//	System.out.println(rs.getString(4));
				md.setCUSTOMER_CODE(rs.getString(4));
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}
		
		return attributeData;
	}
	
	//added for M2M
/*public ArrayList<InputOptionUpgrade> getOptionDetailsForUpgr1(String ROOT_INST_CODE, MasterData md) throws DBException{
		
		ArrayList<InputOptionUpgrade> attributeData = new ArrayList<InputOptionUpgrade>();
		System.out.println("fffffffffffffffM2M");
		String query = "SELECT " +
				"       DISTINCT " +
				"       SA1.name," +
				"       AP.ROOT_INT_ID," +
				"       AP.ACS_BILLING_ACCOUNT," +
				"       AP.ACS_CUSTOMER" +
				"  FROM " +
				"        PIN.SERVICE_ALIAS_LIST_T SA1, " +
				"        ACSCONFIG.ACS_PROD_INST  AP " +
				" WHERE " +
				"         AP.acs_billing_account = 'LIKE''"  + ROOT_INST_CODE + "'" +
				"        AND (SA1.name not like 'TX%' and SA1.name like '39%') " +
				"        AND AP.PARENT_INT_ID is null " +
				"		 AND SA1.obj_id0 = SUBSTR (OWNER_POID,(INSTR (OWNER_POID, ' ', 1, 2) + 1),((INSTR (OWNER_POID, ' ', 1, 3))-(INSTR (OWNER_POID, ' ', 1, 2) + 1 )))  ";
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			System.out.println("*********"+query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();	
			//System.out.println("INSIDE 1");
			
			while (rs.next()) {
				InputOptionUpgrade io = new InputOptionUpgrade();
				
				io.setMsisdn(rs.getString(1));
				io.setRoot_inst_id(rs.getString(2));
				io.setAccount_code(rs.getString(3));
				System.out.println("%%%%%"+io.getAccount_code());
				
				attributeData.add(io);
			//	System.out.println(rs.getString(4));
				md.setCUSTOMER_CODE(rs.getString(4));
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
			rs.close();
			stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}
		
		return attributeData;
	}*/
	
	public void getMarketSegment(String custCode, MasterData md, AcsLabels al) throws DBException{
		
		String query = "SELECT " +
				"       ACSFLDMARKETSEGMENT " +
				"  FROM " +
				"        PIN.ACSCUSTOMERPROFILE_T " +
				" WHERE " +
				"         ACSFLDACCOUNTNO = '" + custCode + "'" ;
				
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
					
			while (rs.next()) {
				//System.out.println("query data" + rs.getString(1));
				md.setMARKET_SEGMENT(al.markSeg.get(rs.getString(1)));	
				//System.out.println(md.getMARKET_SEGMENT());
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}		
	}
	
	public boolean check_account_level_deal(String product_id) throws DBException{
		
		Boolean flag = false;
		
		String query = "SELECT " +
				"       COUNT(SBL_PRODUCT_ID) " +
				"  FROM " +
				"        ACSCONFIG.ACS_E2E_PRODUCT_MAPPING " +
				" WHERE " +
				"         ENTITY_TYPE = 'Account Level Deal' " +
				"		 AND SBL_PRODUCT_ID = '" + product_id + "'   ";
		
	//	System.out.println("query: "+query);
				
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
					
			while (rs.next()) {
				if(rs.getInt(1)!=0){
					flag = true;
				//	System.out.println("flag: enabled");
				} else {
				//	System.out.println("flag: not enabled");
				}
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				System.out.println("SQL DB error");
			}
		}	
		
		//System.out.println("flag: "+flag);
		return flag;
	}
	
	//added for M2M
public boolean check_account_level_deal1(String product_id) throws DBException{
		
		Boolean flag = false;
		String query = "SELECT " +
				"       COUNT(SBL_PRODUCT_ID) " +
				"  FROM " +
				"        ACSCONFIG.ACS_M2M_PRODUCT_MAPPING " +
				" WHERE " +
				"         ENTITY_TYPE = 'Account Level Deal' " +
				"		 AND SBL_PRODUCT_ID = '" + product_id + "'   ";
		
	//	System.out.println("query: "+query);
				
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			//System.out.println(query);
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
					
			while (rs.next()) {
				if(rs.getInt(1)!=0){
					flag = true;
				//	System.out.println("flag: enabled");
				} else {
				//	System.out.println("flag: not enabled");
				}
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}	
		
		//System.out.println("flag: "+flag);
		return flag;
	}
	
	
	public ArrayList<InputPojoAttributes> getAttributeDataForAccountLevel(String product_id, String tariff_id) throws DBException{
		
		product_id = product_id.trim();
		tariff_id = tariff_id.trim();
		ArrayList<InputPojoAttributes> attributeData = new ArrayList<InputPojoAttributes>();
		
		String query = "(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_1 as NAME," +
						"       'EXT_PARAM_TAG_1' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"        E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_1 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.OVERRIDE_PARAMETER_TAG_1 as NAME," +
						"       'OVERRIDE_PARAMETER_TAG_1' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"         E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.OVERRIDE_PARAMETER_TAG_1 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.OVERRIDE_PARAMETER_TAG_2 as NAME," +
						"       'OVERRIDE_PARAMETER_TAG_2' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.OVERRIDE_PARAMETER_TAG_2 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.OVERRIDE_PARAMETER_TAG_3 as NAME," +
						"       'OVERRIDE_PARAMETER_TAG_3' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.OVERRIDE_PARAMETER_TAG_3 <> 'NONE' " +
						")" +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.OVERRIDE_PARAMETER_TAG_4 as NAME," +
						"       'OVERRIDE_PARAMETER_TAG_4' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.OVERRIDE_PARAMETER_TAG_4 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_2 as NAME," +
						"       'EXT_PARAM_TAG_2' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_2 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_3 as NAME," +
						"       'EXT_PARAM_TAG_3' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_3 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_4 as NAME," +
						"       'EXT_PARAM_TAG_4' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_4 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_5 as NAME," +
						"       'EXT_PARAM_TAG_5' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_5 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_6 as NAME," +
						"       'EXT_PARAM_TAG_6' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_6 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_7 as NAME," +
						"       'EXT_PARAM_TAG_7' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_7 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_8 as NAME," +
						"       'EXT_PARAM_TAG_8' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_8 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_9 as NAME," +
						"       'EXT_PARAM_TAG_9' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_9 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_10 as NAME," +
						"       'EXT_PARAM_TAG_10' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_10 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_11 as NAME," +
						"       'EXT_PARAM_TAG_11' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_11 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_12 as NAME," +
						"       'EXT_PARAM_TAG_12' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_12 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_13 as NAME," +
						"       'EXT_PARAM_TAG_13' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_13 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_14 as NAME," +
						"       'EXT_PARAM_TAG_14' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_14 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_15 as NAME," +
						"       'EXT_PARAM_TAG_15' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_15 <> 'NONE' " +
						")"  +
						"UNION" +
						"(" +
						"SELECT " +
						"       DISTINCT " +
						"       E2E.EXT_PARAM_TAG_16 as NAME," +
						"       'EXT_PARAM_TAG_16' as COLUM    " +
						"  FROM " +
						"        ACS_E2E_ATTRIBUTES_CONFIG             E2E" +
						" WHERE " +
						"          E2E.SBL_PRODUCT_ID = '" + product_id + "'" +
						"        AND E2E.EXT_PARAM_TAG_16 <> 'NONE' " +
						")"  ;
		
		
		//System.out.println(query);
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			stmt = dbConn.prepareStatement(query);
			rs = stmt.executeQuery();
			
			while (rs.next()) {
				InputPojoAttributes ip = new InputPojoAttributes();
			//	if(rs.getString(1) != null || rs.getString(1) != "")
			//	System.out.println(rs.getString(1));
				ip.setname(rs.getString(1));
				ip.setcolum(rs.getString(2));
				
				attributeData.add(ip);
			}
			
		} catch (SQLException ex) {
			throw new DBException("Error with query: " + query, ex);
		} finally {
			try{
				if(rs != null)
					rs.close();
				
				if(stmt != null)
					stmt.close();
			}catch(SQLException sqe){
				throw new DBException("Error with query: " + query, sqe);
			}
		}
		return attributeData;
	}
}
