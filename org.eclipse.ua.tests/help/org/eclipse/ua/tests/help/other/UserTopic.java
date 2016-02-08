/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
import org.eclipse.help.ITopic;
import org.eclipse.help.IUAElement;

/**
 * This class is used to test topics created using the ITopic API
 */

public class UserTopic implements ITopic {

	private List<ITopic> children = new ArrayList<ITopic>();
	private boolean isEnabled;
	private String href;
	private String label;

	@Override
	public ITopic[] getSubtopics() {
		return children.toArray(new ITopic[0]);
	}

	@Override
	public IUAElement[] getChildren() {
		return getSubtopics();
	}

	@Override
	public boolean isEnabled(IEvaluationContext context) {
		return isEnabled;
	}

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public String getLabel() {
		return label;
	}

	public void addTopic(ITopic child) {
		children.add(child);
	}

	public UserTopic(String label, String href, boolean isEnabled) {
		this.label = label;
		this.href = href;
		this.isEnabled = isEnabled;
	}

}
