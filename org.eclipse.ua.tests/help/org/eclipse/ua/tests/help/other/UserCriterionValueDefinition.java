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

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.ICriterionValueDefinition;
import org.eclipse.help.IUAElement;

public class UserCriterionValueDefinition implements ICriterionValueDefinition {

	private String id;
	private String name;
	
	public UserCriterionValueDefinition(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public IUAElement[] getChildren() {
        return new IUAElement[0];
	}

}
