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

package org.eclipse.ui.internal.contexts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "contexts"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_CONTEXT = "context"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static ContextElement readContextElement(IMemento memento, String pluginIdOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String parentId = memento.getString(TAG_PARENT_ID);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return ContextElement.create(description, id, name, parentId, pluginId);
	}

	static List readContextElements(IMemento memento, String name, String pluginIdOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readContextElement(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeContextElement(IMemento memento, ContextElement contextElement)
		throws IllegalArgumentException {
		if (memento == null || contextElement == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, contextElement.getDescription());
		memento.putString(TAG_ID, contextElement.getId());
		memento.putString(TAG_NAME, contextElement.getName());
		memento.putString(TAG_PARENT_ID, contextElement.getParentId());
		memento.putString(TAG_PLUGIN_ID, contextElement.getPluginId());
	}

	static void writeContextElements(IMemento memento, String name, List contextElements)
		throws IllegalArgumentException {
		if (memento == null || name == null || contextElements == null)
			throw new IllegalArgumentException();
		
		contextElements = new ArrayList(contextElements);
		Iterator iterator = contextElements.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof ContextElement))
				throw new IllegalArgumentException();

		iterator = contextElements.iterator();

		while (iterator.hasNext()) 
			writeContextElement(memento.createChild(name), (ContextElement) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
