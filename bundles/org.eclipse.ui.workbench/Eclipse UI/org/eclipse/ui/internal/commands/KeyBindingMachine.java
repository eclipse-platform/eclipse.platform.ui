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
import org.eclipse.ui.internal.commands.registry.IKeyBindingDefinition;
import org.eclipse.ui.internal.commands.registry.IKeyConfigurationDefinition;
import org.eclipse.ui.internal.contexts.registry.IContextDefinition;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

final class KeyBindingMachine {

	private final static String SEPARATOR = "_"; //$NON-NLS-1$

	private static SortedMap build(SortedSet[] keyBindings, SortedMap contextDefinitionMap, SortedMap keyConfigurationDefinitionMap) {
		SortedMap tree = new TreeMap();
		
		for (int i = 0; i < keyBindings.length; i++) {		
			Iterator iterator = keyBindings[i].iterator();
			
			while (iterator.hasNext()) {
				IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();
				Path context = (Path) contextDefinitionMap.get(keyBindingDefinition.getContextId());
			
				if (context == null)
					continue;
	
				Path keyConfiguration = (Path) keyConfigurationDefinitionMap.get(keyBindingDefinition.getKeyConfigurationId());
					
				if (keyConfiguration == null)
					continue;
	
				List paths = new ArrayList();
				paths.add(context);
				paths.add(keyConfiguration);
				State contextKeyConfiguration = new State(paths);						
				paths = new ArrayList();
				paths.add(getPath(keyBindingDefinition.getPlatform(), SEPARATOR));
				paths.add(getPath(keyBindingDefinition.getLocale(), SEPARATOR));
				State platformLocale = new State(paths);		
				KeyBindingNode.add(tree, keyBindingDefinition.getKeySequence(), contextKeyConfiguration, i, platformLocale, keyBindingDefinition.getCommandId());
			}
		}
		
		return tree;
	}

	private static SortedMap buildPathMapForKeyConfigurationDefinitionMap(SortedMap keyConfigurationDefinitionMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = keyConfigurationDefinitionMap.keySet().iterator();

		while (iterator.hasNext()) {
			String keyConfigurationDefinitionId = (String) iterator.next();
			
			if (keyConfigurationDefinitionId != null) {			
				Path path = getPathForKeyConfigurationDefinition(keyConfigurationDefinitionId, keyConfigurationDefinitionMap);
			
				if (path != null)
					pathMap.put(keyConfigurationDefinitionId, path);
			}			
		}

		return pathMap;		
	}

	private static SortedMap buildPathMapForContextDefinitionMap(SortedMap contextDefinitionMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = contextDefinitionMap.keySet().iterator();

		while (iterator.hasNext()) {
			String contextDefinitionId = (String) iterator.next();
			
			if (contextDefinitionId != null) {			
				Path path = getPathForContextDefinition(contextDefinitionId, contextDefinitionMap);
			
				if (path != null)
					pathMap.put(contextDefinitionId, path);
			}			
		}

		return pathMap;		
	}

	private static Path getPathForKeyConfigurationDefinition(String keyConfigurationDefinitionId, Map keyConfigurationDefinitionMap) {
		Path path = null;

		if (keyConfigurationDefinitionId != null) {
			List strings = new ArrayList();

			while (keyConfigurationDefinitionId != null) {	
				if (strings.contains(keyConfigurationDefinitionId))
					return null;
					
				IKeyConfigurationDefinition keyConfigurationDefinition = (IKeyConfigurationDefinition) keyConfigurationDefinitionMap.get(keyConfigurationDefinitionId);
				
				if (keyConfigurationDefinition == null)
					return null;
							
				strings.add(0, keyConfigurationDefinitionId);
				keyConfigurationDefinitionId = keyConfigurationDefinition.getParentId();
			}
		
			path = new Path(strings);
		}
		
		return path;			
	}

	private static Path getPathForContextDefinition(String contextDefinitionId, Map contextDefinitionMap) {
		Path path = null;

		if (contextDefinitionId != null) {
			List strings = new ArrayList();

			while (contextDefinitionId != null) {	
				if (strings.contains(contextDefinitionId))
					return null;
							
				IContextDefinition contextDefinition = (IContextDefinition) contextDefinitionMap.get(contextDefinitionId);
				
				if (contextDefinition == null)
					return null;
							
				strings.add(0, contextDefinitionId);
				contextDefinitionId = contextDefinition.getParentId();
			}
		
			path = new Path(strings);
		}
		
		return path;			
	}	

	private static Path getPath(String locale, String separator) {
		Path path = null;

		if (locale != null && separator != null) {
			List strings = new ArrayList();				
			locale = locale.trim();
			
			if (locale.length() > 0) {
				StringTokenizer st = new StringTokenizer(locale, separator);
						
				while (st.hasMoreElements()) {
					String string = ((String) st.nextElement()).trim();
					
					if (string != null)
						strings.add(string);
				}
			}

			path = new Path(strings);
		}
			
		return path;		
	}

	private static void solve(String[] activeContextIds, String activeKeyConfigurationId, String activeLocale, String activePlatform, SortedMap contextDefinitionMap, SortedMap keyConfigurationDefinitionMap, SortedMap tree) {
		State[] contextKeyConfigurations = new State[activeContextIds.length];
		Path keyConfiguration = (Path) keyConfigurationDefinitionMap.get(activeKeyConfigurationId);
			
		if (keyConfiguration == null)
			keyConfiguration = new Path();
							
		for (int i = 0; i < activeContextIds.length; i++) {
			Path context = (Path) contextDefinitionMap.get(activeContextIds[i]);
			
			if (context == null)
				context = new Path();

			List paths = new ArrayList();
			paths.add(context);
			paths.add(keyConfiguration);		
			contextKeyConfigurations[i] = new State(paths);
		}

		List paths = new ArrayList();
		paths.add(getPath(activePlatform, SEPARATOR));
		paths.add(getPath(activeLocale, SEPARATOR));
		State platformLocale = new State(paths);	
		KeyBindingNode.solve(tree, contextKeyConfigurations, new State[] { platformLocale } );
	}

	private String[] activeContextIds;
	private String activeKeyConfigurationId;
	private String activeLocale;
	private String activePlatform;
	private Map commandMap;
	private Map commandMapForMode;	
	private SortedSet[] keyBindings;
	private SortedMap keyConfigurationDefinitionMap;	
	private SortedMap keySequenceMap;
	private SortedMap keySequenceMapForMode;
	private KeySequence mode;	
	private SortedMap contextDefinitionMap;
	private boolean solved;
	private SortedMap tree;

	KeyBindingMachine() {
		activeContextIds = new String[] { Util.ZERO_LENGTH_STRING };
		activeKeyConfigurationId = Util.ZERO_LENGTH_STRING;
		String systemLocale = Locale.getDefault().toString();
		activeLocale = systemLocale != null ? systemLocale : Util.ZERO_LENGTH_STRING;
		String systemPlatform = SWT.getPlatform();
		activePlatform = systemPlatform != null ? systemPlatform : Util.ZERO_LENGTH_STRING;
		contextDefinitionMap = new TreeMap();	
		keyBindings = new SortedSet[] { new TreeSet(), new TreeSet() };
		keyConfigurationDefinitionMap = new TreeMap();		
		mode = KeySequence.getInstance();	
	}

	String[] getActiveContextIds() {
		return (String[]) activeContextIds.clone();
	}		

	String getActiveKeyConfigurationId() {
		return activeKeyConfigurationId;
	}		
	
	String getActiveLocale() {
		return activeLocale;
	}

	String getActivePlatform() {
		return activePlatform;
	}

	Map getCommandMap() {
		if (commandMap == null) {
			solve();
			commandMap = Collections.unmodifiableMap(KeyBindingNode.toCommandMap(getKeySequenceMap()));				
		}
		
		return commandMap;
	}
	
	Map getCommandMapForMode() {
		if (commandMapForMode == null) {
			solve();
			SortedMap tree = KeyBindingNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();

			commandMapForMode = Collections.unmodifiableMap(KeyBindingNode.toCommandMap(getKeySequenceMapForMode()));				
		}
		
		return commandMapForMode;
	}

	SortedMap getContextDefinitionMap() {
		return contextDefinitionMap;	
	}

	KeySequence getFirstKeySequenceForCommand(String commandId)
		throws IllegalArgumentException {
		if (commandId == null)
			throw new IllegalArgumentException();					

		SortedSet keySequenceSet = (SortedSet) getCommandMap().get(commandId);
		
		if (keySequenceSet != null && !keySequenceSet.isEmpty())
			return (KeySequence) keySequenceSet.first();
		
		return null;
	}

	SortedSet getKeyBindings0() {
		return keyBindings[0];	
	}

	SortedSet getKeyBindings1() {
		return keyBindings[1];	
	}

	SortedMap getKeyConfigurationDefinitionMap() {
		return keyConfigurationDefinitionMap;	
	}

	SortedMap getKeySequenceMap() {
		if (keySequenceMap == null) {
			solve();
			keySequenceMap = Collections.unmodifiableSortedMap(KeyBindingNode.toKeySequenceMap(tree, KeySequence.getInstance()));				
		}
		
		return keySequenceMap;
	}

	SortedMap getKeySequenceMapForMode() {
		if (keySequenceMapForMode == null) {
			solve();
			SortedMap tree = KeyBindingNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();
							
			keySequenceMapForMode = Collections.unmodifiableSortedMap(KeyBindingNode.toKeySequenceMap(tree, mode));				
		}
		
		return keySequenceMapForMode;
	}

	KeySequence getMode() {
		return mode;	
	}
	
	boolean setActiveContextIds(String[] activeContextIds) {
		if (activeContextIds == null || activeContextIds.length == 0)
			throw new NullPointerException();

		activeContextIds = (String[]) activeContextIds.clone();
		
		for (int i = 0; i < activeContextIds.length; i++)
			if (activeContextIds[i] == null)
				throw new IllegalArgumentException();	
		
		if (!Arrays.equals(this.activeContextIds, activeContextIds)) {
			this.activeContextIds = activeContextIds;
			invalidateSolution();
			return true;		
		}
			
		return false;		
	}

	boolean setActiveKeyConfigurationId(String activeKeyConfigurationId) {
		if (activeKeyConfigurationId == null)
			throw new NullPointerException();
			
		if (!this.activeKeyConfigurationId.equals(activeKeyConfigurationId)) {
			this.activeKeyConfigurationId = activeKeyConfigurationId;
			invalidateSolution();
			return true;
		}
		
		return false;		
	}
	
	boolean setActiveLocale(String activeLocale) {
		if (activeLocale == null)
			throw new NullPointerException();
			
		if (!this.activeLocale.equals(activeLocale)) {
			this.activeLocale = activeLocale;
			invalidateSolution();
			return true;
		}
		
		return false;		
	}

	boolean setActivePlatform(String activePlatform) {
		if (activePlatform == null)
			throw new NullPointerException();
			
		if (!this.activePlatform.equals(activePlatform)) {
			this.activePlatform = activePlatform;
			invalidateSolution();
			return true;
		}
		
		return false;	
	}	

	boolean setContextDefinitionMap(SortedMap contextDefinitionMap) {
		contextDefinitionMap = Util.safeCopy(contextDefinitionMap, String.class, Path.class);
			
		if (!this.contextDefinitionMap.equals(contextDefinitionMap)) {
			this.contextDefinitionMap = contextDefinitionMap;
			invalidateTree();
			return true;
		}
			
		return false;
	}

	boolean setKeyBindings0(SortedSet keyBindings0) {
		keyBindings0 = Util.safeCopy(keyBindings0, IKeyBindingDefinition.class);
		
		if (!this.keyBindings[0].equals(keyBindings0)) {
			this.keyBindings[0] = keyBindings0;
			invalidateTree();
			return true;
		}			
			
		return false;		
	}

	boolean setKeyBindings1(SortedSet keyBindings1) {
		keyBindings1 = Util.safeCopy(keyBindings1, IKeyBindingDefinition.class);
		
		if (!this.keyBindings[1].equals(keyBindings1)) {
			this.keyBindings[1] = keyBindings1;
			invalidateTree();
			return true;
		}			
			
		return false;		
	}

	boolean setKeyConfigurationDefinitionMap(SortedMap keyConfigurationDefinitionMap) {
		keyConfigurationDefinitionMap = Util.safeCopy(keyConfigurationDefinitionMap, String.class, Path.class);

		if (!this.keyConfigurationDefinitionMap.equals(keyConfigurationDefinitionMap)) {
			this.keyConfigurationDefinitionMap = keyConfigurationDefinitionMap;
			invalidateTree();
			return true;
		}

		return false;
	}

	boolean setMode(KeySequence mode) {
		if (mode == null)
			throw new NullPointerException();
			
		if (!this.mode.equals(mode)) {
			this.mode = mode;
			invalidateMode();
			return true;
		}

		return false;		
	}
	
	private void build() {
		if (tree == null)
			tree = build(keyBindings, contextDefinitionMap, keyConfigurationDefinitionMap);
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
			solve(activeContextIds, activeKeyConfigurationId, activeLocale, activePlatform, contextDefinitionMap, keyConfigurationDefinitionMap, tree);
			solved = true;
		}
	}
}
