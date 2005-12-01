package org.eclipse.jface.tests.databinding.scenarios.model;

import org.eclipse.jface.databinding.validator.IValidator;

public class PhoneValidator implements IValidator {

	public String isPartiallyValid(Object value) {
		return isValid(value);
	}

	public String isValid(Object value) {
		String rawPhoneNumber = PhoneConverter.removeFormatting((String)value);
		if(rawPhoneNumber.length() != 10){
			return "Phone number must be 10 characters";
		} else {
			return null;
		}
	}

}
