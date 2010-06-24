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

package org.eclipse.ua.tests.help.scope;

import java.util.Locale;

import org.eclipse.help.IIndexEntry;
import org.eclipse.help.IIndexSee;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.base.AbstractHelpScope;

public class MockScope extends AbstractHelpScope {
	
	// Used for testing of scope based classes and utilities
	// Elements are in scope if they contain the letter specified 
	// in the constructor
	
	private char letter;
	private boolean isHierarchical;

	public MockScope(char letter, boolean isHierarchical) {
		this.letter = letter;
		this.isHierarchical = isHierarchical;
	}

	public boolean inScope(IToc toc) {
		return testForInScope(toc.getLabel());
	}

	public boolean inScope(ITopic topic) {
		return testForInScope(topic.getLabel());
	}

	public boolean inScope(IIndexEntry entry) {
		return testForInScope(entry.getKeyword());
	}

	public boolean inScope(IIndexSee see) {
		return true;
	}

	public String getName(Locale locale) {
		return null;
	}
	
	private boolean testForInScope(String label) {
		return label.indexOf(letter) >= 0;
	}
	
	public boolean isHierarchicalScope() {
		return isHierarchical;
	}

}
