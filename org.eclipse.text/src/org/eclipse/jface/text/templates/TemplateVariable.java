/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.templates;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.TextUtilities;

/**
 * A <code>TemplateVariable</code> represents a set of positions into a
 * <code>TemplateBuffer</code> with identical content each. <code>TemplateVariableResolver</code>s
 * can be used to resolve a template variable to a symbol available from the
 * <code>TemplateContext</code>. A resolved variable may have one or more possible
 * {@link #getValues() values} which may be presented to the user as choices. If there is no user
 * interaction the {@link #getDefaultValue() default value} is chosen as textual representation of
 * the variable.
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
	private final TemplateVariableType fType;
	/** The name of the variable. */
	private final String fName;
	/** The initial length in the template pattern. */
	private final int fInitialLength;
	/** The offsets of the variable. */
	private int[] fOffsets;
	/** Flag indicating if the variable has been resolved unambiguously. */
	private boolean fIsUnambiguous;
	/** Flag indicating if the variable has been resolved by a resolver. */
	private boolean fIsResolved;
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
	 * Creates a template variable.
	 *
	 * @param type the type of the variable
	 * @param name the name of the variable
	 * @param defaultValue the default value of the variable
	 * @param offsets the array of offsets of the variable
	 * @since 3.3
	 */
	public TemplateVariable(TemplateVariableType type, String name, String defaultValue, int[] offsets) {
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
		this(new TemplateVariableType(type), name, values, offsets);
	}

	/**
	 * Creates a template variable with multiple possible values.
	 *
	 * @param type the type of the variable
	 * @param name the name of the variable
	 * @param values the values available at this variable, non-empty
	 * @param offsets the array of offsets of the variable
	 * @since 3.3
	 */
	TemplateVariable(TemplateVariableType type, String name, String[] values, int[] offsets) {
		Assert.isNotNull(type);
		Assert.isNotNull(name);
		fType= type;
		fName= name;
		setValues(values);
		setOffsets(offsets);
		setUnambiguous(false);
		setResolved(false);
		fInitialLength= values[0].length();
	}

	/**
	 * Returns the type name of the variable.
	 *
	 * @return the type name of the variable
	 */
	public String getType() {
	    return fType.getName();
	}

	/**
	 * Returns the type of the variable.
	 *
	 * @return the type of the variable
	 * @since 3.3
	 */
	public TemplateVariableType getVariableType() {
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
	 * Returns the default value of the variable. Typically, this is the first of
	 * the possible values (see {@link #getValues()}.
	 *
	 * @return the default value of the variable
	 */
	public String getDefaultValue() {
	 	return getValues()[0];
	}

	/**
	 * Returns the possible values for this variable. The returned array is owned by this variable
	 * and must not be modified. The array is not empty.
	 *
	 * @return the possible values for this variable
	 */
	public String[] getValues() {
		return fValues;
	}

	/**
	 * Returns the length of the variable's default value.
	 *
	 * @return the length of the variable
	 */
	public int getLength() {
	 	return getDefaultValue().length();
	}

	/**
	 * Returns the initial length of the variable. The initial length is the lenght as it occurred
	 * in the template pattern and is used when resolving a template to update the pattern with the
	 * resolved values of the variable.
	 *
	 * @return the initial length of the variable
	 * @since 3.3
	 */
	final int getInitialLength() {
		return fInitialLength;
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
	 * Resolves the variable to a single value. This is a shortcut for
	 * <code>setValues(new String[] { value })</code>.
	 *
	 * @param value the new default value
	 */
	public final void setValue(String value) {
		setValues(new String[] { value });
	}

	/**
	 * Resolves the variable to several possible values for this variable, with the first being the
	 * default value.
	 *
	 * @param values a non-empty array of values
	 */
	public void setValues(String[] values) {
		Assert.isTrue(values.length > 0);
		fValues= TextUtilities.copy(values);
		setResolved(true);
	}

	/**
	 * Sets the <em>isUnambiguous</em> flag of the variable.
	 *
	 * @param unambiguous the new unambiguous state of the variable
	 */
	public void setUnambiguous(boolean unambiguous) {
	    fIsUnambiguous= unambiguous;
	    if (unambiguous)
	    	setResolved(true);
	}

	/**
	 * Returns <code>true</code> if the variable is unambiguously resolved, <code>false</code> otherwise.
	 *
	 * @return <code>true</code> if the variable is unambiguously resolved, <code>false</code> otherwise
	 */
	public boolean isUnambiguous() {
	 	return fIsUnambiguous;
	}

	/**
	 * Sets the <em>resolved</em> flag of the variable.
	 *
	 * @param resolved the new <em>resolved</em> state
	 * @since 3.3
	 */
	public void setResolved(boolean resolved) {
		fIsResolved= resolved;
	}

	/**
	 * Returns <code>true</code> if the variable has been resolved, <code>false</code>
	 * otherwise.
	 *
	 * @return <code>true</code> if the variable has been resolved, <code>false</code> otherwise
	 * @since 3.3
	 */
	public boolean isResolved() {
		return fIsResolved;
	}
}
