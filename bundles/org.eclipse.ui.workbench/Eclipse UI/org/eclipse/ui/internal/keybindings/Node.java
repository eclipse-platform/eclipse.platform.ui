/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

class Node {
	
	static boolean add(SortedMap contributions, 
		Contributor contributor, Action action)
		throws IllegalArgumentException {
		if (contributions == null || contributor == null || action == null)
			throw new IllegalArgumentException();
						
		SortedSet actions = (SortedSet) contributions.get(contributor);
			
		if (actions == null) {
			actions = new TreeSet();
			contributions.put(contributor, actions);
		}	
		
		return actions.add(action);
	}

	static boolean remove(SortedMap contributions, 
		Contributor contributor, Action action)
		throws IllegalArgumentException {
		if (contributions == null || contributor == null || action == null)
			throw new IllegalArgumentException();

		SortedSet actions = (SortedSet) contributions.get(contributor);

		if (actions == null)
			return false;

		boolean removed = actions.remove(action);
		
		if (actions.isEmpty())
			contributions.remove(contributor);

		return removed;			
	}

	static Action solve(SortedMap contributions)
		throws IllegalArgumentException {
		if (contributions == null)
			throw new IllegalArgumentException();	
	
		SortedSet actions = 
			(SortedSet) contributions.get(Contributor.create(null));
		
		if (actions == null) {
			actions = new TreeSet();
			Iterator iterator = contributions.values().iterator();
		
			while (iterator.hasNext())
				actions.addAll((SortedSet) iterator.next());
		}

		return actions.size() == 1 ? 
			(Action) actions.first() : Action.create(null);
	}

	static boolean add(SortedMap states, State state, 
		Contributor contributor, Action action)
		throws IllegalArgumentException {
		if (states == null || state == null || contributor == null || 
			action == null)
			throw new IllegalArgumentException();		
		
		SortedMap contributions = (SortedMap) states.get(state);
			
		if (contributions != null) {
			contributions = new TreeMap();
			states.put(state, contributions);
		}	
		
		return add(contributions, contributor, action);
	}

	static boolean remove(SortedMap states, State state, 
		Contributor contributor, Action action)
		throws IllegalArgumentException {
		if (states == null || state == null || contributor == null || 
			action == null)
			throw new IllegalArgumentException();			
		
		SortedMap contributions = (SortedMap) states.get(state);

		if (contributions == null)
			return false;

		boolean removed = remove(contributions, contributor, action);
		
		if (contributions.isEmpty())
			states.remove(contributions);

		return removed;			 
	}

	static ActionMatch solve(SortedMap states, State state)
		throws IllegalArgumentException {
		if (states == null || state == null)
			throw new IllegalArgumentException();			
	
		ActionMatch actionMatch = null;
		Iterator iterator = states.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testState = (State) entry.getKey();
			SortedMap testContributions = (SortedMap) entry.getValue();

			if (testContributions != null) {
				Action testAction = solve(testContributions);
			
				if (testAction != null && testAction.getValue() != null) {
					int match = testState.match(state);
					
					if (match >= 0) {
						if (match == 0)
							return ActionMatch.create(testAction, 0);
						else if (actionMatch == null || 
							match < actionMatch.getMatch())
							actionMatch = ActionMatch.create(testAction, match);
					}
				}
			}
		}
			
		return actionMatch;	
	}

	static ActionMatch solve(SortedMap states, State[] stack)
		throws IllegalArgumentException {
		if (states == null || stack == null)
			throw new IllegalArgumentException();
	
		for (int i = 0; i < stack.length; i++) {
			if (stack == null)
				throw new IllegalArgumentException();	
		}
	
		for (int i = 0; i < stack.length; i++) {
			ActionMatch actionMatch = solve(states, stack[i]);
				
			if (actionMatch != null)
				return actionMatch;
		}
		
		return null;
	}
	
	static void addToTree(SortedMap tree, KeyBinding keyBinding) {
		List keyStrokes = keyBinding.getKeySequence().getKeyStrokes();		
		SortedMap root = tree;
		Node node = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			node = (Node) root.get(keyStroke);
			
			if (node == null) {
				node = new Node();	
				root.put(keyStroke, node);
			}
			
			root = node.children;
		}

		if (node != null) {
			SortedMap states = node.states;			
			State state = keyBinding.getState();			
			SortedMap contributorToActionSetMap = (SortedMap) states.get(state);
			
			if (contributorToActionSetMap == null) {
				contributorToActionSetMap = new TreeMap();	
				states.put(state, contributorToActionSetMap);
			}
			
			add(contributorToActionSetMap, keyBinding.getContributor(),
				keyBinding.getAction());			
		}
	}

	static boolean removeFromTree(SortedMap tree, 
		KeyBinding keyBinding) {
		// TBD
		return false;
	}

	static void solveTree(SortedMap tree, State[] stack) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();			
			node.bestActionMatch = solve(node.states, stack);
			solveTree(node.children, stack);								
			Iterator iterator2 = node.children.values().iterator();	
			
			while (iterator2.hasNext()) {
				Node child = (Node) iterator2.next();
				ActionMatch childActionMatch = child.bestActionMatch;				
				
				if (childActionMatch != null && 
					(node.bestChildActionMatch == null || 
					childActionMatch.getMatch() < 
					node.bestChildActionMatch.getMatch())) 
					node.bestChildActionMatch = childActionMatch;
			}
		}		
	}

	static SortedMap find(SortedMap tree, KeySequence prefix) {	
		Iterator iterator = prefix.getKeyStrokes().iterator();
	
		while (iterator.hasNext()) {
			Node node = (Node) tree.get(iterator.next());
			
			if (node == null)
				return null;
				
			tree = node.children;
		}		
		
		return tree;			
	}

	static List toBindings(SortedMap tree) {
		List bindings = new ArrayList();
		toBindings(tree, KeySequence.create(), bindings);
		return bindings;
	}
	
	private static void toBindings(SortedMap tree, KeySequence prefix,
		List bindings) {
		Iterator iterator = tree.entrySet().iterator();	
			
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.create(keyStrokes);				
			Node node = (Node) entry.getValue();
			Iterator iterator2 = node.states.entrySet().iterator();	
			
			while (iterator2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();
				State state = (State) entry2.getKey();	
				SortedMap contributions = (SortedMap) entry2.getValue();			
				Iterator iterator3 = contributions.entrySet().iterator();
				
				while (iterator3.hasNext()) {
					Map.Entry entry3 = (Map.Entry) iterator3.next();
					Contributor contributor = (Contributor) entry3.getKey();	
					SortedSet actions = (SortedSet) entry3.getValue();
					Iterator iterator4 = actions.iterator();
					
					while (iterator4.hasNext()) {
						Action action = (Action) iterator4.next();
						bindings.add(KeyBinding.create(keySequence, state, 
							contributor, action));					
					}				
				}			
			}
			
			toBindings(node.children, keySequence, bindings);
		}	
	}

	static SortedMap toKeySequenceActionMap(SortedMap tree) {
		SortedMap keySequenceActionMap = new TreeMap();
		toKeySequenceActionMap(tree, KeySequence.create(),
			keySequenceActionMap);
		return keySequenceActionMap;	
	}				

	private static void toKeySequenceActionMap(SortedMap tree, 
		KeySequence prefix, SortedMap keySequenceActionMap) {
		Iterator iterator = tree.entrySet().iterator();	
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.create(keyStrokes);				
			Node node = (Node) entry.getValue();			
			
			if (node.bestChildActionMatch != null && 
				(node.bestActionMatch == null ||
				node.bestChildActionMatch.getMatch() < 
				node.bestActionMatch.getMatch()))
				toKeySequenceActionMap(node.children, keySequence, 
					keySequenceActionMap);	
			else if (node.bestActionMatch != null) {
				Action action = node.bestActionMatch.getAction();
				
				if (action.getValue() != null)				
					keySequenceActionMap.put(keySequence, action);				
			}
		}	
	}

	SortedMap children = new TreeMap();	
	SortedMap states = new TreeMap();
		
	ActionMatch bestActionMatch = null;
	ActionMatch bestChildActionMatch = null;
}
