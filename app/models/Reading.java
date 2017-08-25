package models;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;

import play.db.jpa.Model;

public class Reading{

	private long readingTimeStamp;
	private double readingValue;
	private String day;
	private Date date;
	private double zScore =0;
	private String outlier="-";
	
	public String getOutlier() {
		return outlier;
	}

	public void setOutlier(String outlier) {
		this.outlier = outlier;
	}

	public double getzScore() {
		return zScore;
	}

	public void setzScore(double zScore) {
		this.zScore = zScore;
	}

	public Reading(long readingTimeStamp, double readingValue, String day) {
		super();
 		//DecimalFormat ft = new DecimalFormat("##########.###");

		this.readingTimeStamp = readingTimeStamp;
		//this.readingValue = Double.parseDouble(ft.format(readingValue));
		this.readingValue = readingValue;

		this.day = day;
		this.date = new Date(readingTimeStamp);
	}

		public double getReadingValue() {
		return readingValue;
	}

	public void setReadingValue(double readingValue) {
		this.readingValue = readingValue;
	}

	public long getReadingTimeStamp() {
		return readingTimeStamp;
	}

	public void setReadingTimeStamp(long readingTimeStamp) {
		this.readingTimeStamp = readingTimeStamp;
		this.date = new Date(this.readingTimeStamp);
	}

	public String getDay() {
		if (day == null) {
			return "no readings available";  
		}
		else
		{
		return day;
		}
	}

	public void setDay(String day) {
		this.day = day;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public String toString (){

		String result = "{\n"
				+ "readingTimeStamp: "+ this.readingTimeStamp +",\n"
				+ "readingValue: " +this.readingValue+",\n"
				+ "day: \"" + this.day +"\",\n"
				+ "date: \"" +this.date+ "\",\n"
				+ "zScore: "+ this.zScore +",\n"
				+ "outlier: \""+this.outlier+"\",\n"
				+"}\n";
		return result;
	}
	

}
