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
	final static String TAG_ICON = "icon"; //$NON-NLS-1$
	final static String TAG_ID = "id"; //$NON-NLS-1$
	final static String TAG_KEY_BINDING = "keyBinding"; //$NON-NLS-1$
	final static String TAG_KEY_CONFIGURATION = "keyConfiguration"; //$NON-NLS-1$
	final static String TAG_KEY_SEQUENCE = "keySequence"; //$NON-NLS-1$
	final static String TAG_KEY_STRING = "keyString"; //$NON-NLS-1$
	final static String TAG_KEY_STROKE = "keyStroke"; //$NON-NLS-1$
	final static String TAG_LOCALE = "locale"; //$NON-NLS-1$		
	final static String TAG_NAME = "name"; //$NON-NLS-1$
	final static String TAG_PACKAGE = "org.eclipse.ui.commands"; //$NON-NLS-1$
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

	static String readActiveGestureConfiguration(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		return memento.getString(TAG_ID);
	}

	static String readActiveKeyConfiguration(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		return memento.getString(TAG_ID);
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

	static Item readItem(IMemento memento, String pluginOverride)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			

		String description = memento.getString(TAG_DESCRIPTION);
		String icon = memento.getString(TAG_ICON);
		String id = memento.getString(TAG_ID);

		if (id == null)
			id = ZERO_LENGTH_STRING;
		
		String name = memento.getString(TAG_NAME);

		if (name == null)
			name = ZERO_LENGTH_STRING;
		
		String parent = memento.getString(TAG_PARENT);		
		String plugin = pluginOverride != null ? pluginOverride : memento.getString(TAG_PLUGIN);
		return Item.create(description, icon, id, name, parent, plugin);
	}

	static List readItems(IMemento memento, String name, String pluginOverride)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readItem(mementos[i], pluginOverride));
	
		return list;				
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

	static void writeItem(IMemento memento, Item item)
		throws IllegalArgumentException {
		if (memento == null || item == null)
			throw new IllegalArgumentException();

		memento.putString(TAG_DESCRIPTION, item.getDescription());
		memento.putString(TAG_ICON, item.getIcon());
		memento.putString(TAG_ID, item.getId());
		memento.putString(TAG_NAME, item.getName());
		memento.putString(TAG_PARENT, item.getParent());
		memento.putString(TAG_PLUGIN, item.getPlugin());
	}

	static void writeItems(IMemento memento, String name, List items)
		throws IllegalArgumentException {
		if (memento == null || name == null || items == null)
			throw new IllegalArgumentException();
		
		items = new ArrayList(items);
		Iterator iterator = items.iterator();
		
		while (iterator.hasNext()) 
			if (!(iterator.next() instanceof Item))
				throw new IllegalArgumentException();

		iterator = items.iterator();

		while (iterator.hasNext()) 
			writeItem(memento.createChild(name), (Item) iterator.next());
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

	private Persistence() {
		super();
	}	
}
