/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

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

import org.eclipse.ui.commands.IKeyBindingDefinition;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

final class KeyBindingNode {

	static void add(SortedMap tree, IKeyBindingDefinition keyBinding, State contextConfiguration, State platformLocale) {
		List keyStrokes = keyBinding.getKeySequence().getKeyStrokes();		
		SortedMap root = tree;
		KeyBindingNode node = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			node = (KeyBindingNode) root.get(keyStroke);
			
			if (node == null) {
				node = new KeyBindingNode();	
				root.put(keyStroke, node);
			}
			
			root = node.childMap;
		}

		if (node != null)
			add(node.contextConfigurationMap, contextConfiguration, new Integer(keyBinding.getRank()), platformLocale, keyBinding.getCommandId());
	}

	static void add(SortedMap contextConfigurationMap, State contextConfiguration, Integer rank, State platformLocale, String command) {			
		SortedMap rankMap = (SortedMap) contextConfigurationMap.get(contextConfiguration);
		
		if (rankMap == null) {
			rankMap = new TreeMap();	
			contextConfigurationMap.put(contextConfiguration, rankMap);
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

	static SortedMap find(SortedMap tree, KeySequence prefix) {	
		Iterator iterator = prefix.getKeyStrokes().iterator();
	
		while (iterator.hasNext()) {
			KeyBindingNode node = (KeyBindingNode) tree.get(iterator.next());
			
			if (node == null)
				return new TreeMap();
								
			tree = node.childMap;
		}		
		
		return tree;			
	}

	static void remove(SortedMap tree, IKeyBindingDefinition keyBinding, State contextConfiguration, State platformLocale) {
		List keyStrokes = keyBinding.getKeySequence().getKeyStrokes();		
		SortedMap root = tree;
		KeyBindingNode node = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			node = (KeyBindingNode) root.get(keyStroke);
			
			if (node == null)
				break;
			
			root = node.childMap;
		}

		if (node != null)
			remove(node.contextConfigurationMap, contextConfiguration, new Integer(keyBinding.getRank()), platformLocale, keyBinding.getCommandId());
	}

	static void remove(SortedMap contextConfigurationMap, State contextConfiguration, Integer rank, State platformLocale, String command) {
		SortedMap rankMap = (SortedMap) contextConfigurationMap.get(contextConfiguration);

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
								contextConfigurationMap.remove(contextConfiguration);
						}
					}					
				}
			}			
		}
	}

	static void solve(SortedMap tree, State[] contextConfigurations, State[] platformLocales) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			KeyBindingNode node = (KeyBindingNode) iterator.next();			
			CommandEnvelope commandEnvelope = solveContextConfigurationMap(node.contextConfigurationMap, contextConfigurations, platformLocales);					
			node.command = commandEnvelope != null ? commandEnvelope.getCommand() : null;
			solve(node.childMap, contextConfigurations, platformLocales);								
		}		
	}
	
	static String solveCommandSet(Set commandSet) {	
		return commandSet != null && commandSet.size() == 1 ? (String) commandSet.iterator().next() : null;
	}

	static CommandEnvelope solvePlatformLocaleMap(SortedMap platformLocaleMap, State platformLocale) {
		int bestMatch = -1;
		String bestCommand = null;		
		Iterator iterator = platformLocaleMap.entrySet().iterator();
		boolean match = false;

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testPlatformLocale = (State) entry.getKey();
			Set testCommandSet = (Set) entry.getValue();
			int testMatch = testPlatformLocale.match(platformLocale);

			if (testMatch >= 0) {
				match = true;
				String testCommand = solveCommandSet(testCommandSet);

				if (testCommand != null) {
					if (bestMatch == -1 || testMatch < bestMatch) {
						bestMatch = testMatch;
						bestCommand = testCommand;
					}
								
					if (bestMatch == 0)
						break;				
				}					
			}	
		}

		return match ? CommandEnvelope.create(bestCommand) : null;
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
			CommandEnvelope commandEnvelope = solvePlatformLocaleMap(platformLocaleMap, platformLocales);
			
			if (commandEnvelope != null)
				return commandEnvelope.getCommand();
		}

		return null;
	}

	static CommandEnvelope solveContextConfigurationMap(SortedMap contextConfigurationMap, State contextConfiguration, State[] platformLocales) {
		int bestMatch = -1;
		String bestCommand = null;
		Iterator iterator = contextConfigurationMap.entrySet().iterator();
		boolean match = false;

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testContextConfiguration = (State) entry.getKey();
			SortedMap testRankMap = (SortedMap) entry.getValue();
			int testMatch = testContextConfiguration.match(contextConfiguration);	

			if (testMatch >= 0) {
				match = true;
				String testCommand = solveRankMap(testRankMap, platformLocales);

				if (testCommand != null) {
					if (bestMatch == -1 || testMatch < bestMatch) {
						bestMatch = testMatch;
						bestCommand = testCommand;
					}
								
					if (bestMatch == 0)
						break;
				}					
			}	
		}

		return match ? CommandEnvelope.create(bestCommand) : null;
	}

	static CommandEnvelope solveContextConfigurationMap(SortedMap contextConfigurationMap, State[] contextConfigurations, State[] platformLocales) {
		for (int i = 0; i < contextConfigurations.length; i++) {
			CommandEnvelope commandEnvelope = solveContextConfigurationMap(contextConfigurationMap, contextConfigurations[i], platformLocales);
				
			if (commandEnvelope != null)
				return commandEnvelope;
		}
		
		return null;
	}

	static Map toCommandMap(SortedMap keySequenceMap) {
		Map commandMap = new HashMap();
		Iterator iterator = keySequenceMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();			
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

	static SortedMap toKeySequenceMap(SortedMap tree, KeySequence prefix) {
		SortedMap keySequenceMap = new TreeMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();			
			KeyBindingNode node = (KeyBindingNode) entry.getValue();					
			List list = new ArrayList(prefix.getKeyStrokes());
			list.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(list);
			SortedMap childKeySequenceMap = toKeySequenceMap(node.childMap, keySequence);

			if (childKeySequenceMap.size() >= 1)
				keySequenceMap.putAll(childKeySequenceMap);
			else if (node.command != null && !node.command.equals(Util.ZERO_LENGTH_STRING))		
				keySequenceMap.put(keySequence, node.command);
		}

		return keySequenceMap;
	}

	private SortedMap childMap = new TreeMap();	
	private String command = null;
	private SortedMap contextConfigurationMap = new TreeMap();
	
	private KeyBindingNode() {
		super();
	}
}
