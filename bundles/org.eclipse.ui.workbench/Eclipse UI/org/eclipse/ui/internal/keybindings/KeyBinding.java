/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import org.eclipse.ui.IMemento;

final class KeyBinding implements Comparable {

	final static String ELEMENT = "keybinding"; //$NON-NLS-1$
	private final static String ATTRIBUTE_ACTION = "action"; //$NON-NLS-1$
	private final static String ATTRIBUTE_CONFIGURATION = "configuration"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_LOCALE = "locale"; //$NON-NLS-1$	
	private final static String ATTRIBUTE_PLATFORM = "platform"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_PLUGIN = "plugin"; //$NON-NLS-1$		
	private final static String ATTRIBUTE_SCOPE = "scope"; //$NON-NLS-1$
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
				compareTo = configuration.compareTo(keyBinding.configuration);

				if (compareTo == 0) {
					compareTo = locale.compareTo(keyBinding.locale);

					if (compareTo == 0) {
						compareTo = platform.compareTo(keyBinding.platform);

						if (compareTo == 0) {
							compareTo = Util.compare(plugin, keyBinding.plugin);

							if (compareTo == 0)
								compareTo = scope.compareTo(keyBinding.scope);
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
			configuration.equals(keyBinding.configuration) && locale.equals(keyBinding.locale) && platform.equals(keyBinding.platform) && 
			Util.equals(plugin, keyBinding.plugin) && scope.equals(keyBinding.scope);
	}

	public int hashCode() {
		final int i0 = 42;
		final int i1 = 17;
		int result = i0;
		result = result * i1 + keySequence.hashCode();		
		result = result * i1 + Util.hashCode(action);		
		result = result * i1 + configuration.hashCode();		
		result = result * i1 + locale.hashCode();		
		result = result * i1 + platform.hashCode();		
		result = result * i1 + Util.hashCode(plugin);		
		result = result * i1 + scope.hashCode();		
		return result;
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
