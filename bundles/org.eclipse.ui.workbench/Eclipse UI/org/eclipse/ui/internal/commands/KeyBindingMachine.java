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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.ui.commands.IKeyBinding;
import org.eclipse.ui.contexts.IContextDefinition;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

public final class KeyBindingMachine {

	public static KeyBindingMachine create() {
		return new KeyBindingMachine();
	}

	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static Locale SYSTEM_LOCALE = Locale.getDefault();
	private final static String SYSTEM_PLATFORM = SWT.getPlatform();

	static SortedMap buildPathMapForKeyConfigurationMap(SortedMap keyConfigurationMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = keyConfigurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = getPathForKeyConfiguration(id, keyConfigurationMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	static SortedMap buildPathMapForContextMap(SortedMap contextMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = contextMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = getPathForContext(id, contextMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	static Path getPathForKeyConfiguration(String id, Map keyConfigurationMap) {
		Path path = null;

		if (id != null) {
			List strings = new ArrayList();

			while (id != null) {	
				if (strings.contains(id))
					return null;
							
				KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationMap.get(id);
				
				if (keyConfiguration == null)
					return null;
							
				strings.add(0, id);
				id = keyConfiguration.getParentId();
			}
		
			path = Path.getInstance(strings);
		}
		
		return path;			
	}

	static Path getPathForContext(String id, Map contextMap) {
		Path path = null;

		if (id != null) {
			List strings = new ArrayList();

			while (id != null) {	
				if (strings.contains(id))
					return null;
							
				IContextDefinition context = (IContextDefinition) contextMap.get(id);
				
				if (context == null)
					return null;
							
				strings.add(0, id);
				id = context.getParentId();
			}
		
			path = Path.getInstance(strings);
		}
		
		return path;			
	}	

	static Path getPathForLocale(String locale) {
		Path path = null;

		if (locale != null) {
			List strings = new ArrayList();				
			locale = locale.trim();
			
			if (locale.length() > 0) {
				StringTokenizer st = new StringTokenizer(locale, LOCALE_SEPARATOR);
						
				while (st.hasMoreElements()) {
					String string = ((String) st.nextElement()).trim();
					
					if (string != null)
						strings.add(string);
				}
			}

			path = Path.getInstance(strings);
		}
			
		return path;		
	}

	static Path getPathForPlatform(String platform) {
		Path path = null;

		if (platform != null) {
			List strings = new ArrayList();				
			platform = platform.trim();
			
			if (platform.length() > 0)
				strings.add(platform);

			path = Path.getInstance(strings);
		}
			
		return path;		
	}

	static Path getSystemLocale() {
		return SYSTEM_LOCALE != null ? getPathForLocale(SYSTEM_LOCALE.toString()) : null;
	}

	static Path getSystemPlatform() {
		return getPathForPlatform(SYSTEM_PLATFORM);
	}

	private Map commandMap;
	private Map commandMapForMode;	
	private SortedSet keyBindingSet;
	private String keyConfiguration;
	private SortedMap keyConfigurationMap;	
	private SortedMap keySequenceMap;
	private SortedMap keySequenceMapForMode;
	private KeySequence mode;	
	private SortedMap contextMap;
	private String[] contexts;
	private boolean solved;
	private SortedMap tree;

	private KeyBindingMachine() {
		super();
		keyConfigurationMap = new TreeMap();
		contextMap = new TreeMap();
		keyBindingSet = new TreeSet();
		keyConfiguration = Util.ZERO_LENGTH_STRING;
		contexts = new String[] { Util.ZERO_LENGTH_STRING };
		mode = KeySequence.getInstance();	
	}

	public Map getCommandMap() {
		if (commandMap == null) {
			solve();
			commandMap = Collections.unmodifiableMap(KeyBindingNode.toCommandMap(getKeySequenceMap()));				
		}
		
		return commandMap;
	}
	
	public Map getCommandMapForMode() {
		if (commandMapForMode == null) {
			solve();
			SortedMap tree = KeyBindingNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();

			commandMapForMode = Collections.unmodifiableMap(KeyBindingNode.toCommandMap(getKeySequenceMapForMode()));				
		}
		
		return commandMapForMode;
	}

	public SortedSet getKeyBindingSet() {
		return keyBindingSet;	
	}

	public String getKeyConfiguration() {
		return keyConfiguration;
	}		

	public SortedMap getKeyConfigurationMap() {
		return keyConfigurationMap;	
	}

	public KeySequence getFirstKeySequenceForCommand(String command)
		throws IllegalArgumentException {
		if (command == null)
			throw new IllegalArgumentException();					

		SortedSet keySequenceSet = (SortedSet) getCommandMap().get(command);
		
		if (keySequenceSet != null && !keySequenceSet.isEmpty())
			return (KeySequence) keySequenceSet.first();
		
		return null;
	}

	public SortedMap getKeySequenceMap() {
		if (keySequenceMap == null) {
			solve();
			keySequenceMap = Collections.unmodifiableSortedMap(KeyBindingNode.toKeySequenceMap(tree, KeySequence.getInstance()));				
		}
		
		return keySequenceMap;
	}

	public SortedMap getKeySequenceMapForMode() {
		if (keySequenceMapForMode == null) {
			solve();
			SortedMap tree = KeyBindingNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();
							
			keySequenceMapForMode = Collections.unmodifiableSortedMap(KeyBindingNode.toKeySequenceMap(tree, mode));				
		}
		
		return keySequenceMapForMode;
	}

	public KeySequence getMode() {
		return mode;	
	}	

	public SortedMap getContextMap() {
		return contextMap;	
	}	
	
	public String[] getContexts() {
		return (String[]) contexts.clone();
	}		

	public boolean setBindingSet(SortedSet keyBindingSet)
		throws IllegalArgumentException {
		if (keyBindingSet == null)
			throw new IllegalArgumentException();
		
		keyBindingSet = new TreeSet(keyBindingSet);
		Iterator iterator = keyBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof IKeyBinding))
				throw new IllegalArgumentException();

		if (this.keyBindingSet.equals(keyBindingSet))
			return false;
		
		this.keyBindingSet = Collections.unmodifiableSortedSet(keyBindingSet);
		invalidateTree();
		return true;
	}

	public boolean setKeyConfiguration(String keyConfiguration) {
		if (keyConfiguration == null)
			throw new IllegalArgumentException();
			
		if (this.keyConfiguration.equals(keyConfiguration))
			return false;
		
		this.keyConfiguration = keyConfiguration;
		invalidateSolution();
		return true;
	}

	public boolean setKeyConfigurationMap(SortedMap keyConfigurationMap)
		throws IllegalArgumentException {
		if (keyConfigurationMap == null)
			throw new IllegalArgumentException();
			
		keyConfigurationMap = new TreeMap(keyConfigurationMap);
		Iterator iterator = keyConfigurationMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Path))
				throw new IllegalArgumentException();			
		}

		if (this.keyConfigurationMap.equals(keyConfigurationMap))
			return false;
					
		this.keyConfigurationMap = Collections.unmodifiableSortedMap(keyConfigurationMap);
		invalidateTree();
		return true;
	}

	public boolean setMode(KeySequence mode)
		throws IllegalArgumentException {
		if (mode == null)
			throw new IllegalArgumentException();
			
		if (this.mode.equals(mode))
			return false;
		
		this.mode = mode;
		invalidateMode();
		return true;
	}
	
	public boolean setContextMap(SortedMap contextMap)
		throws IllegalArgumentException {
		if (contextMap == null)
			throw new IllegalArgumentException();
			
		contextMap = new TreeMap(contextMap);
		Iterator iterator = contextMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Path))
				throw new IllegalArgumentException();			
		}
		
		if (this.contextMap.equals(contextMap))
			return false;
				
		this.contextMap = Collections.unmodifiableSortedMap(contextMap);
		invalidateTree();
		return true;
	}	
	
	public boolean setContexts(String[] contexts)
		throws IllegalArgumentException {
		if (contexts == null || contexts.length == 0)
			throw new IllegalArgumentException();

		contexts = (String[]) contexts.clone();
		
		for (int i = 0; i < contexts.length; i++)
			if (contexts[i] == null)
				throw new IllegalArgumentException();	
		
		if (Arrays.equals(this.contexts, contexts))
			return false;
		
		this.contexts = contexts;
		invalidateSolution();
		return true;		
	}	

	private void build() {
		if (tree == null) {		
			tree = new TreeMap();
			Iterator iterator = keyBindingSet.iterator();
		
			while (iterator.hasNext()) {
				IKeyBinding keyBinding = (IKeyBinding) iterator.next();
				Path context = (Path) contextMap.get(keyBinding.getContextId());
		
				if (context == null)
					continue;

				Path keyConfiguration = (Path) keyConfigurationMap.get(keyBinding.getKeyConfigurationId());
					
				if (keyConfiguration == null)
					continue;

				List paths = new ArrayList();
				paths.add(context);
				paths.add(keyConfiguration);
				State contextKeyConfiguration = State.getInstance(paths);						
				paths = new ArrayList();
				paths.add(getPathForPlatform(keyBinding.getPlatform()));
				paths.add(getPathForLocale(keyBinding.getLocale()));
				State platformLocale = State.getInstance(paths);		
				KeyBindingNode.add(tree, keyBinding, contextKeyConfiguration, platformLocale);
			}
		}
	}

	private void invalidateMode() {
		commandMapForMode = null;
		keySequenceMapForMode = null;
	}

	private void invalidateSolution() {
		solved = false;
		commandMap = null;	
		keySequenceMap = null;
		invalidateMode();
	}
	
	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}
	
	private void solve() {
		if (!solved) {
			build();
			State[] contextKeyConfigurations = new State[contexts.length];
			Path keyConfiguration = (Path) keyConfigurationMap.get(this.keyConfiguration);
			
			if (keyConfiguration == null)
				keyConfiguration = Path.getInstance();
							
			for (int i = 0; i < contexts.length; i++) {
				Path context = (Path) contextMap.get(contexts[i]);
			
				if (context == null)
					context = Path.getInstance();

				List paths = new ArrayList();
				paths.add(context);
				paths.add(keyConfiguration);		
				contextKeyConfigurations[i] = State.getInstance(paths);
			}
			
			List paths = new ArrayList();
			paths.add(getSystemPlatform());
			paths.add(getSystemLocale());
			State platformLocale = State.getInstance(paths);			
			KeyBindingNode.solve(tree, contextKeyConfigurations, new State[] { platformLocale } );
			solved = true;
		}
	}
}
