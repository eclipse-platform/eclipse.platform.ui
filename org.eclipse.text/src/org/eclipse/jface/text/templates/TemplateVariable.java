/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.jface.text.Assert;

/**
 * A <code>TemplateVariable</code> represents a set of positions into a
 * <code>TemplateBuffer</code> with identical content each. <code>TemplateVariableResolver</code>
 * s can be used to resolve a template variable to a symbol available from the
 * <code>TemplateContext</code>.
 * 
 * @see TemplateVariableResolver
 * @see TemplateBuffer
 * @since 3.0
 */
public class TemplateVariable {

	/** The type name of the variable */
	private final String fType;
	/** The length of the variable. */
	private int fLength;
	/** The offsets of the variable. */
	private int[] fOffsets;
	/** A flag indicating if the variable has been resolved. */
	private boolean fResolved;
	/**
	 * The proposal strings available for this variable. The first string is
	 * the default value.
	 */
	private String[] fValues;
	
	/**
	 * Creates a template variable.
	 * 
	 * @param type the type of the variable
	 * @param defaultValue the default value of the variable
	 * @param offsets the array of offsets of the variable
	 * @param length the length of the variable
	 */
	public TemplateVariable(String type, String defaultValue, int[] offsets, int length) {
		this(type, new String[] { defaultValue }, offsets, length);
	}

	/**
	 * Creates a template variable.
	 * 
	 * @param type the type of the template variable
	 * @param values the values available at this variable, non-empty
	 * @param offsets the array of offsets of the variable
	 * @param length the length of the variable
	 */
	public TemplateVariable(String type, String[] values, int[] offsets, int length) {
		fType= type;
		setValues(values);
		setOffsets(offsets);
		setResolved(false);
	}

	/**
	 * Returns the type name of the variable.
	 * 
	 * @return the type name of the variable
	 */
	public String getType() {
	    return fType;
	}	

	/**
	 * Returns the default value of the variable.
	 * 
	 * @return the default value of the variable
	 */
	public String getDefaultValue() {
	 	return fValues[0];
	}
	
	/**
	 * Returns the possible values for this variable. The returned array is
	 * owned by this variable and must not be modified.
	 * 
	 * @return the possible values for this variable
	 */
	public String[] getValues() {
		return fValues;
	}
	
	/**
	 * Returns the length of the variable.
	 * 
	 * @return the length of the variable
	 */
	public int getLength() {
	 	return fLength;   
	}
	
	/**
	 * Sets the offsets of the variable.
	 * 
	 * @param offsets the new offsets of the variable
	 */
	public void setOffsets(int[] offsets) {
	 	fOffsets= offsets; 
	}
	
	/**
	 * Returns the offsets of the variable.
	 * 
	 * @return the length of the variable
	 */
	public int[] getOffsets() {
	 	return fOffsets;   
	}
	
	/**
	 * Sets the default value for this variable. This is a shortcut for
	 * <code>setValues(new String[] { value })</code>.
	 * 
	 * @param value the new default value
	 */
	public final void setValue(String value) {
		setValues(new String[] { value });
	}
	
	/**
	 * Sets the possible values for this variable, with the first being the
	 * default value.
	 * 
	 * @param values a non-empty array of values
	 */
	public void setValues(String[] values) {
		Assert.isTrue(values.length > 0);
		fValues= values;
		fLength= getDefaultValue().length();
	}

	/**
	 * Sets the resolved flag of the variable.
	 * 
	 * @param resolved the new resolved state of the variable
	 */
	public void setResolved(boolean resolved) {
	    fResolved= resolved;
	}

	/**
	 * Returns <code>true</code> if the variable is resolved, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the variable is resolved, <code>false</code> otherwise
	 */	
	public boolean isResolved() {
	 	return fResolved;   
	}

}
