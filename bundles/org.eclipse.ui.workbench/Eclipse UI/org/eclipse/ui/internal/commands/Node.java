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

	static void add(SortedMap tree, Binding binding, State scopeConfiguration, State platformLocale) {
		List strokes = binding.getSequence().getStrokes();		
		SortedMap root = tree;
		Node node = null;
	
		for (int i = 0; i < strokes.size(); i++) {
			Stroke stroke = (Stroke) strokes.get(i);
			node = (Node) root.get(stroke);
			
			if (node == null) {
				node = new Node();	
				root.put(stroke, node);
			}
			
			root = node.childMap;
		}

		if (node != null)
			add(node.scopeConfigurationMap, scopeConfiguration, new Integer(binding.getRank()), platformLocale, binding.getCommand());
	}

	static void add(SortedMap scopeConfigurationMap, State scopeConfiguration, Integer rank, State platformLocale, String command) {			
		SortedMap rankMap = (SortedMap) scopeConfigurationMap.get(scopeConfiguration);
		
		if (rankMap == null) {
			rankMap = new TreeMap();	
			scopeConfigurationMap.put(scopeConfiguration, rankMap);
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

	static void remove(SortedMap tree, Binding binding, State scopeConfiguration, State platformLocale) {
		List strokes = binding.getSequence().getStrokes();		
		SortedMap root = tree;
		Node node = null;
	
		for (int i = 0; i < strokes.size(); i++) {
			Stroke stroke = (Stroke) strokes.get(i);
			node = (Node) root.get(stroke);
			
			if (node == null)
				break;
			
			root = node.childMap;
		}

		if (node != null)
			remove(node.scopeConfigurationMap, scopeConfiguration, new Integer(binding.getRank()), platformLocale, binding.getCommand());
	}

	static void remove(SortedMap scopeConfigurationMap, State scopeConfiguration, Integer rank, State platformLocale, String command) {
		SortedMap rankMap = (SortedMap) scopeConfigurationMap.get(scopeConfiguration);

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
								scopeConfigurationMap.remove(scopeConfiguration);
						}
					}					
				}
			}			
		}
	}

	static void solve(SortedMap tree, State[] scopeConfigurations, State[] platformLocales) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			Node node = (Node) iterator.next();			
			node.command = solveScopeConfigurationMap(node.scopeConfigurationMap, scopeConfigurations, platformLocales);
			solve(node.childMap, scopeConfigurations, platformLocales);								
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

	static String solveScopeConfigurationMap(SortedMap scopeConfigurationMap, State scopeConfiguration, State[] platformLocales) {
		int bestMatch = -1;
		String bestCommand = null;
		Iterator iterator = scopeConfigurationMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testScopeConfiguration = (State) entry.getKey();
			SortedMap testRankMap = (SortedMap) entry.getValue();

			if (testRankMap != null) {
				String testCommand = solveRankMap(testRankMap, platformLocales);
				
				if (testCommand != null) {
					int testMatch = testScopeConfiguration.match(scopeConfiguration);	
				
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

	static String solveScopeConfigurationMap(SortedMap scopeConfigurationMap, State[] scopeConfigurations, State[] platformLocales) {
		for (int i = 0; i < scopeConfigurations.length; i++) {
			String command = solveScopeConfigurationMap(scopeConfigurationMap, scopeConfigurations[i], platformLocales);
				
			if (command != null)
				return command;
		}
		
		return null;
	}

	static Map toCommandMap(SortedMap sequenceMap) {
		Map commandMap = new HashMap();
		Iterator iterator = sequenceMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Sequence sequence = (Sequence) entry.getKey();			
			String command = (String) entry.getValue();
			SortedSet sequenceSet = (SortedSet) commandMap.get(command);
			
			if (sequenceSet == null) {
				sequenceSet = new TreeSet();
				commandMap.put(command, sequenceSet);			
			}
			
			sequenceSet.add(sequence);
		}	
		
		return commandMap;		
	}

	static SortedMap toSequenceMap(SortedMap tree, Sequence prefix) {
		SortedMap sequenceMap = new TreeMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Stroke stroke = (Stroke) entry.getKey();			
			Node node = (Node) entry.getValue();					
			List list = new ArrayList(prefix.getStrokes());
			list.add(stroke);
			Sequence sequence = Sequence.create(list);
			SortedMap childSequenceMap = toSequenceMap(node.childMap, sequence);

			if (childSequenceMap.size() >= 1)
				sequenceMap.putAll(childSequenceMap);
			else if (node.command != null)		
				sequenceMap.put(sequence, node.command);
		}

		return sequenceMap;
	}

	private SortedMap childMap = new TreeMap();	
	private String command = null;
	private SortedMap scopeConfigurationMap = new TreeMap();
	
	private Node() {
		super();
	}
}
