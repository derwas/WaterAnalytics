package controllers;

import play.*;
import play.mvc.*;

import java.text.DecimalFormat;
import java.util.*;

import models.*;

public class Application extends Controller {
static String error ="";
    public static void index() {
    	//Logger.info("msg for index: " + msg);
    	String msg = error;
    	render(msg);
    }
    
    public static void analytics(String APIKey, String userID, String sensorID, int month, int year) throws Exception {
		Logger.info("[Application] Request for Data Analytics from user "+userID+" for the sensor " + sensorID +" ["+month+"/"+year+"]");
    	String next = "#";
    	String previous = "#";
    	int mm = month;
    	int yy = year;
		try {
			//Logger.info("Authorised user!");
			String dataSource = Organizations.getDataSourceFromSensorID(sensorID);
			//Logger.info("Datasource ="+dataSource);
			if(dataSource==null || dataSource.contains("Unable to identify the datasource!")){
				error = "Unable to identify the datasource!";
				//Logger.info("msg = "+error);
			}else{
				//Logger.info("CHK user");
				if(!Organizations.verifyUserandDataSource(userID,dataSource)){
					error = "Unauthorised User!";
				}
				else
				{
					error ="";
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
		    	
		    		if(Readings.AVGNonWorkingDay==0 && Readings.AVGWorkingDay==0 && queries<5){
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
		    		//Logger.info("All readings = "+Readings.toStr());
		    		String timeseries="";
		    		int i = utils.DaysChecking.getNumberOfdaysOfMonth(mm, yy);
		    		for (int j =1; j< i; j++)
		    		{
		    			String m ;
		    			if(mm <10){
		    				m="0"+mm;
		    			}
		    			else{
		    				m = ""+mm;
		    			}
		    			if (j<10){
		    			timeseries =  timeseries + "'"+yy+"-"+m+"-0"+j+"',";
		    			}
		    			else
		    			{
		        			timeseries =  timeseries + "'"+yy+"-"+m+"-"+j+"',";
		    			}
		    		}
		    		timeseries =  timeseries + "'"+yy+"-"+mm+"-"+i+"'";
		    		String dataWD = Readings.allWDReadingstoC3Data();
		    		String dataNWD = Readings.allNWDReadingstoC3Data();
		
		    		//Logger.info(data);
		    		DecimalFormat ft = new DecimalFormat("#.###");

		    		String avgWD = ft.format(Readings.AVGWorkingDay);
		    		String avgNWD = ft.format(Readings.AVGNonWorkingDay);
					if (mm == 13) {
						mm = 1;
						yy ++;
					}
					
				    	 render(dataWD,dataNWD,timeseries,avgWD, avgNWD, sensorID, mm, yy, next , previous );
		    	    }

			}
			index();

		}catch(Exception e){
			//TODO
			}
		}
}