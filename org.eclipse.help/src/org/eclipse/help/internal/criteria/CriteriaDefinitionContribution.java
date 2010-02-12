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
import org.eclipse.help.ICriteriaDefinitionContribution;


public class CriteriaDefinitionContribution implements ICriteriaDefinitionContribution{

	private String id;
	private ICriteriaDefinition criteriaDefinition;
	private String locale;
	
	public String getId() {
		return id;
	}
	
	public ICriteriaDefinition getCriteriaDefinition() {
		return criteriaDefinition;
	}
	
	public String getLocale() {
		return locale;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setCriteriaDefinition(ICriteriaDefinition criteriaDefinition) {
		this.criteriaDefinition = criteriaDefinition;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
