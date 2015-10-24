/**
 * 
 */
package com.cc.api.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cc.api.dto.CompanyInfoDTO;
import com.cc.api.dto.QuestionAnswerDTO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
/**
 * @author kotlaven
 *
 */
public class DropDB {

	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		DropDB setupDB = new DropDB();
		
		MongoClient mongoClient;
		DB dbName;
		
		
		
		try{
			mongoClient = new MongoClient("localhost",27017);
			dbName = mongoClient.getDB("Captcha2dotO");			
			
			//Create Companies			
			DBCollection companyCollection = dbName.getCollection("companyInfo");
			companyCollection.drop();		
			
			//Create Private Random Questions for each Customer
			DBCollection privateQuestionCollection = dbName.getCollection("privateRandomQuestions");
			privateQuestionCollection.drop();
			
			//create public Random Questions
			DBCollection publicQuestionCollection = dbName.getCollection("publicRandomQuestions");
			publicQuestionCollection.drop();
				
		}catch(MongoException me){
			
		}

	}
	
	
}
