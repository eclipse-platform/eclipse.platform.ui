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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public final class Machine {

	public static Machine create() {
		return new Machine();
	}

	private Map commandMap;
	private Map commandMapForMode;
	
	private SortedSet keyBindingSet;
	private String keyConfiguration;
	private SortedMap keyConfigurationMap;
	
	private SortedMap keySequenceMap;
	private SortedMap keySequenceMapForMode;

	private KeySequence mode;
	
	private SortedMap scopeMap;
	private String[] scopes;
	private boolean solved;
	private SortedMap tree;

	private Machine() {
		super();
		keyConfigurationMap = new TreeMap();
		scopeMap = new TreeMap();
		keyBindingSet = new TreeSet();		
		keyConfiguration = "org.eclipse.ui.defaultConfiguration"; //$NON-NLS-1$
		scopes = new String[] { "org.eclipse.ui.globalScope" }; //$NON-NLS-1$
		mode = KeySequence.create();	
	}

	public Map getCommandMap() {
		if (commandMap == null) {
			solve();
			commandMap = Collections.unmodifiableMap(KeyNode.toCommandMap(getKeySequenceMap()));				
		}
		
		return commandMap;
	}
	
	public Map getCommandMapForMode() {
		if (commandMapForMode == null) {
			solve();
			SortedMap tree = KeyNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();

			commandMapForMode = Collections.unmodifiableMap(KeyNode.toCommandMap(getKeySequenceMapForMode()));				
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

	public SortedMap getKeySequenceMap() {
		if (keySequenceMap == null) {
			solve();
			keySequenceMap = Collections.unmodifiableSortedMap(KeyNode.toKeySequenceMap(tree, KeySequence.create()));				
		}
		
		return keySequenceMap;
	}

	public SortedMap getKeySequenceMapForMode() {
		if (keySequenceMapForMode == null) {
			solve();
			SortedMap tree = KeyNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();
							
			keySequenceMapForMode = Collections.unmodifiableSortedMap(KeyNode.toKeySequenceMap(tree, mode));				
		}
		
		return keySequenceMapForMode;
	}

	public KeySequence getMode() {
		return mode;	
	}	

	public SortedMap getScopeMap() {
		return scopeMap;	
	}	
	
	public String[] getScopes() {
		return (String[]) scopes.clone();
	}		

	public boolean setKeyBindingSet(SortedSet keyBindingSet)
		throws IllegalArgumentException {
		if (keyBindingSet == null)
			throw new IllegalArgumentException();
		
		keyBindingSet = new TreeSet(keyBindingSet);
		Iterator iterator = keyBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof KeyBinding))
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
	
	public boolean setScopeMap(SortedMap scopeMap)
		throws IllegalArgumentException {
		if (scopeMap == null)
			throw new IllegalArgumentException();
			
		scopeMap = new TreeMap(scopeMap);
		Iterator iterator = scopeMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Path))
				throw new IllegalArgumentException();			
		}
		
		if (this.scopeMap.equals(scopeMap))
			return false;
				
		this.scopeMap = Collections.unmodifiableSortedMap(scopeMap);
		invalidateTree();
		return true;
	}	
	
	public boolean setScopes(String[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length == 0)
			throw new IllegalArgumentException();

		scopes = (String[]) scopes.clone();
		
		for (int i = 0; i < scopes.length; i++)
			if (scopes[i] == null)
				throw new IllegalArgumentException();	
		
		if (Arrays.equals(this.scopes, scopes))
			return false;
		
		this.scopes = scopes;
		invalidateSolution();
		return true;		
	}	

	private void build() {
		if (tree == null) {		
			tree = new TreeMap();
			Iterator iterator = keyBindingSet.iterator();
		
			while (iterator.hasNext()) {
				KeyBinding keyBinding = (KeyBinding) iterator.next();
				Path scope = (Path) scopeMap.get(keyBinding.getScope());
		
				if (scope == null)
					continue;

				Path keyConfiguration = (Path) keyConfigurationMap.get(keyBinding.getKeyConfiguration());
					
				if (keyConfiguration == null)
					continue;

				List pathItems = new ArrayList();
				pathItems.add(scope);
				pathItems.add(keyConfiguration);
				State scopeKeyConfiguration = State.create(pathItems);						
				pathItems = new ArrayList();
				pathItems.add(Manager.pathForPlatform(keyBinding.getPlatform()));
				pathItems.add(Manager.pathForLocale(keyBinding.getLocale()));
				State platformLocale = State.create(pathItems);		
				KeyNode.add(tree, keyBinding, scopeKeyConfiguration, platformLocale);
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
			State[] scopeKeyConfigurations = new State[scopes.length];
			Path keyConfiguration = (Path) keyConfigurationMap.get(this.keyConfiguration);
			
			if (keyConfiguration == null)
				keyConfiguration = Path.create();
							
			for (int i = 0; i < scopes.length; i++) {
				Path scope = (Path) scopeMap.get(scopes[i]);
			
				if (scope == null)
					scope = Path.create();

				List pathItems = new ArrayList();
				pathItems.add(scope);
				pathItems.add(keyConfiguration);		
				scopeKeyConfigurations[i] = State.create(pathItems);
			}
			
			List pathItems = new ArrayList();
			pathItems.add(Manager.systemPlatform());
			pathItems.add(Manager.systemLocale());
			State platformLocale = State.create(pathItems);			
			KeyNode.solve(tree, scopeKeyConfigurations, new State[] { platformLocale } );
			solved = true;
		}
	}
}
