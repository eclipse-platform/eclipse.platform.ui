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
	private SortedSet bindingSet;
	private String configuration;
	private SortedMap configurationMap;	
	private SortedMap sequenceMap;
	private SortedMap sequenceMapForMode;
	private Sequence mode;	
	private SortedMap scopeMap;
	private String[] scopes;
	private boolean solved;
	private SortedMap tree;

	private Machine() {
		super();
		configurationMap = new TreeMap();
		scopeMap = new TreeMap();
		bindingSet = new TreeSet();
		configuration = ""; //$NON-NLS-1$
		scopes = new String[] { "" }; //$NON-NLS-1$
		mode = Sequence.create();	
	}

	public Map getCommandMap() {
		if (commandMap == null) {
			solve();
			commandMap = Collections.unmodifiableMap(Node.toCommandMap(getSequenceMap()));				
		}
		
		return commandMap;
	}
	
	public Map getCommandMapForMode() {
		if (commandMapForMode == null) {
			solve();
			SortedMap tree = Node.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();

			commandMapForMode = Collections.unmodifiableMap(Node.toCommandMap(getSequenceMapForMode()));				
		}
		
		return commandMapForMode;
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

	public SortedMap getSequenceMap() {
		if (sequenceMap == null) {
			solve();
			sequenceMap = Collections.unmodifiableSortedMap(Node.toSequenceMap(tree, Sequence.create()));				
		}
		
		return sequenceMap;
	}

	public SortedMap getSequenceMapForMode() {
		if (sequenceMapForMode == null) {
			solve();
			SortedMap tree = Node.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();
							
			sequenceMapForMode = Collections.unmodifiableSortedMap(Node.toSequenceMap(tree, mode));				
		}
		
		return sequenceMapForMode;
	}

	public Sequence getMode() {
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

	public boolean setMode(Sequence mode)
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

				List paths = new ArrayList();
				paths.add(scope);
				paths.add(configuration);
				State scopeConfiguration = State.create(paths);						
				paths = new ArrayList();
				paths.add(Manager.pathForPlatform(binding.getPlatform()));
				paths.add(Manager.pathForLocale(binding.getLocale()));
				State platformLocale = State.create(paths);		
				Node.add(tree, binding, scopeConfiguration, platformLocale);
			}
		}
	}

	private void invalidateMode() {
		commandMapForMode = null;
		sequenceMapForMode = null;
	}

	private void invalidateSolution() {
		solved = false;
		commandMap = null;	
		sequenceMap = null;
		invalidateMode();
	}
	
	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}
	
	private void solve() {
		if (!solved) {
			build();
			State[] scopeConfigurations = new State[scopes.length];
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
				scopeConfigurations[i] = State.create(paths);
			}
			
			List paths = new ArrayList();
			paths.add(Manager.systemPlatform());
			paths.add(Manager.systemLocale());
			State platformLocale = State.create(paths);			
			Node.solve(tree, scopeConfigurations, new State[] { platformLocale } );
			solved = true;
		}
	}
}
