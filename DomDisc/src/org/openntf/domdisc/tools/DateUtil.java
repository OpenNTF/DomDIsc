package org.openntf.domdisc.tools;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
	
	/*
	 * @return format is yyyy-MM-dd
	 */
	public static String getDateShort(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date);
	}

	/*
	 * @return format is yyyy-MM-dd HH:mm:ss
	 */
	public static String getDateLong(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return dateFormat.format(date);
	}

	/*
	 * @return format is yyyy-MM-ddTHH:mm:ss
	 */
	public static String getDateXml(Date date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ss");
		return dateFormat.format(date) + "T" + dateFormat2.format(date);
	}
	/**
	 * @param source
	 *            The lexical form of the date is yyyy '-' mm '-' dd 'T' HH ':'
	 *            mm ':' ss
	 * @return Returns Date.
	 */
	public static Date convertToDate(String source) {

		// Lexical form of the date is yyyy '-' mm '-' dd 'T' HH ':' mm ':' ss
		if ((source == null) || source.trim().equals("")) {
			return null;
		}
		source = source.trim();
		
		int year = 0;
		int month = 0;
		int day = 0;
		int hours = 0;
		int minits = 0;

		if (source.length() >= 10) {
			
			// first 10 numbers must give the year
			if ((source.charAt(4) != '-') || (source.charAt(7) != '-')) {
				throw new RuntimeException("invalid date format (" + source
						+ ") with out - s at correct place ");
			}
			year = Integer.parseInt(source.substring(0, 4));
			month = Integer.parseInt(source.substring(5, 7));
			day = Integer.parseInt(source.substring(8, 10));

			if (source.length() > 10) {
				String restpart = source.substring(10);
				if (restpart.startsWith("T")) {
					// this is a specific time format string
					if (restpart.charAt(3) != ':') {
						throw new RuntimeException("invalid time format ("
								+ source + ") without : at correct place");
					}
					hours = Integer.parseInt(restpart.substring(1, 3));
					minits = Integer.parseInt(restpart.substring(4, 6));
				} else {
					throw new RuntimeException("In valid string sufix");
				}
			}
		} else {
			throw new RuntimeException("In valid string to parse");
		}

		Calendar calendar = Calendar.getInstance();
		calendar.clear();

		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month - 1);
		calendar.set(Calendar.DAY_OF_MONTH, day);

		calendar.set(Calendar.HOUR, hours);
		calendar.set(Calendar.MINUTE, minits);

		return calendar.getTime();

	}
}
