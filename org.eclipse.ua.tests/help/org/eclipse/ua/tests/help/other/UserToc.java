/*******************************************************************************
 *  Copyright (c) 2005, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ua.tests.help.other;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

public class UserToc implements IToc {
	
	private List<ITopic> children = new ArrayList<ITopic>();
	private boolean isEnabled;
	private String href;
	private String label;
	
	public UserToc(String label, String href, boolean isEnabled) {
		this.label = label;
		this.href = href;
		this.isEnabled = isEnabled;
	}

	/*
	 * Not exercised by any test so return of null is OK for now
	 */
	public ITopic getTopic(String href) {
		return null;
	}

	public ITopic[] getTopics() {
		return children.toArray(new ITopic[0]);
	}

	public IUAElement[] getChildren() {
		return getTopics();
	}

	public void addTopic(ITopic child) {
		children.add(child);
	}

	public boolean isEnabled(IEvaluationContext context) {
		return isEnabled;
	}

	public String getHref() {
		return href;
	}

	public String getLabel() {
		return label;
	}

}
