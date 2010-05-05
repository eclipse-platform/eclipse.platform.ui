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

import org.eclipse.help.ICriterionValueDefinition;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

public class CriterionValueDefinition extends UAElement implements ICriterionValueDefinition {
    
	public static final String NAME = "criterion-value"; //$NON-NLS-1$
	public static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$
	public static final String ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
	
	public CriterionValueDefinition(ICriterionValueDefinition src) {
		super(NAME, src);
		setId(src.getId());
		setName(src.getName());
	}
	
	public CriterionValueDefinition(Element src) {
		super(src);
	}

	public String getId() {
		return getAttribute(ATTRIBUTE_ID);
	}
	
	public String getName() {
		return getAttribute(ATTRIBUTE_NAME);
	}
	
	public void setId(String id) {
		setAttribute(ATTRIBUTE_ID, id);
	}
	
	public void setName(String name){
		setAttribute(ATTRIBUTE_NAME, name);
	}
}
