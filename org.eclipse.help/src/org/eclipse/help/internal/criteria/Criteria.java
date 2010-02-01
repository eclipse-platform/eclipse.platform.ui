/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.criteria;

import org.eclipse.help.ICriteria;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

/**
 * A directive indicating the criteria information of a Toc or Topic described in xml
 * 
 * @since 3.5
 */

public class Criteria extends UAElement implements ICriteria {
	
	public static final String NAME = "criteria"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	public static final String ATTRIBUTE_VALUE = "value"; //$NON-NLS-1$
	
	public Criteria(ICriteria src) {
		super(NAME,src);	
		setName(src.getName());
		setValue(src.getValue());
	}
	
	public Criteria(Element element) {
		super(element);  
	}

	public String getName() { 
		return getAttribute(ATTRIBUTE_NAME);
	}

	public String getValue() { 
		return getAttribute(ATTRIBUTE_VALUE);
	}
	
	public void setName(String name) {
		setAttribute(ATTRIBUTE_NAME, name);
	}
	
	public void setValue(String value) {
		setAttribute(ATTRIBUTE_VALUE, value);
	}
}
