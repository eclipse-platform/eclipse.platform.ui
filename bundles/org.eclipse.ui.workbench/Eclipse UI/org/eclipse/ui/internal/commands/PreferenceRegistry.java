/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

public final class PreferenceRegistry {

	private final static String DEPRECATED_TAG_BINDING = "binding"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_BINDINGS = "bindings"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_ACCELERATOR = "accelerator"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_ACTION = "action"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_CONFIGURATION = "configuration"; //$NON-NLS-1$		
	private final static String DEPRECATED_TAG_KEY = "org.eclipse.ui.keybindings"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_KEY_SEQUENCE = "keysequence"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_KEY_STROKE = "keystroke"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_PLUGIN = "plugin"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_RANK = "rank"; //$NON-NLS-1$
	private final static String DEPRECATED_TAG_SCOPE = "scope"; //$NON-NLS-1$
	private final static String KEY = Persistence.TAG_PACKAGE;

	public static PreferenceRegistry instance;
	
	public static PreferenceRegistry getInstance() {
		if (instance == null)
			instance = new PreferenceRegistry();
	
		return instance;
	}

	private String activeGestureConfiguration;
	private String activeKeyConfiguration;
	private List commands = Collections.EMPTY_LIST; 
	private List gestureBindings = Collections.EMPTY_LIST;
	private List gestureConfigurations = Collections.EMPTY_LIST;
	private List groups = Collections.EMPTY_LIST; 
	private List keyBindings = Collections.EMPTY_LIST;
	private List keyConfigurations = Collections.EMPTY_LIST;
	private List scopes = Collections.EMPTY_LIST; 

	private PreferenceRegistry() {
		super();
	}

	public String getActiveGestureConfiguration() {
		return activeGestureConfiguration;
	}

	public String getActiveKeyConfiguration() {
		return activeKeyConfiguration;
	}

	public List getCommands() {
		return commands;
	}

	public List getGestureBindings() {
		return gestureBindings;
	}

	public List getGestureConfigurations() {
		return gestureConfigurations;
	}
	
	public List getGroups() {
		return groups;
	}

	public List getKeyBindings() {
		return keyBindings;
	}

	public List getKeyConfigurations() {
		return keyConfigurations;
	}

	public List getScopes() {
		return scopes;
	}

	public void load() 
		throws IOException {
		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		String deprecatedPreferenceString = preferenceStore.getString(DEPRECATED_TAG_KEY);
		List deprecatedKeyBindings = Collections.EMPTY_LIST;

		if (deprecatedPreferenceString != null && deprecatedPreferenceString.length() != 0) {
			Reader reader = new BufferedReader(new StringReader(deprecatedPreferenceString));
		
			try {
				IMemento memento = XMLMemento.createReadRoot(reader);
				IMemento mementoKeyBindings = memento.getChild(DEPRECATED_TAG_BINDINGS);
	
				if (mementoKeyBindings != null)
					deprecatedKeyBindings = Collections.unmodifiableList(readDeprecatedKeyBindings(mementoKeyBindings, DEPRECATED_TAG_BINDING));
			} catch (WorkbenchException eWorkbench) {
				throw new IOException();
			} finally {
				reader.close();
			}
		}	
		
		String preferenceString = preferenceStore.getString(KEY);

		//if (preferenceString == null || preferenceString.length() == 0)
		//	throw new IOException();
		
		if (preferenceString != null && preferenceString.length() != 0) {
			Reader reader = new StringReader(preferenceString);
			
			try {
				IMemento memento = XMLMemento.createReadRoot(reader);
				activeGestureConfiguration = Persistence.readActiveGestureConfiguration(memento);
				activeKeyConfiguration = Persistence.readActiveKeyConfiguration(memento);
				commands = Collections.unmodifiableList(Persistence.readItems(memento, Persistence.TAG_COMMAND, null));
				gestureBindings = Collections.unmodifiableList(Persistence.readGestureBindings(memento, Persistence.TAG_GESTURE_BINDING, null));
				gestureConfigurations = Collections.unmodifiableList(Persistence.readItems(memento, Persistence.TAG_GESTURE_CONFIGURATION, null));
				groups = Collections.unmodifiableList(Persistence.readItems(memento, Persistence.TAG_GROUP, null));
				keyBindings = Collections.unmodifiableList(Persistence.readKeyBindings(memento, Persistence.TAG_KEY_BINDING, null));
				keyConfigurations = Collections.unmodifiableList(Persistence.readItems(memento, Persistence.TAG_KEY_CONFIGURATION, null));
				scopes = Collections.unmodifiableList(Persistence.readItems(memento, Persistence.TAG_SCOPE, null));
			} catch (WorkbenchException eWorkbench) {
				throw new IOException();
			} finally {
				reader.close();
			}
		}
	
		List keyBindings = new ArrayList();
		keyBindings.addAll(deprecatedKeyBindings);
		keyBindings.addAll(this.keyBindings);
		this.keyBindings = Collections.unmodifiableList(keyBindings);
	}
	
	public void save()
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(Persistence.TAG_PACKAGE);		
		Persistence.writeItems(xmlMemento, Persistence.TAG_COMMAND, commands);
		Persistence.writeGestureBindings(xmlMemento, Persistence.TAG_GESTURE_BINDING, gestureBindings);
		Persistence.writeItems(xmlMemento, Persistence.TAG_GESTURE_CONFIGURATION, gestureConfigurations);
		Persistence.writeItems(xmlMemento, Persistence.TAG_GROUP, groups);		
		Persistence.writeKeyBindings(xmlMemento, Persistence.TAG_KEY_BINDING, keyBindings);
		Persistence.writeItems(xmlMemento, Persistence.TAG_KEY_CONFIGURATION, keyConfigurations);
		Persistence.writeItems(xmlMemento, Persistence.TAG_SCOPE, scopes);
		Writer writer = new StringWriter();

		try {
			xmlMemento.save(writer);
			IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
			preferenceStore.setValue(KEY, writer.toString());					
		} finally {
			writer.close();
		}
	}

	public void setActiveGestureConfiguration(String activeGestureConfiguration) {
		this.activeGestureConfiguration = activeGestureConfiguration;
	}

	public void setActiveKeyConfiguration(String activeKeyConfiguration) {
		this.activeKeyConfiguration = activeKeyConfiguration;
	}

	public void setCommands(List commands)
		throws IllegalArgumentException {
		this.commands = Util.safeCopy(commands, Item.class);	
	}

	public void setGestureBindings(List gestureBindings)
		throws IllegalArgumentException {
		this.gestureBindings = Util.safeCopy(gestureBindings, GestureBinding.class);	
	}

	public void setGestureConfigurations(List gestureConfigurations)
		throws IllegalArgumentException {
		this.gestureConfigurations = Util.safeCopy(gestureConfigurations, Item.class);	
	}

	public void setGroups(List groups)
		throws IllegalArgumentException {
		this.groups = Util.safeCopy(groups, Item.class);	
	}

	public void setKeyBindings(List keyBindings)
		throws IllegalArgumentException {
		this.keyBindings = Util.safeCopy(keyBindings, KeyBinding.class);	
	}

	public void setKeyConfigurations(List keyConfigurations)
		throws IllegalArgumentException {
		this.keyConfigurations = Util.safeCopy(keyConfigurations, Item.class);		
	}

	public void setScopes(List scopes)
		throws IllegalArgumentException {
		this.scopes = Util.safeCopy(scopes, Item.class);;		
	}

	private static KeyBinding readDeprecatedKeyBinding(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		String command = memento.getString(DEPRECATED_TAG_ACTION);
		String keyConfiguration = memento.getString(DEPRECATED_TAG_CONFIGURATION);
		
		if (keyConfiguration == null)
			keyConfiguration = Persistence.ZERO_LENGTH_STRING;

		KeySequence keySequence = null;
		IMemento mementoKeySequence = memento.getChild(DEPRECATED_TAG_KEY_SEQUENCE);
		
		if (mementoKeySequence != null) 
			keySequence = readDeprecatedKeySequence(mementoKeySequence);	

		if (keySequence == null)
			keySequence = Persistence.ZERO_LENGTH_KEY_SEQUENCE;
		
		String plugin = memento.getString(DEPRECATED_TAG_PLUGIN);
		Integer rank = memento.getInteger(DEPRECATED_TAG_RANK);
		
		if (rank == null)
			rank = Persistence.ZERO;	
		
		String scope = memento.getString(DEPRECATED_TAG_SCOPE);

		if (scope == null)
			scope = Persistence.ZERO_LENGTH_STRING;

		return KeyBinding.create(command, keyConfiguration, keySequence, plugin, rank.intValue(), scope);
	}

	private static List readDeprecatedKeyBindings(IMemento memento, String name)
		throws IllegalArgumentException {		
		if (memento == null || name == null)
			throw new IllegalArgumentException();			
	
		IMemento[] mementos = memento.getChildren(name);
	
		if (mementos == null)
			throw new IllegalArgumentException();
	
		List list = new ArrayList(mementos.length);
	
		for (int i = 0; i < mementos.length; i++)
			list.add(readDeprecatedKeyBinding(mementos[i]));
	
		return list;				
	}
	
	private static KeySequence readDeprecatedKeySequence(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
			
		IMemento[] mementos = memento.getChildren(DEPRECATED_TAG_KEY_STROKE);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List keyStrokes = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			keyStrokes.add(readDeprecatedKeyStroke(mementos[i]));
		
		return KeySequence.create(keyStrokes);
	}

	private static KeyStroke readDeprecatedKeyStroke(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		Integer value = memento.getInteger(DEPRECATED_TAG_ACCELERATOR);
		
		if (value == null)
			value = Persistence.ZERO;
		
		return KeyStroke.create(value.intValue());
	}
}
