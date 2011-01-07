/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
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
import org.eclipse.help.IIndexSee;
import org.eclipse.help.IIndexSubpath;
import org.eclipse.help.IUAElement;

public class UserIndexSee implements IIndexSee {
	
	private List<IIndexSubpath> children = new ArrayList<IIndexSubpath>();
	private boolean isEnabled;
	private String keyword;

	public IUAElement[] getChildren() {
		return getSubpathElements();
	}

	public boolean isEnabled(IEvaluationContext context) {
		return isEnabled;
	}
	
	public void addSubpath(IIndexSubpath child) {
		children.add(child);
	}
	
	public UserIndexSee(String keyword, boolean isEnabled) {
		this.keyword = keyword;
		this.isEnabled = isEnabled;
	}

	public String getKeyword() {
		return keyword;
	}

	public IIndexSubpath[] getSubpathElements() {
		return children.toArray(new IIndexSubpath[0]);
	}

	public boolean isSeeAlso() {
		return false;
	}

}
