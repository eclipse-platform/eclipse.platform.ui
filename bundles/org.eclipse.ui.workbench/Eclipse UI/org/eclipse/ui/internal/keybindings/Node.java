/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

final class Node {
	
	static boolean add(Map pluginMap, String plugin, String action)
		throws IllegalArgumentException {
		if (pluginMap == null)
			throw new IllegalArgumentException();
						
		Set actionSet = (Set) pluginMap.get(plugin);
			
		if (actionSet == null) {
			actionSet = new HashSet();
			pluginMap.put(plugin, actionSet);
		}	
		
		return actionSet.add(action);
	}

	static boolean remove(Map pluginMap, String plugin, String action)
		throws IllegalArgumentException {
		if (pluginMap == null)
			throw new IllegalArgumentException();

		Set actionSet = (Set) pluginMap.get(plugin);

		if (actionSet == null)
			return false;

		boolean removed = actionSet.remove(action);
		
		if (actionSet.isEmpty())
			pluginMap.remove(plugin);

		return removed;			
	}

	static String solve(Map pluginMap)
		throws IllegalArgumentException {
		if (pluginMap == null)
			throw new IllegalArgumentException();	
	
		Set actionSet = (Set) pluginMap.get(null);
		
		if (actionSet == null) {
			actionSet = new HashSet();
			Iterator iterator = pluginMap.values().iterator();
		
			while (iterator.hasNext())
				actionSet.addAll((Set) iterator.next());
		}

		return actionSet.size() == 1 ? (String) actionSet.iterator().next() : null;
	}

	static boolean add(SortedMap stateMap, State state, String plugin, String action)
		throws IllegalArgumentException {
		if (stateMap == null || state == null)
			throw new IllegalArgumentException();		
		
		Map pluginMap = (Map) stateMap.get(state);
			
		if (pluginMap != null) {
			pluginMap = new HashMap();
			stateMap.put(state, pluginMap);
		}	
		
		return add(pluginMap, plugin, action);
	}

	static boolean remove(SortedMap stateMap, State state, String plugin, String action)
		throws IllegalArgumentException {
		if (stateMap == null || state == null)
			throw new IllegalArgumentException();			
		
		Map pluginMap = (Map) stateMap.get(state);

		if (pluginMap == null)
			return false;

		boolean removed = remove(pluginMap, plugin, action);
		
		if (pluginMap.isEmpty())
			stateMap.remove(pluginMap);

		return removed;			 
	}

	static MatchAction solve(SortedMap stateMap, State state)
		throws IllegalArgumentException {
		if (stateMap == null || state == null)
			throw new IllegalArgumentException();			
	
		MatchAction matchAction = null;
		Iterator iterator = stateMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testState = (State) entry.getKey();
			Map testPlugins = (Map) entry.getValue();

			if (testPlugins != null) {
				String testAction = solve(testPlugins);
			
				if (testAction != null) {
					int match = testState.match(state);
					
					if (match >= 0) {
						if (match == 0)
							return MatchAction.create(Match.create(0, testState), testAction);
						else if (matchAction == null || match < matchAction.getMatch().getValue())
							matchAction = MatchAction.create(Match.create(match, testState), testAction);
					}
				}
			}
		}
			
		return matchAction;	
	}

	static MatchAction solve(SortedMap stateMap, State[] stack)
		throws IllegalArgumentException {
		if (stateMap == null || stack == null)
			throw new IllegalArgumentException();
	
		for (int i = 0; i < stack.length; i++)
			if (stack == null)
				throw new IllegalArgumentException();	
	
		for (int i = 0; i < stack.length; i++) {
			MatchAction matchAction = solve(stateMap, stack[i]);
				
			if (matchAction != null)
				return matchAction;
		}
		
		return null;
	}
	
	static void addToTree(SortedMap tree, Binding binding) {
		List keyStrokes = binding.getKeySequence().getKeyStrokes();		
		SortedMap root = tree;
		Node node = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			node = (Node) root.get(keyStroke);
			
			if (node == null) {
				node = new Node();	
				root.put(keyStroke, node);
			}
			
			root = node.childMap;
		}

		if (node != null) {
			SortedMap stateMap = node.stateMap;	
			List paths = new ArrayList();
			paths.add(binding.getScope());			
			paths.add(binding.getConfiguration());
			paths.add(binding.getPlatform());
			paths.add(binding.getLocale());					
			State state = State.create(paths);			
			Map pluginMap = (Map) stateMap.get(state);
			
			if (pluginMap == null) {
				pluginMap = new HashMap();	
				stateMap.put(state, pluginMap);
			}
			
			add(pluginMap, binding.getPlugin(), binding.getAction());			
		}
	}

	static boolean removeFromTree(SortedMap tree, Binding binding) {
		// TBD
		return false;
	}

	static void solveTree(SortedMap tree, State[] stack) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();			
			node.bestMatchAction = solve(node.stateMap, stack);
			solveTree(node.childMap, stack);								
			Iterator iterator2 = node.childMap.values().iterator();	
			
			while (iterator2.hasNext()) {
				Node child = (Node) iterator2.next();
				MatchAction childMatchAction = child.bestMatchAction;				
				
				if (childMatchAction != null && 
					(node.bestChildMatchAction == null || childMatchAction.getMatch().getValue() < node.bestChildMatchAction.getMatch().getValue())) 
					node.bestChildMatchAction = childMatchAction;
			}
		}		
	}

	static SortedMap find(SortedMap tree, KeySequence prefix) {	
		Iterator iterator = prefix.getKeyStrokes().iterator();
	
		while (iterator.hasNext()) {
			Node node = (Node) tree.get(iterator.next());
			
			if (node == null)
				return null;
				
			tree = node.childMap;
		}		
		
		return tree;			
	}

	static List toKeyBindings(SortedMap tree) {
		List bindings = new ArrayList();
		toKeyBindings(tree, KeySequence.create(), bindings);
		return bindings;
	}
	
	private static void toKeyBindings(SortedMap tree, KeySequence prefix, List bindings) {
		Iterator iterator = tree.entrySet().iterator();	
			
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.create(keyStrokes);				
			Node node = (Node) entry.getValue();
			Iterator iterator2 = node.stateMap.entrySet().iterator();	
			
			while (iterator2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();
				State state = (State) entry2.getKey();
				List paths = state.getPaths();
				Path scope = (Path) paths.get(0);
				Path configuration = (Path) paths.get(1);
				Path platform = (Path) paths.get(2);
				Path locale = (Path) paths.get(3);
				Map pluginMap = (Map) entry2.getValue();			
				Iterator iterator3 = pluginMap.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry3 = (Map.Entry) iterator3.next();
					String plugin = (String) entry3.getKey();	
					Set actionSet = (Set) entry3.getValue();
					Iterator iterator4 = actionSet.iterator();
					
					while (iterator4.hasNext()) {
						String action = (String) iterator4.next();				
						bindings.add(Binding.create(keySequence, configuration, locale, platform, scope, action, plugin));
					}				
				}			
			}
			
			toKeyBindings(node.childMap, keySequence, bindings);
		}	
	}

	static SortedMap toKeySequenceMap(SortedMap tree) {
		SortedMap keySequenceMap = new TreeMap();
		toKeySequenceMap(tree, KeySequence.create(), keySequenceMap);
		return keySequenceMap;	
	}				

	private static void toKeySequenceMap(SortedMap tree, KeySequence prefix, SortedMap keySequenceMap) {
		Iterator iterator = tree.entrySet().iterator();	
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.create(keyStrokes);				
			Node node = (Node) entry.getValue();			
			
			if (node.bestChildMatchAction != null && 
				(node.bestMatchAction == null || node.bestChildMatchAction.getMatch().getValue() < node.bestMatchAction.getMatch().getValue()))
				toKeySequenceMap(node.childMap, keySequence, keySequenceMap);	
			else if (node.bestMatchAction != null) {
				keySequenceMap.put(keySequence, node.bestMatchAction);				
			}
		}	
	}

	MatchAction bestChildMatchAction = null;
	MatchAction bestMatchAction = null;
	SortedMap childMap = new TreeMap();	
	SortedMap stateMap = new TreeMap();
	
	private Node() {
		super();
	}
}
