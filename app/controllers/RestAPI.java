package controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import play.Logger;
import play.db.jpa.JPA;
import play.mvc.Controller;
import play.mvc.Util;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.json.JSONException;
import org.json.JSONObject;

import models.Reading;


public class RestAPI extends Controller {

    public static void index() {
        render();
    }
    
//    public static void getAnalytics(String sensorID, int month, int year) throws Exception {
//		System.out.println("==============get analytics from restapi =====================");
//		
//		int mm = month;
//		int yy = year;
//		
//		if(sensorID==null || sensorID==""){
//			sensorID="309";
//		}
//		if(month == 0){
//			mm = utils.DaysChecking.getMonth(System.currentTimeMillis());
//		}
//		if (year ==0){
//			yy = utils.DaysChecking.getYear(System.currentTimeMillis());
//		}
//		
//		int queries =0; // counter used to limit the querying to only 5 months before the actual day.
//		boolean exit = false;
//		while(!exit){
//			queries++;
//		
//			Logger.info("Month = "+mm +" year = "+yy);
//		
//			Readings.getAllreadingsFromDRUID(sensorID, mm, yy);
//		
//			if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0 && queries<1){
//		    	mm --;
//		    	if (mm ==0){
//		    		mm = 12;
//		    		yy --;
//		    	}
//			}
//			else{
//			exit = true;
//			}
//		} 
//		if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0){
//			
//				for (int i = 1 ; i < queries ; i++){
//					mm ++;
//			    	if (mm ==13){
//			    		mm = 1;
//			    		yy ++;
//			    	}	
//				}
//				
//			}
//		Collections.sort(Readings.allReadings, new Comparator<Reading>() {
//		
//		    public int compare(Reading r1, Reading r2) {
//		        if (r2.getReadingTimeStamp() > r1.getReadingTimeStamp()){
//		        	return -1;
//		        }
//		        else{
//		        	return 1;
//		        }
//		    }
//		});
//		//Logger.info("=======>", Readings.AVGWorkingDay);
//		RestAPI.renderJSON(Readings.allReadingsToHashMap(sensorID, month, year));
//    }
    
    private static Object getAnalysedData(String sensorID, int month, int year) throws Exception {
 		//System.out.println("==============get analytics from restapi =====================");
 		
 		int mm = month;
 		int yy = year;
 		
 		if(sensorID==null || sensorID==""){
 			sensorID="M1n";
 		}
 		if(month == 0){
 			mm = utils.DaysChecking.getMonth(System.currentTimeMillis());
 		}
 		if (year ==0){
 			yy = utils.DaysChecking.getYear(System.currentTimeMillis());
 		}
 		
 		int queries =0; // counter used to limit the querying to only 5 months before the actual day.
 		boolean exit = false;
 		while(!exit){
 			queries++;
 		
 			//Logger.info("Month = "+mm +" year = "+yy);
 		
 			Readings.getAllreadingsFromDRUID(sensorID, mm, yy);
 		
 			if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0 && queries<1){
 		    	mm --;
 		    	if (mm ==0){
 		    		mm = 12;
 		    		yy --;
 		    	}
 			}
 			else{
 			exit = true;
 			}
 		} 
 		if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0){
 			
 				for (int i = 1 ; i < queries ; i++){
 					mm ++;
 			    	if (mm ==13){
 			    		mm = 1;
 			    		yy ++;
 			    	}	
 				}
 				
 			}
 		//Readings.addThisMomthReadingsFromDRUID(sensorID, month, year);
 		Collections.sort(Readings.allReadings, new Comparator<Reading>() {
 		
 		    public int compare(Reading r1, Reading r2) {
 		        if (r2.getReadingTimeStamp() > r1.getReadingTimeStamp()){
 		        	return -1;
 		        }
 		        else{
 		        	return 1;
 		        }
 		    }
 		});
 		//Logger.info("=======>", Readings.AVGWorkingDay);
 		return Readings.allReadingsToHashMap(sensorID, month, year);
     }
    
    public static void getAnalytics(String APIKey, String userID,String sensorID, int month, int year) throws Exception {
		Logger.info("Request for Data Analytics from user "+userID+" for the sensor " + sensorID +" ["+month+"/"+year+"]");
		boolean resultsRendered = false;
		
		String result = "{\"error\": \"Unauthorised User!\"}";
    	if (utils.UserVerification.verifyIDandAPIKey(userID,APIKey)){
    		try {
    			//Logger.info("Authorised user!");
    			String dataSource = Organizations.getDataSourceFromSensorID(sensorID);
    			//Logger.info("Datasource ="+dataSource);
				if (!dataSource.contains("Unable to identify the datasource!")){
					if(Organizations.verifyUserandDataSource(userID,dataSource)){
						//result = Readings.getAllreadingsFromDRUID(jsonQuery);
						RestAPI.renderJSON( getAnalysedData(sensorID, month, year));
						resultsRendered = true;
					}else{
						result = "{\"error\": \"Unauthorised User!\"}";
					}
				}
				else{
					result = dataSource;
				}
				//Logger.info("result = "+result);
				if(result.length()==0){
					result = "{\"error\": \"No readings available for this sensor!\"}";
				}
			} catch (Exception e) {
				result = "{\"error\": \"Unable to retrieve data!\"}";
				e.printStackTrace();

			}
    	}

		//Logger.info("=======>", Readings.AVGWorkingDay);
		//RestAPI.renderJSON(Readings.allReadingsToHashMap(sensorID, month, year));
		if(!resultsRendered){
			RestAPI.renderJSON(result);
			}

    }
    
    public static void getDayAnalytics(String APIKey, String userID,String sensorID,  int day, int month, int year) throws Exception {
    	Logger.info("Request for Day Analytics from user "+userID+" for the sensor " + sensorID +" ["+day+"/"+month+"/"+year+"]");
		boolean resultsRendered = false;
		
		String result = "{\"error\": \"Unauthorised User!\"}";
    	if (utils.UserVerification.verifyIDandAPIKey(userID,APIKey)){
    		try {
    			//Logger.info("Authorised user!");
    			String dataSource = Organizations.getDataSourceFromSensorID(sensorID);
    			//Logger.info("Datasource ="+dataSource);
				if (!dataSource.contains("Unable to identify the datasource!")){
					if(Organizations.verifyUserandDataSource(userID,dataSource)){
						//result = Readings.getAllreadingsFromDRUID(jsonQuery);
						RestAPI.renderJSON( getAnalysedDayData(sensorID, day,month, year));
						resultsRendered = true;
					}else{
						result = "{\"error\": \"Unauthorised User!\"}";
					}
				}
				else{
					result = dataSource;
				}
				//Logger.info("result = "+result);
				if(result.length()==0){
					result = "{\"error\": \"No readings available for this sensor!\"}";
				}
			} catch (Exception e) {
				result = "{\"error\": \"Unable to retrieve data!\"}";
				e.printStackTrace();

			}
    	}

		//Logger.info("=======>", Readings.AVGWorkingDay);
		//RestAPI.renderJSON(Readings.allReadingsToHashMap(sensorID, month, year));
		if(!resultsRendered){
			RestAPI.renderJSON(result);
			}

    }
    
    public static void getRawReadings(String jsonQuery, String APIKey, String userID){
		//Logger.info("getRawReadings with jsonQuery = "+jsonQuery +" userid= "+userID+" and APIKey= "+APIKey );
    	Logger.info("Request for Raw Data by user: "+ userID);

    	String result = "{\"error\": \"Unauthorised User!\"}";
    	if (utils.UserVerification.verifyIDandAPIKey(userID,APIKey)){
    		try {
    			//Logger.info("Authorised user!");
    			String dataSource = getDataSourceFromQuery(jsonQuery);
    			//Logger.info("Datasource ="+dataSource);
				if (!dataSource.contains("Unable to identify the datasource!")){
					if(Organizations.verifyUserandDataSource(userID,dataSource)){
						result = Readings.getAllreadingsFromDRUID(jsonQuery);
					}else{
						result = "{\"error\": \"Unauthorised User!\"}";
					}
				}
				else{
					result = dataSource;
				}
				//Logger.info("result = "+result);
				if(result.length()==0){
					result = "{\"error\": \"No readings available for this query!\"}";
				}
			} catch (Exception e) {
				result = "{\"error\": \"Unable to retrieve data!\"}";
				e.printStackTrace();

			}
    	}
		//Logger.info("result = "+result);

//   if (repeat && result.contains("error")){
//		Organizations.updateOrganisations();
//		repeat = false;
//		getRawReadings( jsonQuery,  APIKey,  userID);
//		
//   }else{
//			repeat = true;
    		RestAPI.renderJSON(result);
//   }
    	
    }
    
    
    public static void getAggregatedReadings(String sensorID, String APIKey, String userID){
		//Logger.info("getRawReadings with jsonQuery = "+jsonQuery +" userid= "+userID+" and APIKey= "+APIKey );
    	Logger.info("Request for Aggregagted Data by user: "+ userID+" for the sensor " + sensorID);
    	String result = "{\"error\": \"Unauthorised User!\"}";
    	if (utils.UserVerification.verifyIDandAPIKey(userID,APIKey)){
    		try {
    			//Logger.info("Authorised user!");
    			String dataSource = Organizations.getDataSourceFromSensorID(sensorID);
    			//Logger.info("Datasource ="+dataSource);
				if (!dataSource.contains("Unable to identify the datasource!")){
					if(Organizations.verifyUserandDataSource(userID,dataSource)){
						result = Readings.getAllreadingsFromDRUIDBySensorID(sensorID);
					}else{
						result = "{\"error\": \"Unauthorised User!\"}";
					}
				}
				else{
					result = dataSource;
				}
				//Logger.info("result = "+result);
				if (result != null){
					if(result.length()==0){
						result = "{\"error\": \"No readings available for this sensor!\"}";
					}
				}else{
					result = "{\"error\": \"No Results availale : Unable to retrieve data!\"}";
				}
				
			} catch (Exception e) {
				result = "{\"error\": \"Unable to retrieve data!\"}";
				e.printStackTrace();

			}
    	}
		//Logger.info("result = "+result);

//   if (repeat && result.contains("error")){
//		Organizations.updateOrganisations();
//		repeat = false;
//		getRawReadings( jsonQuery,  APIKey,  userID);
//		
//   }else{
//			repeat = true;
    		RestAPI.renderJSON(result);
//   }
    	
    }
    
    
    private static String getDataSourceFromQuery(String jsonQuery){
    	String result;

    	try {
			JSONObject jsonObj = new JSONObject(jsonQuery);
			result = jsonObj.getString("dataSource");
		} catch (JSONException e) {
			result = "{\"error\": \"Unable to identify the datasource!\"}";
		}
    	
    	return result;
    }


//    public static void getDayAnalytics(String sensorID, int day, int month, int year) throws Exception {
//		System.out.println("==============get day analytics from restapi =====================");
//		double deviation=0 ;
//		String deviationLabel="";
//		int dd = day;
//		int mm = month;
//		int yy = year;
//		
//		if (day ==0){
//			dd = utils.DaysChecking.getDayOfMonth(System.currentTimeMillis());
//		}
//
//		if(month == 0){
//			mm = utils.DaysChecking.getMonth(System.currentTimeMillis());
//		}
//		if (year ==0){
//			yy = utils.DaysChecking.getYear(System.currentTimeMillis());
//		}
//		
//		
//		Reading rd = Readings.getOneDayReadingFromDRUID(sensorID, dd, mm, yy);
//		Logger.info("One day result , args); "+rd);
//
//		int backDays =0; // if a day does not have readings, we can search for the previous days while backDays < 10
//		while (rd==null && backDays < 10){
//			backDays++;
//			
//				dd--;
//				if(dd==0){
//					 mm--;
//					if(mm==0){
//						mm = 12;
//						yy --;
//					}
//					dd=utils.DaysChecking.getNumberOfdaysOfMonth(mm, yy);
//				}
//				rd = Readings.getOneDayReadingFromDRUID(sensorID, dd, mm, yy);
//
//		}
//		
//		if (rd != null){
//		int queries =0; // counter used to limit the querying to only 5 months before the actual day.
//		int previousMonth = mm -1;
//		int previousYear = yy;
//		if(previousMonth==0){
//			previousMonth = 12;
//			previousYear --;
//		}
//		boolean exit = false;
//		while(!exit){
//			queries++;
//		
//			Logger.info("Month = "+previousMonth +" year = "+previousYear);
//		
//			Readings.getAllreadingsFromDRUID(sensorID, previousMonth, previousYear);
//		
//			if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0 && queries<2){
//				previousMonth --;
//				if(previousMonth==0){
//					previousMonth = 12;
//					previousYear --;
//				}
//			}
//			else{
//			exit = true;
//			}
//		} 
//		if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0){
//			
//				for (int i = 1 ; i < queries ; i++){
//					previousMonth ++;
//					if(previousMonth==0){
//						previousMonth = 12;
//						previousYear --;
//					}	
//				}
//				
//			}
//		Collections.sort(Readings.allReadings, new Comparator<Reading>() {
//		
//		    public int compare(Reading r1, Reading r2) {
//		        if (r2.getReadingTimeStamp() > r1.getReadingTimeStamp()){
//		        	return -1;
//		        }
//		        else{
//		        	return 1;
//		        }
//		    }
//		});
////		//Logger.info("=======>", Readings.AVGWorkingDay);
//		
//
//		double avg =0;
//		double std =0;
//		String wDay =rd.getDay();
//		if(wDay.equals("no readings available")){
//			//check if this is a working day
//			//TODO
//			boolean publicholiday = utils.DaysChecking.isPublicHoliday(day, month, year, "irl");
//			if (publicholiday
//					||utils.DaysChecking.getDayOfWeek(day, month, year)== Calendar.SUNDAY
//					||utils.DaysChecking.getDayOfWeek(day, month, year)==Calendar.SATURDAY){
//				wDay = "non working day";
//			}
//			else {
//				//System.out.println();
//				wDay = "working day";
//			}
//		}
//
//		
//		if(wDay.equals("working day")) {
//			avg = Readings.AVGWorkingDay;
//			std = Readings.StDevWorkingDay;
//		}
//		else{
//			if(wDay.equals("non working day")) {
//				avg = Readings.AVGNonWorkingDay;
//				std = Readings.StDevNonWorkingDay;
//			}
//			else{
//				deviationLabel = "Unable to compute deviation!";
//			}
//		}
//		
//		if (avg >0){
//		deviation = ((rd.getReadingValue() - avg)/avg) * 100;
//
//		
//		if(rd.getReadingValue() >= avg + 1.5 * std){
//			deviationLabel = "Very High!";
//		}
//		
//		if(rd.getReadingValue() < avg + 1.5 * std && rd.getReadingValue() >= avg + std){
//				deviationLabel = "High!";
//		}
//		
//		if(rd.getReadingValue() < avg + std && rd.getReadingValue() >= avg - std){
//					deviationLabel = "Normal Consumption!";
//		}
//
//		if(rd.getReadingValue() < avg - std && rd.getReadingValue() >= avg - 1.5*std){
//						deviationLabel = "Low Consumption!";
//		}
//		
//		if(rd.getReadingValue() <= avg - 1.5 * std){
//						deviationLabel = "Very Low Consumption!";
//		}
//
//		}
//		else{
//			deviation = 0;
//			deviationLabel = "Normal Consumption!";
//		}
//				Object PreviousMonth = Readings.allReadingsToHashMap(sensorID, month, year);
//				RestAPI.renderJSON(Readings.OneReadingToHashMap(sensorID, dd, mm, yy, rd,deviation, deviationLabel, avg, std,previousMonth) );
//
//		}
//		else{
//			RestAPI.renderJSON(new HashMap<String, Object>().put("error", "No readings are available for the entered date!"));
//		}
//    }

    private static Object getAnalysedDayData(String sensorID, int day, int month, int year) throws Exception {
		//System.out.println("==============get day analytics from restapi =====================");
		double deviation=0 ;
		String deviationLabel="";
		int dd = day;
		int mm = month;
		int yy = year;
		
		if (day ==0){
			dd = utils.DaysChecking.getYesterdayDayOfMonth(System.currentTimeMillis());
		}
		if(month == 0){
			mm = utils.DaysChecking.getYesterdayMonth(System.currentTimeMillis());
		}
		if (year ==0){
			yy = utils.DaysChecking.getYesterdayYear(System.currentTimeMillis());
		}
		
		Reading rd = Readings.getOneDayReadingFromDRUID(sensorID, dd, mm, yy);
		//Logger.info("One day result , args); "+rd);

		int backDays =0; // if a day does not have readings, we can search for the previous days while backDays < 10
		while (rd==null && backDays < 10){
			backDays++;
			
				dd--;
				if(dd==0){
					 mm--;
					if(mm==0){
						mm = 12;
						yy --;
					}
					dd=utils.DaysChecking.getNumberOfdaysOfMonth(mm, yy);
				}
				rd = Readings.getOneDayReadingFromDRUID(sensorID, dd, mm, yy);

		}		
		
		if (rd != null){
		int queries =0; // counter used to limit the querying to only x months before the actual day.
		int previousMonth = mm -1;
		int previousYear = yy;
		if(previousMonth==0){
			previousMonth = 12;
			previousYear --;
		}
		boolean exit = false;
		while(!exit){
			queries++;
		
			//Logger.info("Month = "+previousMonth +" year = "+previousYear);
		
			Readings.getAllreadingsFromDRUID(sensorID, previousMonth, previousYear);
		
			if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0 && queries<2){
				previousMonth --;
				if(previousMonth==0){
					previousMonth = 12;
					previousYear --;
				}
			}
			else{
			exit = true;
			}
		} 
		if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0){
			
				for (int i = 1 ; i < queries ; i++){
					previousMonth ++;
					if(previousMonth==0){
						previousMonth = 12;
						previousYear --;
					}	
				}
				
			}
		//Logger.info("adding readings of this month");
		Readings.addThisMomthReadingsFromDRUID(sensorID, mm, yy);
		Collections.sort(Readings.allReadings, new Comparator<Reading>() {
		
		    public int compare(Reading r1, Reading r2) {
		        if (r2.getReadingTimeStamp() > r1.getReadingTimeStamp()){
		        	return -1;
		        }
		        else{
		        	return 1;
		        }
		    }
		});
//		//Logger.info("=======>", Readings.AVGWorkingDay);
		

		double avg =0;
		double std =0;
		String wDay =rd.getDay();
		if(wDay.equals("no readings available")){
			//check if this is a working day
			//TODO
			boolean publicholiday = utils.DaysChecking.isPublicHoliday(day, month, year, "irl");
			if (publicholiday
					||utils.DaysChecking.getDayOfWeek(day, month, year)== Calendar.SUNDAY
					||utils.DaysChecking.getDayOfWeek(day, month, year)==Calendar.SATURDAY){
				wDay = "non working day";
			}
			else {
				//System.out.println();
				wDay = "working day";
			}
		}

		
		if(wDay.equals("working day")) {
			avg = Readings.AVGWorkingDay;
			std = Readings.StDevWorkingDay;
		}
		else{
			if(wDay.equals("non working day")) {
				avg = Readings.AVGNonWorkingDay;
				std = Readings.StDevNonWorkingDay;
			}
			else{
				deviationLabel = "Unable to compute deviation!";
			}
		}
		
		if (avg >0){
		deviation = ((rd.getReadingValue() - avg)/avg) * 100;

		
		if(rd.getReadingValue() >= avg + 1.5 * std){
			deviationLabel = "Very High!";
		}
		
		if(rd.getReadingValue() < avg + 1.5 * std && rd.getReadingValue() >= avg + std){
				deviationLabel = "High!";
		}
		
		if(rd.getReadingValue() < avg + std && rd.getReadingValue() >= avg - std){
					deviationLabel = "Normal Consumption!";
		}

		if(rd.getReadingValue() < avg - std && rd.getReadingValue() >= avg - 1.5*std){
						deviationLabel = "Low Consumption!";
		}
		
		if(rd.getReadingValue() <= avg - 1.5 * std){
						deviationLabel = "Very Low Consumption!";
		}

		}
		else{
			deviation = 0;
			deviationLabel = "Normal Consumption!";
		}
				Object PreviousMonth = Readings.allReadingsToHashMap(sensorID, month, year);
		 		return Readings.OneReadingToHashMap(sensorID, dd, mm, yy, rd,deviation, deviationLabel, avg, std,PreviousMonth) ;

		}
		else{
			return new HashMap<String, Object>();
		}
    }
     
    
    @Util
    public static void renderJSON(Object o) {
    	//Logger.info("ORIGINAL STRING = " + o.toString());

        GsonBuilder gsb = new GsonBuilder();// FIXME: .excludeFieldsWithoutExposeAnnotation();
        Gson gson = gsb.create();
        String out = gson.toJson(o);
    	//Logger.info("ORIGINAL STRING = " + o.toString());

        if (request.params._contains("callback")) {
            response.contentType = "text/json";
            renderText(request.params.get("callback") + "(" + out + ")");
        } else {
        	//Logger.info("result = " + out);
            Controller.renderJSON(out);
        }
    }
    @Util
    public static Map<String, String> getHttpQueryParameters() {
        Map<String, String> tmpParams = params.allSimple();
        // remove extra parameters
        tmpParams.remove("body");
        tmpParams.remove("callback");
        return tmpParams;
    }
    @Util
    public static <T> List<T> findByParameters(Map<String, String> params, Class<T> cls) {
        String queryString = "SELECT t.* FROM " + cls.getSimpleName() + " AS t ";

        String whereString = "\n WHERE ";

        //System.out.println(params.keySet());
        int index = 0;
        int lastIndex = params.size() - 1;
        Map<String, String> joinNames = new HashMap<String, String>();
        Map<String, String> queryKeys = new HashMap<String, String>();

        for (Map.Entry<String, String> param : params.entrySet()) {
            final String KEY = param.getKey();
            String queryKey = "t."+KEY;
           /*
            if (KEY.contains(".")) {

                String parts[] = KEY.split("\\.");
                System.out.println(parts.length);
                int sencondLast = parts.length - 2;
                int last = parts.length - 1;

                if (!joinNames.containsKey(parts[sencondLast]))
                    joinNames.put(parts[sencondLast], "t" + (joinNames.size()+1));
                queryKey = joinNames.get(parts[sencondLast]) + "." + parts[last];
            }
            */
            whereString += "\n " + queryKey + " = " + params.get(KEY) + " ";
            queryKeys.put(KEY, "p"+index);
            if (index<lastIndex) whereString += " AND ";
            index++;
        }

        for (String name : joinNames.keySet())
            queryString += "INNER JOIN " + name + " AS " + joinNames.get(name) +
                    " ON t." + name.toLowerCase() +"_id = " + joinNames.get(name) + ".id ";
        if (params.size()>0)
            queryString += whereString;

        //System.out.println(queryString);

        Query query = JPA.em().createNativeQuery(queryString, cls);

       /* for (String key : params.keySet())
            query.setParameter(queryKeys.get(key), params.get(key));*/

        List<T> results = query.getResultList();

        //System.out.println(results.size());
        return results;
    }


}
