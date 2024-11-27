/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.model;

import java.util.Date;

import org.eclipse.jface.examples.databinding.ModelObject;

public class Account extends ModelObject {

	private String country;
	private String firstName;
	private String lastName;
	private String state;
	private String phone;
	private Date expiryDate;

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

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		firePropertyChange("expiryDate", this.expiryDate, this.expiryDate = expiryDate);
	}

}