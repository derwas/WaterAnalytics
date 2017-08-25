package controllers;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import models.Organization;
import play.Logger;
import play.Play;

public class Organizations {

	public static void updateOrganisations(){
		Organization.deleteAll();
		String gettUrl=Play.configuration.getProperty("WKAN")+"/api/3/action/organization_list";// put in your url
		//Logger.info("Org List URL "+gettUrl);

			try {
				String organizations = utils.UserVerification.getHTML(gettUrl);
				JSONObject jsonObj = new JSONObject(organizations);
				 if( jsonObj.getBoolean("success")){
						JSONArray jsonArrayResult = jsonObj.getJSONArray("result");
						//Logger.info("json array  "+jsonArrayResult);

						for (int i = 0; i<jsonArrayResult.length(); i++ ){// loop over the list of organisations and create one by one
							Organization o = new Organization();

							String orgID = jsonArrayResult.getString(i);
							o.setid(orgID);
							Logger.info("ORG : "+o.getid());
							String orgDS ="";
							String gettUrl2=Play.configuration.getProperty("WKAN")+"/api/3/action/organization_show?id="+orgID;// put in your url
							//Logger.info("url2 "+gettUrl2);
							String packages = utils.UserVerification.getHTML(gettUrl2);
							JSONObject jsonPackages = new JSONObject(packages);
							//Logger.info("Packages"+jsonPackages);
							JSONArray jsonArrayPackages = jsonPackages.getJSONObject("result").getJSONArray("packages");
							JSONArray jsonArrayUsers = jsonPackages.getJSONObject("result").getJSONArray("users");
							for (int j = 0; j<jsonArrayPackages.length(); j++ ){
								String packageName = jsonArrayPackages.getJSONObject(j).getString("name");
								String gettUrl3=Play.configuration.getProperty("WKAN")+"/api/3/action/package_show?id="+packageName;// put in your url
								String resources = utils.UserVerification.getHTML(gettUrl3);
								//Logger.info("URL 3 "+gettUrl3);
								JSONObject jsonResources = new JSONObject(resources);
								JSONArray jsonArrayResources = jsonResources.getJSONObject("result").getJSONArray("resources");
								//JSONArray jsonArrayGroups = jsonResources.getJSONObject("result").getJSONArray("groups");
								for (int k = 0; k<jsonArrayResources.length(); k++ ){
									String url ;
									String id;
									boolean datastoreActive = false;
									try{
									url = jsonArrayResources.getJSONObject(k).getString("url");
									}catch(Exception e){
										url ="";
									}
									try{
										id = jsonArrayResources.getJSONObject(k).getString("id");
									}catch(Exception e){
											id ="";
										}
									try{
										datastoreActive = jsonArrayResources.getJSONObject(k).getBoolean("datastore_active");
										//Logger.info("=====>"+o.getid()+"   "+id);
	
									}catch(Exception e){
											datastoreActive =false;
										}
									if(url.contains(Play.configuration.getProperty("DATASOURCES"))){
										orgDS = url.substring(Play.configuration.getProperty("DATASOURCES").length()+1, url.length());
										o.setDatasource(orgDS);
										//Logger.info("Data Source : "+o.getDatasources());
									}else{
										//if (datastoreActive){
											String gettUrl4=Play.configuration.getProperty("WKAN")+"/api/3/action/datastore_search?resource_id="+id;// put in your url
											//Logger.info("ORG = "+o.getid()+" URL:"+gettUrl4);
											try{
											String entitiesStr = utils.UserVerification.getHTML(gettUrl4);
											
											JSONObject entities = new JSONObject(entitiesStr);
											
											if(entities.getBoolean("success")){
													JSONArray jsonArrayEntities = entities.getJSONObject("result").getJSONArray("records");
													//Logger.info("result = "+jsonArrayEntities.toString());
													for (int l = 0; l<jsonArrayEntities.length(); l++ ){
														String sensorID = jsonArrayEntities.getJSONObject(l).getString("Identifier");
														String type = jsonArrayEntities.getJSONObject(l).getString("Type");
														if(type.toLowerCase().contains("sensor")){
															o.addSensor(sensorID);
														//Logger.info("org: "+o.toString());
														}
													}
	
											}
										
											}catch(Exception e){
												//TODO
												
											}
										//}
									}
								}
//								for (int k = 0; k<jsonArrayGroups.length(); k++ ){
//									String title ;
//									try{
//									title = jsonArrayGroups.getJSONObject(k).getString("title");
//									}catch(Exception e){
//										title ="";
//									}
//									if(title.length()>0){
//										o.addSensor(title);
//									}
//								}
							}
							
							for (int l = 0; l<jsonArrayUsers.length(); l++ ){
								String userID=jsonArrayUsers.getJSONObject(l).getString("name");
								o.addUser(userID);
							}

							o.save();
							//Logger.info("Organisation updated: " + o.toString());

						}
				 }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			showOrganisations();
	}
	
	public static boolean verifyUserandDataSource(String userId, String dataSource){
		//TODO
		//Logger.info("verifyUserandDataSource");

		//updateOrganisations();
		
		//showOrganisations();
		//Logger.info("Organizations "+Organization.count());
		String orgID = getOrganisationByDataSource(dataSource);
		if (orgID!=null){
			return userInOrganisation(userId, orgID);
		}else
		{
			return false;
		}
	}
	
	public static void showOrganisations() {
		Iterator<Organization> it = ((List)Organization.findAll()).iterator();
		while(it.hasNext()){
			Organization org = it.next();
			System.out.println(org.toString());
		}		
	}

	public static boolean userInOrganisation(String userID, String orgID){
		Iterator<Organization> it = ((List)Organization.findAll()).iterator();
		while(it.hasNext()){
			Organization org = it.next();
			if (org.getid().equals(orgID)){
				return org.getUsers().contains(";"+userID+";");
			}
		}
		return false;
	}
	public static String getOrganisationByDataSource(String dataSource){
		Iterator<Organization> it = ((List)Organization.findAll()).iterator();
		while(it.hasNext()){
			Organization org = it.next();
			if(org.getDatasources()!=null){
			if (org.getDatasources().contains(dataSource)){
				return org.getid();
			}
			}
		}
		return null;
	}
	
    public static String getDataSourceFromSensorID(String sensorID){
		Iterator<Organization> it = ((List)Organization.findAll()).iterator();
		while(it.hasNext()){
			Organization org = it.next();
			if(org.getSensors().length()>1){
				if (org.getSensors().contains(";"+sensorID+";")){
					//Logger.info("Org"+org.toString());

					return org.getDatasources();
				}
			}
		}
    	
    	return null;
    }
}
