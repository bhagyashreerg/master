package clientWebservice;

/**
 * New class for a better Exception management
 * @author mario.lagana
 * ISC00007428 06/03/2012 - This class has been created to improve daily performance
 */
public class DBException extends Exception {

	public DBException(String message, Exception e) {
		super(message, e);
	}

	public DBException(Exception e) {
		super(e);
	}

	public DBException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5545583718429477325L;

}
