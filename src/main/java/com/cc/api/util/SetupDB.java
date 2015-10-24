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
public class SetupDB {
	
	
	public BasicDBObject createPublicQuestions(String questionId, String question, String answer){
		BasicDBObject publicQuestion = new BasicDBObject();
		publicQuestion.put("questionId", questionId);
		publicQuestion.put("question", question);
		publicQuestion.put("answer", answer);
		return publicQuestion;
	}
	
	public BasicDBObject createPrivateQuestion(String questionId, String question, String answer, String companyCode){
		BasicDBObject privateQuestion = new BasicDBObject();
		privateQuestion.put("questionId", questionId);
		privateQuestion.put("question", question);
		privateQuestion.put("answer", answer);
		privateQuestion.put("companyCode", companyCode);
		return privateQuestion;
	}
	
	public BasicDBObject putCompanyInfo(String companyCode, String companyName, String phoneNumber, String authKey){
		BasicDBObject companyInfo = new BasicDBObject();
		companyInfo.put("companyCode", companyCode);
		companyInfo.put("companyName", companyName);
		companyInfo.put("phoneNumber", phoneNumber);
		companyInfo.put("authKey", authKey);		
		return companyInfo;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		SetupDB setupDB = new SetupDB();		
		MongoClient mongoClient;
		DB dbName;	
		
		try{
			mongoClient = new MongoClient("localhost",27017);
			dbName = mongoClient.getDB("Captcha2dotO");
			
			
			//Create Companies			
			DBCollection companyCollection = dbName.getCollection("companyInfo");
			
			List<CompanyInfoDTO> companyInfoDTOList = setupDB.readCompanyExcelData();
			
			for(CompanyInfoDTO companyInfoDto : companyInfoDTOList){
				BasicDBObject companyInfo = setupDB.putCompanyInfo(companyInfoDto.getCompanyCode(), companyInfoDto.getCompany(), companyInfoDto.getPhone(), companyInfoDto.getAuthKey());
				companyCollection.insert(companyInfo);
			}
			
			DBCursor companyInfoCursor = companyCollection.find();
			while (companyInfoCursor.hasNext()) {				
				System.out.println(companyInfoCursor.next());
			}
			
			
			//Create Private Random Questions for each Customer
			DBCollection privateQuestionCollection = dbName.getCollection("privateRandomQuestions");
			
			List<QuestionAnswerDTO> privateQuestionDTOList = setupDB.readPrivateQuestionData();
			
			for(QuestionAnswerDTO privateQuestionDto:privateQuestionDTOList){
				BasicDBObject privateQuestion = setupDB.createPrivateQuestion(privateQuestionDto.getQuestionId(), privateQuestionDto.getQuestion(), privateQuestionDto.getAnswer(),privateQuestionDto.getCompanyCode());
				privateQuestionCollection.insert(privateQuestion);
			}
			
			DBCursor privateQuestionCursor = privateQuestionCollection.find();
			while(privateQuestionCursor.hasNext()){
				System.out.println(privateQuestionCursor.next());
			}
			
			
			//create public Random Questions
			DBCollection publicQuestionCollection = dbName.getCollection("publicRandomQuestions");
			
			List<QuestionAnswerDTO> publicQuestionDTOList = setupDB.readPublicQuestionData();
			
			for(QuestionAnswerDTO publicQuestionDto: publicQuestionDTOList){
				BasicDBObject publicQuestion = setupDB.createPublicQuestions(publicQuestionDto.getQuestionId(), publicQuestionDto.getQuestion(), publicQuestionDto.getAnswer());
				publicQuestionCollection.insert(publicQuestion);
			}
			
//			long maxNumber = publicQuestionCollection.getCount();
//			
			DBCursor prQuestionsCursor = publicQuestionCollection.find();
			while (prQuestionsCursor.hasNext()) {				
				System.out.println(prQuestionsCursor.next());
			}
//			
//			
//			//Get Random Record
//			
//			long randomNumber = ThreadLocalRandom.current().nextLong(0, maxNumber);
//			
//			DBObject dbObject = publicQuestionCollection.find().limit(-1).skip((int) randomNumber).next();
//			
//			if(dbObject != null){
//				PublicRandomQuestionDTO prqDto = new PublicRandomQuestionDTO();
//				prqDto.setQuestionId(dbObject.get("questionId").toString());
//				prqDto.setQuestion(dbObject.get("question").toString());
//				prqDto.setAnswer(dbObject.get("answer").toString());
//			}			
			
		}catch(MongoException me){
			
		}

	}
	
	public List<CompanyInfoDTO> readCompanyExcelData(){		
		try{
            FileInputStream file = new FileInputStream(new File("cleanCap.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(2);
            
            List<CompanyInfoDTO> companyInfoDTOList = new ArrayList<CompanyInfoDTO>();
            
            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();
            while (rowIterator.hasNext()){
            	CompanyInfoDTO companyInfoDto = new CompanyInfoDTO();
                Row row = rowIterator.next();                
                companyInfoDto.setCompanyCode(row.getCell(0).getStringCellValue());
                companyInfoDto.setCompany(row.getCell(1).getStringCellValue());
                companyInfoDto.setPhone(row.getCell(2).getStringCellValue());
                companyInfoDto.setAuthKey(authKey(row.getCell(0).getStringCellValue()));
                companyInfoDTOList.add(companyInfoDto);
            }
            file.close();            
            return companyInfoDTOList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
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
	
	public List<QuestionAnswerDTO> readPrivateQuestionData(){
		
		try{
            FileInputStream file = new FileInputStream(new File("cleanCap.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(1);
            
            List<QuestionAnswerDTO> privateQuestionDTOList = new ArrayList<QuestionAnswerDTO>();
            
            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();
            while (rowIterator.hasNext()){
            	QuestionAnswerDTO privateQuestionDto = new QuestionAnswerDTO();
                Row row = rowIterator.next();  
                
              //For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();

                while(cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    //This will change all Cell Types to String
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    switch(cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_BOOLEAN:
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            break;
                        case Cell.CELL_TYPE_STRING:
                        	break;
                    }
                }
                
                privateQuestionDto.setQuestionId(row.getCell(0).getStringCellValue());
                privateQuestionDto.setQuestion(row.getCell(1).getStringCellValue());
                privateQuestionDto.setAnswer(row.getCell(2).getStringCellValue());
                privateQuestionDto.setCompanyCode(row.getCell(3).getStringCellValue());
                privateQuestionDTOList.add(privateQuestionDto);
            }
            file.close();            
            return privateQuestionDTOList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
		
	}
	
	public List<QuestionAnswerDTO> readPublicQuestionData(){
		try{
            FileInputStream file = new FileInputStream(new File("cleanCap.xlsx"));
 
            //Create Workbook instance holding reference to .xlsx file
            XSSFWorkbook workbook = new XSSFWorkbook(file);
 
            //Get first/desired sheet from the workbook
            XSSFSheet sheet = workbook.getSheetAt(0);
            
            List<QuestionAnswerDTO> publicQuestionDTOList = new ArrayList<QuestionAnswerDTO>();
            
            //Iterate through each rows one by one
            Iterator<Row> rowIterator = sheet.iterator();
            rowIterator.next();
            while (rowIterator.hasNext()){
            	QuestionAnswerDTO publicQuestionDto = new QuestionAnswerDTO();
            	Row row = rowIterator.next();  
                
                //For each row, iterate through each columns
                Iterator<Cell> cellIterator = row.cellIterator();
                
                while(cellIterator.hasNext())
                {
                    Cell cell = cellIterator.next();
                    //This will change all Cell Types to String
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    switch(cell.getCellType()) 
                    {
                        case Cell.CELL_TYPE_BOOLEAN:
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            break;
                        case Cell.CELL_TYPE_STRING:
                        	break;
                    }
                }
                                
                publicQuestionDto.setQuestionId(row.getCell(0).getStringCellValue());
                publicQuestionDto.setQuestion(row.getCell(1).getStringCellValue());
                publicQuestionDto.setAnswer(row.getCell(2).getStringCellValue());
                publicQuestionDTOList.add(publicQuestionDto);
            }
            file.close();            
            return publicQuestionDTOList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
	}
}
