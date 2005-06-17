/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.TextUtilities;

/**
 * A <code>TemplateVariable</code> represents a set of positions into a
 * <code>TemplateBuffer</code> with identical content each. <code>TemplateVariableResolver</code>s
 * can be used to resolve a template variable to a symbol available from the
 * <code>TemplateContext</code>.
 * <p>
 * Clients may instantiate and extend this class.
 * </p>
 *
 * @see TemplateVariableResolver
 * @see TemplateBuffer
 * @since 3.0
 */
public class TemplateVariable {

	/** The type name of the variable */
	private final String fType;
	/** The name of the variable. */
	private final String fName;
	/** The offsets of the variable. */
	private int[] fOffsets;
	/** Flag indicating if the variable has been resolved unambiguously. */
	private boolean fIsUnambiguous;
	/**
	 * The proposal strings available for this variable. The first string is
	 * the default value.
	 */
	private String[] fValues;

	/**
	 * Creates a template variable. The type is used as the name of the
	 * variable.
	 *
	 * @param type the type of the variable
	 * @param defaultValue the default value of the variable
	 * @param offsets the array of offsets of the variable
	 */
	public TemplateVariable(String type, String defaultValue, int[] offsets) {
		this(type, new String[] { defaultValue }, offsets);
	}

	/**
	 * Creates a template variable.
	 *
	 * @param type the type of the variable
	 * @param name the name of the variable
	 * @param defaultValue the default value of the variable
	 * @param offsets the array of offsets of the variable
	 */
	public TemplateVariable(String type, String name, String defaultValue, int[] offsets) {
		this(type, name, new String[] { defaultValue }, offsets);
	}

	/**
	 * Creates a template variable with multiple possible values. The type is
	 * used as the name of the template.
	 *
	 * @param type the type of the template variable
	 * @param values the values available at this variable, non-empty
	 * @param offsets the array of offsets of the variable
	 */
	public TemplateVariable(String type, String[] values, int[] offsets) {
		this(type, type, values, offsets);
	}

	/**
	 * Creates a template variable with multiple possible values.
	 *
	 * @param type the type of the variable
	 * @param name the name of the variable
	 * @param values the values available at this variable, non-empty
	 * @param offsets the array of offsets of the variable
	 */
	public TemplateVariable(String type, String name, String[] values, int[] offsets) {
		Assert.isNotNull(type);
		Assert.isNotNull(name);
		fType= type;
		fName= name;
		setValues(values);
		setOffsets(offsets);
		setUnambiguous(false);
	}

	/**
	 * Returns the type of the variable.
	 *
	 * @return the type of the variable
	 */
	public String getType() {
	    return fType;
	}

	/**
	 * Returns the name of the variable.
	 *
	 * @return the name of the variable
	 */
	public String getName() {
	    return fName;
	}

	/**
	 * Returns the default value of the variable.
	 *
	 * @return the default value of the variable
	 */
	public String getDefaultValue() {
	 	return getValues()[0];
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
	 	return getDefaultValue().length();
	}

	/**
	 * Sets the offsets of the variable.
	 *
	 * @param offsets the new offsets of the variable
	 */
	public void setOffsets(int[] offsets) {
	 	fOffsets= TextUtilities.copy(offsets);
	}

	/**
	 * Returns the offsets of the variable. The returned array is
	 * owned by this variable and must not be modified.
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
		fValues= TextUtilities.copy(values);
	}

	/**
	 * Sets the isUnambiguous flag of the variable.
	 *
	 * @param unambiguous the new unambiguous state of the variable
	 */
	public void setUnambiguous(boolean unambiguous) {
	    fIsUnambiguous= unambiguous;
	}

	/**
	 * Returns <code>true</code> if the variable is unambiguously resolved, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the variable is unambiguously resolved, <code>false</code> otherwise
	 */
	public boolean isUnambiguous() {
	 	return fIsUnambiguous;
	}

}
