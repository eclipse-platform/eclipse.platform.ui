/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.commands.keys;

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

public final class KeyMachine {

	public static KeyMachine create() {
		return new KeyMachine();
	}

	private Map actionMap;
	private Map actionMapForMode;
	private SortedSet bindingSet;
	private String configuration;
	private SortedMap configurationMap;
	private Map keySequenceMap;
	private Map keySequenceMapForMode;
	private SortedSet matchSet;	
	private SortedSet matchSetForMode;
	private KeySequence mode;
	private SortedMap scopeMap;
	private String[] scopes;
	private boolean solved;
	private SortedMap tree;

	private KeyMachine() {
		super();
		configurationMap = new TreeMap();
		scopeMap = new TreeMap();
		bindingSet = new TreeSet();		
		configuration = "org.eclipse.ui.defaultConfiguration";
		scopes = new String[] { "org.eclipse.ui.globalScope" };
		mode = KeySequence.create();	
	}

	public Map getActionMap() {
		if (actionMap == null)
			actionMap = Collections.unmodifiableMap(Node.toActionMap(getMatchSet()));				
		
		return actionMap;
	}
	
	public Map getActionMapForMode() {
		if (actionMapForMode == null)
			actionMapForMode = Collections.unmodifiableMap(Node.toActionMap(getMatchSetForMode()));				
		
		return actionMapForMode;
	}

	public SortedSet getBindingSet() {
		return bindingSet;	
	}

	public String getConfiguration() {
		return configuration;
	}		

	public SortedMap getConfigurationMap() {
		return configurationMap;	
	}

	public Map getKeySequenceMap() {
		if (keySequenceMap == null)
			keySequenceMap = Collections.unmodifiableMap(Node.toKeySequenceMap(getMatchSet()));				
		
		return keySequenceMap;
	}

	public Map getKeySequenceMapForMode() {
		if (keySequenceMapForMode == null)
			keySequenceMapForMode = Collections.unmodifiableMap(Node.toKeySequenceMap(getMatchSetForMode()));				
		
		return keySequenceMapForMode;
	}

	public SortedSet getMatchSet() {
		if (matchSet == null) {
			solve();
			SortedSet matchSet = new TreeSet();			
			Node.toMatchSet(tree, matchSet);
			this.matchSet = Collections.unmodifiableSortedSet(matchSet);
		}
		
		return matchSet;
	}

	public SortedSet getMatchSetForMode() {
		if (matchSetForMode == null) {
			SortedSet matchSetForMode = new TreeSet();
			Iterator iterator = getMatchSet().iterator();
			
			while (iterator.hasNext()) {
				Match match = (Match) iterator.next();

				if (match.getBinding().getKeySequence().isChildOf(mode, false))
					matchSetForMode.add(match);				
			}

			this.matchSetForMode = Collections.unmodifiableSortedSet(matchSetForMode);
		}
		
		return matchSetForMode;
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

	public boolean setBindingSet(SortedSet bindingSet)
		throws IllegalArgumentException {
		if (bindingSet == null)
			throw new IllegalArgumentException();
		
		bindingSet = new TreeSet(bindingSet);
		Iterator iterator = bindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();

		if (this.bindingSet.equals(bindingSet))
			return false;
		
		this.bindingSet = Collections.unmodifiableSortedSet(bindingSet);
		invalidateTree();
		return true;
	}

	public boolean setConfiguration(String configuration) {
		if (configuration == null)
			throw new IllegalArgumentException();
			
		if (this.configuration.equals(configuration))
			return false;
		
		this.configuration = configuration;
		invalidateSolution();
		return true;
	}

	public boolean setConfigurationMap(SortedMap configurationMap)
		throws IllegalArgumentException {
		if (configurationMap == null)
			throw new IllegalArgumentException();
			
		configurationMap = new TreeMap(configurationMap);
		Iterator iterator = configurationMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Path))
				throw new IllegalArgumentException();			
		}

		if (this.configurationMap.equals(configurationMap))
			return false;
					
		this.configurationMap = Collections.unmodifiableSortedMap(configurationMap);
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
			Iterator iterator = bindingSet.iterator();
		
			while (iterator.hasNext()) {
				Binding binding = (Binding) iterator.next();
				Path scope = (Path) scopeMap.get(binding.getScope());
		
				if (scope == null)
					continue;

				Path configuration = (Path) configurationMap.get(binding.getConfiguration());
					
				if (configuration == null)
					continue;
	
				List listPaths = new ArrayList();
				listPaths.add(scope);
				listPaths.add(configuration);					
				Node.add(tree, binding, State.create(listPaths));
			}
		}
	}

	private void invalidateMode() {
		actionMapForMode = null;
		keySequenceMapForMode = null;
		matchSetForMode = null;
	}

	private void invalidateSolution() {
		solved = false;
		actionMap = null;	
		keySequenceMap = null;
		matchSet = null;
		invalidateMode();
	}
	
	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}
	
	private void solve() {
		if (!solved) {
			build();
			State[] states = new State[scopes.length];
			Path configuration = (Path) configurationMap.get(this.configuration);
			
			if (configuration == null)
				configuration = Path.create();
							
			for (int i = 0; i < scopes.length; i++) {
				Path scope = (Path) scopeMap.get(scopes[i]);
			
				if (scope == null)
					scope = Path.create();

				List paths = new ArrayList();
				paths.add(scope);			
				paths.add(configuration);					
				states[i] = State.create(paths);
			}
			
			Node.solve(tree, states);
			solved = true;
		}
	}
}
