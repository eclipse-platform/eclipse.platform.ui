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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

final class Node {

	static void add(SortedMap tree, Binding binding, State state) {
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

		if (node != null)
			node.add(binding, state);
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

	static void remove(SortedMap tree, Binding binding, State state) {
		List keyStrokes = binding.getKeySequence().getKeyStrokes();		
		SortedMap root = tree;
		Node node = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			node = (Node) root.get(keyStroke);
			
			if (node == null)
				break;
			
			root = node.childMap;
		}

		if (node != null)
			node.remove(binding, state);
	}

	static void solve(SortedMap tree, State[] stack) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();			
			node.match = solveStateMap(node.stateMap, stack);
			solve(node.childMap, stack);								
			node.bestChildMatch = null;			
			Iterator iterator2 = node.childMap.values().iterator();	
			
			while (iterator2.hasNext()) {
				Node child = (Node) iterator2.next();
				Match childMatch = child.match;				
				
				if (childMatch != null && (node.bestChildMatch == null || childMatch.getValue() < node.bestChildMatch.getValue())) 
					node.bestChildMatch = childMatch;
			}
		}		
	}

	static Binding solveActionMap(Map actionMap) {	
		Set bindingSet = (Set) actionMap.get(null);
			
		if (bindingSet == null) {
			bindingSet = new TreeSet();
			Iterator iterator = actionMap.values().iterator();
		
			while (iterator.hasNext())
				bindingSet.addAll((Set) iterator.next());
		}

		return bindingSet.size() == 1 ? (Binding) bindingSet.iterator().next() : null;		
	}

	static Binding solvePluginMap(Map pluginMap) {	
		Map actionMap = (Map) pluginMap.get(null);
		
		if (actionMap != null)
			return solveActionMap(actionMap);
		else {
			Set bindingSet = new TreeSet();
			Iterator iterator = pluginMap.values().iterator();
		
			while (iterator.hasNext())
				bindingSet.add(solveActionMap((Map) iterator.next()));	
			
			return bindingSet.size() == 1 ? (Binding) bindingSet.iterator().next() : null;	
		}			
	}
	
	static Match solveStateMap(SortedMap stateMap, State state) {
		Match match = null;
		Iterator iterator = stateMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testState = (State) entry.getKey();
			Map testPluginMap = (Map) entry.getValue();

			if (testPluginMap != null) {
				Binding testBinding = solvePluginMap(testPluginMap);
			
				if (testBinding != null) {
					int testMatch = testState.match(state);
					
					if (testMatch >= 0) {
						if (testMatch == 0)
							return Match.create(testBinding, 0);
						else if (match == null || testMatch < match.getValue())
							match = Match.create(testBinding, testMatch);
					}
				}
			}
		}
			
		return match;	
	}

	static Match solveStateMap(SortedMap stateMap, State[] stack) {
		for (int i = 0; i < stack.length; i++) {
			Match match = solveStateMap(stateMap, stack[i]);
				
			if (match != null)
				return match;
		}
		
		return null;
	}

	static Map toActionMap(Set matches) {
		Map actionMap = new HashMap();
		Iterator iterator = matches.iterator();
		
		while (iterator.hasNext()) {
			Match match = (Match) iterator.next();
			String action = match.getBinding().getAction();
			Set matchSet = (Set) actionMap.get(action);
			
			if (matchSet == null) {
				matchSet = new TreeSet();
				actionMap.put(action, matchSet);
			}

			matchSet.add(match);
		}
		
		return actionMap;
	}

	static void toBindingSet(SortedMap tree, Set bindingSet) {
		Iterator iterator = tree.values().iterator();	
			
		while (iterator.hasNext())
			toBindingSet((Node) iterator.next(), bindingSet);
	}

	static void toBindingSet(Node node, Set bindingSet) {
		toBindingSet(node.childMap, bindingSet);		
		Iterator iterator = node.stateMap.values().iterator();
		
		while (iterator.hasNext()) {
			Map pluginMap = (Map) iterator.next();
			Iterator iterator2 = pluginMap.values().iterator();
			
			while (iterator2.hasNext()) {
				Map actionMap = (Map) iterator2.next();
				Iterator iterator3 = actionMap.values().iterator();
				
				while (iterator3.hasNext())
					bindingSet.addAll((Set) iterator3.next());
			}
		}
	}

	static void toMatchSet(SortedMap tree, SortedSet matchSet) {
		Iterator iterator = tree.values().iterator();	
			
		while (iterator.hasNext())
			toMatchSet((Node) iterator.next(), matchSet);		
	}

	static void toMatchSet(Node node, SortedSet matchSet) {
		if (node.bestChildMatch != null && (node.match == null || node.bestChildMatch.getValue() < node.match.getValue()))
			toMatchSet(node.childMap, matchSet);
		else if (node.match != null)
			matchSet.add(node.match);
	}

	static Map toKeySequenceMap(Set matches) {
		Map keySequenceMap = new TreeMap();
		Iterator iterator = matches.iterator();
		
		while (iterator.hasNext()) {
			Match match = (Match) iterator.next();
			KeySequence keySequence = match.getBinding().getKeySequence();
			Set matchSet = (Set) keySequenceMap.get(keySequence);
			
			if (matchSet == null) {
				matchSet = new TreeSet();
				keySequenceMap.put(keySequence, matchSet);
			}

			matchSet.add(match);
		}
		
		return keySequenceMap;
	}

	Match bestChildMatch = null;
	SortedMap childMap = new TreeMap();	
	Match match = null;
	SortedMap stateMap = new TreeMap();
	
	private Node() {
		super();
	}

	void add(Binding binding, State state) {			
		Map pluginMap = (Map) stateMap.get(state);
		
		if (pluginMap == null) {
			pluginMap = new HashMap();	
			stateMap.put(state, pluginMap);
		}

		String plugin = binding.getPlugin();
		Map actionMap = (Map) pluginMap.get(plugin);
		
		if (actionMap == null) {
			actionMap = new HashMap();
			pluginMap.put(plugin, actionMap);
		}	
	
		String action = binding.getAction();
		Set bindingSet = (Set) actionMap.get(action);
		
		if (bindingSet == null) {
			bindingSet = new TreeSet();
			actionMap.put(action, bindingSet);
		}
		
		bindingSet.add(binding);
	}

	void remove(Binding binding, State state) {		
		Map pluginMap = (Map) stateMap.get(state);
		
		if (pluginMap != null) {
			String plugin = binding.getPlugin();
			Map actionMap = (Map) pluginMap.get(plugin);
			
			if (actionMap != null) {
				String action = binding.getAction();
				Set bindingSet = (Set) actionMap.get(action);
				
				if (bindingSet != null) {
					bindingSet.remove(binding);
					
					if (bindingSet.isEmpty()) {
						actionMap.remove(action);
						
						if (actionMap.isEmpty()) {
							pluginMap.remove(plugin);	
							
							if (pluginMap.isEmpty())
								stateMap.remove(state);	
						}						
					}
				}
			}
		}
	}
}
