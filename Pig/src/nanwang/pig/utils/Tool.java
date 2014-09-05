package nanwang.pig.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class provides utilities functions
 * 
 * @author wangnan
 * @since 2014-05-01
 */

public class Tool {
	
	/**
	 * This method is used for obtaining the current time and date 
	 * with format yyyy/MM/dd HH:mm:ss:SSS
	 * @return
	 */
	
	public static String getCurrentTime(){
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS");
		return "[" + formatter.format(date) + "]";
	}

	/**
	 * This method is used for obtaining the date
	 * @return
	 */
	public static String getDate(){
		Date date = new Date();
		DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
		return formatter.format(date);
	}
	
	/**
	 * This method is used for extracting the Pig script name from file path String
	 * @param string
	 * @return String
	 */
	public static String extractPigName(String filePath){
		String[] list = filePath.split("/");
		return list[list.length - 1];
	}
	
	/**
	 * Concatenate the strings
	 * @param strings
	 * @return StringBuilder
	 */
	public static StringBuilder join(String... strings){
		StringBuilder stringBuilder = new StringBuilder();
		for(String string : strings){
			if(string != null){
				stringBuilder.append(string);
			}
		}
		return stringBuilder;
	}
	
	/**
	 * 
	 * @param string
	 * @return
	 */
	public static String[] toArray(String string) {
	    return string == null ? new String[0] : string.trim().split(",");
	}
}
