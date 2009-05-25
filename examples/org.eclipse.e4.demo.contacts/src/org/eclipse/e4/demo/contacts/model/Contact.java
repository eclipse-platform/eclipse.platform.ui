/*******************************************************************************
 * Copyright (c) 2009 Siemens AG and others.
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 * 
 * Contributors:
 *     Kai Tödter - initial implementation
 ******************************************************************************/

package org.eclipse.e4.demo.contacts.model;

import org.eclipse.swt.graphics.Image;

public class Contact {
	private String firstName;
	private String middleName;
	private String lastName;
	private String title;
	private String company;
	private String jobTitle;
	private String street;
	private String city;
	private String zip;
	private String state;
	private String country;
	private String email;
	private String webPage;
	private String phone;
	private String mobile;
	private String note;
	private Image image;

	public Contact() {
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String businessCity) {
		this.city = businessCity;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String businessZip) {
		this.zip = businessZip;
	}

	public String getState() {
		return state;
	}

	public void setState(String businessState) {
		this.state = businessState;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String businessCountry) {
		this.country = businessCountry;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String businessEmail) {
		this.email = businessEmail;
	}

	public String getWebPage() {
		return webPage;
	}

	public void setWebPage(String businessWebPage) {
		this.webPage = businessWebPage;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String businessPhone) {
		this.phone = businessPhone;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String businessMobile) {
		this.mobile = businessMobile;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String comment) {
		this.note = comment;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	@Override
	public String toString() {
		return firstName + " " + lastName;
	}
}
