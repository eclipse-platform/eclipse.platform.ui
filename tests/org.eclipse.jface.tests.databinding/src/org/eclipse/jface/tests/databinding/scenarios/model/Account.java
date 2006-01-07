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
package org.eclipse.jface.tests.databinding.scenarios.model;

public class Account extends ModelObject {

	private String country;

	public void setFirstName(String string) {
		// TODO Auto-generated method stub
		
	}

	public void setLastName(String string) {
		// TODO Auto-generated method stub
		
	}

	public void setState(String string) {
		// TODO Auto-generated method stub
		
	}

	public void setPhone(String string) {
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		return null;
	}

	public String getLastName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPhone() {
		// TODO Auto-generated method stub
		return null;
	}

}
