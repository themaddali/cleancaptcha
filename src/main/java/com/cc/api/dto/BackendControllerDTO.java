/**
 * 
 */
package com.cc.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author kotlaven
 *
 */
@JsonInclude(Include.NON_NULL)
public class BackendControllerDTO {
	
	String setupStatus;
	String dropStatus;
	
	public String getSetupStatus() {
		return setupStatus;
	}
	public void setSetupStatus(String setupStatus) {
		this.setupStatus = setupStatus;
	}
	public String getDropStatus() {
		return dropStatus;
	}
	public void setDropStatus(String dropStatus) {
		this.dropStatus = dropStatus;
	}
}
