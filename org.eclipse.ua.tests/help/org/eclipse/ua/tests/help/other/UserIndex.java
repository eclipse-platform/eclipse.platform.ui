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
import org.eclipse.help.IIndex;
import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IUAElement;

/**
 * This class is used to test topics created using the IIndex API
 */

public class UserIndex implements IIndex {
	
	private List<IIndexEntry> entries = new ArrayList<IIndexEntry>();
	private boolean enabled;
	
	public UserIndex(boolean enabled) {
		this.enabled = true;
	}

	public IUAElement[] getChildren() {
		return getEntries();
	}
	
	public void addEntry(IIndexEntry child) {
		entries.add(child);
	}

	public IIndexEntry[] getEntries() {
		return entries.toArray(new IIndexEntry[0]);
	}

	public boolean isEnabled(IEvaluationContext context) {
		return enabled;
	}

}
