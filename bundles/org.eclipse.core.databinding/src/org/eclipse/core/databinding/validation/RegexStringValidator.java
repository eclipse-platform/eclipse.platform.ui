/*
 * Copyright (C) 2005, 2006 db4objects Inc. (http://www.db4o.com) and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 *     Boris Bokowski (IBM Corporation) - bug 118429
 */
package org.eclipse.core.databinding.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * RegularExpressionVerifier. A generic String validator that uses regular
 * expressions to specify validation rules.
 */
public class RegexStringValidator implements IValidator {

	private Pattern fragmentRegex;
	private Pattern fullValueRegex;
	private String hint;

	/**
	 * Constructor RegularExpressionValidator. Construct a string validator
	 * based on regular expressions.
	 * 
	 * Verify input using regular expressions.
	 * 
	 * @param partiallyValidRegex
	 *            A regex that matches iff the value is partially valid
	 * @param fullyValidRegex
	 *            A regex that matches iff the value is fully valid
	 * @param hint
	 *            The hint to display if the value is invalid
	 */
	public RegexStringValidator(String partiallyValidRegex,
			String fullyValidRegex, String hint) {
		super();
		this.fragmentRegex = Pattern.compile(partiallyValidRegex);
		this.fullValueRegex = Pattern.compile(fullyValidRegex);
		this.hint = hint;
	}

	public IStatus validatePartial(Object fragment) {
		Matcher matcher = fragmentRegex.matcher((String) fragment);
		if (matcher.find())
			return Status.OK_STATUS;

		return ValidationStatus.error(hint);
	}

	public IStatus validate(Object value) {
		String stringValue = (String) value;
		Matcher matcher = fullValueRegex.matcher(stringValue);
		if (matcher.find())
			return Status.OK_STATUS;

		return ValidationStatus.error(hint);
	}

}
