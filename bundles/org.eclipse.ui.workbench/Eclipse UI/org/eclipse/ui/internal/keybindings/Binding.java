/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

final class Binding implements Comparable {

	private final static int HASH_INITIAL = 17;
	private final static int HASH_FACTOR = 27;

	static Binding create(KeySequence keySequence, Path configuration, Path locale, Path platform, Path scope, String action, String plugin)
		throws IllegalArgumentException {
		return new Binding(keySequence, configuration, locale, platform, scope, action, plugin);
	}

	static void filterConfiguration(List bindings, Set configurationSet, boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();
			
			if (exclusive ^ !configurationSet.contains(binding.getConfiguration()))
				iterator.remove();
		}
	}

	static void filterLocale(List bindings, Set localeSet, boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();
			
			if (exclusive ^ !localeSet.contains(binding.getLocale()))
				iterator.remove();
		}
	}

	static void filterPlatform(List bindings, Set platformSet, boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();
			
			if (exclusive ^ !platformSet.contains(binding.getPlatform()))
				iterator.remove();
		}
	}

	static void filterScope(List bindings, Set scopeSet, boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();
			
			if (exclusive ^ !scopeSet.contains(binding.getScope()))
				iterator.remove();
		}
	}

	static void filterAction(List bindings, Set actionSet, boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();
			
			if (exclusive ^ !actionSet.contains(binding.getAction()))
				iterator.remove();
		}
	}

	static void filterPlugin(List bindings, Set pluginSet, boolean exclusive) {
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext()) {
			Binding binding = (Binding) iterator.next();
			
			if (exclusive ^ !pluginSet.contains(binding.getPlugin()))
				iterator.remove();
		}
	}

	private KeySequence keySequence;
	private Path configuration;
	private Path locale;
	private Path platform;	
	private Path scope;
	private String action;
	private String plugin;

	private Binding(KeySequence keySequence, Path configuration, Path locale, Path platform, Path scope, String action, String plugin)
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

	KeySequence getKeySequence() {
		return keySequence;	
	}

	Path getConfiguration() {
		return configuration;
	}
	
	Path getLocale() {
		return locale;
	}
	
	Path getPlatform() {
		return platform;
	}

	Path getScope() {
		return scope;
	}

	String getAction() {
		return action;
	}

	String getPlugin() {
		return plugin;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof Binding))
			throw new ClassCastException();		
		
		Binding binding = (Binding) object;
		int compareTo = keySequence.compareTo(binding.keySequence);

		if (compareTo == 0) {
			compareTo = configuration.compareTo(binding.configuration);

			if (compareTo == 0) {
				compareTo = locale.compareTo(binding.locale);

				if (compareTo == 0) {
					compareTo = platform.compareTo(binding.platform);

					if (compareTo == 0) {
						compareTo = scope.compareTo(binding.scope);

						if (compareTo == 0) {
							compareTo = Util.compare(action, binding.action);

							if (compareTo == 0)
								compareTo = Util.compare(plugin, binding.plugin);
						}
					}
				}
			}
		}
		
		return compareTo;
	}
	
	public boolean equals(Object object) {
		if (!(object instanceof Binding))
			return false;
		
		Binding binding = (Binding) object;
		return keySequence.equals(binding.keySequence) && configuration.equals(binding.configuration) && locale.equals(binding.locale) && 
			platform.equals(binding.platform) && scope.equals(binding.scope) && Util.equals(action, binding.action) && 
			Util.equals(plugin, binding.plugin);
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
