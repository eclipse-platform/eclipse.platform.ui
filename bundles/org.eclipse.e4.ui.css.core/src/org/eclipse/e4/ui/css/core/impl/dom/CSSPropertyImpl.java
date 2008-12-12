/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.io.Serializable;

import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyImpl implements CSSProperty, Serializable {

	/*
	 * The string used to signify a property's priority as "important"
	 * e.g. @see org.w3c.dom.css.CSSStyleDeclaration#getPropertyPriority(String)
	 */
	protected static String IMPORTANT_IDENTIFIER = "important"; 

	private String name;
	private CSSValue value;
	private boolean important;

	public static boolean sameName(CSSProperty property, String testName) {
		return property.getName().equalsIgnoreCase(testName);
	}
	
	/** Creates new Property */
	public CSSPropertyImpl(String name, CSSValue value, boolean important) {
		this.name = name; 
		this.value = value;
		this.important = important;
	}

	public String getName() {
		return name;
	}

	public CSSValue getValue() {
		return value;
	}

	public boolean isImportant() {
		return important;
	}

	public void setImportant(boolean important) {
		this.important = important;
	}

	public void setValue(CSSValue value) {
		this.value = value;		
	}

}
