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

package org.eclipse.ua.tests.help.other;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ICriteriaDefinition;
import org.eclipse.help.ICriterionDefinition;
import org.eclipse.help.IUAElement;

public class UserCriteriaDefinition implements ICriteriaDefinition {

	private List children = new ArrayList();
	
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	public ICriterionDefinition[] getCriterionDefinitions() {
		return (ICriterionDefinition[]) children.toArray(new ICriterionDefinition[0]);
	}
	
	public void addDefinition(ICriterionDefinition definition) {
		children.add(definition);
	}

	public IUAElement[] getChildren() {
		return getCriterionDefinitions();
	}


}
