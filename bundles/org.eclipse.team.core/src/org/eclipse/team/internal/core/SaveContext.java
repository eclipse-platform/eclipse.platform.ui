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
package org.eclipse.team.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Something (as a mark of visible sign) left by a material thing formely present but now 
 * lost or unknown.
 * 
 * TODO: API doc if this class is to remain. Ideally it should replaced by a core mechanism which
 * allows persisting somewhat like IMemento. 
 */
public class SaveContext {
	
	private String name;
	
	private String value;
	
	private Map attributes;
	
	private List children = new ArrayList(2);
	
	public SaveContext() {}
	
	public String getAttribute(String name) {
		if(attributes == null) {
			return null;
		}
		return (String)attributes.get(name);
	}
	
	public void putInteger(String key, int n) {
		addAttribute(key, String.valueOf(n));
	}
	
	public void putFloat(String key, float n) {
		addAttribute(key, String.valueOf(n));
	}
		
	public void putString(String key, String n) {
		addAttribute(key, n);
	}
	
	public void putBoolean(String key, boolean n) {
		addAttribute(key, String.valueOf(n));		
	}
	
	public int getInteger(String key) {
		String f = getAttribute(key);
		if(f != null) {
			return new Integer(f).intValue();
		}
		return 0;
	}
	
	public float getFloat(String key) {
		String f = getAttribute(key);
		if(f != null) {
			return new Float(f).floatValue();
		}
		return 0;
	}
		
	public String getString(String key) {
		return getAttribute(key);
	}
	
	public boolean getBoolean(String key) {
		String bool = getAttribute(key);
		if(bool != null) {
			return bool.equals("true") ? true : false; //$NON-NLS-1$
		}
		return true;
	}
	
	public String[] getAttributeNames() {
		if(attributes == null) {
			return new String[0];
		}
		return (String[])attributes.keySet().toArray(new String[attributes.keySet().size()]);
	}
	
	public SaveContext[] getChildren() {
		return (SaveContext[]) children.toArray(new SaveContext[children.size()]);
	}

	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setAttributes(Map map) {
		attributes = map;
	}

	public void setChildren(SaveContext[] items) {
		children = new ArrayList(Arrays.asList(items));
	}
	
	public void putChild(SaveContext child) {
		children.add(child);
	}

	public void setName(String string) {
		name = string;
	}

	public void setValue(String string) {
		value = string;
	}

	public void addAttribute(String key, String value) {
		if(attributes == null) {
			attributes = new HashMap();
		}
		attributes.put(key, value);
	}
	
	public String toString() {
		return getName() + " ->" + attributes.toString(); //$NON-NLS-1$
	}
}