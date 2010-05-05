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
import org.eclipse.help.ICriteriaDefinition;
import org.eclipse.help.ICriteriaDefinitionContribution;

public class UserCriteriaDefinitionContribution implements ICriteriaDefinitionContribution {

	private ICriteriaDefinition criteriaDefinition;
	private String id;
	private String locale;
	
	public boolean isEnabled(IEvaluationContext context) {
		return true;
	}

	public void setCriteriaDefinition(ICriteriaDefinition criteriaDefinition) {
		this.criteriaDefinition = criteriaDefinition;
	}

	public ICriteriaDefinition getCriteriaDefinition() {
		return criteriaDefinition;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getLocale() {
		return locale;
	}

}
