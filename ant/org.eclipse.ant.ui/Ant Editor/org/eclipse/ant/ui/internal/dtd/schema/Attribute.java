/*******************************************************************************
 * Copyright (c) 2002, 2003 Object Factory Inc.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *		Object Factory Inc. - Initial implementation
 *******************************************************************************/
package org.eclipse.ant.ui.internal.dtd.schema;

import org.eclipse.ant.ui.internal.dtd.*;

/**
 * Attr contains information about a single attribute.
 * @author Bob Foster
 */
public class Attribute extends Atom implements IAttribute {
	private String fType;
	private String[] fEnum;
	private IElement fElement;
	private String fDefault;
	private boolean fFixed;
	private boolean fRequired;
	
	/**
	 * Constructor.
	 * @param name Attribute qname.
	 * @param element Parent element.
	 */
	public Attribute(String name, IElement element) {
		super(ATTRIBUTE, name);
		fElement = element;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.dtd.IAttribute#getType()
	 */
	public String getType() {
		return fType;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.dtd.IAttribute#getEnum()
	 */
	public String[] getEnum() {
		return fEnum;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.dtd.IAttribute#getElement()
	 */
	public IElement getElement() {
		return fElement;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.dtd.IAttribute#getDefault()
	 */
	public String getDefault() {
		return fDefault;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.dtd.IAttribute#isFixed()
	 */
	public boolean isFixed() {
		return fFixed;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.ui.internal.dtd.IAttribute#isRequired()
	 */
	public boolean isRequired() {
		return fRequired;
	}
	
	public void setType(String type) {
		fType = type;
	}
	
	/**
	 * Sets the default value.
	 * @param defaultValue Value
	 */
	public void setDefault(String defaultValue) {
		fDefault = defaultValue;
	}

	/**
	 * Sets the enum.
	 * @param enum The enum to set
	 */
	public void setEnum(String[] enum) {
		fEnum = enum;
	}

	/**
	 * Sets the fixed.
	 * @param fixed The fixed to set
	 */
	public void setFixed(boolean fixed) {
		fFixed = fixed;
	}

	/**
	 * Sets the required.
	 * @param required The required to set
	 */
	public void setRequired(boolean required) {
		fRequired = required;
	}
}
