package utils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import play.Logger;


public class DaysChecking {

	public static  boolean isPublicHoliday(int day, int month, int year, String country) {
	    boolean result = false;
	    
	    String dateAsString = day+"-"+month+"-"+year;
	    //Logger.info(dateAsString);
	    String httpRequest="http://kayaposoft.com/enrico/json/v1.0/?action=isPublicHoliday&date="+dateAsString+"&country="+country;
//System.out.println(httpRequest);
		//HttpClient client = null ;//= new HttpClient();
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(httpRequest);
		try {
			CloseableHttpResponse response = client.execute(httpGet);

			HttpEntity entity = response.getEntity();
		//BufferedReader rd = new BufferedReader (new InputStreamReader(((BasicHttpResponse) response).getEntity().getContent()));
		
		String jsonText = EntityUtils.toString(entity);
		JSONObject json = new JSONObject(jsonText);
		//System.out.println("jsontxt"+jsonText);
		result = json.getBoolean("isPublicHoliday");
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			result = false;
		}
		//if(result)
		//{
		 //   System.out.println(httpRequest);

			//System.out.println(dateAsString + " is a public holiday!");
		//}
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return result;

	}
	
	/**
	  * Return last day of week before specified date.
	  * @param date - reference date.
	  * @param day - DoW field from Calendar class.
	  * @return
	  */
	public static int getDayOfWeek(int day, int month, int year) {

	    Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.DAY_OF_MONTH,day);
	    cal.set(Calendar.MONTH,month-1);
	    cal.set(Calendar.YEAR,year);
		//Logger.info("Checking ==>"+new Date(cal.getTimeInMillis()));

	    //cal.set(Calendar.DAY_OF_WEEK, day);
	    //cal.set(Calendar.HOUR_OF_DAY,0);
	    //cal.set(Calendar.MINUTE,0);
	    //cal.set(Calendar.SECOND,0);
	    //cal.set(Calendar.MILLISECOND,0);
	    //Logger.info("Day of week"+cal.get(Calendar.DAY_OF_WEEK));
	    return cal.get(Calendar.DAY_OF_WEEK);
	}
	
	public static int thisYear (){
	    Calendar cal = Calendar.getInstance();
	    return (cal.get(Calendar.YEAR));
	}
	public static int thisMonth (){
	    Calendar cal = Calendar.getInstance();
	    return (cal.get(Calendar.MONTH)+1);
	}
	
	public static int thisDay (){
	    Calendar cal = Calendar.getInstance();
	    return (cal.get(Calendar.DAY_OF_MONTH));
	}
	
	public static boolean checkDate (int day, int month , int year){
		int thisYear = utils.DaysChecking.thisYear();

		if(!(month>0 && month <=12 && year >2014 && year <= thisYear )){
			return false;
		}
		else{
			try{Calendar cal = Calendar.getInstance();
			cal.set(cal.MONTH, month);
			cal.set(cal.YEAR, year);
			cal.set(cal.DAY_OF_MONTH, day);
			}
			catch (Exception e){
				return false;
			}
			return true;			
		}

	}
	
	public static int getNumberOfdaysOfMonth( int month, int year) 	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR,year);
		cal.set(Calendar.MONTH, month);
	    cal.set(Calendar.DAY_OF_MONTH,1);
	    cal.set(Calendar.HOUR_OF_DAY,0);
	    cal.set(Calendar.MINUTE,0);
	    cal.set(Calendar.SECOND,0);
	    cal.set(Calendar.MILLISECOND,0);
	    
		Date EndDate= new Date(cal.getTime().getTime() -1);
	    cal.setTime(EndDate);

		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	public static String getInterval(int month, int year){

		String result = "";
		  int day =0;
		  int m2 = month;
		  int y2 = year;
		  if (month ==Calendar.getInstance().get(Calendar.MONTH)+1 && year ==Calendar.getInstance().get(Calendar.YEAR)) { 
			  day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		  }
		  else
		  {
			  day = 1;
			  m2 ++;
			  if (m2 == 13){
				  m2 = 1 ;
				  y2 ++;
			  }
		  }
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result = year + "-" + String.format("%02d", month) +"-01/"+y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", day);
		return result;
	}
	

	
	public static String getInterval(int day,int month, int year){

		String result = "";
		  int d2 =day;
		  int m2 = month;
		  int y2 = year;
		  

		  Calendar cl = Calendar.getInstance(); // create an instance of calendar with the date provided
		  cl.set(cl.DAY_OF_MONTH, day);
		  cl.set(cl.MONTH, month-1);
		  cl.set(cl.YEAR, year);
		  //Logger.info("Date = " + new Date(cl.getTimeInMillis()));

		  cl.add(cl.DAY_OF_MONTH, 1); // add a day to the current calendar 
		  
		  d2 = cl.get(cl.DAY_OF_MONTH); // get the date of the following day
		  m2= cl.get(cl.MONTH) +1;
		  y2 = cl.get(cl.YEAR);
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result = year + "-" + String.format("%02d", month) +"-"+String.format("%02d", day)+"/"+y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", d2);
		return result;
	}
	
	public static String getYesterdayInterval(int day,int month, int year){

		String result = "";
		  int d2 =day;
		  int m2 = month;
		  int y2 = year;
		  

		  Calendar cl = Calendar.getInstance(); // create an instance of calendar with the date provided
		  cl.set(cl.DAY_OF_MONTH, day);
		  cl.set(cl.MONTH, month-1);
		  cl.set(cl.YEAR, year);
		  //Logger.info("Date = " + new Date(cl.getTimeInMillis()));

		  cl.add(cl.DAY_OF_MONTH, 1); // add a day to the current calendar 
		  
		  d2 = cl.get(cl.DAY_OF_MONTH); // get the date of the following day
		  m2= cl.get(cl.MONTH) +1;
		  y2 = cl.get(cl.YEAR);
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result =year + "-" + String.format("%02d", month) +"-"+String.format("%02d", day)+"/"+y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", d2);
		return result;
	}
	
	public static Date startDateOfMonth(int startMonth,int startYear){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR,startYear);
		cal.set(Calendar.MONTH, startMonth-1);
	    cal.set(Calendar.DAY_OF_MONTH,1);
	    cal.set(Calendar.HOUR_OF_DAY,0);
	    cal.set(Calendar.MINUTE,0);
	    cal.set(Calendar.SECOND,0);
	    cal.set(Calendar.MILLISECOND,0);
	    
		Date StartDate = (Date) cal.getTime();
		
		return StartDate;
	}
	
	
	public static Date endDateOfMonth(int startMonth,int startYear){
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR,startYear);
		cal.set(Calendar.MONTH, startMonth-1);
	    cal.set(Calendar.DAY_OF_MONTH,1);
	    cal.set(Calendar.HOUR_OF_DAY,0);
	    cal.set(Calendar.MINUTE,0);
	    cal.set(Calendar.SECOND,0);
	    cal.set(Calendar.MILLISECOND,0);
	    
		cal.add(Calendar.MONTH, +1);
		Date EndDate= new Date(cal.getTime().getTime() -1);
		return EndDate;
	}
	
	public static int getMonth(long time){
		int result ;
	
		Calendar cl =  Calendar.getInstance();
		cl.setTimeInMillis(time);
		result =  cl.get(Calendar.MONTH);
		result ++;
		
		return result;
	
	}
	
	public static int getYesterdayMonth(long time){
		
		int result ;
		
		Calendar cl =  Calendar.getInstance();
		cl.setTimeInMillis(time);
		cl.add(Calendar.DAY_OF_MONTH, -1);
		result =  cl.get(Calendar.MONTH);
		result ++;
		
		return result;
	
	}
	
	public static int getDayOfMonth(long time){
	
		Calendar cl =  Calendar.getInstance();
		cl.setTimeInMillis(time);
		
		return cl.get(Calendar.DAY_OF_MONTH);
	
	}
	
	public static int getYesterdayDayOfMonth(long time){
		
		Calendar cl =  Calendar.getInstance();
		cl.setTimeInMillis(time);
		cl.add(Calendar.DAY_OF_MONTH, -1);
		
		return cl.get(Calendar.DAY_OF_MONTH);
	
	}
	
	public static int getYesterdayYear(long time){
		
		Calendar cl =  Calendar.getInstance();
		cl.setTimeInMillis(time);
		cl.add(Calendar.DAY_OF_MONTH, -1);
		
		return cl.get(Calendar.YEAR);
	
	}
	
	public static int getYear(long time){
	
		Calendar cl =  Calendar.getInstance();
		cl.setTimeInMillis(time);
		
		return cl.get(Calendar.YEAR);
	
	}

	public static String getLastWeekInterval(int day, int month, int year) {
		String result = "";
		  int d2 =day;
		  int m2 = month;
		  int y2 = year;
		  

		  Calendar cl = Calendar.getInstance(); // create an instance of calendar with the date provided
		  cl.set(cl.DAY_OF_MONTH, day);
		  cl.set(cl.MONTH, month-1);
		  cl.set(cl.YEAR, year);
		  //Logger.info("Date = " + new Date(cl.getTimeInMillis()));

		  cl.add(cl.DAY_OF_MONTH, -7); // add a day to the current calendar 
		  
		  d2 = cl.get(cl.DAY_OF_MONTH); // get the date of the following day
		  m2= cl.get(cl.MONTH) +1;
		  y2 = cl.get(cl.YEAR);
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result = y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", d2)+"/"+year + "-" + String.format("%02d", month) +"-"+String.format("%02d", day);
		return result;
	}

	public static String getLast30DaysInterval(int day, int month, int year) {
		String result = "";
		  int d2 =day;
		  int m2 = month;
		  int y2 = year;
		  

		  Calendar cl = Calendar.getInstance(); // create an instance of calendar with the date provided
		  cl.set(cl.DAY_OF_MONTH, day);
		  cl.set(cl.MONTH, month-1);
		  cl.set(cl.YEAR, year);
		  //Logger.info("Date = " + new Date(cl.getTimeInMillis()));

		  cl.add(cl.DAY_OF_MONTH, -30); // add a day to the current calendar 
		  
		  d2 = cl.get(cl.DAY_OF_MONTH); // get the date of the following day
		  m2= cl.get(cl.MONTH) +1;
		  y2 = cl.get(cl.YEAR);
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result = y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", d2)+"/"+year + "-" + String.format("%02d", month) +"-"+String.format("%02d", day);
		return result;
	}

	public static String getLastYearInterval(int day, int month, int year) {
		String result = "";
		  int d2 =day;
		  int m2 = month;
		  int y2 = year;
		  

		  Calendar cl = Calendar.getInstance(); // create an instance of calendar with the date provided
		  cl.set(cl.DAY_OF_MONTH, day);
		  cl.set(cl.MONTH, month-1);
		  cl.set(cl.YEAR, year);
		  //Logger.info("Date = " + new Date(cl.getTimeInMillis()));

		  cl.add(cl.YEAR,-1); // last year
		  
		  d2 = cl.get(cl.DAY_OF_MONTH); // get the date of the following day
		  m2= cl.get(cl.MONTH) +1;
		  y2 = cl.get(cl.YEAR);
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result = y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", d2)+"/"+year + "-" + String.format("%02d", month) +"-"+String.format("%02d", day);
		return result;
	}

	public static String getLast10YearsInterval(int day, int month, int year) {
		String result = "";
		  int d2 =day;
		  int m2 = month;
		  int y2 = year;
		  

		  Calendar cl = Calendar.getInstance(); // create an instance of calendar with the date provided
		  cl.set(cl.DAY_OF_MONTH, day);
		  cl.set(cl.MONTH, month-1);
		  cl.set(cl.YEAR, year);
		  //Logger.info("Date = " + new Date(cl.getTimeInMillis()));

		  cl.add(cl.YEAR, -10); // 10 year erlier
		  
		  d2 = cl.get(cl.DAY_OF_MONTH); // get the date of the following day
		  m2= cl.get(cl.MONTH) +1;
		  y2 = cl.get(cl.YEAR);
//		  if (year == 2016){
//			  year= 2015; //TODO this needs to be removed!!!
//			  y2 --;
//		  }
		result = y2 + "-" + String.format("%02d", m2)+"-"+String.format("%02d", d2)+"/"+year + "-" + String.format("%02d", month) +"-"+String.format("%02d", day);
		return result;
	}
}
