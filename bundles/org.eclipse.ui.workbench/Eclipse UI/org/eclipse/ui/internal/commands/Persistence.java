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
import org.eclipse.ui.commands.ICategory;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.IGestureConfiguration;
import org.eclipse.ui.commands.IKeyConfiguration;
import org.eclipse.ui.internal.util.Util;

final class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_CATEGORY = "category"; //$NON-NLS-1$	
	final static String TAG_CATEGORY_ID = "categoryId"; //$NON-NLS-1$
	final static String TAG_COMMAND = "command"; //$NON-NLS-1$	
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_GESTURE_CONFIGURATION = "gestureConfiguration"; //$NON-NLS-1$	
	final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$	
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_NAME = "name"; //$NON-NLS-1$	
	final static String TAG_PARENT_ID = "parentId"; //$NON-NLS-1$
	final static String TAG_PLUGIN_ID = "pluginId"; //$NON-NLS-1$

	static List readCategories(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCategory(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static ICategory readCategory(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new Category(description, id, name, pluginId);
	}

	static ICommand readCommand(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String categoryId = memento.getString(TAG_CATEGORY_ID);
		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new Command(categoryId, description, id, name, pluginId);
	}

	static List readCommands(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommand(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IGestureConfiguration readGestureConfiguration(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new GestureConfiguration(description, id, name, pluginId);
	}

	static List readGestureConfigurations(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readGestureConfiguration(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static IKeyConfiguration readKeyConfiguration(IMemento memento, String pluginIdOverride) {
		if (memento == null)
			throw new NullPointerException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
		String pluginId = pluginIdOverride != null ? pluginIdOverride : memento.getString(TAG_PLUGIN_ID);
		return new KeyConfiguration(description, id, name, pluginId);
	}

	static List readKeyConfigurations(IMemento memento, String name, String pluginIdOverride) {
		if (memento == null || name == null)
			throw new NullPointerException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new NullPointerException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyConfiguration(mementos[i], pluginIdOverride));
	
		return list;				
	}

	static void writeCategories(IMemento memento, String name, List categories) {
		if (memento == null || name == null || categories == null)
			throw new NullPointerException();
		
		categories = new ArrayList(categories);
		Iterator iterator = categories.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object == null)
				throw new NullPointerException();
			else if (!(iterator.next() instanceof ICategory))
				throw new IllegalArgumentException();
		}		

		iterator = categories.iterator();

		while (iterator.hasNext()) 
			writeCategory(memento.createChild(name), (ICategory) iterator.next());
	}
	
	static void writeCategory(IMemento memento, ICategory category) {
		if (memento == null || category == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, category.getDescription());
		memento.putString(TAG_ID, category.getId());
		memento.putString(TAG_NAME, category.getName());
		memento.putString(TAG_PLUGIN_ID, category.getPluginId());
	}

	static void writeCommand(IMemento memento, ICommand command) {
		if (memento == null || command == null)
			throw new NullPointerException();

		memento.putString(TAG_CATEGORY_ID, command.getCategoryId());
		memento.putString(TAG_DESCRIPTION, command.getDescription());
		memento.putString(TAG_ID, command.getId());
		memento.putString(TAG_NAME, command.getName());
		memento.putString(TAG_PLUGIN_ID, command.getPluginId());
	}

	static void writeCommands(IMemento memento, String name, List commands) {
		if (memento == null || name == null || commands == null)
			throw new NullPointerException();
		
		commands = new ArrayList(commands);
		Iterator iterator = commands.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object == null)
				throw new NullPointerException();
			else if (!(iterator.next() instanceof ICommand))
				throw new IllegalArgumentException();
		}		

		iterator = commands.iterator();

		while (iterator.hasNext()) 
			writeCommand(memento.createChild(name), (ICommand) iterator.next());
	}
	
	static void writeGestureConfiguration(IMemento memento, IGestureConfiguration gestureConfiguration) {
		if (memento == null || gestureConfiguration == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, gestureConfiguration.getDescription());
		memento.putString(TAG_ID, gestureConfiguration.getId());
		memento.putString(TAG_NAME, gestureConfiguration.getName());
		memento.putString(TAG_PLUGIN_ID, gestureConfiguration.getPluginId());
	}

	static void writeGestureConfigurations(IMemento memento, String name, List gestureConfigurations) {
		if (memento == null || name == null || gestureConfigurations == null)
			throw new NullPointerException();
		
		gestureConfigurations = new ArrayList(gestureConfigurations);
		Iterator iterator = gestureConfigurations.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object == null)
				throw new NullPointerException();
			else if (!(iterator.next() instanceof IGestureConfiguration))
				throw new IllegalArgumentException();
		}		

		iterator = gestureConfigurations.iterator();

		while (iterator.hasNext()) 
			writeGestureConfiguration(memento.createChild(name), (IGestureConfiguration) iterator.next());
	}
	
	static void writeKeyConfiguration(IMemento memento, IKeyConfiguration keyConfiguration) {
		if (memento == null || keyConfiguration == null)
			throw new NullPointerException();

		memento.putString(TAG_DESCRIPTION, keyConfiguration.getDescription());
		memento.putString(TAG_ID, keyConfiguration.getId());
		memento.putString(TAG_NAME, keyConfiguration.getName());
		memento.putString(TAG_PLUGIN_ID, keyConfiguration.getPluginId());
	}

	static void writeKeyConfigurations(IMemento memento, String name, List keyConfigurations) {
		if (memento == null || name == null || keyConfigurations == null)
			throw new NullPointerException();
		
		keyConfigurations = new ArrayList(keyConfigurations);
		Iterator iterator = keyConfigurations.iterator();

		while (iterator.hasNext()) {
			Object object = iterator.next();
			
			if (object == null)
				throw new NullPointerException();
			else if (!(iterator.next() instanceof IKeyConfiguration))
				throw new IllegalArgumentException();
		}		

		iterator = keyConfigurations.iterator();

		while (iterator.hasNext()) 
			writeKeyConfiguration(memento.createChild(name), (IKeyConfiguration) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
