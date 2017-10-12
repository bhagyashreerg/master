package clientWebservice;
import java.io.IOException;

public class PropertiesException extends Exception{
	public PropertiesException(String message) {
		super(message);
	}
	public PropertiesException(String message, IOException e) {
		super(message, e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
}
