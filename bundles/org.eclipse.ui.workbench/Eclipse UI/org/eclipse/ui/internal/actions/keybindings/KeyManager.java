/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal.actions.keybindings;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class KeyManager {

	private final static java.util.Locale systemLocale = java.util.Locale.getDefault();
	private final static String systemPlatform = SWT.getPlatform(); // "carbon"

	private final static String KEY_SEQUENCE_SEPARATOR = ", "; //$NON-NLS-1$
	private final static String KEY_STROKE_SEPARATOR = " "; //$NON-NLS-1$
	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static String OR_SEPARATOR = "||"; //$NON-NLS-1$

	private static KeyManager instance;

	public static KeyManager getInstance() {
		if (instance == null)
			instance = new KeyManager();
			
		return instance;	
	}

	public static List parseKeySequences(String keys) {
		List keySequences = null;
		
		if (keys != null) {
			keySequences = new ArrayList();
			StringTokenizer orTokenizer = new StringTokenizer(keys, OR_SEPARATOR); 
			
			while (orTokenizer.hasMoreTokens()) {
				List keyStrokes = new ArrayList();
				StringTokenizer spaceTokenizer = new StringTokenizer(orTokenizer.nextToken());
				
				while (spaceTokenizer.hasMoreTokens()) {
					int accelerator = Action.convertAccelerator(spaceTokenizer.nextToken());
					
					if (accelerator != 0)
						keyStrokes.add(KeyStroke.create(accelerator));
				}
				
				if (keyStrokes.size() >= 1)
					keySequences.add(KeySequence.create(keyStrokes));		
			}
		}

		return keySequences;
	}

	public static KeySequence parseKeySequenceStrict(String keys) {
		if (keys != null) {
			List keyStrokes = new ArrayList();
			StringTokenizer spaceTokenizer = new StringTokenizer(keys);
				
			while (spaceTokenizer.hasMoreTokens()) {
				int accelerator = Action.convertAccelerator(spaceTokenizer.nextToken());
					
				if (accelerator != 0)
					keyStrokes.add(KeyStroke.create(accelerator));
				else
					return null;
			}

			return KeySequence.create(keyStrokes);
		}
		
		return null;
	}		

	private static Path pathForConfiguration(String id, Map configurationMap) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				Configuration configuration = (Configuration) configurationMap.get(id);
				
				if (configuration == null)
					return null;
							
				pathItems.add(0, id);
				id = configuration.getParent();
			}
		
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

	private static Path systemLocale() {
		return systemLocale != null ? pathForLocale(systemLocale.toString()) : null;
	}

	private static Path systemPlatform() {
		return pathForPlatform(systemPlatform);
	}
	
	private static SortedMap buildConfigurationMap(SortedMap registryConfigurationMap) {
		SortedMap configurationMap = new TreeMap();
		Iterator iterator = registryConfigurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForConfiguration(id, registryConfigurationMap);
			
				if (path != null)
					configurationMap.put(id, path);
			}			
		}

		return configurationMap;		
	}

	private static SortedMap buildScopeMap(Map registryScopeMap) {
		SortedMap scopeMap = new TreeMap();
		Iterator iterator = registryScopeMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {
				Path path = pathForScope(id, registryScopeMap);
			
				if (path != null)
					scopeMap.put(id, path);
			}
		}

		return scopeMap;		
	}

	private static SortedSet readBindingSet(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			
		
		IMemento[] mementos = memento.getChildren(Binding.ELEMENT);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		SortedSet bindingSet = new TreeSet();
		
		for (int i = 0; i < mementos.length; i++)
			bindingSet.add(Binding.read(mementos[i]));
		
		return bindingSet;		
	}

	private static void writeBindingSet(IMemento memento, SortedSet bindingSet)
		throws IllegalArgumentException {
		if (memento == null || bindingSet == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = bindingSet.iterator();
		
		while (iterator.hasNext())
			((Binding) iterator.next()).write(memento.createChild(Binding.ELEMENT)); 
	}

	private SortedSet solveRegionalBindingSet(SortedSet regionalBindingSet, State[] states) {
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
			RegionalBinding regionalBinding = (RegionalBinding) iterator.next();
			Binding binding = regionalBinding.getBinding();
			List pathItems = new ArrayList();
			pathItems.add(pathForPlatform(regionalBinding.getPlatform()));
			pathItems.add(pathForLocale(regionalBinding.getLocale()));
			State state = State.create(pathItems);
			Key key = new Key();
			key.keySequence = binding.getKeySequence();
			key.configuration = binding.getConfiguration();
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
					Binding binding = (Binding) iterator4.next();
					bindingSet.add(Binding.create(binding.getAction(), binding.getConfiguration(), binding.getKeySequence(), binding.getPlugin(),
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

	private KeyMachine keyMachine;	
	private SortedSet preferenceBindingSet;
	private SortedMap registryConfigurationMap;
	private SortedMap registryScopeMap;
	private SortedSet registryRegionalBindingSet;
	private SortedSet registryBindingSet;	
	
	private KeyManager() {
		super();
		keyMachine = KeyMachine.create();
		loadPreference();
		loadRegistry();
		update();		
	}

	public KeyMachine getKeyMachine() {
		return keyMachine;
	}

	public SortedSet getPreferenceBindingSet() {
		return preferenceBindingSet;
	}

	public SortedSet getRegistryBindingSet() {
		return registryBindingSet;	
	}

	public SortedMap getRegistryConfigurationMap() {
		return registryConfigurationMap;	
	}	

	public SortedSet getRegistryRegionalBindingSet() {
		return registryRegionalBindingSet;	
	}
	
	public SortedMap getRegistryScopeMap() {
		return registryScopeMap;	
	}

	public void loadPreference() {	
		preferenceBindingSet = Collections.unmodifiableSortedSet(new TreeSet());
		
		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		String preferenceString = preferenceStore.getString("org.eclipse.ui.keybindings");
		
		if (preferenceString != null && preferenceString.length() != 0) {
			StringReader stringReader = new StringReader(preferenceString);

			try {
				XMLMemento xmlMemento = XMLMemento.createReadRoot(stringReader);
				IMemento memento = xmlMemento.getChild("bindings");
			
				if (memento != null) 
					preferenceBindingSet = Collections.unmodifiableSortedSet(readBindingSet(memento));
			} catch (WorkbenchException eWorkbench) {
			}
		}
	}

	public void loadRegistry() {		
		Registry registry = Registry.getInstance();	
		registryConfigurationMap = Collections.unmodifiableSortedMap(registry.getConfigurationMap());
		registryScopeMap = Collections.unmodifiableSortedMap(registry.getScopeMap());
		registryRegionalBindingSet = Collections.unmodifiableSortedSet(registry.getRegionalBindingSet());	
		List pathItems = new ArrayList();
		pathItems.add(systemPlatform());
		pathItems.add(systemLocale());
		State[] states = new State[] { State.create(pathItems) };		
		registryBindingSet = Collections.unmodifiableSortedSet(solveRegionalBindingSet(registryRegionalBindingSet, states));
	}

	public void savePreference() {		
		XMLMemento xmlMemento = XMLMemento.createWriteRoot("org.eclipse.ui.keybindings");
		IMemento memento = xmlMemento.createChild("bindings");
		writeBindingSet(memento, preferenceBindingSet);
		StringWriter stringWriter = new StringWriter();
		String preferenceString = null;
		
		try {
			xmlMemento.save(stringWriter);
			preferenceString = stringWriter.toString();
		} catch (IOException eIO) {
		}

		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue("org.eclipse.ui.keybindings", preferenceString);
	}

	public boolean setPreferenceBindingSet(SortedSet preferenceBindingSet)
		throws IllegalArgumentException {			
		if (preferenceBindingSet == null)
			throw new IllegalArgumentException();
		
		preferenceBindingSet = new TreeSet(preferenceBindingSet);
		Iterator iterator = preferenceBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();
	
		if (this.preferenceBindingSet.equals(preferenceBindingSet))
			return false;
					
		this.preferenceBindingSet = Collections.unmodifiableSortedSet(preferenceBindingSet);
		return true;
	}

	public void update() {
		SortedMap configurationMap = buildConfigurationMap(registryConfigurationMap);
		SortedMap scopeMap = buildScopeMap(registryScopeMap);
		SortedSet bindingSet = new TreeSet();
		bindingSet.addAll(preferenceBindingSet);
		bindingSet.addAll(registryBindingSet);
		keyMachine.setConfigurationMap(configurationMap);
		keyMachine.setScopeMap(scopeMap);
		keyMachine.setBindingSet(bindingSet);		
	}

	public String getTextForAction(String action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();					

		String text = null;
		Map actionMap = getKeyMachine().getActionMap();		
		SortedSet matchSet = (SortedSet) actionMap.get(action);
		
		if (matchSet != null && !matchSet.isEmpty()) {
			Match match = (Match) matchSet.first();
		
			if (match != null)
				text = getTextForKeySequence(match.getBinding().getKeySequence());
		}
		
		return text;
	}

	public String getTextForKeySequence(KeySequence keySequence)
		throws IllegalArgumentException {
		if (keySequence == null)
			throw new IllegalArgumentException();		
	
	    StringBuffer stringBuffer = new StringBuffer();
		Iterator iterator = keySequence.getKeyStrokes().iterator();
		int i = 0;
		
		while (iterator.hasNext()) {					
			if (i != 0)
				stringBuffer.append(KEY_STROKE_SEPARATOR);

			KeyStroke keyStroke = (KeyStroke) iterator.next();
			int accelerator = keyStroke.getAccelerator();
			stringBuffer.append(Action.convertAccelerator(accelerator));					
			i++;
		}

		return stringBuffer.toString();
	}
}
