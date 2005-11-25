/*
 * Copyright (C) 2005 db4objects Inc.  http://www.db4o.com
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     db4objects - Initial API and implementation
 */
package org.eclipse.jface.databinding.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * RegularExpressionVerifier.  A generic String validator that uses regular expressions to
 * specify validation rules.
 */
public class RegularExpressionStringValidator implements IValidator {
    
    private Pattern fragmentRegex;
    private Pattern fullValueRegex;
    private String hint;
    
	/**
     * Constructor RegularExpressionValidator.  Construct a string validator based on regular
     * expressions.
     * 
     * Verify input using regular expressions.
     * 
	 * @param partiallyValidRegex A regex that matches iff the value is partially valid
	 * @param fullyValidRegex A regex that matches iff the value is fully valid
	 * @param hint The hint to display if the value is invalid
	 */
	public RegularExpressionStringValidator(String partiallyValidRegex,
			String fullyValidRegex, String hint) {
		super();
		this.fragmentRegex = Pattern.compile(partiallyValidRegex);
		this.fullValueRegex = Pattern.compile(fullyValidRegex);
		this.hint = hint;
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.databinding.validator.IValidator#isPartiallyValid(java.lang.Object)
	 */
	public String isPartiallyValid(Object fragment) {
        Matcher matcher = fragmentRegex.matcher((String)fragment);
		if (matcher.find())
            return null;

        return hint;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellEditorValidator#isValid(java.lang.Object)
	 */
	public String isValid(Object value) {
        String stringValue = (String) value;
        Matcher matcher = fullValueRegex.matcher(stringValue);
        if (matcher.find())
            return null;

        return hint;
	}

}
