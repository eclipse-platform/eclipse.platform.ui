/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IMemento;

final class Persistence {

	final static String PACKAGE_BASE = "commands"; //$NON-NLS-1$
	final static String PACKAGE_FULL = "org.eclipse.ui." + PACKAGE_BASE; //$NON-NLS-1$
	final static String TAG_ACTIVE_GESTURE_CONFIGURATION = "activeGestureConfiguration"; //$NON-NLS-1$
	final static String TAG_ACTIVE_KEY_CONFIGURATION = "activeKeyConfiguration"; //$NON-NLS-1$
	final static String TAG_CATEGORY = "category"; //$NON-NLS-1$
	final static String TAG_COMMAND = "command"; //$NON-NLS-1$
	final static String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	final static String TAG_GESTURE_BINDING = "gestureBinding"; //$NON-NLS-1$
	final static String TAG_GESTURE_CONFIGURATION = "gestureConfiguration"; //$NON-NLS-1$
	final static String TAG_GESTURE_STRING = "gestureString"; //$NON-NLS-1$
	final static String TAG_GESTURE_SEQUENCE = "gestureSequence"; //$NON-NLS-1$
	final static String TAG_GESTURE_STROKE = "gestureStroke"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_KEY_BINDING = "keyBinding"; //$NON-NLS-1$
	final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$
	final static String TAG_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$
	final static String TAG_KEY_STRING = "keyString"; //$NON-NLS-1$
	final static String TAG_KEY_STROKE = "keyStroke"; //$NON-NLS-1$
	final static String TAG_LOCALE = "locale"; //$NON-NLS-1$		
	final static String TAG_NAME = "name"; //$NON-NLS-1$
	final static String TAG_PARENT = "parent"; //$NON-NLS-1$
	final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$		
	final static String TAG_PLUGIN = "plugin"; //$NON-NLS-1$
	final static String TAG_RANK = "rank"; //$NON-NLS-1$
	final static String TAG_REGIONAL_GESTURE_BINDING = "regionalGestureBinding"; //$NON-NLS-1$
	final static String TAG_REGIONAL_KEY_BINDING = "regionalKeyBinding"; //$NON-NLS-1$
	final static String TAG_SCOPE = "scope"; //$NON-NLS-1$
	final static String TAG_VALUE = "value"; //$NON-NLS-1$
	final static Integer ZERO = new Integer(0);
	final static GestureSequence ZERO_LENGTH_GESTURE_SEQUENCE = GestureSequence.create(); //$NON-NLS-1$
	final static KeySequence ZERO_LENGTH_KEY_SEQUENCE = KeySequence.create(); //$NON-NLS-1$
	final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	static ActiveGestureConfiguration readActiveGestureConfiguration(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		String value = memento.getString(TAG_VALUE);

		if (value == null)
			value = ZERO_LENGTH_STRING;

		return ActiveGestureConfiguration.create(plugin, value);
	}

	static List readActiveGestureConfigurations(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readActiveGestureConfiguration(mementos[i], pluginOverride));
	
		return list;				
	}

	static ActiveKeyConfiguration readActiveKeyConfiguration(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		String value = memento.getString(TAG_VALUE);

		if (value == null)
			value = ZERO_LENGTH_STRING;

		return ActiveKeyConfiguration.create(plugin, value);
	}

	static List readActiveKeyConfigurations(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readActiveKeyConfiguration(mementos[i], pluginOverride));
	
		return list;				
	}

	static Category readCategory(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = ZERO_LENGTH_STRING;
		
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return Category.create(description, id, name, plugin);
	}

	static List readCategories(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCategory(mementos[i], pluginOverride));
	
		return list;				
	}

	static Command readCommand(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String category = memento.getString(TAG_CATEGORY);
		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = ZERO_LENGTH_STRING;
		
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return Command.create(category, description, id, name, plugin);
	}

	static List readCommands(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readCommand(mementos[i], pluginOverride));
	
		return list;				
	}

	static GestureBinding readGestureBinding(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		String command = memento.getString(TAG_COMMAND);
		String gestureConfiguration = memento.getString(TAG_GESTURE_CONFIGURATION);
		
		if (gestureConfiguration == null)
			gestureConfiguration = ZERO_LENGTH_STRING;

		GestureSequence gestureSequence = null;
		IMemento mementoGestureSequence = memento.getChild(TAG_GESTURE_SEQUENCE);
		
		if (mementoGestureSequence != null) 
			gestureSequence = readGestureSequence(mementoGestureSequence);	
		else {
			String gestureString = memento.getString(TAG_GESTURE_STRING);
			
			if (gestureString != null)
				try {			
					gestureSequence = GestureSequence.parse(gestureString);
				} catch (IllegalArgumentException eIllegalArgument) {					
				}
		}
		
		if (gestureSequence == null)
			gestureSequence = ZERO_LENGTH_GESTURE_SEQUENCE;
		
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		Integer rank = memento.getInteger(TAG_RANK);
		
		if (rank == null)
			rank = ZERO;	
		
		String scope = memento.getString(TAG_SCOPE);

		if (scope == null)
			scope = ZERO_LENGTH_STRING;

		return GestureBinding.create(command, gestureConfiguration, gestureSequence, plugin, rank.intValue(), scope);
	}

	static List readGestureBindings(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readGestureBinding(mementos[i], pluginOverride));
	
		return list;				
	}

	static GestureConfiguration readGestureConfiguration(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = ZERO_LENGTH_STRING;
		
		String parent = memento.getString(TAG_PARENT);
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return GestureConfiguration.create(description, id, name, parent, plugin);
	}

	static List readGestureConfigurations(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readGestureConfiguration(mementos[i], pluginOverride));
	
		return list;				
	}
	
	static GestureSequence readGestureSequence(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren(TAG_GESTURE_STROKE);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List gestureStrokes = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			gestureStrokes.add(readGestureStroke(mementos[i]));
		
		return GestureSequence.create(gestureStrokes);
	}

	static GestureStroke readGestureStroke(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		Integer value = memento.getInteger(TAG_VALUE);
		
		if (value == null)
			value = ZERO;
		
		return GestureStroke.create(value.intValue());
	}

	static KeyBinding readKeyBinding(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		String command = memento.getString(TAG_COMMAND);
		String keyConfiguration = memento.getString(TAG_KEY_CONFIGURATION);
		
		if (keyConfiguration == null)
			keyConfiguration = ZERO_LENGTH_STRING;

		KeySequence keySequence = null;
		IMemento mementoKeySequence = memento.getChild(TAG_KEY_SEQUENCE);
		
		if (mementoKeySequence != null) 
			keySequence = readKeySequence(mementoKeySequence);	
		else {
			String keyString = memento.getString(TAG_KEY_STRING);
			
			if (keyString != null)			
				try {			
					keySequence = KeySequence.parse(keyString);
				} catch (IllegalArgumentException eIllegalArgument) {					
				}
		}
		
		if (keySequence == null)
			keySequence = ZERO_LENGTH_KEY_SEQUENCE;
		
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		Integer rank = memento.getInteger(TAG_RANK);
		
		if (rank == null)
			rank = ZERO;	
		
		String scope = memento.getString(TAG_SCOPE);

		if (scope == null)
			scope = ZERO_LENGTH_STRING;

		return KeyBinding.create(command, keyConfiguration, keySequence, plugin, rank.intValue(), scope);
	}

	static List readKeyBindings(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyBinding(mementos[i], pluginOverride));
	
		return list;				
	}

	static KeyConfiguration readKeyConfiguration(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = ZERO_LENGTH_STRING;
		
		String parent = memento.getString(TAG_PARENT);
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return KeyConfiguration.create(description, id, name, parent, plugin);
	}

	static List readKeyConfigurations(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readKeyConfiguration(mementos[i], pluginOverride));
	
		return list;				
	}
	
	static KeySequence readKeySequence(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren(TAG_KEY_STROKE);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List keyStrokes = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			keyStrokes.add(readKeyStroke(mementos[i]));
		
		return KeySequence.create(keyStrokes);
	}

	static KeyStroke readKeyStroke(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		Integer value = memento.getInteger(TAG_VALUE);
		
		if (value == null)
			value = ZERO;
		
		return KeyStroke.create(value.intValue());
	}

	static RegionalGestureBinding readRegionalGestureBinding(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		IMemento mementoGestureBinding = memento.getChild(TAG_GESTURE_BINDING);
		GestureBinding gestureBinding = null;
		
		if (mementoGestureBinding == null)
			gestureBinding = GestureBinding.create(null, ZERO_LENGTH_STRING, ZERO_LENGTH_GESTURE_SEQUENCE, null, 0, ZERO_LENGTH_STRING);
		else
			gestureBinding = readGestureBinding(mementoGestureBinding, pluginOverride);	

		String locale = memento.getString(TAG_LOCALE);
	
		if (locale == null)
			locale = ZERO_LENGTH_STRING;

		String platform = memento.getString(TAG_PLATFORM);

		if (platform == null)
			platform = ZERO_LENGTH_STRING;

		return RegionalGestureBinding.create(gestureBinding, locale, platform);
	}

	static List readRegionalGestureBindings(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readRegionalGestureBinding(mementos[i], pluginOverride));
	
		return list;				
	}

	static RegionalKeyBinding readRegionalKeyBinding(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		IMemento mementoKeyBinding = memento.getChild(TAG_KEY_BINDING);
		KeyBinding keyBinding = null;
		
		if (mementoKeyBinding == null)
			keyBinding = KeyBinding.create(null, ZERO_LENGTH_STRING, ZERO_LENGTH_KEY_SEQUENCE, null, 0, ZERO_LENGTH_STRING);
		else
			keyBinding = readKeyBinding(mementoKeyBinding, pluginOverride);	

		String locale = memento.getString(TAG_LOCALE);
	
		if (locale == null)
			locale = ZERO_LENGTH_STRING;

		String platform = memento.getString(TAG_PLATFORM);

		if (platform == null)
			platform = ZERO_LENGTH_STRING;

		return RegionalKeyBinding.create(keyBinding, locale, platform);
	}

	static List readRegionalKeyBindings(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readRegionalKeyBinding(mementos[i], pluginOverride));
	
		return list;				
	}

	static Scope readScope(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = ZERO_LENGTH_STRING;
		
		String parent = memento.getString(TAG_PARENT);
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return Scope.create(description, id, name, parent, plugin);
	}

	static List readScopes(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readScope(mementos[i], pluginOverride));
	
		return list;				
	}

	static void writeActiveGestureConfiguration(IMemento memento, ActiveGestureConfiguration activeGestureConfiguration)
		throws IllegalArgumentException {
		if (memento == null || activeGestureConfiguration == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_PLUGIN, activeGestureConfiguration.getPlugin());
		memento.putString(TAG_VALUE, activeGestureConfiguration.getValue());
	}

	static void writeActiveGestureConfigurations(IMemento memento, String name, List activeGestureConfigurations)
		throws IllegalArgumentException {
		if (memento == null || name == null || activeGestureConfigurations == null)
			throw new IllegalArgumentException();
		
		activeGestureConfigurations = new ArrayList(activeGestureConfigurations);
		Iterator iterator = activeGestureConfigurations.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof ActiveGestureConfiguration))
				throw new IllegalArgumentException();

		iterator = activeGestureConfigurations.iterator();

		while (iterator.hasNext()) 
			writeActiveGestureConfiguration(memento.createChild(name), (ActiveGestureConfiguration) iterator.next());
	}
	
	static void writeActiveKeyConfiguration(IMemento memento, ActiveKeyConfiguration activeKeyConfiguration)
		throws IllegalArgumentException {
		if (memento == null || activeKeyConfiguration == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_PLUGIN, activeKeyConfiguration.getPlugin());
		memento.putString(TAG_VALUE, activeKeyConfiguration.getValue());
	}

	static void writeActiveKeyConfigurations(IMemento memento, String name, List activeKeyConfigurations)
		throws IllegalArgumentException {
		if (memento == null || name == null || activeKeyConfigurations == null)
			throw new IllegalArgumentException();
		
		activeKeyConfigurations = new ArrayList(activeKeyConfigurations);
		Iterator iterator = activeKeyConfigurations.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof ActiveKeyConfiguration))
				throw new IllegalArgumentException();

		iterator = activeKeyConfigurations.iterator();

		while (iterator.hasNext()) 
			writeActiveKeyConfiguration(memento.createChild(name), (ActiveKeyConfiguration) iterator.next());
	}

	static void writeCategory(IMemento memento, Category category)
		throws IllegalArgumentException {
		if (memento == null || category == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, category.getDescription());
		memento.putString(TAG_ID, category.getId());
		memento.putString(TAG_NAME, category.getName());
		memento.putString(TAG_PLUGIN, category.getPlugin());
	}

	static void writeCategories(IMemento memento, String name, List categories)
		throws IllegalArgumentException {
		if (memento == null || name == null || categories == null)
			throw new IllegalArgumentException();
		
		categories = new ArrayList(categories);
		Iterator iterator = categories.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof Category))
				throw new IllegalArgumentException();

		iterator = categories.iterator();

		while (iterator.hasNext()) 
			writeCategory(memento.createChild(name), (Category) iterator.next());
	}

	static void writeCommand(IMemento memento, Command command)
		throws IllegalArgumentException {
		if (memento == null || command == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_CATEGORY, command.getCategory());
		memento.putString(TAG_DESCRIPTION, command.getDescription());
		memento.putString(TAG_ID, command.getId());
		memento.putString(TAG_NAME, command.getName());
		memento.putString(TAG_PLUGIN, command.getPlugin());
	}

	static void writeCommands(IMemento memento, String name, List commands)
		throws IllegalArgumentException {
		if (memento == null || name == null || commands == null)
			throw new IllegalArgumentException();
		
		commands = new ArrayList(commands);
		Iterator iterator = commands.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof Command))
				throw new IllegalArgumentException();

		iterator = commands.iterator();

		while (iterator.hasNext()) 
			writeCommand(memento.createChild(name), (Command) iterator.next());
	}
	
	static void writeGestureBinding(IMemento memento, GestureBinding gestureBinding)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_COMMAND, gestureBinding.getCommand());
		memento.putString(TAG_GESTURE_CONFIGURATION, gestureBinding.getGestureConfiguration());
		writeGestureSequence(memento.createChild(TAG_GESTURE_SEQUENCE), gestureBinding.getGestureSequence());		
		memento.putString(TAG_PLUGIN, gestureBinding.getPlugin());
		memento.putInteger(TAG_RANK, gestureBinding.getRank());
		memento.putString(TAG_SCOPE, gestureBinding.getScope());
	}	

	static void writeGestureBindings(IMemento memento, String name, List gestureBindings)
		throws IllegalArgumentException {
		if (memento == null || name == null || gestureBindings == null)
			throw new IllegalArgumentException();
		
		gestureBindings = new ArrayList(gestureBindings);
		Iterator iterator = gestureBindings.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof GestureBinding))
				throw new IllegalArgumentException();

		iterator = gestureBindings.iterator();

		while (iterator.hasNext()) 
			writeGestureBinding(memento.createChild(name), (GestureBinding) iterator.next());
	}

	static void writeGestureConfiguration(IMemento memento, GestureConfiguration gestureConfiguration)
		throws IllegalArgumentException {
		if (memento == null || gestureConfiguration == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, gestureConfiguration.getDescription());
		memento.putString(TAG_ID, gestureConfiguration.getId());
		memento.putString(TAG_NAME, gestureConfiguration.getName());
		memento.putString(TAG_PARENT, gestureConfiguration.getParent());
		memento.putString(TAG_PLUGIN, gestureConfiguration.getPlugin());
	}

	static void writeGestureConfigurations(IMemento memento, String name, List gestureConfigurations)
		throws IllegalArgumentException {
		if (memento == null || name == null || gestureConfigurations == null)
			throw new IllegalArgumentException();
		
		gestureConfigurations = new ArrayList(gestureConfigurations);
		Iterator iterator = gestureConfigurations.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof GestureConfiguration))
				throw new IllegalArgumentException();

		iterator = gestureConfigurations.iterator();

		while (iterator.hasNext()) 
			writeGestureConfiguration(memento.createChild(name), (GestureConfiguration) iterator.next());
	}

	static void writeGestureSequence(IMemento memento, GestureSequence gestureSequence)
		throws IllegalArgumentException {
		if (memento == null || gestureSequence == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = gestureSequence.getGestureStrokes().iterator();

		while (iterator.hasNext())
			writeGestureStroke(memento.createChild(TAG_GESTURE_STROKE), (GestureStroke) iterator.next());
	}

	static void writeGestureStroke(IMemento memento, GestureStroke gestureStroke)
		throws IllegalArgumentException {
		if (memento == null || gestureStroke == null)
			throw new IllegalArgumentException();
			
		memento.putInteger(TAG_VALUE, gestureStroke.getValue());
	}

	static void writeKeyBinding(IMemento memento, KeyBinding keyBinding)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_COMMAND, keyBinding.getCommand());
		memento.putString(TAG_KEY_CONFIGURATION, keyBinding.getKeyConfiguration());
		writeKeySequence(memento.createChild(TAG_KEY_SEQUENCE), keyBinding.getKeySequence());		
		memento.putString(TAG_PLUGIN, keyBinding.getPlugin());
		memento.putInteger(TAG_RANK, keyBinding.getRank());
		memento.putString(TAG_SCOPE, keyBinding.getScope());
	}	

	static void writeKeyBindings(IMemento memento, String name, List keyBindings)
		throws IllegalArgumentException {
		if (memento == null || name == null || keyBindings == null)
			throw new IllegalArgumentException();
		
		keyBindings = new ArrayList(keyBindings);
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof KeyBinding))
				throw new IllegalArgumentException();

		iterator = keyBindings.iterator();

		while (iterator.hasNext()) 
			writeKeyBinding(memento.createChild(name), (KeyBinding) iterator.next());
	}

	static void writeKeyConfiguration(IMemento memento, KeyConfiguration keyConfiguration)
		throws IllegalArgumentException {
		if (memento == null || keyConfiguration == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, keyConfiguration.getDescription());
		memento.putString(TAG_ID, keyConfiguration.getId());
		memento.putString(TAG_NAME, keyConfiguration.getName());
		memento.putString(TAG_PARENT, keyConfiguration.getParent());
		memento.putString(TAG_PLUGIN, keyConfiguration.getPlugin());
	}

	static void writeKeyConfigurations(IMemento memento, String name, List keyConfigurations)
		throws IllegalArgumentException {
		if (memento == null || name == null || keyConfigurations == null)
			throw new IllegalArgumentException();
		
		keyConfigurations = new ArrayList(keyConfigurations);
		Iterator iterator = keyConfigurations.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof KeyConfiguration))
				throw new IllegalArgumentException();

		iterator = keyConfigurations.iterator();

		while (iterator.hasNext()) 
			writeKeyConfiguration(memento.createChild(name), (KeyConfiguration) iterator.next());
	}

	static void writeKeySequence(IMemento memento, KeySequence keySequence)
		throws IllegalArgumentException {
		if (memento == null || keySequence == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = keySequence.getKeyStrokes().iterator();

		while (iterator.hasNext())
			writeKeyStroke(memento.createChild(TAG_KEY_STROKE), (KeyStroke) iterator.next());
	}

	static void writeKeyStroke(IMemento memento, KeyStroke keyStroke)
		throws IllegalArgumentException {
		if (memento == null || keyStroke == null)
			throw new IllegalArgumentException();
			
		memento.putInteger(TAG_VALUE, keyStroke.getValue());
	}

	static void writeRegionalGestureBinding(IMemento memento, RegionalGestureBinding regionalGestureBinding)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		writeGestureBinding(memento.createChild(TAG_GESTURE_BINDING), regionalGestureBinding.getGestureBinding());
		memento.putString(TAG_LOCALE, regionalGestureBinding.getLocale());
		memento.putString(TAG_PLATFORM, regionalGestureBinding.getPlatform());
	}

	static void writeRegionalGestureBindings(IMemento memento, String name, List regionalGestureBindings)
		throws IllegalArgumentException {
		if (memento == null || name == null || regionalGestureBindings == null)
			throw new IllegalArgumentException();
		
		regionalGestureBindings = new ArrayList(regionalGestureBindings);
		Iterator iterator = regionalGestureBindings.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof RegionalGestureBinding))
				throw new IllegalArgumentException();

		iterator = regionalGestureBindings.iterator();

		while (iterator.hasNext()) 
			writeRegionalGestureBinding(memento.createChild(name), (RegionalGestureBinding) iterator.next());
	}

	static void writeRegionalKeyBinding(IMemento memento, RegionalKeyBinding regionalKeyBinding)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		writeKeyBinding(memento.createChild(TAG_KEY_BINDING), regionalKeyBinding.getKeyBinding());
		memento.putString(TAG_LOCALE, regionalKeyBinding.getLocale());
		memento.putString(TAG_PLATFORM, regionalKeyBinding.getPlatform());
	}

	static void writeRegionalKeyBindings(IMemento memento, String name, List regionalKeyBindings)
		throws IllegalArgumentException {
		if (memento == null || name == null || regionalKeyBindings == null)
			throw new IllegalArgumentException();
		
		regionalKeyBindings = new ArrayList(regionalKeyBindings);
		Iterator iterator = regionalKeyBindings.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof RegionalKeyBinding))
				throw new IllegalArgumentException();

		iterator = regionalKeyBindings.iterator();

		while (iterator.hasNext()) 
			writeRegionalKeyBinding(memento.createChild(name), (RegionalKeyBinding) iterator.next());
	}

	static void writeScope(IMemento memento, Scope scope)
		throws IllegalArgumentException {
		if (memento == null || scope == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, scope.getDescription());
		memento.putString(TAG_ID, scope.getId());
		memento.putString(TAG_NAME, scope.getName());
		memento.putString(TAG_PARENT, scope.getParent());
		memento.putString(TAG_PLUGIN, scope.getPlugin());
	}

	static void writeScopes(IMemento memento, String name, List scopes)
		throws IllegalArgumentException {
		if (memento == null || name == null || scopes == null)
			throw new IllegalArgumentException();
		
		scopes = new ArrayList(scopes);
		Iterator iterator = scopes.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof Scope))
				throw new IllegalArgumentException();

		iterator = scopes.iterator();

		while (iterator.hasNext()) 
			writeScope(memento.createChild(name), (Scope) iterator.next());
	}

	private Persistence() {
		super();
	}	
}
