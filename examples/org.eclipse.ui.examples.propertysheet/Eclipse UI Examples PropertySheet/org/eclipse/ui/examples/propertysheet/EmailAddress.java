package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Vector;

import org.eclipse.ui.views.properties.*;

/**
 * Example IPropertySource is editable and whose childern properties are itself not editable.
 * The values of "userid" and "mailserver" are parsed from seting "email"
 */
public class EmailAddress implements IPropertySource {

	//Property-Value
	private String userid;
	private String domain;

	//Default Property-Value
	private static final String USERID_DEFAULT = "unknownUser";
	private static final String DOMAIN_DEFAULT = "unknownDomain";
	
	//Property unique keys
	public static final String P_ID_USERID = "EmailAddress.userid";
	public static final String P_ID_DOMAIN = "EmailAddress.domain";
	
	//Property display keys
	public static final String P_USERID = "userid";
	public static final String P_DOMAIN = "domain";

	//Property-Descriptors
	private static Vector descriptors;

	static{
		descriptors = new Vector(2,2);
		//non-editable child properties --> provide no editors
		descriptors.addElement(new PropertyDescriptor(P_ID_USERID, P_USERID));
		descriptors.addElement(new PropertyDescriptor(P_ID_DOMAIN, P_DOMAIN));	
	}
/**
 * EmailAddress Default Constructor
 */
public EmailAddress() {
	super();
}
/**
 * Convience EmailAddress constructor.
 * Calls setEmailAddress() to parse emailAddress
 * @param emailAddress java.lang.String, in the form userid@domain
 * @throws java.lang.IllegalArgumentException, if does not subscribe to form
 */
public EmailAddress(String emailAddress) throws IllegalArgumentException{
	super();
	setEmailAddress(emailAddress);

}
/**
 * Returns the descriptors
 */
private static Vector getDescriptors() {
	return descriptors;
}
/**
 * Returns the domain
 */
private String getDomain() {
	if(domain == null)
		domain = DOMAIN_DEFAULT;
	return domain;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public Object getEditableValue() {
	return this.toString();
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
}
/** 
 * The <code>EmailAddress</code> implementation of this
 * <code>IPropertySource</code> method returns the following properties
 *
 * 	1) P_USERID returns String, values before "@"
 *	2) P_DOMAIN returns String, values after "@"
 *
 * Observe the available properties must always equal those listed
 * in the property descriptors
 */
public Object getPropertyValue(Object propKey) {
	if (propKey.equals(P_ID_USERID))
		return getUserid();
	if (propKey.equals(P_ID_DOMAIN))
		return getDomain();
	return null;
}
/**
 * Returns the userid
 */
private String getUserid() {
	if(userid == null)
		userid = USERID_DEFAULT;
	return userid;
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
	if (property.equals(P_ID_USERID)) {
		setUserid(USERID_DEFAULT);
		return;
	}
	if (property.equals(P_ID_DOMAIN)) {
		setDomain(DOMAIN_DEFAULT);
		return;
	}
	return;
}
/**
 * Sets the domain
 */
private void setDomain(java.lang.String newDomain) {
	domain = newDomain;
}
/**
 * Parses emailAddress into domain and userid. Throws SetPropertyVetoException
 * if emailAddress does not contain an userid and domain seperated by '@'.
 *
 * @param emailAddress the email address
 * @throws IllegalArgumentException
 */
private void setEmailAddress(String emailAddress) throws IllegalArgumentException {
	if(emailAddress == null)
		throw new IllegalArgumentException("emailaddress cannot be set to null");
	int index = emailAddress.indexOf('@');
	int length = emailAddress.length();
	if (index > 0 && index < length) {
		setUserid(emailAddress.substring(0, index));
		setDomain(emailAddress.substring(index + 1));
		return;
	}
	throw new IllegalArgumentException("invalid email address format, should have been validated");
}
/** 
 * The <code>Address</code> implementation of this
 * <code>IPropertySource</code> method 
 * defines the following Setable properties
 *
 *	1) P_USERID, expects String
 *	2) P_DOMAIN, expects String
 */
public void setPropertyValue(Object name, Object value) {
	if (name.equals(P_ID_USERID)) {
		setUserid((String) value);
		return;
	}
	if (name.equals(P_ID_DOMAIN)) {
		setDomain((String) value);
		return;
	}
}
/**
 * Sets the userid
 */
private void setUserid(String newUserid) {
	userid = newUserid;
}
/**
 * The value as displayed in the Property Sheet. Will not print default values
 * @return java.lang.String
 */
public String toString() {
	StringBuffer strbuffer = new StringBuffer(getUserid());
	strbuffer.append('@');
	strbuffer.append(getDomain());
	return strbuffer.toString();
}
}
