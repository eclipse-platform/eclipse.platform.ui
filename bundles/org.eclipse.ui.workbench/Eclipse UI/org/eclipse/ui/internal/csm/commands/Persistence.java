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

package org.eclipse.ui.internal.csm.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_PREFIX = "org.eclipse.ui"; //$NON-NLS-1$	
	final static String PACKAGE_FULL = PACKAGE_PREFIX + '.' + PACKAGE_BASE;
	final static String TAG_COMMAND = "command"; //$NON-NLS-1$	
	final static String TAG_COMMAND_ID = "commandId"; //$NON-NLS-1$	
	final static String TAG_COMMAND_PATTERN_BINDING = "commandPatternBinding"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_INCLUSIVE = "inclusive"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PATTERN = "pattern"; //$NON-NLS-1$	
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static ICommandDefinition readCommandDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);
		String name = memento.getString(TAG_NAME);
		String parentId = memento.getString(TAG_PARENT_ID);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new CommandDefinition(description, id, name, parentId, pluginId);
	}

	static List readCommandDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommandDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}	
	
	static ICommandPatternBindingDefinition readCommandPatternBindingDefinition(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String commandId = memento.getString(TAG_COMMAND_ID);
		boolean inclusive = Boolean.valueOf(memento.getString(TAG_INCLUSIVE)).booleanValue();
		String pattern = memento.getString(TAG_PATTERN);
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new CommandPatternBindingDefinition(commandId, inclusive, pattern, pluginId);
	}

	static List readCommandPatternBindingDefinitions(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommandPatternBindingDefinition(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeCommandDefinition(IMemento memento, ICommandDefinition commandDefinition) {
		if (memento == null || commandDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, commandDefinition.getDescription());
		memento.putString(TAG_ID, commandDefinition.getId());
		memento.putString(TAG_NAME, commandDefinition.getName());
		memento.putString(TAG_PARENT_ID, commandDefinition.getParentId());
		memento.putString(TAG_PLUGIN_ID, commandDefinition.getPluginId());
	}

	static void writeCommandDefinitions(IMemento memento, String name, List commandDefinitions) {
		if (memento == null || name == null || commandDefinitions == null)
			throw new NullPointerException();
		
		commandDefinitions = new ArrayList(commandDefinitions);
		Iterator iterator = commandDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICommandDefinition.class);

		iterator = commandDefinitions.iterator();

		while (iterator.hasNext()) 
			writeCommandDefinition(memento.createChild(name), (ICommandDefinition) iterator.next());
	}	
	
	static void writeCommandPatternBindingDefinition(IMemento memento, ICommandPatternBindingDefinition commandPatternBindingDefinition) {
		if (memento == null || commandPatternBindingDefinition == null)
			throw new NullPointerException();

		memento.putString(TAG_COMMAND_ID, commandPatternBindingDefinition.getCommandId());
		memento.putString(TAG_INCLUSIVE, Boolean.toString(commandPatternBindingDefinition.isInclusive()));
		memento.putString(TAG_PATTERN, commandPatternBindingDefinition.getCommandId());
		memento.putString(TAG_PLUGIN_ID, commandPatternBindingDefinition.getPluginId());
	}

	static void writeCommandPatternBindingDefinitions(IMemento memento, String name, List commandPatternBindingDefinitions) {
		if (memento == null || name == null || commandPatternBindingDefinitions == null)
			throw new NullPointerException();
		
		commandPatternBindingDefinitions = new ArrayList(commandPatternBindingDefinitions);
		Iterator iterator = commandPatternBindingDefinitions.iterator();

		while (iterator.hasNext())
			Util.assertInstance(iterator.next(), ICommandPatternBindingDefinition.class);

		iterator = commandPatternBindingDefinitions.iterator();

		while (iterator.hasNext()) 
			writeCommandPatternBindingDefinition(memento.createChild(name), (ICommandPatternBindingDefinition) iterator.next());
	}

	private Persistence() {
	}	
}
