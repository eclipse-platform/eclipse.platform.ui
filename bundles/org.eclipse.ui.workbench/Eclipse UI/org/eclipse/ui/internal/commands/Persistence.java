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
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_KEY_BINDING = "keyBinding"; //$NON-NLS-1$
	final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$
	final static String TAG_CONFIGURATION = "configuration"; //$NON-NLS-1$
	final static String TAG_SEQUENCE = "sequence"; //$NON-NLS-1$
	final static String TAG_STRING = "string"; //$NON-NLS-1$
	final static String TAG_STROKE = "stroke"; //$NON-NLS-1$
	final static String TAG_LOCALE = "locale"; //$NON-NLS-1$		
	final static String TAG_NAME = "name"; //$NON-NLS-1$
	final static String TAG_PARENT = "parent"; //$NON-NLS-1$
	final static String TAG_PLATFORM = "platform"; //$NON-NLS-1$		
	final static String TAG_PLUGIN = "plugin"; //$NON-NLS-1$
	final static String TAG_SCOPE = "scope"; //$NON-NLS-1$
	final static String TAG_VALUE = "value"; //$NON-NLS-1$
	final static Integer ZERO = new Integer(0);
	final static Sequence ZERO_LENGTH_SEQUENCE = Sequence.create(); //$NON-NLS-1$

	static ActiveConfiguration readActiveConfiguration(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		String value = memento.getString(TAG_VALUE);

		if (value == null)
			value = Util.ZERO_LENGTH_STRING;

		return ActiveConfiguration.create(plugin, value);
	}

	static List readActiveConfigurations(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readActiveConfiguration(mementos[i], pluginOverride));
	
		return list;				
	}

	static Category readCategory(IMemento memento, String pluginOverride)
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
			id = Util.ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = Util.ZERO_LENGTH_STRING;
		
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

	static SequenceBinding readBinding(IMemento memento, String pluginOverride, int rank)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		String command = memento.getString(TAG_COMMAND);
		String configuration = memento.getString(TAG_CONFIGURATION);
			
		// TODO remove after 2.1 ships (BEGIN)
		if (configuration == null)
			configuration = memento.getString("keyConfiguration"); //$NON-NLS-1$
		// TODO remove after 2.1 ships (END)

		if (configuration == null)
			configuration = Util.ZERO_LENGTH_STRING;

		Sequence sequence = null;
		IMemento mementoSequence = memento.getChild(TAG_SEQUENCE);
		
		if (mementoSequence != null) 
			sequence = readSequence(mementoSequence);	
		else {
			String string = memento.getString(TAG_STRING);
			
			if (string != null)			
				try {			
					sequence = KeySupport.parseSequence(string);
				} catch (IllegalArgumentException eIllegalArgument) {					
				}
		}

		// TODO remove after 2.1 ships (BEGIN)
		if (sequence == null) {
			mementoSequence = memento.getChild("keySequence"); //$NON-NLS-1$
		
			if (mementoSequence != null) 
				sequence = readSequence(mementoSequence);	
			else {
				String string = memento.getString("keyString"); //$NON-NLS-1$
			
				if (string != null)			
					try {			
						sequence = KeySupport.parseSequence(string);
					} catch (IllegalArgumentException eIllegalArgument) {					
					}
			}
		}
		// TODO remove after 2.1 ships (END)
		
		if (sequence == null)
			sequence = ZERO_LENGTH_SEQUENCE;

		String locale = memento.getString(TAG_LOCALE);
	
		if (locale == null)
			locale = Util.ZERO_LENGTH_STRING;

		String platform = memento.getString(TAG_PLATFORM);

		if (platform == null)
			platform = Util.ZERO_LENGTH_STRING;
		
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		String scope = memento.getString(TAG_SCOPE);

		if (scope == null)
			scope = Util.ZERO_LENGTH_STRING;

		return SequenceBinding.create(configuration, command, locale, platform, plugin, rank, scope, sequence);
	}

	static List readBindings(IMemento memento, String name, String pluginOverride, int rank)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readBinding(mementos[i], pluginOverride, rank));
	
		return list;				
	}

	static Configuration readConfiguration(IMemento memento, String pluginOverride)
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
		
		String parent = memento.getString(TAG_PARENT);
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return Configuration.create(description, id, name, parent, plugin);
	}

	static List readConfigurations(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readConfiguration(mementos[i], pluginOverride));
	
		return list;				
	}
	
	static Sequence readSequence(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren(TAG_STROKE);

		// TODO remove after 2.1 ships (BEGIN)
		if (mementos == null)
			mementos = memento.getChildren("keyStroke"); //$NON-NLS-1$
		// TODO remove after 2.1 ships (END)

		if (mementos == null)
			throw new IllegalArgumentException();
		
		List strokes = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			strokes.add(readStroke(mementos[i]));
		
		return Sequence.create(strokes);
	}

	static Stroke readStroke(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		Integer value = memento.getInteger(TAG_VALUE);
		
		if (value == null)
			value = ZERO;
		
		return Stroke.create(value.intValue());
	}

	static Scope readScope(IMemento memento, String pluginOverride)
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

	static void writeActiveConfiguration(IMemento memento, ActiveConfiguration activeConfiguration)
		throws IllegalArgumentException {
		if (memento == null || activeConfiguration == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_PLUGIN, activeConfiguration.getPlugin());
		memento.putString(TAG_VALUE, activeConfiguration.getValue());
	}

	static void writeActiveConfigurations(IMemento memento, String name, List activeConfigurations)
		throws IllegalArgumentException {
		if (memento == null || name == null || activeConfigurations == null)
			throw new IllegalArgumentException();
		
		activeConfigurations = new ArrayList(activeConfigurations);
		Iterator iterator = activeConfigurations.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof ActiveConfiguration))
				throw new IllegalArgumentException();

		iterator = activeConfigurations.iterator();

		while (iterator.hasNext()) 
			writeActiveConfiguration(memento.createChild(name), (ActiveConfiguration) iterator.next());
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
	
	static void writeBinding(IMemento memento, SequenceBinding sequenceBinding)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_COMMAND, sequenceBinding.getCommand());
		memento.putString(TAG_CONFIGURATION, sequenceBinding.getConfiguration());
		writeSequence(memento.createChild(TAG_SEQUENCE), sequenceBinding.getSequence());		
		memento.putString(TAG_LOCALE, sequenceBinding.getLocale());
		memento.putString(TAG_PLATFORM, sequenceBinding.getPlatform());
		memento.putString(TAG_PLUGIN, sequenceBinding.getPlugin());
		memento.putString(TAG_SCOPE, sequenceBinding.getScope());
	}	

	static void writeBindings(IMemento memento, String name, List sequenceBindings)
		throws IllegalArgumentException {
		if (memento == null || name == null || sequenceBindings == null)
			throw new IllegalArgumentException();
		
		sequenceBindings = new ArrayList(sequenceBindings);
		Iterator iterator = sequenceBindings.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof SequenceBinding))
				throw new IllegalArgumentException();

		iterator = sequenceBindings.iterator();

		while (iterator.hasNext()) 
			writeBinding(memento.createChild(name), (SequenceBinding) iterator.next());
	}

	static void writeConfiguration(IMemento memento, Configuration configuration)
		throws IllegalArgumentException {
		if (memento == null || configuration == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, configuration.getDescription());
		memento.putString(TAG_ID, configuration.getId());
		memento.putString(TAG_NAME, configuration.getName());
		memento.putString(TAG_PARENT, configuration.getParent());
		memento.putString(TAG_PLUGIN, configuration.getPlugin());
	}

	static void writeConfigurations(IMemento memento, String name, List configurations)
		throws IllegalArgumentException {
		if (memento == null || name == null || configurations == null)
			throw new IllegalArgumentException();
		
		configurations = new ArrayList(configurations);
		Iterator iterator = configurations.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof Configuration))
				throw new IllegalArgumentException();

		iterator = configurations.iterator();

		while (iterator.hasNext()) 
			writeConfiguration(memento.createChild(name), (Configuration) iterator.next());
	}

	static void writeSequence(IMemento memento, Sequence sequence)
		throws IllegalArgumentException {
		if (memento == null || sequence == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = sequence.getStrokes().iterator();

		while (iterator.hasNext())
			writeStroke(memento.createChild(TAG_STROKE), (Stroke) iterator.next());
	}

	static void writeStroke(IMemento memento, Stroke stroke)
		throws IllegalArgumentException {
		if (memento == null || stroke == null)
			throw new IllegalArgumentException();
			
		memento.putInteger(TAG_VALUE, stroke.getValue());
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
