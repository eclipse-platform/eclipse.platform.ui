/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

final class KeyBinding implements Comparable {

	private final static String ATTRIBUTE_ACTION = "action"; //$NON-NLS-1$
	private final static String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$	
	private final static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_PLUGIN = "plugin"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$
	private final static String ELEMENT_KEYBINDING = "keybinding"; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	static KeyBinding create(KeySequence keySequence, String action, String configuration, String locale, String platform, String plugin, 
		String scope)
		throws IllegalArgumentException {
		return new KeyBinding(keySequence, action, configuration, locale, platform, plugin, scope);
	}

	static KeyBinding read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		KeySequence keySequence = KeySequence.read(memento.getChild(KeySequence.ELEMENT));
		String action = memento.getString(ATTRIBUTE_ACTION);
		String configuration = memento.getString(ATTRIBUTE_CONFIGURATION);
		
		if (configuration == null)
			configuration = ZERO_LENGTH_STRING;
		
		String locale = memento.getString(ATTRIBUTE_LOCALE);

		if (locale == null)
			locale = ZERO_LENGTH_STRING;

		String platform = memento.getString(ATTRIBUTE_PLATFORM);

		if (platform == null)
			platform = ZERO_LENGTH_STRING;

		String plugin = memento.getString(ATTRIBUTE_PLUGIN);
		String scope = memento.getString(ATTRIBUTE_SCOPE);

		if (scope == null)
			scope = ZERO_LENGTH_STRING;

		return KeyBinding.create(keySequence, action, configuration, locale, platform, plugin, scope);
	}

	static List readKeyBindingsFromReader(Reader reader)
		throws IOException {
		try {
			XMLMemento xmlMemento = XMLMemento.createReadRoot(reader);
			return readKeyBindings(xmlMemento);
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();	
		}
	}
	
	static List readKeyBindings(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			
		
		IMemento[] mementos = memento.getChildren(ELEMENT_KEYBINDING);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List keyBindings = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			keyBindings.add(KeyBinding.read(mementos[i]));
		
		return keyBindings;		
	}

	static void writeKeyBindingsToWriter(Writer writer, String root, List keyBindings)
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(root);
		writeKeyBindings(xmlMemento, keyBindings);
		xmlMemento.save(writer);
	}

	static void writeKeyBindings(IMemento memento, List keyBindings)
		throws IllegalArgumentException {
		if (memento == null || keyBindings == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext())
			((KeyBinding) iterator.next()).write(memento.createChild(ELEMENT_KEYBINDING)); 
	}

	static void filterAction(List keyBindings, Set actions, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !actions.contains(keyBinding.getAction()))
				iterator.remove();
		}
	}

	static void filterConfiguration(List keyBindings, Set configurations, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !configurations.contains(keyBinding.getConfiguration()))
				iterator.remove();
		}
	}

	static void filterLocale(List keyBindings, Set locales, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !locales.contains(keyBinding.getLocale()))
				iterator.remove();
		}
	}

	static void filterPlatform(List keyBindings, Set platforms, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !platforms.contains(keyBinding.getPlatform()))
				iterator.remove();
		}
	}

	static void filterPlugin(List keyBindings, Set plugins, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !plugins.contains(keyBinding.getPlugin()))
				iterator.remove();
		}
	}

	static void filterScope(List keyBindings, Set scopes, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !scopes.contains(keyBinding.getScope()))
				iterator.remove();
		}
	}

	private KeySequence keySequence;
	private String action;
	private String configuration;
	private String locale;
	private String platform;	
	private String scope;
	private String plugin;

	private KeyBinding(KeySequence keySequence, String action, String configuration, String locale, String platform, String plugin, String scope)
		throws IllegalArgumentException {
		super();
		
		if (keySequence == null || keySequence.getKeyStrokes().size() <= 0 || configuration == null || locale == null || platform == null || 
			scope == null)
			throw new IllegalArgumentException();	
		
		this.keySequence = keySequence;
		this.action = action;	
		this.configuration = configuration;
		this.locale = locale;
		this.platform = platform;
		this.plugin = plugin;
		this.scope = scope;
	}

	KeySequence getKeySequence() {
		return keySequence;	
	}

	String getAction() {
		return action;
	}

	String getConfiguration() {
		return configuration;
	}
	
	String getLocale() {
		return locale;
	}
	
	String getPlatform() {
		return platform;
	}

	String getPlugin() {
		return plugin;
	}
	
	String getScope() {
		return scope;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof KeyBinding))
			throw new ClassCastException();		
		
		KeyBinding keyBinding = (KeyBinding) object;
		int compareTo = keySequence.compareTo(keyBinding.keySequence);

		if (compareTo == 0) {
			compareTo = Util.compare(action, keyBinding.action);

			if (compareTo == 0) {
				compareTo = Util.compare(configuration, keyBinding.configuration);

				if (compareTo == 0) {
					compareTo = Util.compare(locale, keyBinding.locale);

					if (compareTo == 0) {
						compareTo = Util.compare(platform, keyBinding.platform);

						if (compareTo == 0) {
							compareTo = Util.compare(plugin, keyBinding.plugin);

							if (compareTo == 0)
								compareTo = Util.compare(scope, keyBinding.scope);
						}
					}
				}
			}
		}
		
		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof KeyBinding))
			return false;
		
		KeyBinding keyBinding = (KeyBinding) object;
		return keySequence.equals(keyBinding.keySequence) && Util.equals(action, keyBinding.action) && 
			Util.equals(configuration, keyBinding.configuration) && Util.equals(locale, keyBinding.locale) &&
			Util.equals(platform, keyBinding.platform) && Util.equals(plugin, keyBinding.plugin) && Util.equals(scope, keyBinding.scope);
	}

	public int hashCode() {
		// TBD!
		return 0;
	}

	void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		keySequence.write(memento.createChild(KeySequence.ELEMENT));
		memento.putString(ATTRIBUTE_ACTION, action);
		memento.putString(ATTRIBUTE_CONFIGURATION, configuration);
		memento.putString(ATTRIBUTE_LOCALE, locale);
		memento.putString(ATTRIBUTE_PLATFORM, platform);
		memento.putString(ATTRIBUTE_PLUGIN, plugin);
		memento.putString(ATTRIBUTE_SCOPE, scope);
	}
}
