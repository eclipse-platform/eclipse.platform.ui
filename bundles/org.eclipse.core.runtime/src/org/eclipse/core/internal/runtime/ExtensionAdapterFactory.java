/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.runtime;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.*;

/**
 * The extension adapter factory creates a list of available adapters
 * based on information provided by extensions to the 
 * org.eclipse.core.runtime.adapters extension point. 
 */
class ExtensionAdapterFactory {
	/**
	 * This table has two purposes - string internment, and mapping of
	 * adaptables to the array of adapters for which a factory exists.
	 * The key of the table is always a String representing the fully
	 * qualfied name of a class. The value is either a String or a String[].
	 * When the value is a String, it will be identical to the key, and simply
	 * used for internment purposes.  When the value is a String[], the first
	 * entry in the array will be identical to the key.  All subsequent entries
	 * in the array will be fully qualified names of adapter classes.
	 */
	private final Map table;
	ExtensionAdapterFactory() {
		table = new HashMap();
		loadExtensions();
	}
	/* (non-Javadoc)
	 * @see IAdapterManager.hasAdapter(Object, Class)
	 */
	public boolean hasAdapter(Object adaptableObject, String adapter) {
		String adaptable = intern(adaptableObject.getClass().getName(), false);
		//if it's not in the intern table then we can't possibly have a registered adapter
		if (adaptable == null)
			return false;
		adapter = intern(adapter, false);
		//if it's not in the intern table then we can't possibly have a registered adapter
		if (adapter == null)
			return false;
		Object result = table.get(adaptable);
		//table contains no entries for the adaptable
		if (result == null)
			return false;
		//table just contains an interned string, so there are no adapters
		if (result instanceof String)
			return false;
		//table contains adapters - look for a match
		String[] adapters = (String[])result;
		for (int i = 1; i < adapters.length; i++) 
			if (adapters[i] == adapter)
				return true;
		return false;
	}
	/**
	 * Returns a String that is unique within the extension table. If
	 * add is true, the string will be added to the table if not already
	 * found. If add is false, this method returns null if the string
	 * is not found in the table.
	 */
	private String intern(String string, boolean add) {
		Object result  = table.get(string);
		if (result instanceof String)
			return (String)result;
		if (result instanceof String[])
			return ((String[])result)[0];
		if (!add)
			return null;
		//add the string to the intern table
		table.put(string, string);
		return string;
	}
	/**
	 * Loads adapters registered with the adapters extension point.
	 */
	private void loadExtensions() {
		IPluginRegistry registry = Platform.getPluginRegistry();
		IExtensionPoint point = registry.getExtensionPoint(Platform.PI_RUNTIME, Platform.PT_ADAPTERS);
		if (point == null)
			return;
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				if (!elements[j].getName().equals("adapter")) //$NON-NLS-1$
					continue;//malformed extension
				String adapter= elements[j].getAttribute("adapterType"); //$NON-NLS-1$
				String adaptable = elements[j].getAttribute("adaptableType"); //$NON-NLS-1$
				if (adapter == null || adaptable == null)
					continue;//malformed extension
				add(adaptable, adapter);
			}
		}
	}
	/**
	 * Adds an adaptable -> adapter pair to the table.
	 */
	private void add(String adaptable, String adapter) {
		//make sure the names are unique
		adapter = intern(adapter, true);
		adaptable = intern(adaptable, true);
		Object value = table.get(adaptable);
		//new pair
		if (value == null) {
			table.put(adaptable, new String[] {adaptable, adapter});
			return;
		}
		//an interned string exists but no adapters, so add it 
		if (value instanceof String) {
			table.put(value, new String[] {(String)value, adapter});
			return;
		}
		//this adaptable already has adapters, so first check if it already has the same one
		String[] adapters = (String[])value;
		for (int i = 1; i < adapters.length; i++) 
			if (adapters[i]==adapter)
				return;
		//add the new adapter
		int oldLen = adapters.length;
		String[] newAdapters = new String[oldLen+1];
		System.arraycopy(adapters, 0, newAdapters, 0, oldLen);
		newAdapters[oldLen] = adapter;
		table.put(adapters[0], newAdapters);
	}
	public void registerAdapters(IAdapterFactory factory, Class adaptableClass) {
		String adaptable = intern(adaptableClass.getName(), true);
		Class[] list = factory.getAdapterList();
		for (int i = 0; i < list.length; i++) 
			add(adaptable, list[i].getName());
	}
}