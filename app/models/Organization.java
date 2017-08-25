package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class Organization extends Model{


	private String name;
	private String datasource=";";
	private String users=";";
@Column(columnDefinition="TEXT")
	private String sensors=";";

	
	public String getSensors() {
		return sensors;
	}
	public void setSensors(String sensors) {
		this.sensors = sensors;
	}
	public void addUser(String usr){
		users=users+usr+";";
	}
	
	public String getUsers() {
		return users;
	}

	public void addSensor(String sensorID){
		sensors=sensors+sensorID+";";
	}

	public String getid() {
		return name;
	}

	public void setid(String id) {
		this.name = id;
	}

	
	@Override
	public String toString() {
		return "Organization [name=" + name + ", datasource=" + datasource + ", users=" + users + ", sensors=" + sensors
				+ "]";
	}
	public Organization(String orgID, String orgDS) {
		this.name = orgID;
		this.setDatasource(orgDS);
	}

	public Organization() {
		// TODO Auto-generated constructor stub
	}
	public String getDatasources() {
		return datasource;
	}

	public void setDatasource(String datasource) {
		this.datasource = this.datasource +datasource+";";
	}
}
