package org.apache.camel.component.msmq;

import java.io.Serializable;

public class Person implements Serializable {

	private String firstName;
	private String secondName;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5644593143259145034L;

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

	public String getSecondName() {
		return secondName;
	}

}
