/**
 * 
 */
package com.cc.api.rest.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cc.api.dao.RandomQuestionDAO;
import com.cc.api.dto.CompanyInfoDTO;
import com.cc.api.dto.QuestionAnswerDTO;

/**
 * @author kotlaven
 *
 */
@ComponentScan
@Controller
public class CaptchaMainController {
	
	private static final Logger logger = Logger.getLogger(CaptchaMainController.class);
	
//	//Public Random Question
//	@RequestMapping(value="/prquestion",method=RequestMethod.GET, produces="application/json")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	public PublicRandomQuestionDTO getPubRandomQuestion(){
//		
//		RandomQuestionDAO randomQnDao = new RandomQuestionDAO();
//		
//		PublicRandomQuestionDTO randomQuestionDto = randomQnDao.getPubRandQuestion();
//		
//		return randomQuestionDto;
//	}
//	
//	//Validate Public Random Question
//	@RequestMapping(value="/validateprquestion",method=RequestMethod.POST, produces="application/json")
//	@ResponseStatus(HttpStatus.OK)
//	@ResponseBody
//	public PublicRandomQuestionDTO validatePubRandomQuestion(@RequestBody PublicRandomQuestionDTO prqnDto){
//		
//		RandomQuestionDAO randomQnDao = new RandomQuestionDAO();
//		
//		PublicRandomQuestionDTO randomQuestionDto = randomQnDao.validateRandomQuestion(prqnDto.getQuestionId(), prqnDto.getAnswer());
//		
//		return randomQuestionDto;
//	}
	
	//Random Question - Private and Public
	@RequestMapping(value="/getCleanCaptcha",method=RequestMethod.GET, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public QuestionAnswerDTO getPrivateQuestion(HttpServletRequest request){
		
		QuestionAnswerDTO qandaDto = new QuestionAnswerDTO();
		RandomQuestionDAO randomQnDao = new RandomQuestionDAO();
		String authKey = request.getHeader("authKey");
		boolean validAuthKey = false;
		if(authKey != null){
			//validate authKey and get the company code			
			String companyCode = randomQnDao.validateAuthkey_companyCode(authKey); 
			if(companyCode != null){
				validAuthKey = true;
				//send new private random question based on company code
				qandaDto = randomQnDao.randomPrivateQuestion(companyCode);
			}			
		}
		//Get public Random Question
		else if(!validAuthKey){
			qandaDto = randomQnDao.getPubRandQuestion();
		}
		return qandaDto;
	}
	
	//Validate Random Question - Private and Public
	@RequestMapping(value="/validateResponse",method=RequestMethod.POST, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public QuestionAnswerDTO validatePrivateQuestion(HttpServletRequest request, @RequestBody QuestionAnswerDTO requestQandADto){
		
		QuestionAnswerDTO qandaDto = new QuestionAnswerDTO();
		RandomQuestionDAO randomQnDao = new RandomQuestionDAO();
		String authKey = request.getHeader("authKey");
		boolean validAuthKey = false;
		if(authKey != null && requestQandADto.getQuestionId() != null && requestQandADto.getAnswer() != null){
			//validate authKey and companyCode			
			String companyCode = randomQnDao.validateAuthkey_companyCode(authKey); 
			if(companyCode != null){
				validAuthKey = true;
				//send new private random question based on company code
				qandaDto = randomQnDao.validatePrivateQuestion(requestQandADto.getQuestionId(), requestQandADto.getAnswer(), companyCode);
			}else if(!validAuthKey){
				qandaDto.setError("Invalid authKey Provided!");
				return qandaDto;
			}
		}else if(authKey == null && requestQandADto.getQuestionId() != null && requestQandADto.getAnswer() != null){
			qandaDto = randomQnDao.validateRandomQuestion(requestQandADto.getQuestionId(), requestQandADto.getAnswer());
		}
		return qandaDto;
	}
	
	//Register new Client
	@RequestMapping(value="/registerClient",method=RequestMethod.POST, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public CompanyInfoDTO validatePrivateQuestion(@RequestBody CompanyInfoDTO requestDto){
		
		CompanyInfoDTO companyInfoDto = new CompanyInfoDTO();
		RandomQuestionDAO randomQnDao = new RandomQuestionDAO();
		
		companyInfoDto = randomQnDao.registerCompany(requestDto);
		
		return companyInfoDto;
	}
}
