package org.eclipse.ui.examples.propertysheet;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.viewers.ICellEditorValidator;
/**
 * Validator for email addresses
 */
public class EmailAddressValidator implements ICellEditorValidator {
/** 
 * The <code>EmailAddressValidator</code> implementation of this
 * <code>ICellEditorValidator</code> method 
 * determines if the value is a valid email address.
 * (check to see if it is non-null and contains an @)
 */
public String isValid(Object value) {
	if (value == null) {
		return MessageUtil.getString("email_address_is_incomplete"); //$NON-NLS-1$
	}
	String emailAddress = (String)value;
	int index = emailAddress.indexOf('@');
	int length = emailAddress.length();
	if (index > 0 && index < length) {
		return null;
	}
	return MessageUtil.getString("email_address_does_not_have_a_valid_format"); //$NON-NLS-1$
}
}
