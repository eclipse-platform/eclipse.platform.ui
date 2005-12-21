/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

public class Account extends ModelObject {

	private String country;
	private String firstName;
	private String lastName;
	private String state;
	private String phone;	

	public void setFirstName(String string) {
		String oldValue = firstName;
		firstName = string;
		firePropertyChange("firstName", oldValue, string);		
	}

	public void setLastName(String string) {
		String oldValue = lastName;
		lastName = string;
		firePropertyChange("lastName", oldValue, string);				
	}

	public void setState(String string) {
		String oldValue = state;
		state = string;
		firePropertyChange("state", oldValue, string);		
	}

	public void setPhone(String string) {
		String oldValue = phone;
		phone = string;
		firePropertyChange("phone", oldValue, phone);		
	}

	public void setCountry(String string) {
		Object oldValue = country;
		country = string;
		firePropertyChange("country", oldValue, string);
	}

	public String getCountry() {
		return country;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getState() {
		return state;
	}

	public String getPhone() {
		return phone;
	}

}
