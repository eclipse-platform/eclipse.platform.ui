package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Vector;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.*;

/**
 * Example IPropertySource who itself is NOT editable, but whose children are.
 * The values of the children determine the value of the address.
 */
public class Address implements IPropertySource {

	//properties
	private String city;
	private Integer province;
	private String postalCode;
	private StreetAddress street;

	//Property unique keys
	public static final String P_ID_STREET = "Address.Street";
	public static final String P_ID_CITY = "Address.City";
	public static final String P_ID_PROVINCE = "Address.Province";
	public static final String P_ID_POSTALCODE = "Address.PostalCode";

	//Property display keys
	public static final String P_STREET = "Street";
	public static final String P_CITY = "City";
	public static final String P_PROVINCE = "Province";
	public static final String P_POSTALCODE = "PostalCode";
	public static final String P_DESCRIPTORS = "properties";


	//default values	
	private static final StreetAddress STREET_DEFAULT = new StreetAddress();
	private static final String CITY_DEFAULT = "unspecified city";
	private static final Integer PROVINCE_DEFAULT = new Integer(0);
	private static final String POSTALCODE_DEFAULT = "A1B2C3";

	//
	static private class ProvinceLabelProvider extends LabelProvider {
		public String getText(Object element) {
			String[] provinceValues = new String[] {"British Columbia", "Alberta", "Saskatchewan", "Manitoba", "Ontario", "Quebec", "Newfoundland", "Prince Edward Island", "Nova Scotia", "New Brunswick", "Yukon", "North West Territories", "Nunavut"};
			return provinceValues[((Integer)element).intValue()];
		}
	}

	//
	private static Vector descriptors;
	private static String[] provinceValues;
	static {
		descriptors = new Vector();
		provinceValues = new String[] {"British Columbia", "Alberta", "Saskatchewan", "Manitoba", "Ontario", "Quebec", "Newfoundland", "Prince Edward Island", "Nova Scotia", "New Brunswick", "Yukon", "North West Territories", "Nunavut"};
		descriptors.addElement(new PropertyDescriptor(P_ID_STREET, P_STREET));
		descriptors.addElement(new TextPropertyDescriptor(P_ID_CITY, P_CITY));


		//PostalCode
		PropertyDescriptor propertyDescriptor = new TextPropertyDescriptor(P_ID_POSTALCODE, P_POSTALCODE);
		//add custom validator to propertyDescriptor limiting postalcode
		//values to XYXYXY, where X is a letter and Y is a digit
		propertyDescriptor.setValidator(new ICellEditorValidator() {
			public String isValid(Object value) {
				if (value == null)
					return "postal code is incomplete";

				//
				String testPostalCode = ((String) value).toUpperCase();
				final int length = testPostalCode.length();
				final char space = ' ';

				//removes white space
				StringBuffer postalCodeBuffer = new StringBuffer(6);
				char current;
				for (int i = 0; i < length; i++) {
					current = testPostalCode.charAt(i);
					if (current != space)
						postalCodeBuffer.append(current);
				}
				testPostalCode = postalCodeBuffer.toString();

				//check for proper length
				if (testPostalCode.length() != 6) {
					//fail	
					return testPostalCode + " is an invalid format for a postal code";
				}

				//check for proper format
				if (
					testPostalCode.charAt(1) < '0' || testPostalCode.charAt(1) > '9' || 
					testPostalCode.charAt(3) < '0' || testPostalCode.charAt(3) > '9' ||
					testPostalCode.charAt(5) < '0' || testPostalCode.charAt(5) > '9' || 
					testPostalCode.charAt(0) < 'A' || testPostalCode.charAt(0) > 'Z' ||
					testPostalCode.charAt(2) < 'A' || testPostalCode.charAt(2) > 'Z' ||
					testPostalCode.charAt(4) < 'A' || testPostalCode.charAt(4) > 'Z'
				) {
					//fail
					return testPostalCode + " is an invalid format for a postal code";
				}

				//all pass
				return null;
			}
		});
		descriptors.addElement(propertyDescriptor);

		//
		ComboBoxPropertyDescriptor desc = new ComboBoxPropertyDescriptor(P_ID_PROVINCE, P_PROVINCE, provinceValues);
		desc.setLabelProvider(new ProvinceLabelProvider());
		descriptors.addElement(desc);
	}
/**
 * Address Default Constructor
 */
Address() {
	super();
}
/**
 * Creates a new address.
 *
 * @param street the street 
 * @param city the city
 * @param province the province
 * @param postalCode has the form XYXYXY: where X is a letter and Y is a digit
 * @exception IllegalArgumentException, if postalcode not in above form
 */
public Address(StreetAddress street, String city, Integer province, String postalCode) {
	super();
	setStreet(street);
	setCity(city);
	setPostalCode(postalCode);
	setProvince(province);
}
/**
 * Returns the city
 */
private String getCity() {
	if(city == null)
		city = CITY_DEFAULT;
	return city;
}
/* 
 * Standard Accessor
 */
private static Vector getDescriptors() {
	return descriptors;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public Object getEditableValue() {
	return this.toString();
}
/**
 * Returns the postal code
 */
private String getPostalCode() {
	if(postalCode == null)
		postalCode = POSTALCODE_DEFAULT;
	return postalCode;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
}
/** 
 * The <code>Address</code> implementation of this
 * <code>IPropertySource</code> method returns the following properties
 *
 * 	1) P_CITY returns java.lang.String
 * 	2) P_POSTALCODE returns java.lang.String
 *  3) P_PROVINCE returns java.lang.String
 *	4) P_STREET returns StreetAddress
 */
public Object getPropertyValue(Object propKey) {
	if (propKey.equals(P_ID_PROVINCE))
		return getProvince();
	if (propKey.equals(P_ID_STREET))
		return getStreet();
	if (propKey.equals(P_ID_CITY))
		return getCity();
	if (propKey.equals(P_ID_POSTALCODE))
		return getPostalCode();
	return null;
}
/**
 * Returns the province
 */
private Integer getProvince() {
	if(province == null)
		province = PROVINCE_DEFAULT;
	return province;
}
/**
 * Returns the street
 */
public StreetAddress getStreet() {
	if(street == null)
		street = STREET_DEFAULT;
	return street;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public boolean isPropertySet(Object property) {
	return false;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public void resetPropertyValue(Object property) {
	if (P_ID_POSTALCODE.equals(property)) {
		setPostalCode(POSTALCODE_DEFAULT);
		return;
	}
	if (P_ID_CITY.equals(property)) {
		setCity(CITY_DEFAULT);
		return;
	}
	if (P_ID_PROVINCE.equals(property)) {
		setProvince(PROVINCE_DEFAULT);
		return;
	};
	if (P_ID_STREET.equals(property)) {
		setStreet(STREET_DEFAULT);
		return;
	}
}
/**
 * Sets the city
 */
private void setCity(String newCity) {
	city = newCity;
}
/**
 * Sets the postal code
 */
private void setPostalCode(String newPostalCode) {
	//validation in ICellEditorValidator registered in PropertyDescriptor
	this.postalCode = newPostalCode.toUpperCase();		
}
/** 
 * The <code>Address</code> implementation of this
 * <code>IPropertySource</code> method 
 * defines the following Setable properties
 *
 * 	1) P_CITY expects java.lang.String
 * 	2) P_POSTALCODE expects java.lang.String
 *  3) P_PROVINCE expects java.lang.String
 *	4) P_STREET expects StreetAddress
 */
public void setPropertyValue(Object name, Object value) {
	if (P_ID_POSTALCODE.equals(name)) {
		setPostalCode((String) value);
		return;
	}
	if (P_ID_CITY.equals(name)) {
		setCity((String) value);
		return;
	}
	if (P_ID_PROVINCE.equals(name)) {
		setProvince((Integer) value);
		return;
	}
	if (P_ID_STREET.equals(name)) {
		//setStreet((StreetAddress) value);
		return;
	} 

}
/**
 * Sets the province
 */
private void setProvince(Integer newProvince) {
	province = newProvince;
}
/**
 * Sets the street
 */
private void setStreet(StreetAddress newStreet) {
	street = newStreet;
}
/**
 * The value as displayed in the Property Sheet. Will not print default values
 * @return java.lang.String
 */
public String toString() {
	StringBuffer outStringBuffer = new StringBuffer();
	final String comma_space = ", ";
	final String space = " ";
	if (!getStreet().equals(STREET_DEFAULT)) {
		outStringBuffer.append(getStreet());
		outStringBuffer.append(comma_space);
	}
	if (!getCity().equals(CITY_DEFAULT)) {
		outStringBuffer.append(getCity());
		outStringBuffer.append(space);
	}
	if (!getProvince().equals(PROVINCE_DEFAULT)) {
		outStringBuffer.append(provinceValues[getProvince().intValue()]);
	}
	if (!getPostalCode().equals(POSTALCODE_DEFAULT)) {
		outStringBuffer.append(comma_space);
		outStringBuffer.append(getPostalCode());
	}
	return outStringBuffer.toString();
}
}
