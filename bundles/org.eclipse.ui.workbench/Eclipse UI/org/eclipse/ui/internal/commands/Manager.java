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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;

public class Manager {

	private final static String KEY_SEQUENCE_SEPARATOR = ", "; //$NON-NLS-1$
	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static java.util.Locale SYSTEM_LOCALE = java.util.Locale.getDefault();
	private final static String SYSTEM_PLATFORM = SWT.getPlatform(); // "carbon"

	private static Manager instance;

	public static Manager getInstance() {
		if (instance == null)
			instance = new Manager();
			
		return instance;	
	}

	private static SortedMap buildPathMapForGestureConfigurationMap(SortedMap gestureConfigurationMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = gestureConfigurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForGestureConfiguration(id, gestureConfigurationMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	private static SortedMap buildPathMapForKeyConfigurationMap(SortedMap keyConfigurationMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = keyConfigurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForKeyConfiguration(id, keyConfigurationMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	private static SortedMap buildPathMapForScopeMap(SortedMap scopeMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = scopeMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForScope(id, scopeMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	private static Path pathForGestureConfiguration(String id, Map gestureConfigurationMap) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				GestureConfiguration gestureConfiguration = (GestureConfiguration) gestureConfigurationMap.get(id);
				
				if (gestureConfiguration == null)
					return null;
							
				pathItems.add(0, id);
				id = gestureConfiguration.getParent();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}

	private static Path pathForKeyConfiguration(String id, Map keyConfigurationMap) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				KeyConfiguration keyConfiguration = (KeyConfiguration) keyConfigurationMap.get(id);
				
				if (keyConfiguration == null)
					return null;
							
				pathItems.add(0, id);
				id = keyConfiguration.getParent();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}

	private static Path pathForLocale(String locale) {
		Path path = null;

		if (locale != null) {
			List pathItems = new ArrayList();				
			locale = locale.trim();
			
			if (locale.length() > 0) {
				StringTokenizer st = new StringTokenizer(locale, LOCALE_SEPARATOR);
						
				while (st.hasMoreElements()) {
					String value = ((String) st.nextElement()).trim();
					
					if (value != null)
						pathItems.add(value);
				}
			}

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	private static Path pathForPlatform(String platform) {
		Path path = null;

		if (platform != null) {
			List pathItems = new ArrayList();				
			platform = platform.trim();
			
			if (platform.length() > 0)
				pathItems.add(platform);

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	private static Path pathForScope(String id, Map scopeMap) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				Scope scope = (Scope) scopeMap.get(id);
				
				if (scope == null)
					return null;
							
				pathItems.add(0, id);
				id = scope.getParent();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}	

	static SortedSet solveRegionalKeyBindingSet(SortedSet regionalBindingSet, State[] states) {
		
		class Key implements Comparable {		
		
			private final static int HASH_INITIAL = 17;
			private final static int HASH_FACTOR = 27;
			
			KeySequence keySequence;
			String configuration;
			String scope;

			public int compareTo(Object object) {
				Key key = (Key) object;
				int compareTo = keySequence.compareTo(key.keySequence);
		
				if (compareTo == 0) {
					compareTo = configuration.compareTo(key.configuration);
		
					if (compareTo == 0)
						compareTo = scope.compareTo(key.scope);
				}
				
				return compareTo;
			}
		
			public boolean equals(Object object) {
				if (!(object instanceof Key))
					return false;
				
				Key key = (Key) object;
				return keySequence.equals(key.keySequence) && configuration.equals(key.configuration) && scope.equals(key.scope);
			}

			public int hashCode() {
				int result = HASH_INITIAL;
				result = result * HASH_FACTOR + keySequence.hashCode();		
				result = result * HASH_FACTOR + configuration.hashCode();		
				result = result * HASH_FACTOR + scope.hashCode();		
				return result;
			}
		}

		SortedSet bindingSet = new TreeSet();
		Map map = new TreeMap();
		Iterator iterator = regionalBindingSet.iterator();
		
		while (iterator.hasNext()) {
			RegionalKeyBinding regionalBinding = (RegionalKeyBinding) iterator.next();
			KeyBinding binding = regionalBinding.getKeyBinding();
			List pathItems = new ArrayList();
			pathItems.add(pathForPlatform(regionalBinding.getPlatform()));
			pathItems.add(pathForLocale(regionalBinding.getLocale()));
			State state = State.create(pathItems);
			Key key = new Key();
			key.keySequence = binding.getKeySequence();
			key.configuration = binding.getKeyConfiguration();
			key.scope = binding.getScope();
			Map stateMap = (Map) map.get(key);
			
			if (stateMap == null) {
				stateMap = new TreeMap();
				map.put(key, stateMap);
			}
			
			List bindings = (List) stateMap.get(state);
			
			if (bindings == null) {
				bindings = new ArrayList();
				stateMap.put(state, bindings);	
			}			
		
			bindings.add(binding);		
		}

		Iterator iterator2 = map.values().iterator();

		while (iterator2.hasNext()) {
			Map stateMap = (Map) iterator2.next();				
			int bestMatch = -1;
			List bindings = null;
			Iterator iterator3 = stateMap.entrySet().iterator();

			while (iterator3.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator3.next();
				State testState = (State) entry.getKey();
				List testBindingSet = (List) entry.getValue();
							
				int testMatch = testState.match(states[0]);
				
				if (testMatch >= 0) {
					if (bindings == null || testMatch < bestMatch) {
						bindings = testBindingSet;
						bestMatch = testMatch;
					}
					
					if (bestMatch == 0)
						break;
				}
			}				

			if (bindings != null) {
				Iterator iterator4 = bindings.iterator();
				
				while (iterator4.hasNext()) {
					KeyBinding binding = (KeyBinding) iterator4.next();
					bindingSet.add(KeyBinding.create(binding.getCommand(), binding.getKeyConfiguration(), binding.getKeySequence(), binding.getPlugin(),
						binding.getRank() + bestMatch, binding.getScope()));								
				}				
			}
		}					

		return bindingSet;
	}

	/*
	private SortedSet solveRegionalBindingSet(SortedSet regionalBindingSet, State[] states) {
		SortedMap tree = new TreeMap();
		Iterator iterator = regionalBindingSet.iterator();
		
		while (iterator.hasNext()) {
			RegionalBinding regionalBinding = (RegionalBinding) iterator.next();
			Binding binding = regionalBinding.getBinding();
			List pathItems = new ArrayList();
			pathItems.add(pathForPlatform(regionalBinding.getPlatform()));
			pathItems.add(pathForLocale(regionalBinding.getLocale()));
			Node.add(tree, binding, State.create(pathItems));
		}

		Node.solve(tree, states);
		SortedSet matchSet = new TreeSet();
		Node.toMatchSet(tree, matchSet);
		SortedSet bindingSet = new TreeSet();
		iterator = matchSet.iterator();
		
		while (iterator.hasNext())
			bindingSet.add(((Match) iterator.next()).getBinding());							

		return bindingSet;
	}
	*/

	static Path systemLocale() {
		return SYSTEM_LOCALE != null ? pathForLocale(SYSTEM_LOCALE.toString()) : null;
	}

	static Path systemPlatform() {
		return pathForPlatform(SYSTEM_PLATFORM);
	}

	private Machine keyMachine;	
	
	private Manager() {
		super();
		keyMachine = Machine.create();
		update();		
	}

	public Machine getKeyMachine() {
		return keyMachine;
	}

	public String getTextForAction(String action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();					

		String text = null;
		Map actionMap = getKeyMachine().getActionMap();		
		SortedSet matchSet = (SortedSet) actionMap.get(action);
		
		if (matchSet != null && !matchSet.isEmpty()) {
			KeyBindingMatch match = (KeyBindingMatch) matchSet.first();
		
			if (match != null)
				text = match.getKeyBinding().getKeySequence().toString();
		}
		
		return text;
	}

	public void update() {
		List pathItems = new ArrayList();
		pathItems.add(systemPlatform());
		pathItems.add(systemLocale());
		State[] states = new State[] { State.create(pathItems) };	

		CoreRegistry coreRegistry = CoreRegistry.getInstance();		
		LocalRegistry localRegistry = LocalRegistry.getInstance();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		try {
			coreRegistry.load();
		} catch (IOException eIO) {
		}

		try {
			localRegistry.load();
		} catch (IOException eIO) {
		}
		
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}

		List registryKeyConfigurations = new ArrayList();
		registryKeyConfigurations.addAll(coreRegistry.getKeyConfigurations());
		registryKeyConfigurations.addAll(localRegistry.getKeyConfigurations());
		registryKeyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		SortedMap registryKeyConfigurationMap = KeyConfiguration.sortedMapById(registryKeyConfigurations);
		SortedMap keyConfigurationMap = buildPathMapForKeyConfigurationMap(registryKeyConfigurationMap);
		
		List registryScopes = new ArrayList();
		registryScopes.addAll(coreRegistry.getScopes());
		registryScopes.addAll(localRegistry.getScopes());
		registryScopes.addAll(preferenceRegistry.getScopes());
		SortedMap registryScopeMap = Scope.sortedMapById(registryScopes);
		SortedMap scopeMap = buildPathMapForScopeMap(registryScopeMap);

		SortedSet coreRegistryKeyBindingSet = new TreeSet();
		coreRegistryKeyBindingSet.addAll(coreRegistry.getKeyBindings());	
		SortedSet coreRegistryRegionalKeyBindingSet = new TreeSet();
		coreRegistryRegionalKeyBindingSet.addAll(coreRegistry.getRegionalKeyBindings());
		coreRegistryKeyBindingSet.addAll(solveRegionalKeyBindingSet(coreRegistryRegionalKeyBindingSet, states));

		SortedSet localRegistryKeyBindingSet = new TreeSet();
		localRegistryKeyBindingSet.addAll(localRegistry.getKeyBindings());	
		SortedSet localRegistryRegionalKeyBindingSet = new TreeSet();
		localRegistryRegionalKeyBindingSet.addAll(localRegistry.getRegionalKeyBindings());
		localRegistryKeyBindingSet.addAll(solveRegionalKeyBindingSet(localRegistryRegionalKeyBindingSet, states));
		
		SortedSet preferenceRegistryKeyBindingSet = new TreeSet();
		preferenceRegistryKeyBindingSet.addAll(preferenceRegistry.getKeyBindings());	
		
		SortedSet keyBindingSet = new TreeSet();		
		keyBindingSet.addAll(coreRegistryKeyBindingSet);
		keyBindingSet.addAll(localRegistryKeyBindingSet);
		keyBindingSet.addAll(preferenceRegistryKeyBindingSet);

		keyMachine.setKeyConfigurationMap(Collections.unmodifiableSortedMap(keyConfigurationMap));
		keyMachine.setScopeMap(Collections.unmodifiableSortedMap(scopeMap));
		keyMachine.setKeyBindingSet(Collections.unmodifiableSortedSet(keyBindingSet));		
	}
}
