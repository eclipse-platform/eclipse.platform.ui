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

	static IContextDefinition readContextDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String parentId = memento.getString(TAG_PARENT_ID);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new ContextDefinition(description, id, name, parentId, pluginId);
	}

	static List readContextDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readContextDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeContextDefinition(IMemento memento, IContextDefinition contextDefinition) {
		if (memento == null || contextDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, contextDefinition.getDescription());
		memento.putString(TAG_ID, contextDefinition.getId());
		memento.putString(TAG_NAME, contextDefinition.getName());
		memento.putString(TAG_PARENT_ID, contextDefinition.getParentId());
		memento.putString(TAG_PLUGIN_ID, contextDefinition.getPluginId());
	}

	static void writeContextDefinitions(IMemento memento, String name, List contextDefinitions) {
		if (memento == null || name == null || contextDefinitions == null)
			throw new NullPointerException();
		
		contextDefinitions = new ArrayList(contextDefinitions);
		Iterator iterator = contextDefinitions.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object == null)
				throw new NullPointerException();
			else if (!(iterator.next() instanceof IContextDefinition))
				throw new IllegalArgumentException();
		}		

		iterator = contextDefinitions.iterator();

		while (iterator.hasNext()) 
			writeContextDefinition(memento.createChild(name), (IContextDefinition) iterator.next());
	}

	private Persistence() {
	}	
}
