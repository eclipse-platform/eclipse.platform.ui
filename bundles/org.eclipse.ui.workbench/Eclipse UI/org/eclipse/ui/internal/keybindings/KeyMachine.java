/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

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

	private List bindings;	
	private Path configuration;
	private Path locale;
	private Path platform;
	private Path[] scopes;
	private KeySequence mode;
	private SortedMap keyMachineMap;
	private boolean solved;
	private SortedMap keySequenceMap;
	private SortedMap keySequenceMapForMode;
	private SortedSet keyStrokeSetForMode;
	private SortedMap actionMap;
	private SortedMap actionMapForMode;

	private KeyMachine() {
		super();
		bindings = Collections.EMPTY_LIST;		
		configuration = Path.create();
		locale = Path.create();
		platform = Path.create();
		scopes = new Path[] { Path.create() };
		mode = KeySequence.create();	
	}

	List getBindings() {
		return bindings;	
	}

	void setBindings(List bindings)
		throws IllegalArgumentException {
		if (bindings == null)
			throw new IllegalArgumentException();
		
		bindings = new ArrayList(bindings);
		Iterator iterator = bindings.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Binding))
				throw new IllegalArgumentException();

		if (!this.bindings.equals(bindings)) {
			this.bindings = Collections.unmodifiableList(bindings);
			invalidateKeyMachineMap();
		}
	}
	
	Path getConfiguration() {
		return configuration;	
	}

	void setConfiguration(Path configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
			
		if (!this.configuration.equals(configuration)) {
			this.configuration = configuration;
			invalidateSolution();
		}
	}

	Path getLocale() {
		return locale;	
	}

	void setLocale(Path locale)
		throws IllegalArgumentException {
		if (locale == null)
			throw new IllegalArgumentException();
			
		if (!this.locale.equals(locale)) {
			this.locale = locale;
			invalidateSolution();
		}
	}
	
	Path getPlatform() {
		return platform;	
	}

	void setPlatform(Path platform)
		throws IllegalArgumentException {
		if (platform == null)
			throw new IllegalArgumentException();
			
		if (!this.platform.equals(platform)) {
			this.platform = platform;
			invalidateSolution();
		}
	}	
	
	Path[] getScopes() {
		return (Path[]) scopes.clone();
	}	
	
	void setScopes(Path[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();

		scopes = (Path[]) scopes.clone();
		
		if (!Arrays.equals(this.scopes, scopes)) {
			this.scopes = scopes;
			invalidateSolution();
		}		
	}	

	KeySequence getMode() {
		return mode;	
	}	
	
	void setMode(KeySequence mode)
		throws IllegalArgumentException {
		if (mode == null)
			throw new IllegalArgumentException();
			
		if (!this.mode.equals(mode)) {
			this.mode = mode;
			invalidateModalMaps();
		}
	}

	public SortedMap getKeySequenceMap() {
		if (keySequenceMap == null) {
			solve();
			keySequenceMap = Collections.unmodifiableSortedMap(Node.toKeySequenceMap(keyMachineMap));
		}
		
		return keySequenceMap;
	}

	public SortedMap getKeySequenceMapForMode() {
		if (keySequenceMapForMode == null) {
			solve();
			SortedMap keyMachineMap = Node.find(this.keyMachineMap, mode);
			
			if (keyMachineMap != null)
				keySequenceMapForMode = Collections.unmodifiableSortedMap(Node.toKeySequenceMap(keyMachineMap));
			else
				keySequenceMapForMode = Collections.unmodifiableSortedMap(new TreeMap());			
		}
		
		return keySequenceMapForMode;
	}

	public SortedSet getKeyStrokeSetForMode() {
		if (keyStrokeSetForMode == null) {
			keyStrokeSetForMode = new TreeSet();
			SortedMap keySequenceMapForMode = getKeySequenceMapForMode();
			Iterator iterator = keySequenceMapForMode.keySet().iterator();
			
			while (iterator.hasNext()) {
				KeySequence keySequence = (KeySequence) iterator.next();			
				List keyStrokes = keySequence.getKeyStrokes();			
				
				if (keyStrokes.size() >= 1)
					keyStrokeSetForMode.add(keyStrokes.get(0));
			}
		}
		
		return keyStrokeSetForMode;			
	}

	public SortedMap getActionMap() {
		if (actionMap == null) {
			actionMap = new TreeMap();
			SortedMap keySequenceMap = getKeySequenceMap();
			Iterator iterator = keySequenceMap.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				MatchAction matchAction = (MatchAction) entry.getValue();		
				Match match = matchAction.getMatch();
				String action = matchAction.getAction();				
				SortedSet keySequenceSet = (SortedSet) actionMap.get(action);
				
				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionMap.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(MatchKeySequence.create(match, keySequence));
			}
		}
				
		return actionMap;		
	}
	
	public SortedMap getActionMapForMode() {
		if (actionMapForMode == null) {
			actionMapForMode = new TreeMap();
			SortedMap keySequenceMapForMode = getKeySequenceMapForMode();
			Iterator iterator = keySequenceMapForMode.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				MatchAction matchAction = (MatchAction) entry.getValue();		
				Match match = matchAction.getMatch();
				String action = matchAction.getAction();				
				SortedSet keySequenceSet = (SortedSet) actionMapForMode.get(action);				

				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionMapForMode.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(MatchKeySequence.create(match, keySequence));
			}
		}
					
		return actionMapForMode;			
	}		
	
	private void invalidateKeyMachineMap() {
		keyMachineMap = null;
		invalidateSolution();
	}
	
	private void invalidateSolution() {
		solved = false;
		invalidateMaps();
	}
	
	private void invalidateMaps() {			
		keySequenceMap = null;
		actionMap = null;
		invalidateModalMaps();
	}
	
	private void invalidateModalMaps() {
		keySequenceMapForMode = null;
		keyStrokeSetForMode = null;		
		actionMapForMode = null;
	}
	
	private void build() {
		if (keyMachineMap == null) {		
			keyMachineMap = new TreeMap();
			Iterator iterator = bindings.iterator();
		
			while (iterator.hasNext())
				Node.addToTree(keyMachineMap, (Binding) iterator.next());
		}
	}
	
	private void solve() {
		if (!solved) {
			build();
			State[] states = new State[scopes.length];
				
			for (int i = 0; i < scopes.length; i++) {
				List paths = new ArrayList();
				paths.add(scopes[i]);			
				paths.add(configuration);
				paths.add(platform);
				paths.add(locale);							
				states[i] = State.create(paths);
			}
			
			Node.solveTree(keyMachineMap, states);
			solved = true;
		}
	}
}
