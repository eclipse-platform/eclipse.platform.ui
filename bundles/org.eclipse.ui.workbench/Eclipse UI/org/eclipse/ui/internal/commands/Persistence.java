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

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_COMMAND = "command"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static CommandElement readCommandElement(IMemento memento, String pluginIdOverride)
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
		return CommandElement.create(description, id, name, parentId, pluginId);
	}

	static List readCommandElements(IMemento memento, String name, String pluginIdOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommandElement(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeCommandElement(IMemento memento, CommandElement commandElement)
		throws IllegalArgumentException {
		if (memento == null || commandElement == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, commandElement.getDescription());
		memento.putString(TAG_ID, commandElement.getId());
		memento.putString(TAG_NAME, commandElement.getName());
		memento.putString(TAG_PARENT_ID, commandElement.getParentId());
		memento.putString(TAG_PLUGIN_ID, commandElement.getPluginId());
	}

	static void writeCommandElements(IMemento memento, String name, List commandElements)
		throws IllegalArgumentException {
		if (memento == null || name == null || commandElements == null)
			throw new IllegalArgumentException();
		
		commandElements = new ArrayList(commandElements);
		Iterator iterator = commandElements.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof CommandElement))
				throw new IllegalArgumentException();

		iterator = commandElements.iterator();

		while (iterator.hasNext()) 
			writeCommandElement(memento.createChild(name), (CommandElement) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
