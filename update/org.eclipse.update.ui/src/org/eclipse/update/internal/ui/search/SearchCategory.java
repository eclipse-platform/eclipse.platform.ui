/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.search;

import java.util.Map;

import org.eclipse.swt.widgets.Control;

public abstract class SearchCategory implements ISearchCategory {
	private Control control;
	private String id;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Control getControl() {
		return control;
	}
	
	public void setControl(Control control) {
		this.control = control;
	}
	protected String getString(String key, Map map) {
		Object obj = map.get(key);
		if (obj!=null) return obj.toString();
		return "";
	}
	protected boolean getBoolean(String key, Map map) {
		return getString(key, map).equals("true");
	}
}
