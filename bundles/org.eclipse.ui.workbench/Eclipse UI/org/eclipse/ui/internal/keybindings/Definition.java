/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

public final class Definition implements Comparable {

	public final static String ELEMENT = "definition"; //$NON-NLS-1$
	private final static int HASH_INITIAL = 37;
	private final static int HASH_FACTOR = 47;
	private final static String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$	
	private final static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$
	private final static String ATTRIBUTE_ACTION = "action"; //$NON-NLS-1$
	private final static String ATTRIBUTE_PLUGIN = "plugin"; //$NON-NLS-1$		
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	public static Definition create(KeySequence keySequence, String configuration, String locale, String platform, String scope, String action, 
		String plugin)
		throws IllegalArgumentException {
		return new Definition(keySequence, configuration, locale, platform, scope, action, plugin);
	}

	public static Definition read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();
		
		KeySequence keySequence = KeySequence.read(memento.getChild(KeySequence.ELEMENT));
		String configuration = memento.getString(ATTRIBUTE_CONFIGURATION);
		
		if (configuration == null)
			configuration = ZERO_LENGTH_STRING;
		
		String locale = memento.getString(ATTRIBUTE_LOCALE);

		if (locale == null)
			locale = ZERO_LENGTH_STRING;

		String platform = memento.getString(ATTRIBUTE_PLATFORM);

		if (platform == null)
			platform = ZERO_LENGTH_STRING;

		String scope = memento.getString(ATTRIBUTE_SCOPE);

		if (scope == null)
			scope = ZERO_LENGTH_STRING;

		String action = memento.getString(ATTRIBUTE_ACTION);
		String plugin = memento.getString(ATTRIBUTE_PLUGIN);
		return Definition.create(keySequence, configuration, locale, platform, scope, action, plugin);
	}
	
	private KeySequence keySequence;
	private String configuration;
	private String locale;
	private String platform;	
	private String scope;
	private String action;
	private String plugin;

	private Definition(KeySequence keySequence, String configuration, String locale, String platform, String scope, String action, String plugin)
		throws IllegalArgumentException {
		super();
		
		if (keySequence == null || keySequence.getKeyStrokes().size() <= 0 || configuration == null || locale == null || platform == null || 
			scope == null)
			throw new IllegalArgumentException();	
		
		this.keySequence = keySequence;
		this.configuration = configuration;
		this.locale = locale;
		this.platform = platform;
		this.scope = scope;
		this.action = action;	
		this.plugin = plugin;
	}

	public KeySequence getKeySequence() {
		return keySequence;	
	}

	public String getConfiguration() {
		return configuration;
	}
	
	public String getLocale() {
		return locale;
	}
	
	public String getPlatform() {
		return platform;
	}

	public String getScope() {
		return scope;
	}
	
	public String getAction() {
		return action;
	}	

	public String getPlugin() {
		return plugin;
	}

	public void write(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		keySequence.write(memento.createChild(KeySequence.ELEMENT));
		memento.putString(ATTRIBUTE_CONFIGURATION, configuration);
		memento.putString(ATTRIBUTE_LOCALE, locale);
		memento.putString(ATTRIBUTE_PLATFORM, platform);
		memento.putString(ATTRIBUTE_SCOPE, scope);
		memento.putString(ATTRIBUTE_ACTION, action);
		memento.putString(ATTRIBUTE_PLUGIN, plugin);
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Definition))
			throw new ClassCastException();		
		
		Definition definition = (Definition) object;
		int compareTo = keySequence.compareTo(definition.keySequence);

		if (compareTo == 0) {
			compareTo = configuration.compareTo(definition.configuration);

			if (compareTo == 0) {
				compareTo = locale.compareTo(definition.locale);

				if (compareTo == 0) {
					compareTo = platform.compareTo(definition.platform);

					if (compareTo == 0) {
						compareTo = scope.compareTo(definition.scope);

						if (compareTo == 0) {
							compareTo = Util.compare(action, definition.action);

							if (compareTo == 0) 
								compareTo = Util.compare(plugin, definition.plugin);
						}
					}
				}
			}
		}
		
		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Definition))
			return false;
		
		Definition definition = (Definition) object;
		return keySequence.equals(definition.keySequence) && configuration.equals(definition.configuration) && locale.equals(definition.locale) && 
			platform.equals(definition.platform) && scope.equals(definition.scope) && Util.equals(action, definition.action) && 
			Util.equals(plugin, definition.plugin);
	}

	public int hashCode() {
		int result = HASH_INITIAL;
		result = result * HASH_FACTOR + keySequence.hashCode();		
		result = result * HASH_FACTOR + configuration.hashCode();		
		result = result * HASH_FACTOR + locale.hashCode();		
		result = result * HASH_FACTOR + platform.hashCode();		
		result = result * HASH_FACTOR + scope.hashCode();		
		result = result * HASH_FACTOR + Util.hashCode(action);		
		result = result * HASH_FACTOR + Util.hashCode(plugin);		
		return result;
	}
}
