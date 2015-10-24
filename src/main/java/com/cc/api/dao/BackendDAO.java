/**
 * 
 */
package com.cc.api.dao;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.cc.api.dto.CompanyInfoDTO;
import com.cc.api.dto.QuestionAnswerDTO;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoException;

/**
 * @author kotlaven
 *
 */
public class BackendDAO {
	
	private static final Logger logger = Logger.getLogger(BackendDAO.class);
	
	public String dropDB(){
		String dropStatus = "";
		
		try{
			
			//Drop Companies			
			DBCollection companyCollection = RandomQuestionDAO.dbName.getCollection("companyInfo");
			companyCollection.drop();		
			
			//Drop Private Random Questions for each Customer
			DBCollection privateQuestionCollection = RandomQuestionDAO.dbName.getCollection("privateRandomQuestions");
			privateQuestionCollection.drop();
			
			//Drop public Random Questions
			DBCollection publicQuestionCollection = RandomQuestionDAO.dbName.getCollection("publicRandomQuestions");
			publicQuestionCollection.drop();
			
			logger.info("Successfully dropped the Collections in Database.");
			dropStatus = "Successfully dropped the Collections in Database.";			
		}catch(MongoException me){
			logger.error("Exception while dropping the collections in Database."+me.getMessage());
			dropStatus = "Exception while dropping the collections in Database.";
		}
		
		return dropStatus;
	}
	
	public String setupDB(){
		String setupStatus = "";
		
		try{
			
			logger.info("Setting up the Database...");
			
			//Create Companies			
			DBCollection companyCollection = RandomQuestionDAO.dbName.getCollection("companyInfo");
			
			List<CompanyInfoDTO> companyInfoDTOList = readCompanyExcelData();
			
			if(companyInfoDTOList != null){			
				for(CompanyInfoDTO companyInfoDto : companyInfoDTOList){
					BasicDBObject companyInfo = putCompanyInfo(companyInfoDto.getCompanyCode(), companyInfoDto.getCompany(), companyInfoDto.getPhone(), companyInfoDto.getAuthKey());
					companyCollection.insert(companyInfo);
				}
				DBCursor companyInfoCursor = companyCollection.find();
				while (companyInfoCursor.hasNext()) {				
					logger.debug(companyInfoCursor.next());
				}
			}else{
				logger.error("Company Data not available in Excel file.");
			}						
			
			//Create Private Random Questions for each Customer
			DBCollection privateQuestionCollection = RandomQuestionDAO.dbName.getCollection("privateRandomQuestions");
			
			List<QuestionAnswerDTO> privateQuestionDTOList = readPrivateQuestionData();
			
			if(privateQuestionDTOList != null){
				for(QuestionAnswerDTO privateQuestionDto:privateQuestionDTOList){
					BasicDBObject privateQuestion = createPrivateQuestion(privateQuestionDto.getQuestionId(), privateQuestionDto.getQuestion(), privateQuestionDto.getAnswer(),privateQuestionDto.getCompanyCode());
					privateQuestionCollection.insert(privateQuestion);
				}
				DBCursor privateQuestionCursor = privateQuestionCollection.find();
				while(privateQuestionCursor.hasNext()){
					logger.debug(privateQuestionCursor.next());
				}
			}else{
				logger.error("Private Questions not available in Excel file.");
			}						
			
			//create public Random Questions
			DBCollection publicQuestionCollection = RandomQuestionDAO.dbName.getCollection("publicRandomQuestions");
			
			List<QuestionAnswerDTO> publicQuestionDTOList = readPublicQuestionData();
			if(publicQuestionDTOList != null){
				for(QuestionAnswerDTO publicQuestionDto: publicQuestionDTOList){
					BasicDBObject publicQuestion = createPublicQuestions(publicQuestionDto.getQuestionId(), publicQuestionDto.getQuestion(), publicQuestionDto.getAnswer());
					publicQuestionCollection.insert(publicQuestion);
				}				
				DBCursor prQuestionsCursor = publicQuestionCollection.find();
				while (prQuestionsCursor.hasNext()) {				
					logger.debug(prQuestionsCursor.next());
				}
			}else{
				logger.error("Public questions not available in Excel File.");
			}		
			
			logger.info("Database setup complete.");			
			setupStatus = "Database setup complete.";
			
		}catch(MongoException me){
			logger.error("Exception while setting up the Database"+ me.getMessage());
			setupStatus = "Exception while setting up the Database.";
		}
		
		return setupStatus;
	}
	
	public List<CompanyInfoDTO> readCompanyExcelData(){		
		try{
			
			
            FileInputStream file = new FileInputStream(getFile("cleanCap.xlsx"));
 
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
            FileInputStream file = new FileInputStream(getFile("cleanCap.xlsx"));
 
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
	        FileInputStream file = new FileInputStream(getFile("cleanCap.xlsx"));
	
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

	private File getFile(String fileName) {

		//Get file from resources folder
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource(fileName).getFile());
			
		return file;

	  }
	
}
