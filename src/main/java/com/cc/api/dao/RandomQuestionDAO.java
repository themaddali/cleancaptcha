package com.cc.api.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;

import com.cc.api.dto.CompanyInfoDTO;
import com.cc.api.dto.QuestionAnswerDTO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

/**
 * @author kotlaven
 *
 */

public class RandomQuestionDAO {
	
	private static final Logger logger = Logger.getLogger(RandomQuestionDAO.class);
	
	public static MongoClient mongoClient;
	public static DB dbName;
	public static DBCollection prQnCollections;
	public static DBCollection companyInfoCollection;
	public static DBCollection privateQuestionCollection;
	
	static{
		try{
			//Connect to MongoDB
			mongoClient = new MongoClient("localhost", 27017);
			dbName = mongoClient.getDB("Captcha2dotO");
			prQnCollections = dbName.getCollection("publicRandomQuestions");
			companyInfoCollection = dbName.getCollection("companyInfo");
			privateQuestionCollection = dbName.getCollection("privateRandomQuestions");
		}catch (MongoException e) {
			e.printStackTrace();
		} 
	}
	
	public CompanyInfoDTO registerCompany(CompanyInfoDTO requestDto){
		
		try {
			BasicDBObject companyInfo = new BasicDBObject();
			String companyCode = requestDto.getCompany().substring(0,3)+requestDto.getPhone().substring(0, 3);
			companyInfo.put("companyCode", companyCode);
			companyInfo.put("companyName", requestDto.getCompany());
			companyInfo.put("phoneNumber", requestDto.getPhone());
			companyInfo.put("authKey", authKey(companyCode));
			
			companyInfoCollection.insert(companyInfo);
			
			BasicDBObject searchQuery = new BasicDBObject();		
			searchQuery.put("companyCode", companyCode);
			
			logger.info("Validate companyCode query: "+searchQuery.toString());
			
			DBCursor cursor = companyInfoCollection.find(searchQuery);
			
			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				requestDto.setCompanyCode(dbObject.get("companyCode").toString());
				requestDto.setCompany(dbObject.get("companyName").toString());
				requestDto.setPhone(dbObject.get("phoneNumber").toString());
				requestDto.setAuthKey(dbObject.get("authKey").toString());
				requestDto.setSuccess("Successfully processed the request!");
				break;
			}
		} catch (Exception e) {			
			e.printStackTrace();
			logger.error(e.getMessage());
			requestDto.setError("Error while creating the Client");
			return requestDto;
		}		
		return requestDto;
	}
	
	public String authKey(String companyCode){		
		String randomUuid = UUID.randomUUID().toString();
		String token = "";
		if(randomUuid != null){
			if(randomUuid.contains("-"))
				randomUuid = randomUuid.replace("-", "");
			randomUuid = randomUuid.substring(0, randomUuid.length()-2);
		}	
		token = randomUuid+companyCode; 
	    return token;
	}
	
	public QuestionAnswerDTO getPubRandQuestion(){
		QuestionAnswerDTO randomQuestionDto = null;
		
		long maxNumber = prQnCollections.getCount();
		int randomNumber = ThreadLocalRandom.current().nextInt(0, (int) maxNumber);
		
		DBObject dbObject = prQnCollections.find().limit(-1).skip((int) randomNumber).next();
		
		if(dbObject != null){
			randomQuestionDto = new QuestionAnswerDTO();
			randomQuestionDto.setQuestionId(dbObject.get("questionId").toString());
			randomQuestionDto.setQuestion(dbObject.get("question").toString());
		}
		
		return randomQuestionDto;
	}
	
	public QuestionAnswerDTO validateRandomQuestion(String questionId, String answer){
		QuestionAnswerDTO randomQuestionDto = null;
		
		BasicDBObject validateQuery = new BasicDBObject();
		List<BasicDBObject> validateList = new ArrayList<BasicDBObject>();
		validateList.add(new BasicDBObject("questionId", questionId));
		validateList.add(new BasicDBObject("answer", answer));
		validateQuery.put("$and",validateList);
		
		DBCursor cursor = prQnCollections.find(validateQuery);
		
		if(cursor.hasNext()) {			
			randomQuestionDto = new QuestionAnswerDTO();
			randomQuestionDto.setAnswerStatus("Pass");
			randomQuestionDto.setSuccess("Successfully processed the Request");
		}else{
			randomQuestionDto = getPubRandQuestion();
			randomQuestionDto.setAnswerStatus("Fail");
			randomQuestionDto.setSuccess("Successfully processed the Request");
		}		
		return randomQuestionDto;
	}
	
	//validate authKey and companyCode
	public String validateAuthkey_companyCode(String authKey){
		
		String companyCode = "";		
		try {
			BasicDBObject searchQuery = new BasicDBObject();		
			searchQuery.put("authKey", authKey);
			
			logger.info("Validate authKey and companyCode query: "+searchQuery.toString());
			
			DBCursor cursor = companyInfoCollection.find(searchQuery);
			
			while (cursor.hasNext()) {
				DBObject dbObject = cursor.next();
				companyCode = dbObject.get("companyCode").toString();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}		
		return companyCode;		
	}
	
	//Get random private question based on companyCode
	public QuestionAnswerDTO randomPrivateQuestion(String companyCode){
		QuestionAnswerDTO qandaDto = null;
		
		try {
			BasicDBObject searchQuery = new BasicDBObject();
			searchQuery.put("companyCode", companyCode);
			
			logger.info("Number of privateQuestions by companyCode query: "+searchQuery.toString());
			
			long maxNumber = privateQuestionCollection.getCount(searchQuery);
			logger.info("Total questions for Company "+companyCode+" : "+maxNumber);
			int randomNumber = ThreadLocalRandom.current().nextInt(0, (int) maxNumber);
			
			DBObject dbObject = privateQuestionCollection.find().limit(-1).skip((int) randomNumber).next();
			
			if(dbObject != null){
				qandaDto = new QuestionAnswerDTO();
				qandaDto.setQuestionId(dbObject.get("questionId").toString());
				qandaDto.setQuestion(dbObject.get("question").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			qandaDto.setError("Exception while Generating a random private question.");
		}		
		return qandaDto;
	}
	
	//Validate Private question based on questionID
	public QuestionAnswerDTO validatePrivateQuestion(String questionId, String answer, String companyCode){
		QuestionAnswerDTO qandaDto = null;
		
		try {
			BasicDBObject validateQuery = new BasicDBObject();
			List<BasicDBObject> validateList = new ArrayList<BasicDBObject>();
			validateList.add(new BasicDBObject("questionId", questionId));
			validateList.add(new BasicDBObject("answer", answer));
			validateQuery.put("$and",validateList);
			
			DBCursor cursor = privateQuestionCollection.find(validateQuery);
			
			if(cursor.hasNext()) {
				qandaDto = new QuestionAnswerDTO();
				qandaDto.setAnswerStatus("Pass");
				qandaDto.setSuccess("Successfully processed the Request");
			}else{
				qandaDto = randomPrivateQuestion(companyCode);
				qandaDto.setAnswerStatus("Fail");
				qandaDto.setSuccess("Successfully processed the Request");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			qandaDto.setError("Exception while validating the private question response.");
		}		
		return qandaDto;
	}
}
