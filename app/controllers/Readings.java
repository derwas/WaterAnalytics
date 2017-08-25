package controllers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import play.Logger;
import play.Play;
import models.Reading;
import models.jsonReading;

public class Readings {

	static double AVGWorkingDay =0;
	static double StDevWorkingDay =0;
	static double AVGNonWorkingDay =0;
	static double StDevNonWorkingDay =0;
	
	static double AVGWorkingDayWithoutOutliers =0;
	static double StDevWorkingDayWithoutOutliers =0;
	static double AVGNonWorkingDayWithoutOutliers =0;
	static double StDevNonWorkingDayWithoutOutliers =0;
	
	static double TotalOfTheMonth = 0;
	
	static List<Reading> allReadings = new ArrayList<Reading>();
	static List <jsonReading> rds = new ArrayList<jsonReading>();

	
	public static Object allReadingsToHashMap(String sensorID, int month, int year){
		 HashMap<String, Object> result = new HashMap<String, Object>();
 		DecimalFormat ft = new DecimalFormat("#.###");

		 result.put("sensorID", sensorID);
		 result.put("month", month);
		 result.put("year", year);
		 result.put("AllReadings", allReadings);

		 result.put("AVGWorkingDay", AVGWorkingDay);
		 result.put("StDevWorkingDay", StDevWorkingDay);

		 result.put("AVGNonWorkingDay", AVGNonWorkingDay);
		 result.put("StDevNonWorkingDay", StDevNonWorkingDay);
//
		 result.put("AVGWorkingDayWithoutOutliers", AVGWorkingDayWithoutOutliers);
		 result.put("StDevWorkingDayWithoutOutliers", StDevWorkingDayWithoutOutliers);
//
		 result.put("AVGNonWorkingDayWithoutOutliers", AVGNonWorkingDayWithoutOutliers);
		 result.put("StDevNonWorkingDayWithoutOutliers", StDevNonWorkingDayWithoutOutliers);
		 
		 result.put("TotalOfTheMonth", TotalOfTheMonth);

		 return result;
		 
	} 
	
	public static Object OneReadingToHashMap(String sensorID, int day,int month, int year, Reading reading, double deviation, String deviationLabel, double avg, double std, Object previousMonth ){
		 HashMap<String, Object> result = new HashMap<String, Object>();
		 
		 result.put("sensorID", sensorID);
		 result.put("day", day);
		 result.put("month", month);
		 result.put("year", year);
		 result.put("Reading", reading);
		 result.put("deviation", deviation);
		 result.put("deviationLabel", deviationLabel);
		 result.put("previousMonth",previousMonth);
		 //result.put("avg", avg);
		 //result.put("std", std);

		 return result;
		 
	} 

	
	
	public static void getAllreadingsFromAWS(String sensorID, int month, int year) throws Exception{
		clear();
		URL u = null;
		  InputStream in=null;
		  
		  for(int i = 1; i<=utils.DaysChecking.getNumberOfdaysOfMonth(month, year);i++){
			  
			  if(i == utils.DaysChecking.getNumberOfdaysOfMonth(month, year)){
				  if(month == 12){
					  int y = year +1;
				    	u = new URL("https://s3-eu-west-1.amazonaws.com/waternomics/neb/"+sensorID+"/"+y+"/"+1+"/"+1+"/raw.csv");

				  }else{
					  int m = month +1;
			    	    u = new URL("https://s3-eu-west-1.amazonaws.com/waternomics/neb/"+sensorID+"/"+year+"/"+m+"/"+1+"/raw.csv");
				  }
			  }
			  else{
				  int j = i+1;
			    	u = new URL("https://s3-eu-west-1.amazonaws.com/waternomics/neb/"+sensorID+"/"+year+"/"+month+"/"+j+"/raw.csv");
			  }
		//Logger.info("url = "+u.toString()+"\n");
			double total = 0;
			String holiday = "";

			try{
			in = u.openStream();
			BufferedReader br = null;
			String line = "";
			String csvSplitBy = ",";
		 
		 
				br =  new BufferedReader(
					    new InputStreamReader(in));

				while ((line = br.readLine()) != null) {
		 
				        // use comma as separator
					String[] reading = line.split(csvSplitBy);
					if (!reading[0].contains("Timestamp") && (reading[0].contains(" 00:00")|| reading[0].contains(" 12:00:00 AM"))){
						
					total = Double.parseDouble(reading[1]); //the first line of the file contains the actual daily reading
					}
					
				}
				boolean publicholiday = utils.DaysChecking.isPublicHoliday(i, month, year, "irl");
				if (publicholiday
					||utils.DaysChecking.getDayOfWeek(i, month, year)==Calendar.SUNDAY
					||utils.DaysChecking.getDayOfWeek(i, month, year)==Calendar.SATURDAY){
					holiday = "non working day";
				}
				else {
					//System.out.println();
					holiday = "working day";
				}
			}
			catch(Exception e){
				total =0;
				holiday = "no readings available";

			}
		
			
			//System.out.println(i+"/"+month+"/"+year+"("+holiday+")  : "+total);
			Calendar cal = Calendar.getInstance();
			//cal.set(year, month, i);
			 cal.set(year, month-1, i, 0, 0, 0);
			Reading rd = new Reading(cal.getTimeInMillis(), total,holiday);
			TotalOfTheMonth+= total;
			allReadings.add(rd);
			//Logger.info("Reading to add"+ rd.toString());
		}
		  computeAVGandSTDev();
	
	}
	public static void getAllreadingsFromDRUID(String sensorID, int month, int year) throws Exception{
		clear();
		//Organizations.showOrganisations();
		int thisYear = utils.DaysChecking.thisYear();
		if(month>0 && month <=12 && year >2014 && year <= thisYear){
			
		
		  String dataSources = Organizations.getDataSourceFromSensorID(sensorID);
		  ArrayList<String> dataSRC= new ArrayList<String>();
		  
		  //Logger.info("Datasources: "+dataSources);

		  while (!dataSources.equals(";")){
			  dataSources = dataSources.substring(1,dataSources.length()); // remove the first ";"
			  //Logger.info("\t source = "+dataSources.substring(0,dataSources.indexOf(";")));
			  dataSRC.add(dataSources.substring(0,dataSources.indexOf(";"))); // get the first datasource in the remaining string
			  dataSources = dataSources.substring(dataSources.indexOf(";"),dataSources.length());
		  }
//TODO

		  String interval = utils.DaysChecking.getInterval(month, year);
		  String json ="";
		  //Logger.info("Interval = "+interval+"\n");	
		  for ( int i = 0 ; i <dataSRC.size();i++){
			  
			  //Logger.info("Datasource: "+dataSRC.get(i));
			  String query = "{"
				  			+"  \"queryType\": \"groupBy\",\n"
				  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
							+"  \"intervals\": [ \""+interval+"\" ],\n"
							+"  \"granularity\": \"day\",\n"
							+"  \"dimensions\" : [\"dDay\", \"dMonth\", \"dYear\"],\n"
							+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
							+"  \"aggregations\": [\n"
							+"     {\"type\": \"longSum\", \"fieldName\": \"count\", \"name\": \"readings\"},\n"
							+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
							+"  ]\n"
							+"}\n";
		  
		 
		//Logger.info("Qeury = "+query+"\n");
		//URL myurl = new URL("http://vmwaternomics01.deri.ie:8023/druid/v2/?pretty");
		
			  CloseableHttpClient httpClient = HttpClientBuilder.create().build();

			  String postUrl=Play.configuration.getProperty("DRUID");// put in your url
			  HttpPost post = new HttpPost(postUrl);
			  StringEntity  postingString =new StringEntity(query);//convert your pojo to   json
			  post.setEntity(postingString);
			  post.setHeader("Content-type", "application/json");
			  HttpResponse  response = httpClient.execute(post);
			   json = EntityUtils.toString(response.getEntity());
			  //Logger.info("Result = "+json);
			  if(json.length()>5){
				  i = dataSRC.size();
			  }
		  }
			  
		//Logger.info("Result = "+json+"\n");
		Gson gson= new Gson();
		rds = gson.fromJson(json, new TypeToken<List<jsonReading>>(){}.getType()) ;

		
		double total = 0;
		String holiday = "";
		Iterator it = rds.iterator();

			while (it.hasNext()) {
				jsonReading jr = (jsonReading) it.next();
				total = Double.parseDouble(jr.event.consumption);
				
			//Logger.info("Checking "+Integer.parseInt(jr.event.dday)+"/"+ month+"/"+ year);
			boolean publicholiday = utils.DaysChecking.isPublicHoliday(Integer.parseInt(jr.event.dDay), month, year, "irl");
			if (publicholiday
					||utils.DaysChecking.getDayOfWeek(Integer.parseInt(jr.event.dDay), month, year)== Calendar.SUNDAY
					||utils.DaysChecking.getDayOfWeek(Integer.parseInt(jr.event.dDay), month, year)==Calendar.SATURDAY){
				holiday = "non working day";
			}
			else {
				//System.out.println();
				holiday = "working day";
			}
		if (total==0){
			holiday = "no readings available";

		}
		//Logger.info("Result = "+holiday);
	
		//System.out.println(jr);
		//System.out.println(Integer.parseInt(jr.event.dday)+"/"+month+"/"+year+"("+holiday+")  : "+total);
		Calendar cal = Calendar.getInstance();
		//cal.set(year, month, i);
		 cal.set(year, month-1, Integer.parseInt(jr.event.dDay), 0, 0, 0);
		Reading rd = new Reading(cal.getTimeInMillis(), total,holiday);
		TotalOfTheMonth+= total;
		allReadings.add(rd);
		//Logger.info("Reading to add"+ rd.toString());
	}
	  computeAVGandSTDev();
		//Logger.info("All readings:" + allReadings.toString());

		}
	}
	
	public static void addThisMomthReadingsFromDRUID(String sensorID, int month, int year) throws Exception{
		//Organizations.showOrganisations();
		Logger.info("adding readings of this month: "+month+"/"+year);

		int thisYear = utils.DaysChecking.thisYear();
		if(month>0 && month <=12 && year >2014 && year <= thisYear){
			
		
		  String dataSources = Organizations.getDataSourceFromSensorID(sensorID);
		  ArrayList<String> dataSRC= new ArrayList<String>();
		  
		  //Logger.info("Datasources: "+dataSources);

		  while (!dataSources.equals(";")){
			  dataSources = dataSources.substring(1,dataSources.length()); // remove the first ";"
			  //Logger.info("\t source = "+dataSources.substring(0,dataSources.indexOf(";")));
			  dataSRC.add(dataSources.substring(0,dataSources.indexOf(";"))); // get the first datasource in the remaining string
			  dataSources = dataSources.substring(dataSources.indexOf(";"),dataSources.length());
		  }
//TODO

		  String interval = utils.DaysChecking.getInterval(month, year);
		  String json ="";
		  //Logger.info("Interval = "+interval+"\n");	
		  for ( int i = 0 ; i <dataSRC.size();i++){
			  
			  //Logger.info("Datasource: "+dataSRC.get(i));
			  String query = "{"
				  			+"  \"queryType\": \"groupBy\",\n"
				  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
							+"  \"intervals\": [ \""+interval+"\" ],\n"
							+"  \"granularity\": \"day\",\n"
							+"  \"dimensions\" : [\"dDay\", \"dMonth\", \"dYear\"],\n"
							+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
							+"  \"aggregations\": [\n"
							+"     {\"type\": \"longSum\", \"fieldName\": \"count\", \"name\": \"readings\"},\n"
							+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
							+"  ]\n"
							+"}\n";
		  
		 
		//Logger.info("Qeury = "+query+"\n");
		//URL myurl = new URL("http://vmwaternomics01.deri.ie:8023/druid/v2/?pretty");
		
			  CloseableHttpClient httpClient = HttpClientBuilder.create().build();

			  String postUrl=Play.configuration.getProperty("DRUID");// put in your url
			  HttpPost post = new HttpPost(postUrl);
			  StringEntity  postingString =new StringEntity(query);//convert your pojo to   json
			  post.setEntity(postingString);
			  post.setHeader("Content-type", "application/json");
			  HttpResponse  response = httpClient.execute(post);
			   json = EntityUtils.toString(response.getEntity());
			  //Logger.info("Result = "+json);
			  if(json.length()>5){
				  i = dataSRC.size();
			  }
		  }
			  
		//Logger.info("Result = "+json+"\n");
		Gson gson= new Gson();
		rds = gson.fromJson(json, new TypeToken<List<jsonReading>>(){}.getType()) ;

		
		double total = 0;
		String holiday = "";
		Iterator it = rds.iterator();

			while (it.hasNext()) {
				jsonReading jr = (jsonReading) it.next();
				total = Double.parseDouble(jr.event.consumption);
				
			//Logger.info("Checking "+Integer.parseInt(jr.event.dday)+"/"+ month+"/"+ year);
			boolean publicholiday = utils.DaysChecking.isPublicHoliday(Integer.parseInt(jr.event.dDay), month, year, "irl");
			if (publicholiday
					||utils.DaysChecking.getDayOfWeek(Integer.parseInt(jr.event.dDay), month, year)== Calendar.SUNDAY
					||utils.DaysChecking.getDayOfWeek(Integer.parseInt(jr.event.dDay), month, year)==Calendar.SATURDAY){
				holiday = "non working day";
			}
			else {
				//System.out.println();
				holiday = "working day";
			}
		if (total==0){
			holiday = "no readings available";

		}
		//Logger.info("Result = "+holiday);
	
		//System.out.println(jr);
		//System.out.println(Integer.parseInt(jr.event.dday)+"/"+month+"/"+year+"("+holiday+")  : "+total);
		Calendar cal = Calendar.getInstance();
		//cal.set(year, month, i);
		 cal.set(year, month-1, Integer.parseInt(jr.event.dDay), 0, 0, 0);
		Reading rd = new Reading(cal.getTimeInMillis(), total,holiday);
		TotalOfTheMonth+= total;
		allReadings.add(rd);
		//Logger.info("Reading to add"+ rd.toString());
	}
	  computeAVGandSTDev();
		//Logger.info("All readings:" + allReadings.toString());

		}
	}
	
	public static String getAllreadingsFromDRUID(String jsonQuery) throws Exception{
		clear();
  
		 //String query = jsonQuery.toString();
		//Logger.info("Qeury = "+query+"\n");
		//URL myurl = new URL("http://vmwaternomics04.deri.ie:8023/druid/v2/?pretty");
		
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();

		String postUrl=Play.configuration.getProperty("DRUID");// put in your url
		HttpPost post = new HttpPost(postUrl);
		StringEntity  postingString =new StringEntity(jsonQuery);//convert your pojo to   json
		post.setEntity(postingString);
		post.setHeader("Content-type", "application/json");
		HttpResponse  response = httpClient.execute(post);
		String json = EntityUtils.toString(response.getEntity());
		//Logger.info("Result = "+json+"\n");
		//Gson gson= new Gson();
		//rds = gson.fromJson(json, new TypeToken<List<jsonReading>>(){}.getType()) ;

		return json;
	}
	public static Reading getOneDayReadingFromDRUID(String sensorID, int day, int month, int year) throws Exception{
		clear();
		
		//Logger.info("Retrieving Day Analytics for "+day+"/"+month+"/"+year);
		if(utils.DaysChecking.checkDate(day, month, year) ){
			
		

			  String dataSoruces = Organizations.getDataSourceFromSensorID(sensorID);
			  ArrayList<String> dataSRC = new ArrayList<String>();
			 // Logger.info("Datasources ="+dataSoruces);
			  while (dataSoruces.length()>1){
				  dataSoruces = dataSoruces.substring(1,dataSoruces.length()); // remove the first ";"
				  //Logger.info("Datasources ="+dataSoruces);
				  dataSRC.add(dataSoruces.substring(0,dataSoruces.indexOf(";"))); // get the first datasource in the remaining string
				  dataSoruces = dataSoruces.substring(dataSoruces.indexOf(";"),dataSoruces.length());
			  }


		  String interval = utils.DaysChecking.getYesterdayInterval(day, month, year);
		  //Logger.info("Interval :"+ interval +" ==>"+day+"-"+month+"-"+year);
		  String json ="";
		  //Logger.info("Interval = "+interval+"\n");	
		  double total=0;
		  for ( int i = 0 ; i <dataSRC.size();i++){
			  String query = "{"
		  			+"  \"queryType\": \"timeseries\",\n"
		  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
					+"  \"intervals\": [ \""+interval+"\" ],\n"
					+"  \"granularity\": \"day\",\n"
					+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
					+"  \"aggregations\": [\n"
					+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
					+"  ]\n"
					+"}\n";
		  

		  
		 
		//Logger.info("Qeury = "+query+"\n");
		//URL myurl = new URL("http://vmwaternomics01.deri.ie:8023/druid/v2/?pretty");
		
//			  CloseableHttpClient httpClient = HttpClientBuilder.create().build();
//
//			  String postUrl=Play.configuration.getProperty("DRUID");// put in your url
//			  HttpPost post = new HttpPost(postUrl);
//			  StringEntity  postingString =new StringEntity(query);//convert your pojo to   json
//			  post.setEntity(postingString);
//			  post.setHeader("Content-type", "application/json");
//			  HttpResponse  response = httpClient.execute(post);
//			   json = EntityUtils.toString(response.getEntity());
			   
			   json = getAllreadingsFromDRUID(query);
			  //Logger.info(json);
			  Gson gson= new Gson();
				rds = gson.fromJson(json, new TypeToken<List<jsonReading>>(){}.getType()) ;
				jsonReading jr = (jsonReading) rds.iterator().next();
				 total = Double.parseDouble(jr.result.consumption);
				
				  if(total >0){
					  i = dataSRC.size();
				  }
		  }
		//Logger.info("Result = "+json+"\n");
		if ( json.equals("[]")){
			return null;
		}
		else{
		Gson gson= new Gson();
		rds = gson.fromJson(json, new TypeToken<List<jsonReading>>(){}.getType()) ;
//
//		//TODO
//		
		String holiday = "";
		Iterator it = rds.iterator();
//
//			while (it.hasNext()) {
				jsonReading jr = (jsonReading) it.next();
				total = Double.parseDouble(jr.result.consumption);
//				
//			//Logger.info("Checking "+Integer.parseInt(jr.event.dday)+"/"+ month+"/"+ year);
			boolean publicholiday = utils.DaysChecking.isPublicHoliday(day, month, year, "irl");
			if (publicholiday
					||utils.DaysChecking.getDayOfWeek(day, month, year)== Calendar.SUNDAY
					||utils.DaysChecking.getDayOfWeek(day, month, year)==Calendar.SATURDAY){
				holiday = "non working day";
			}
			else {
				//System.out.println();
				holiday = "working day";
			}
		if (total==0){
			holiday = "no readings available";

		}
//		//Logger.info("Result = "+holiday);
//	
//		System.out.println(Integer.parseInt(jr.event.dDay)+"/"+month+"/"+year+"("+holiday+")  : "+total);
		Calendar cal = Calendar.getInstance();
//		//cal.set(year, month, i);
		cal.add(cal.DAY_OF_MONTH, -1);
		 cal.set(cal.get(cal.YEAR), cal.get(cal.MONTH), cal.get(cal.DAY_OF_MONTH), 0, 0, 0);
		//Logger.info(cal.toString());
		 Reading rd = new Reading(cal.getTimeInMillis(), total,holiday);
		return rd;
//		TotalOfTheMonth+= total;
//		allReadings.add(rd);
//		//Logger.info("Reading to add"+ rd.toString());
//	}
//	  computeAVGandSTDev();
//		//Logger.info("All readings:" + allReadings.toString());
//
		}
		}
		return null;
	}

	public static void computeAVGandSTDev(){
    	
    	

    	int numberOfWorkingDays = 0;
    	int numberOfNONWorkingDays = 0;
    	
    	Iterator it = allReadings.iterator();
    	
    	while(it.hasNext()){
    		Reading rd = (Reading) it.next();
    		if (rd.getDay().equals("working day")){numberOfWorkingDays++;}
    		if (rd.getDay().equals("non working day")){numberOfNONWorkingDays++;}
    	}
    	

    	if (numberOfWorkingDays!=0||numberOfNONWorkingDays!=0){


    		//Logger.info(Location.toString());

    		double sumWorkingDays = 0;
    		int workingdaysWithout0=0;
    		double sumNONWorkingDays = 0;
    		int NONworkingdaysWithout0=0; 
    		
    		Iterator itAVG = allReadings.iterator();
    		
    		while (itAVG.hasNext()){
    			Reading oneReading = (Reading) itAVG.next();
    			if(oneReading.getDay().equals("working day")){// && oneReading.getReadingValue()!=0){
        				sumWorkingDays += oneReading.getReadingValue();
        				workingdaysWithout0++;    				
    			}
    			
    			if(oneReading.getDay().equals("non working day")){// && oneReading.getReadingValue()!=0){
    				sumNONWorkingDays += oneReading.getReadingValue();
    				NONworkingdaysWithout0++;    				
			}
    		}
    		

    		
    		//Compute averages
        	Logger.info("Computing avg daily consumption of water!");

		double AVGWorkingDays=0;
		double AVGNONWorkingDays=0;

		if(workingdaysWithout0>0){
			AVGWorkingDays = sumWorkingDays / workingdaysWithout0;
			AVGWorkingDay = AVGWorkingDays;
			Logger.info("AVG Working Days = " + AVGWorkingDay);

		}
		if(NONworkingdaysWithout0>0){
			AVGNONWorkingDays = sumNONWorkingDays / NONworkingdaysWithout0;
			AVGNonWorkingDay = AVGNONWorkingDays;
			Logger.info("AVG Non Working Days = " + AVGNonWorkingDay);

		}
		

		Logger.info("Computing stdDev daily consumption of water!");

		//Compute standard deviations
		double STDEVWorkingDays =0;
		double SumSqrtWorkingDays=0;
		double STDEVNONWorkingDays =0;
		double SumSqrtNONWorkingDays=0;
		
		Iterator itSTDev = allReadings.iterator();

		while (itSTDev.hasNext()){
			Reading oneReading = (Reading) itSTDev.next();
			if(oneReading.getDay().equals("working day") ){//&& oneReading.getReadingValue()!=0){
				double v =  oneReading.getReadingValue() - AVGWorkingDay;
				SumSqrtWorkingDays += v * v ;
			}
		
			if(oneReading.getDay().equals("non working day") ){//&& oneReading.getReadingValue()!=0){
				double v =  oneReading.getReadingValue() - AVGNonWorkingDay;
				SumSqrtNONWorkingDays += v * v ;  				
			}
		}
		
		if(workingdaysWithout0-1 >0){
		STDEVWorkingDays = Math.sqrt(SumSqrtWorkingDays/(workingdaysWithout0-1));
		}
		else{
			STDEVWorkingDays = Math.sqrt(SumSqrtWorkingDays/(1));
		}
		StDevWorkingDay = STDEVWorkingDays;

		if(NONworkingdaysWithout0-1 >0){
			STDEVNONWorkingDays = Math.sqrt(SumSqrtNONWorkingDays/(NONworkingdaysWithout0-1));
		}
		else{
			STDEVNONWorkingDays = Math.sqrt(SumSqrtNONWorkingDays/1);
		}
		StDevNonWorkingDay = STDEVNONWorkingDays;

		
		Logger.info("Computing zscores and Identifying outliers!");

		//compute ZScore for every reading
		
		Iterator itZScore = allReadings.iterator();
		
		while(itZScore.hasNext()){
			Reading oneReading = (Reading) itZScore.next();
			if(oneReading.getDay().equals("working day") ){//&& oneReading.getReadingValue()!=0){
				if(STDEVWorkingDays!=0){
				oneReading.setzScore(Math.abs((oneReading.getReadingValue()-AVGWorkingDays)/STDEVWorkingDays));
				if(oneReading.getReadingValue()!=0 && oneReading.getzScore()>1.5){
					oneReading.setOutlier("yes");
				}
				else{
					if(oneReading.getzScore()<=1.5)
					{
						oneReading.setOutlier("no");
					}
				}
				}
			}
			if(oneReading.getDay().equals("non working day") ){//&& oneReading.getReadingValue()!=0){
				if(STDEVNONWorkingDays!=0){
				oneReading.setzScore(Math.abs((oneReading.getReadingValue()-AVGNONWorkingDays)/STDEVNONWorkingDays));
				if(oneReading.getReadingValue()!=0 && oneReading.getzScore()>1.5){
					oneReading.setOutlier("yes");
				}
				else{
					if(oneReading.getzScore()<=1.5)
					{
						oneReading.setOutlier("no");
					}
				}
				}
			}
			
		}
		

		
		//Computer new averages

		Logger.info("Computing AVG after filtering out outliers!");
		double NewsumWorkingDays = 0;
		int NewworkingdaysWithout0=0;
		double NewsumNONWorkingDays = 0;
		int NewNONworkingdaysWithout0=0; 
		
		Iterator itAVGN = allReadings.iterator();
		
		while (itAVGN.hasNext()){
			Reading oneReading = (Reading) itAVGN.next();
			if(oneReading.getDay().equals("working day") && oneReading.getOutlier().equals("no")){//&& oneReading.getReadingValue()!=0 ){
    				NewsumWorkingDays += oneReading.getReadingValue();
    				NewworkingdaysWithout0++;    				
			}
			
			if(oneReading.getDay().equals("non working day")&& oneReading.getOutlier().equals("no") ){//&& oneReading.getReadingValue()!=0 ){
				NewsumNONWorkingDays += oneReading.getReadingValue();
				NewNONworkingdaysWithout0++;    				
		}
		}
	
		double NewAVGWorkingDays=0;
		double NewAVGNONWorkingDays=0;

		if(NewworkingdaysWithout0>0){
			if(NewworkingdaysWithout0 !=0){
			NewAVGWorkingDays = NewsumWorkingDays / NewworkingdaysWithout0;
			}
			AVGWorkingDayWithoutOutliers = NewAVGWorkingDays;
		}
		if(NewNONworkingdaysWithout0>0){
			if(NewNONworkingdaysWithout0!=0){
			NewAVGNONWorkingDays = NewsumNONWorkingDays / NewNONworkingdaysWithout0;
			}
			AVGNonWorkingDayWithoutOutliers =NewAVGNONWorkingDays;
		}

		Logger.info("Computing stdDev after filtering out outliers!");

		//compute new standard deviation		double STDEVWorkingDays =0;
		double NewSTDEVWorkingDays =0;
		double NewSumSqrtWorkingDays=0;
		double NewSTDEVNONWorkingDays =0;
		double NewSumSqrtNONWorkingDays=0;
		
		
		Iterator itSTDevN = allReadings.iterator();

		while (itSTDevN.hasNext()){
			Reading oneReading = (Reading) itSTDevN.next();
			if(oneReading.getDay().equals("working day") && oneReading.getOutlier().equals("no")){//&& oneReading.getReadingValue()!=0 ){
				double v =  oneReading.getReadingValue();
				NewSumSqrtWorkingDays += v * v ;
			}
		
			if(oneReading.getDay().equals("non working day") && oneReading.getOutlier().equals("no")){// && oneReading.getReadingValue()!=0){
				double v =  oneReading.getReadingValue();
				NewSumSqrtNONWorkingDays += v * v ;  				
			}
		}
		
		if((NewworkingdaysWithout0-1) !=0){
		NewSTDEVWorkingDays = Math.sqrt(NewSumSqrtWorkingDays/(NewworkingdaysWithout0-1));
		}
		StDevWorkingDayWithoutOutliers = NewSTDEVWorkingDays;
		if((NewNONworkingdaysWithout0-1)!=0){
		NewSTDEVNONWorkingDays = Math.sqrt(NewSumSqrtNONWorkingDays/(NewNONworkingdaysWithout0-1));
		}
		StDevNonWorkingDayWithoutOutliers = NewSTDEVNONWorkingDays;

    }
    }


	public static void clear() {
		allReadings.clear();
		rds.clear();
		AVGWorkingDay =0;
		StDevWorkingDay =0;
		AVGNonWorkingDay =0;
		StDevNonWorkingDay =0;
		
		AVGWorkingDayWithoutOutliers =0;
		StDevWorkingDayWithoutOutliers =0;
		AVGNonWorkingDayWithoutOutliers =0;
		StDevNonWorkingDayWithoutOutliers =0;
		
		TotalOfTheMonth=0;
		
	}
	
	public static String toStr(){
		String result ="[\n";
		Iterator it = allReadings.iterator();
		while (it.hasNext()){
			Reading rd = (Reading) it.next();
			result = result + rd.toString() + ",\n";
		}
		if(result.contains(",")){
		result = result.substring(0, result.lastIndexOf(","));
		}
		result += "\n]\n";
		return result;
	}
	
	public static String  allReadingstoC3Data (){
		String data = "";
		//timeseries = "";
		Iterator it = allReadings.iterator();
		while (it.hasNext()){
			Reading rd = (Reading) it.next();
			data = data + " " + rd.getReadingValue();
			//timeseries = timeseries + "'"+rd.getDate().toString()+"'";
			if(it.hasNext()){
				data = data + ",";
				//timeseries = timeseries + ",";
			}
		}
		return data;
	}
	public static String  allWDReadingstoC3Data (){
		String data = "";
		//timeseries = "";
		DecimalFormat ft = new DecimalFormat("#0.000");

		Iterator it = allReadings.iterator();
		while (it.hasNext()){
			Reading rd = (Reading) it.next();
			if(rd.getDay().equals("working day")){
				data = data + " " + ft.format(rd.getReadingValue());
				if(it.hasNext()){
					data = data + ",";
				}
			}
			else{
				data = data + " "+0;
				if(it.hasNext()){
					data = data + ",";
				}
			}
		}
		return data;
	}
	public static String  allNWDReadingstoC3Data (){
		String data = "";
		//timeseries = "";
		DecimalFormat ft = new DecimalFormat("#0.000");

		Iterator it = allReadings.iterator();
		while (it.hasNext()){
			Reading rd = (Reading) it.next();
			if(rd.getDay().equals("non working day")){
				data = data + " " + ft.format(rd.getReadingValue());
				if(it.hasNext()){
					data = data + ",";
				}
			}
			else{
				data = data + " " +0;
				if(it.hasNext()){
					data = data + ",";
				}
			}
		}
		return data;
	}

	public static String getAllreadingsFromDRUIDBySensorID(String sensorID) throws Exception {
		// TODO Auto-generated method stub
		clear();
		//Organizations.showOrganisations();
		int thisYear = utils.DaysChecking.thisYear();
		int thisMonth = utils.DaysChecking.thisMonth();
		int thisDay = utils.DaysChecking.thisDay();
		
		String toDayInterval = utils.DaysChecking.getInterval(thisDay, thisMonth, thisYear);
		String thisWeekInterval = utils.DaysChecking.getLastWeekInterval(thisDay, thisMonth, thisYear);
		String thisMonthInterval = utils.DaysChecking.getLast30DaysInterval(thisDay, thisMonth, thisYear);
		//String thisYearInterval = utils.DaysChecking.getLastYearInterval(thisDay, thisMonth, thisYear);
		//String these10Year0Interval = utils.DaysChecking.getLast10YearsInterval(thisDay, thisMonth, thisYear);

//		Logger.info("Interval : "+toDayInterval);
//		Logger.info("Interval : "+thisWeekInterval);
//		Logger.info("Interval : "+thisMonthInterval);
//		Logger.info("Interval : "+thisYearInterval);
//		Logger.info("Interval : "+these10Year0Interval);

		String dataSources = Organizations.getDataSourceFromSensorID(sensorID);
		  ArrayList<String> dataSRC= new ArrayList<String>();
		  
		  //Logger.info("Datasources: "+dataSources);

		  while (!dataSources.equals(";")){
			  dataSources = dataSources.substring(1,dataSources.length()); // remove the first ";"
			  //Logger.info("\t source = "+dataSources.substring(0,dataSources.indexOf(";")));
			  dataSRC.add(dataSources.substring(0,dataSources.indexOf(";"))); // get the first datasource in the remaining string
			  dataSources = dataSources.substring(dataSources.indexOf(";"),dataSources.length());
		  }
//TODO

		  //String interval = utils.DaysChecking.getInterval(month, year);
		 // String json ="";
		  String fifteenMinutes ="[]";
		  String lastWeek ="[]";
		  String lastMonth ="[]";
		  //String lastYear ="";

		  //Logger.info("Interval = "+interval+"\n");	
		  for ( int i = 0 ; i <dataSRC.size();i++){
			  
			  //Logger.info("Datasource: "+dataSRC.get(i));
			  String testQuery ="{"
			  			+"  \"queryType\": \"timeseries\",\n"
			  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
						+"  \"intervals\": [ \""+thisMonthInterval+"\" ],\n"
						+"  \"granularity\": \"all\",\n"
						+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
						+"  \"aggregations\": [\n"
						+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
						+"  ]\n"
						+"}\n";
			  String testResult = getAllreadingsFromDRUID(testQuery);
			  //Logger.info(testQuery);
			  Gson gson= new Gson();
				rds = gson.fromJson(testResult, new TypeToken<List<jsonReading>>(){}.getType()) ;
				jsonReading jr = (jsonReading) rds.iterator().next();
				double total = Double.parseDouble(jr.result.consumption);
					  
			  if(total>0){
			  
			  String query = "{"
				  			+"  \"queryType\": \"timeseries\",\n"
				  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
							+"  \"intervals\": [ \""+toDayInterval+"\" ],\n"
							+"  \"granularity\": \"fifteen_minute\",\n"
							+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
							+"  \"aggregations\": [\n"
							+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
							+"  ]\n"
							+"}\n";
		
				fifteenMinutes=getAllreadingsFromDRUID(query);
				String query2 = "{"
			  			+"  \"queryType\": \"timeseries\",\n"
			  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
						+"  \"intervals\": [ \""+thisWeekInterval+"\" ],\n"
						+"  \"granularity\": \"day\",\n"
						+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
						+"  \"aggregations\": [\n"
						+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
						+"  ]\n"
						+"}\n";
				
				String query3 = "{"
			  			+"  \"queryType\": \"timeseries\",\n"
			  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
						+"  \"intervals\": [ \""+thisMonthInterval+"\" ],\n"
						+"  \"granularity\": \"day\",\n"
						+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
						+"  \"aggregations\": [\n"
						+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
						+"  ]\n"
						+"}\n";
	
//				String query4 = "{"
//			  			+"  \"queryType\": \"timeseries\",\n"
//			  			+"  \"dataSource\": \""+dataSRC.get(i)+"\",\n"
//						+"  \"intervals\": [ \""+thisYearInterval+"\" ],\n"
//						+"  \"granularity\": \"day\",\n"
//						+"  \"filter\": {\"type\": \"selector\",\"dimension\": \"dSensor\", \"value\": \""+sensorID+"\"},\n"
//						+"  \"aggregations\": [\n"
//						+"     {\"type\": \"doubleSum\", \"fieldName\": \"mValue\", \"name\": \"consumption\"}\n"
//						+"  ]\n"
//						+"}\n";
				//Logger.info("Query = "+query2);
			String fifteenMinutesResult=getAllreadingsFromDRUID(query);
			String lastWeekResult=getAllreadingsFromDRUID(query2);
			//Logger.info("result:" + lastWeekResult);
			String lastMonthResult=getAllreadingsFromDRUID(query3);
			//String lastYearResult=getAllreadingsFromDRUID(query4);
			  
				if(fifteenMinutesResult.length()>5){
					fifteenMinutes = fifteenMinutesResult;
				}
				if(lastWeekResult.length()>5){
					lastWeek = lastWeekResult;
				}
				if(lastMonthResult.length()>5){
					lastMonth = lastMonthResult;
				}
//				if(lastYearResult.length()>5){
//					lastYear = lastYearResult;
//				}
			  }
		  }
		  String result = "{"
				  + "\"Today\":" +fifteenMinutes +","
				  + "\"Last7Days\":" +lastWeek +","
				  + "\"Last30Days\":" +lastMonth//+","
				 // + "\"LastYear\":" +lastYear
				  + "}";
		return result;
	}


	
}
