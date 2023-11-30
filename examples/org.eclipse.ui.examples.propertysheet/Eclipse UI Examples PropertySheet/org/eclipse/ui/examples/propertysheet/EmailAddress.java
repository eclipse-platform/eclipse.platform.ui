/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
package org.eclipse.ui.examples.propertysheet;


import java.util.ArrayList;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Example IPropertySource is editable and whose childern properties are itself not editable.
 * The values of "userid" and "mailserver" are parsed from setting "email"
 */
public class EmailAddress implements IPropertySource {

	//Property-Value
	private String userid;

	private String domain;

	//Default Property-Value
	private static final String USERID_DEFAULT = MessageUtil
			.getString("unknownUser"); //$NON-NLS-1$

	private static final String DOMAIN_DEFAULT = MessageUtil
			.getString("unknownDomain"); //$NON-NLS-1$

	//Property unique keys
	public static final String P_ID_USERID = "EmailAddress.userid"; //$NON-NLS-1$

	public static final String P_ID_DOMAIN = "EmailAddress.domain"; //$NON-NLS-1$

	//Property display keys
	public static final String P_USERID = MessageUtil.getString("userid"); //$NON-NLS-1$

	public static final String P_DOMAIN = MessageUtil.getString("domain"); //$NON-NLS-1$

	//Property-Descriptors
	private static ArrayList<PropertyDescriptor> descriptors;

	static {
		descriptors = new ArrayList<>();
		//non-editable child properties --> provide no editors
		descriptors.add(new PropertyDescriptor(P_ID_USERID, P_USERID));
		descriptors.add(new PropertyDescriptor(P_ID_DOMAIN, P_DOMAIN));
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
	 * @throws java.lang.IllegalArgumentException if does not subscribe to form
	 */
	public EmailAddress(String emailAddress) throws IllegalArgumentException {
		super();
		setEmailAddress(emailAddress);

	}

	/**
	 * Returns the descriptors
	 */
	private static ArrayList<PropertyDescriptor> getDescriptors() {
		return descriptors;
	}

	/**
	 * Returns the domain
	 */
	private String getDomain() {
		if (domain == null)
			domain = DOMAIN_DEFAULT;
		return domain;
	}

	@Override
	public Object getEditableValue() {
		return this.toString();
	}

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return getDescriptors().toArray(
				new IPropertyDescriptor[getDescriptors().size()]);
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
	@Override
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
		if (userid == null)
			userid = USERID_DEFAULT;
		return userid;
	}

	@Override
	public boolean isPropertySet(Object property) {
		return false;
	}

	@Override
	public void resetPropertyValue(Object property) {
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
	 */
	private void setEmailAddress(String emailAddress)
			throws IllegalArgumentException {
		if (emailAddress == null)
			throw new IllegalArgumentException(MessageUtil
					.getString("emailaddress_cannot_be_set_to_null")); //$NON-NLS-1$
		int index = emailAddress.indexOf('@');
		int length = emailAddress.length();
		if (index > 0 && index < length) {
			setUserid(emailAddress.substring(0, index));
			setDomain(emailAddress.substring(index + 1));
			return;
		}
		throw new IllegalArgumentException(
				MessageUtil
						.getString("invalid_email_address_format_should_have_been_validated")); //$NON-NLS-1$
	}

	/**
	 * The <code>Address</code> implementation of this
	 * <code>IPropertySource</code> method
	 * defines the following Setable properties
	 *
	 *	1) P_USERID, expects String
	 *	2) P_DOMAIN, expects String
	 */
	@Override
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
	 * The value as displayed in the Property Sheet.
	 * @return java.lang.String
	 */
	@Override
	public String toString() {
		StringBuilder strbuffer = new StringBuilder(getUserid());
		strbuffer.append('@');
		strbuffer.append(getDomain());
		return strbuffer.toString();
	}
}
