package utils;

import java.io.*;
import java.net.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;

import models.Organization;
import play.Logger;
import play.Play;



public class UserVerification {
	
	public static String getHTML(String urlToRead) {
	    StringBuilder result = new StringBuilder();
	    URL url;
		try {
			url = new URL(urlToRead);

	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.addRequestProperty("Authorization", Play.configuration.getProperty("APIKEY"));
	    conn.setRequestMethod("GET");
	    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    String line;
	    while ((line = rd.readLine()) != null) {
	       result.append(line);
	    }
	    rd.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.info("Not a valid query: " + urlToRead);
			//e.printStackTrace();
		}
	    return result.toString();
	 }
	
	public static boolean verifyIDandAPIKey(String userId, String apiKey){
		boolean result = false;
		//TODO query ckan
		String gettUrl=Play.configuration.getProperty("WKAN")+"/api/3/action/user_list?q="+userId;// put in your url
		//Logger.info("\n\nWKAN query  "+gettUrl);
		try {
			String profile = getHTML(gettUrl);
			 JSONObject jsonObj = new JSONObject(profile);
			 if( jsonObj.getBoolean("success")){
					//Logger.info("WKAN json  "+jsonObj);
					JSONArray jsonArrayResult = jsonObj.getJSONArray("result");
					//Logger.info("json array  "+jsonArrayResult);

					for (int i = 0; i<jsonArrayResult.length(); i++ ){
						JSONObject js = jsonArrayResult.getJSONObject(i);
						//Logger.info("\n\n one user  "+js);

						String key = js.getString("apikey");
						//Logger.info("\n\n apikey = "+key);
						if(key.equals(apiKey)){
							result = true;
						}
					}
				 
			 }else{
				 result = false;
			 }
		} catch (Exception e) {
			result= false;
		}
		//if (userId.equals(apiKey)){
		//	result=true;
		//}		
		return result;
	}
	

	
	

}
