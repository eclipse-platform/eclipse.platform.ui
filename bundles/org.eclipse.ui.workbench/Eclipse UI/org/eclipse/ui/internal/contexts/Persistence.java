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
import org.eclipse.ui.contexts.IContext;
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

	static IContext readContext(IMemento memento, String pluginIdOverride) {
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
		return new Context(description, id, name, parentId, pluginId);
	}

	static List readContexts(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readContext(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeContext(IMemento memento, IContext context) {
		if (memento == null || context == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, context.getDescription());
		memento.putString(TAG_ID, context.getId());
		memento.putString(TAG_NAME, context.getName());
		memento.putString(TAG_PARENT_ID, context.getParentId());
		memento.putString(TAG_PLUGIN_ID, context.getPluginId());
	}

	static void writeContexts(IMemento memento, String name, List contexts) {
		if (memento == null || name == null || contexts == null)
			throw new NullPointerException();
		
		contexts = new ArrayList(contexts);
		Iterator iterator = contexts.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object == null)
				throw new NullPointerException();
			else if (!(iterator.next() instanceof IContext))
				throw new IllegalArgumentException();
		}		

		iterator = contexts.iterator();

		while (iterator.hasNext()) 
			writeContext(memento.createChild(name), (IContext) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
