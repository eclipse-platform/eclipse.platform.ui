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
 * A <code>TemplatePosition</code> represents a set of positions into a
 * <code>TemplateBuffer</code> with identical content each. <code>TemplateVariable</code>
 * s can be used to resolve a template position to a symbol available from the
 * <code>TemplateContext</code>.
 * 
 * @see TemplateVariable
 * @see TemplateBuffer
 * @since 3.0
 */
public class TemplatePosition {

	/** The name of the template position */
	private final String fName;
	/** The length of the template positions. */
	private int fLength;
	/** The offsets of the template positions. */
	private int[] fOffsets;
	/** A flag indicating if the template position has been resolved. */
	private boolean fResolved;
	/**
	 * The proposal strings available for this position. The first string is
	 * the default value.
	 */
	private String[] fValues;
	
	/**
	 * Creates a template position.
	 * 
	 * @param name the name of the template position
	 * @param defaultValue the default value of the position
	 * @param offsets the array of offsets of the position
	 * @param length the length of the position
	 */
	public TemplatePosition(String name, String defaultValue, int[] offsets, int length) {
		this(name, new String[] { defaultValue }, offsets, length);
	}

	/**
	 * Creates a template position.
	 * 
	 * @param name the name of the template position
	 * @param values the values available at this position
	 * @param offsets the array of offsets of the position
	 * @param length the length of the position
	 */
	public TemplatePosition(String name, String[] values, int[] offsets, int length) {
		Assert.isTrue(values.length > 0);
		fName= name;
		fValues= values;
		fOffsets= offsets;
		fLength= length;
		fResolved= false;
	}

	/**
	 * Returns the name of the position.
	 * 
	 * @return the name of the position
	 */
	public String getName() {
	    return fName;
	}	

	/**
	 * Returns the default value of the position.
	 * 
	 * @return the default value of the position
	 */
	public String getDefaultValue() {
	 	return fValues[0];
	}
	
	/**
	 * Returns the possible values at this position. The returned array is
	 * owned by this position and must not be modified.
	 * 
	 * @return the possible values at this position
	 */
	public String[] getValues() {
		return fValues;
	}
	
	/**
	 * Sets the length of the position.
	 * 
	 * @param length the new length 
	 */
	public void setLength(int length) {
	    fLength= length;
	}
	
	/**
	 * Returns the length of the position.
	 * 
	 * @return the length of the position
	 */
	public int getLength() {
	 	return fLength;   
	}
	
	/**
	 * Sets the offsets of the position.
	 * 
	 * @param offsets the new offsets of the position
	 */
	public void setOffsets(int[] offsets) {
	 	fOffsets= offsets; 
	}
	
	/**
	 * Returns the offsets of the position.
	 * 
	 * @return the length of the position
	 */
	public int[] getOffsets() {
	 	return fOffsets;   
	}
	
	/**
	 * Sets the resolved flag of the position.
	 * 
	 * @param resolved the new resolved state of the position
	 */
	public void setResolved(boolean resolved) {
	    fResolved= resolved;
	}	

	/**
	 * Returns <code>true</code> if the position is resolved, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if the position is resolved, <code>false</code> otherwise
	 */	
	public boolean isResolved() {
	 	return fResolved;   
	}

}
