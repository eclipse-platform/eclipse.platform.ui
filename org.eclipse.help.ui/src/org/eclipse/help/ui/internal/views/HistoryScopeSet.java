/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.views;

import org.eclipse.jface.preference.IPreferenceStore;

public class HistoryScopeSet extends ScopeSet {
	private static final String KEY_EXPRESSION = "expression"; //$NON-NLS-1$
	public static final String EXT = ".hist"; //$NON-NLS-1$

	public HistoryScopeSet(String expression) {
		this(expression, expression);
	}

	public HistoryScopeSet(String name, String expression) {
		super(name);
		if (expression!=null)
			setExpression(expression);
	}

	public HistoryScopeSet(HistoryScopeSet set) {
		super(set);
		setExpression(set.getExpression());
	}
	
	public void copyFrom(ScopeSet set) {
		String expression = getExpression();
		super.copyFrom(set);
		setExpression(expression);
	}

	public String getExpression() {
		IPreferenceStore store = getPreferenceStore();
		return store.getString(KEY_EXPRESSION);
	}
	
	public boolean isImplicit() {
		return true;
	}	
	
	protected String getExtension() {
		return EXT;
	}	

	protected String encodeFileName(String name) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			switch (c) {
			case '\"':
				buf.append("QUOTE"); //$NON-NLS-1$
				break;
			case ' ':
				buf.append("_"); //$NON-NLS-1$
				break;
			case '?':
				buf.append("QUESTION"); //$NON-NLS-1$
				break;
			case '*':
				buf.append("STAR"); //$NON-NLS-1$
				break;
			default:
				buf.append(c);
				break;
			}
		}
		return buf.toString();
	}

	public void setExpression(String expression) {
		IPreferenceStore store = getPreferenceStore();
		store.setValue(KEY_EXPRESSION, expression);		
	}
}
