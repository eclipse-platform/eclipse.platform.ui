/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
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

import org.eclipse.help.ICriteria;
import org.eclipse.help.ITopic2;
import org.eclipse.help.IUAElement;

public class UserTopic2 extends UserTopic implements ITopic2 {
	
	private List<ICriteria> criteria = new ArrayList<ICriteria>();

	public IUAElement[] getChildren() {
		IUAElement[] criteriaElements = getCriteria();
		IUAElement[] topics = getSubtopics();
		IUAElement[] result = new IUAElement[criteriaElements.length + topics.length];
		System.arraycopy(topics, 0, result, 0, topics.length);
		System.arraycopy(criteriaElements, 0, result, topics.length, criteriaElements.length);
		return result;
	}
	
	public UserTopic2(String label, String href, boolean isEnabled) {
		super(label, href, isEnabled);
	}
	
	public void addCriterion(ICriteria child) {
		criteria.add(child);
	}

	public ICriteria[] getCriteria() {
		return criteria.toArray(new ICriteria[0]);
	}

	public String getIcon() {
		return null;
	}

	public boolean isSorted() {
		return false;
	}

}
