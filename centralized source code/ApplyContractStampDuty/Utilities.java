package ContractStampDuty;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.portal.pcm.Poid;

public class Utilities {

	public static Poid stringToPoid(String poidString) {
		// In prod mapping table the poid is a string with this form: "0.0.0.db
		// type id rev"
		StringTokenizer st = new StringTokenizer(poidString, ". ");
		st.nextToken();// first zero
		st.nextToken();// second zero
		st.nextToken();// third zero
		long db = Long.parseLong(st.nextToken());
		String type = st.nextToken();
		long id = Long.parseLong(st.nextToken());

		return new Poid(db, id, type);
	}
	
	public static int monthsBetween(Date futureDate, Date currentDate)   
	{   
		Calendar cal = Calendar.getInstance();   
		cal.setTime(futureDate);   
		int futureMonth =  cal.get(Calendar.MONTH);   
		int futureYear = cal.get(Calendar.YEAR);   
		cal.setTime(currentDate);   
		int currentMonth =  cal.get(Calendar.MONTH);   
		int currentYear = cal.get(Calendar.YEAR);   
	
		return ((futureYear - currentYear) * cal.getMaximum(Calendar.MONTH)) + (futureMonth - currentMonth);   
	} 
	
	/**
	 * This method retrieves first day of the month
	 * @param inputdate
	 * @return
	 */
	public static Date getFirstDayOfMonth(Date inputdate) {
		Date outputdate = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			//Date d = sdf.parse(inputdate);
			Calendar calendar = Calendar.getInstance();
			
			calendar.set(new Integer(new SimpleDateFormat("yyyy").format(inputdate))
					.intValue(), new Integer(new SimpleDateFormat("MM")
							.format(inputdate)).intValue()-1, new Integer(
									new SimpleDateFormat("dd").format(inputdate)).intValue());
					
			int minDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
			
			calendar.set(new Integer(new SimpleDateFormat("yyyy").format(inputdate))
					.intValue(), new Integer(new SimpleDateFormat("MM")
							.format(inputdate)).intValue()-1, minDay);
			
			outputdate = sdf.parse(sdf.format(calendar.getTime()));
		} catch (Exception e) {
			System.out.println("getFirstDayOfMonth: Error occured during getting first day of the month");
		}
		return outputdate;
	}
	
	/**
	 * This method retrieves last day of the month
	 * @param d
	 * @return
	 */
	public static Date getLastDayOfMonth(Date d) {
		Date date = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy 23:59:59");
			//Date d = sdf.parse(eStartDate);
			Calendar calendar = Calendar.getInstance();
			
			calendar.set(new Integer(new SimpleDateFormat("yyyy").format(d))
					.intValue(), new Integer(new SimpleDateFormat("MM")
							.format(d)).intValue() - 1, new Integer(new SimpleDateFormat("dd").format(d)).intValue());
			
			int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			calendar.set(new Integer(new SimpleDateFormat("yyyy").format(d))
					.intValue(), new Integer(new SimpleDateFormat("MM").format(d)).intValue() - 1, maxDay);
			
			date = sdf.parse(sdf.format(calendar.getTime()));
		} catch (Exception e) {
			System.out.println("Exception ");
		}
		return date;
	}

	/**
     * This method calculate current date timezone+daylight offset
	 * @return Current date timezone+daylight offset
	 */
	public static long deltaDate(){
		long delta = 0;
		GregorianCalendar gC = new GregorianCalendar();
		///System.out.println(gC.getTimeZone());
		try {
			delta = gC.get(Calendar.ZONE_OFFSET)+gC.get(Calendar.DST_OFFSET);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return delta;
	}

    /**
     * This method calculate input date timezone+daylight offset
     * @param data Input date
     * @param formato Formatter to be used to parse date
     * @return Current date timezone+daylight offset
     */
	public static long deltaDate(String data, SimpleDateFormat formato){
		long delta = 0;
		GregorianCalendar gC = new GregorianCalendar();

		long dDWH;
		try {
			dDWH = formato.parse(data).getTime();
			gC.setTimeInMillis(dDWH);
			delta = gC.get(Calendar.ZONE_OFFSET)+gC.get(Calendar.DST_OFFSET);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return delta;
	}
	
	public static Date fetchLatestDate(Date billStDate, Date billEdDate) {
		
		Calendar billEdDtCal = Calendar.getInstance();
		billEdDtCal.setTime(billEdDate);
		billEdDtCal.add(Calendar.MONTH, -2);
		Calendar billStDtCal = Calendar.getInstance();
		billStDtCal.setTime(billStDate);

		if (billEdDtCal.compareTo(billStDtCal) > 0) {
			return billEdDtCal.getTime();
		} else {
			return billStDtCal.getTime();
		}

	}
}
