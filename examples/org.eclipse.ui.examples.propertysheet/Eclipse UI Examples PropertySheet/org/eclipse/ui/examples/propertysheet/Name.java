package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Vector;

import org.eclipse.ui.views.properties.*;

/**
 * Example IPropertySource whose value as well as its children are editable.
 */
public class Name implements IPropertySource
{
	private String firstName = ""; //$NON-NLS-1$
	private String lastName = ""; //$NON-NLS-1$
	private String initial = ""; //$NON-NLS-1$
	
	// property unique keys
	public static String P_ID_FIRSTNAME = "Name.FirstName"; //$NON-NLS-1$
	public static String P_ID_LASTNAME = "Name.LastName"; //$NON-NLS-1$
	public static String P_ID_MIDDLENAME = "Name.Middle"; //$NON-NLS-1$

	// property display keys
	public static String P_FIRSTNAME = MessageUtil.getString("FirstName"); //$NON-NLS-1$
	public static String P_LASTNAME = MessageUtil.getString("LastName"); //$NON-NLS-1$
	public static String P_MIDDLENAME = MessageUtil.getString("Middle"); //$NON-NLS-1$
	
	public static final String P_DESCRIPTORS = "properties"; //$NON-NLS-1$
	static private Vector descriptors;	
	static
	{
		descriptors = new Vector();
		descriptors.addElement(new TextPropertyDescriptor(P_ID_FIRSTNAME, P_FIRSTNAME));
		descriptors.addElement(new TextPropertyDescriptor(P_ID_LASTNAME, P_LASTNAME));
		descriptors.addElement(new TextPropertyDescriptor(P_ID_MIDDLENAME, P_MIDDLENAME));
	}
/**
 * Creates a new Name.
 * @param name String in the form "firstname initial lastname"
 */
public Name(String name) {
	int index1, index2, length;
	index1 = name.indexOf(' ');
	if (index1 < 0)
		index1 = name.length();
	index2 = name.lastIndexOf(' ');
	length = name.length();
	if (index2 > 0)
		lastName = name.substring(index2 + 1);
	firstName = name.substring(0, index1);
	if (index1 < index2)
		initial = name.substring(index1 + 1, index2);
}
/**
 * Returns the descriptors
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
 * Returns the first name
 */
private String getFirstName()
{	
	return firstName;
}
/**
 * Returns the initial
 */
private String getInitial()
{	
	return initial;
}
/**
 * Returns the last name
 */
private String getLastName()
{	
	return lastName;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public IPropertyDescriptor[] getPropertyDescriptors() {
	return (IPropertyDescriptor[])getDescriptors().toArray(new IPropertyDescriptor[getDescriptors().size()]);
}
/** 
 * The <code>Name</code> implementation of this
 * <code>IPropertySource</code> method returns the following properties
 *
 * 	1) P_FIRSTNAME returns String, firstname
 * 	2) P_LASTNAME returns String, lastname
 *  3) P_MIDDLENAME returns String, middle
 */
public Object getPropertyValue(Object propKey) {
	if (P_ID_FIRSTNAME.equals(propKey))
		return getFirstName();
	if (P_ID_LASTNAME.equals(propKey))
		return getLastName();
	if (P_ID_MIDDLENAME.equals(propKey))
		return getInitial();
	return null;
}
/* (non-Javadoc)
 * Method declared on IPropertySource
 */
public boolean isPropertySet(Object key) {
	return false;
}
/**
 * Implemented as part of IPropertySource framework. Sets the specified property 
 * to its default value.
 * 
 * @see 	IPropertySource#resetPropertyValue(Object)
 * @param 	property 	The property to reset.
 */
public void resetPropertyValue(Object property) {
	if (P_ID_FIRSTNAME.equals(property)) {
		setFirstName(null);
		return;
	}
	if (P_ID_LASTNAME.equals(property)) {
		setLastName(null);
		return;
	}
	if (P_ID_MIDDLENAME.equals(property)) {
		setInitial(null);
		return;
	}
}
/**
 * Sets the first name
 */
private void setFirstName(String newFirstName)
{	
	firstName = newFirstName;
}
/**
 * Sets the initial
 */
private void setInitial(String newInitial)
{	
	initial = newInitial;
}
/**
 * Sets the last name
 */
private void setLastName(String newLastName)
{	
	lastName = newLastName;
}
/** 
 * The <code>Name</code> implementation of this
 * <code>IPropertySource</code> method 
 * defines the following Setable properties
 *
 *	1) P_FIRST, expects String, sets the firstname of this OrganizationElement
 *  2) P_MIDDLENAME, expects String, sets middlename of this OrganizationElement
 *  3) P_LASTNAME, expects String, sets lastname of this OrganizationElement
 */
public void setPropertyValue(Object propName, Object val) {
	if (P_ID_FIRSTNAME.equals(propName)) {
		setFirstName((String) val);
		return;
	}
	if (P_ID_LASTNAME.equals(propName)) {
		setLastName((String) val);
		return;
	}
	if (P_ID_MIDDLENAME.equals(propName)){
		setInitial((String) val);
		return;
	}
}
/**
 * The value as displayed in the Property Sheet. Will not print default values
 * @return java.lang.String
 */
public String toString(){
	StringBuffer outStringBuffer = new StringBuffer();
	if(getFirstName()!=null)
	{	outStringBuffer.append(getFirstName());
		outStringBuffer.append(" "); //$NON-NLS-1$
		if(getInitial()!=null)
		{	outStringBuffer.append(getInitial());
			outStringBuffer.append(" "); //$NON-NLS-1$
		}
	}
	if(getLastName()!=null)
	{	outStringBuffer.append(getLastName());
	}
	
	return outStringBuffer.toString();
}
}
