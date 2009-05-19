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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;

import org.eclipse.osgi.internal.signedcontent.Base64;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class Contact {
	private String firstName;
	private String lastName;
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

	public String getNote() {
		return note;
	}

	public void setNote(String comment) {
		this.note = comment;
	}

	private Image image;
	private Image scaledImage;

	public Image getScaledImage() {
		return scaledImage;
	}

	public void setScaledImage(Image scaledImage) {
		this.scaledImage = scaledImage;
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

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

	@Override
	public String toString() {
		return firstName + " " + lastName;
	}

	public void saveAsVCard(String fileName) {
		String charSet = "CHARSET="
				+ java.nio.charset.Charset.defaultCharset().name();
		String vCard = "BEGIN:VCARD" + "\nVERSION:2.1" + "\nN;" + charSet + ":"
				+ lastName + ";" + firstName + "\nFN;" + charSet + ":"
				+ firstName + " " + lastName + "\nORG;" + charSet + ":"
				+ company + "\nTITLE:" + jobTitle + "\nTEL;WORK;VOICE:" + phone
				+ "\nTEL;CELL;VOICE:" + mobile + "\nADR;WORK;" + charSet + ":"
				+ ";;" + street + ";" + city + ";" + state + ";" + zip + ";"
				+ country + "\nURL;WORK:" + webPage + "\nEMAIL;PREF;INTERNET:"
				+ email + "\nEND:VCARD\n";

		PrintWriter out;
		try {
			out = new PrintWriter(fileName);
			out.println(vCard);
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readFromVCard(String fileName) {
		BufferedReader bufferedReader = null;
		String charSet = "Windows-1252";

		// First try to guess the char set
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				int index = line.indexOf("CHARSET=");
				if (index != -1) {
					int endIndex = index + 8;
					while (line.charAt(endIndex) != ':'
							&& line.charAt(endIndex) != ';') {
						endIndex += 1;
					}
					charSet = line.substring(index + 8, endIndex);
					break;
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Then parse the vCard
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), charSet));
			String line;
			String value;
			while ((line = bufferedReader.readLine()) != null) {
				value = getVCardValue(line, "N");
				if (value != null) {
					int separator = value.indexOf(";");
					lastName = value.substring(0, separator);
					firstName = value.substring(separator + 1);
					continue;
				}
				value = getVCardValue(line, "TEL;WORK");
				if (value != null) {
					phone = value;
					continue;
				}
				value = getVCardValue(line, "TEL;CELL");
				if (value != null) {
					mobile = value;
					continue;
				}
				value = getVCardValue(line, "ADR;WORK");
				if (value != null) {
					StringTokenizer tokenizer = new StringTokenizer(value, ";");
					if (tokenizer.hasMoreElements()) {
						street = tokenizer.nextToken();
					}
					if (tokenizer.hasMoreElements()) {
						city = tokenizer.nextToken();
					}
					if (tokenizer.hasMoreElements()) {
						zip = tokenizer.nextToken();
					}
					// if (tokenizer.hasMoreElements()) {
					// state = tokenizer.nextToken();
					// }
					if (tokenizer.hasMoreElements()) {
						country = tokenizer.nextToken();
					}
					continue;
				}
				value = getVCardValue(line, "EMAIL;PREF;INTERNET");
				if (value != null) {
					email = value;
					continue;
				}
				value = getVCardValue(line, "URL;WORK");
				if (value != null) {
					webPage = value;
					continue;
				}
				value = getVCardValue(line, "ORG");
				if (value != null) {
					company = value;
					continue;
				}
				value = getVCardValue(line, "TITLE");
				if (value != null) {
					jobTitle = value;
					continue;
				}
				value = getVCardValue(line, "NOTE");
				if (value != null) {
					note = value;
					continue;
				}
				value = getVCardValue(line, "PHOTO;TYPE=JPEG;ENCODING=BASE64");
				if (value != null) {
					line = bufferedReader.readLine();
					String base64 = "";
					while (line != null && line.length() > 0
							&& line.charAt(0) == ' ') {
						base64 += line.trim();
						line = bufferedReader.readLine();
					}
					byte[] imageBytes = Base64.decode(base64.getBytes());
					ByteArrayInputStream is = new ByteArrayInputStream(
							imageBytes);
					ImageData imageData = new ImageData(is);
					double ratio = imageData.height / 99.0;
					int width = (int) (imageData.width / ratio);
					if (width > 80)
						width = 80;
					ImageData ScaledImageData = imageData.scaledTo(width, 99);

					image = new Image(Display.getCurrent(), imageData);
					scaledImage = new Image(Display.getCurrent(),
							ScaledImageData);

					continue;
				}

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String getVCardValue(String line, String token) {
		if (line.startsWith(token + ":") || line.startsWith(token + ";")) {
			String value = line.substring(line.indexOf(":") + 1);
			return value;
		}
		return null;
	}
}
