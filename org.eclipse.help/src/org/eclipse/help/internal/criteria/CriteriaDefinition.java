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

import org.eclipse.help.ICriteriaDefinition;
import org.eclipse.help.ICriterionDefinition;
import org.eclipse.help.internal.UAElement;
import org.w3c.dom.Element;

public class CriteriaDefinition extends UAElement implements ICriteriaDefinition {
    
	public static final String NAME = "criteriaDefinition"; //$NON-NLS-1$

	public CriteriaDefinition() {
		super(NAME);
	}
	
	public CriteriaDefinition(ICriteriaDefinition src) {
		super(NAME, src);
		appendChildren(src.getChildren());
	}
	
	public CriteriaDefinition(Element src) {
		super(src);
	}

	public ICriterionDefinition[] getCriterionDefinitions() {
		return (ICriterionDefinition[])getChildren(ICriterionDefinition.class);
	}
	
}
