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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

final class Node {

	static void add(SortedMap tree, Binding keyBinding, State scopeKeyConfiguration, State platformLocale) {
		List keyStrokes = keyBinding.getSequence().getStrokes();		
		SortedMap root = tree;
		Node keyNode = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			Stroke keyStroke = (Stroke) keyStrokes.get(i);
			keyNode = (Node) root.get(keyStroke);
			
			if (keyNode == null) {
				keyNode = new Node();	
				root.put(keyStroke, keyNode);
			}
			
			root = keyNode.childMap;
		}

		if (keyNode != null)
			keyNode.add(scopeKeyConfiguration, new Integer(keyBinding.getRank()), platformLocale, keyBinding.getCommand());
	}

	static SortedMap find(SortedMap tree, Sequence prefix) {	
		Iterator iterator = prefix.getStrokes().iterator();
	
		while (iterator.hasNext()) {
			Node node = (Node) tree.get(iterator.next());
			
			if (node == null)
				return new TreeMap();
								
			tree = node.childMap;
		}		
		
		return tree;			
	}

	static void remove(SortedMap tree, Binding keyBinding, State scopeKeyConfiguration, State platformLocale) {
		List keyStrokes = keyBinding.getSequence().getStrokes();		
		SortedMap root = tree;
		Node keyNode = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			Stroke keyStroke = (Stroke) keyStrokes.get(i);
			keyNode = (Node) root.get(keyStroke);
			
			if (keyNode == null)
				break;
			
			root = keyNode.childMap;
		}

		if (keyNode != null)
			keyNode.remove(scopeKeyConfiguration, new Integer(keyBinding.getRank()), platformLocale, keyBinding.getCommand());
	}

	static void solve(SortedMap tree, State[] scopeKeyConfigurations, State[] platformLocales) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			Node keyNode = (Node) iterator.next();			
			keyNode.command = solveScopeKeyConfigurationMap(keyNode.scopeKeyConfigurationMap, scopeKeyConfigurations, platformLocales);
			solve(keyNode.childMap, scopeKeyConfigurations, platformLocales);								
		}		
	}
	
	static CommandEnvelope solveCommandSet(Set commandSet) {	
		return commandSet.size() == 1 ? CommandEnvelope.create((String) commandSet.iterator().next()) : null;
	}

	static CommandEnvelope solvePlatformLocaleMap(SortedMap platformLocaleMap, State platformLocale) {
		int bestMatch = -1;
		CommandEnvelope bestCommandEnvelope = null;
		Iterator iterator = platformLocaleMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testPlatformLocale = (State) entry.getKey();
			Set testCommandSet = (Set) entry.getValue();

			if (testCommandSet != null) {
				CommandEnvelope testCommandEnvelope = solveCommandSet(testCommandSet);
				
				if (testCommandEnvelope != null) {
					int testMatch = testPlatformLocale.match(platformLocale);	
				
					if (testMatch >= 0) {
						if (bestMatch == -1 || testMatch < bestMatch) {
							bestMatch = testMatch;
							bestCommandEnvelope = testCommandEnvelope;
						}
						
						if (bestMatch == 0)
							break;
					}								
				}
			}
		}
		
		return bestCommandEnvelope;
	}	

	static CommandEnvelope solvePlatformLocaleMap(SortedMap platformLocaleMap, State[] platformLocales) {
		for (int i = 0; i < platformLocales.length; i++) {
			CommandEnvelope commandEnvelope = solvePlatformLocaleMap(platformLocaleMap, platformLocales[i]);
				
			if (commandEnvelope != null)
				return commandEnvelope;
		}
		
		return null;
	}

	static String solveRankMap(SortedMap rankMap, State[] platformLocales) {
		Iterator iterator = rankMap.values().iterator();
		
		while (iterator.hasNext()) {
			SortedMap platformLocaleMap = (SortedMap) iterator.next();
			
			if (platformLocaleMap != null) {
				CommandEnvelope commandEnvelope = solvePlatformLocaleMap(platformLocaleMap, platformLocales);
			
				if (commandEnvelope != null)
					return commandEnvelope.getCommand();
			}								
		}

		return null;
	}

	static String solveScopeKeyConfigurationMap(SortedMap scopeKeyConfigurationMap, State scopeKeyConfiguration, State[] platformLocales) {
		int bestMatch = -1;
		String bestCommand = null;
		Iterator iterator = scopeKeyConfigurationMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testScopeKeyConfiguration = (State) entry.getKey();
			SortedMap testRankMap = (SortedMap) entry.getValue();

			if (testRankMap != null) {
				String testCommand = solveRankMap(testRankMap, platformLocales);
				
				if (testCommand != null) {
					int testMatch = testScopeKeyConfiguration.match(scopeKeyConfiguration);	
				
					if (testMatch >= 0) {
						if (bestMatch == -1 || testMatch < bestMatch) {
							bestMatch = testMatch;
							bestCommand = testCommand;
						}
						
						if (bestMatch == 0)
							break;
					}								
				}
			}
		}
		
		return bestCommand;
	}

	static String solveScopeKeyConfigurationMap(SortedMap scopeKeyConfigurationMap, State[] scopeKeyConfigurations, State[] platformLocales) {
		for (int i = 0; i < scopeKeyConfigurations.length; i++) {
			String command = solveScopeKeyConfigurationMap(scopeKeyConfigurationMap, scopeKeyConfigurations[i], platformLocales);
				
			if (command != null)
				return command;
		}
		
		return null;
	}

	static Map toCommandMap(SortedMap keySequenceMap) {
		Map commandMap = new HashMap();
		Iterator iterator = keySequenceMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Sequence keySequence = (Sequence) entry.getKey();			
			String command = (String) entry.getValue();
			SortedSet keySequenceSet = (SortedSet) commandMap.get(command);
			
			if (keySequenceSet == null) {
				keySequenceSet = new TreeSet();
				commandMap.put(command, keySequenceSet);			
			}
			
			keySequenceSet.add(keySequence);
		}	
		
		return commandMap;		
	}

	static SortedMap toKeySequenceMap(SortedMap tree, Sequence prefix) {
		SortedMap keySequenceMap = new TreeMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Stroke keyStroke = (Stroke) entry.getKey();			
			Node keyNode = (Node) entry.getValue();					
			List list = new ArrayList(prefix.getStrokes());
			list.add(keyStroke);
			Sequence keySequence = Sequence.create(list);
			SortedMap childKeySequenceMap = toKeySequenceMap(keyNode.childMap, keySequence);

			if (childKeySequenceMap.size() >= 1)
				keySequenceMap.putAll(childKeySequenceMap);
			else if (keyNode.command != null)		
				keySequenceMap.put(keySequence, keyNode.command);
		}

		return keySequenceMap;
	}

	SortedMap childMap = new TreeMap();	
	String command = null;
	SortedMap scopeKeyConfigurationMap = new TreeMap();
	
	private Node() {
		super();
	}

	void add(State scopeKeyConfiguration, Integer rank, State platformLocale, String command) {			
		SortedMap rankMap = (SortedMap) scopeKeyConfigurationMap.get(scopeKeyConfiguration);
		
		if (rankMap == null) {
			rankMap = new TreeMap();	
			scopeKeyConfigurationMap.put(scopeKeyConfiguration, rankMap);
		}

		SortedMap platformLocaleMap = (SortedMap) rankMap.get(rank);

		if (platformLocaleMap == null) {
			platformLocaleMap = new TreeMap();	
			rankMap.put(rank, platformLocaleMap);
		}

		Set commandSet = (Set) platformLocaleMap.get(platformLocale);

		if (commandSet == null) {
			commandSet = new HashSet();	
			platformLocaleMap.put(platformLocale, commandSet);
		}

		commandSet.add(command);		
	}

	void remove(State scopeKeyConfiguration, Integer rank, State platformLocale, String command) {
		SortedMap rankMap = (SortedMap) scopeKeyConfigurationMap.get(scopeKeyConfiguration);

		if (rankMap != null) {
			SortedMap platformLocaleMap = (SortedMap) rankMap.get(rank);
			
			if (platformLocaleMap != null) {
				Set commandSet = (Set) platformLocaleMap.get(platformLocale);
				
				if (commandSet != null) {
					commandSet.remove(command);	
						
					if (commandSet.isEmpty()) {
						platformLocaleMap.remove(platformLocale);
								
						if (platformLocaleMap.isEmpty()) {
							rankMap.remove(rank);
							
							if (rankMap.isEmpty())
								scopeKeyConfigurationMap.remove(scopeKeyConfiguration);
						}
					}					
				}
			}			
		}
	}
}
