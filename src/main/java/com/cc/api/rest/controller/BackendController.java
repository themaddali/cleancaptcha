/**
 * 
 */
package com.cc.api.rest.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.cc.api.dao.BackendDAO;
import com.cc.api.dto.BackendControllerDTO;

/**
 * @author kotlaven
 *
 */
@ComponentScan
@Controller
public class BackendController {
	
	//Setup Entire DB - CompanyInfo, Private and Public Questions
	@RequestMapping(value="/setupDB",method=RequestMethod.POST, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public BackendControllerDTO setupDB(HttpServletRequest request){
		
		BackendControllerDTO beController = new BackendControllerDTO();
		BackendDAO backendDao = new BackendDAO();
		String authKey = request.getHeader("authKey");
		String setupStatus = "";
		if(authKey != null && authKey.equals("c!3anc@ptcha")){			
			setupStatus = backendDao.setupDB();			
		}else{
			setupStatus = "AuthKey is not valid.Database Not Setup.";
		}
		beController.setSetupStatus(setupStatus);
		
		return beController;
	}
	
	//Setup Entire DB - CompanyInfo, Private and Public Questions
	@RequestMapping(value="/dropDB",method=RequestMethod.POST, produces="application/json")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public BackendControllerDTO dropDB(HttpServletRequest request){
		
		BackendControllerDTO beController = new BackendControllerDTO();
		BackendDAO backendDao = new BackendDAO();
		String authKey = request.getHeader("authKey");
		String dropStatus = "";
		if(authKey != null && authKey.equals("c!3anc@ptcha")){			
			dropStatus = backendDao.dropDB();			
		}else{
			dropStatus = "AuthKey is not valid. Database Collections not dropped!";
		}
		beController.setDropStatus(dropStatus);
		
		return beController;
	}

}
