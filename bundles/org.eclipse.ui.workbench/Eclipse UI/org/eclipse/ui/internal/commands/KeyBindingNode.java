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

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

final class KeyBindingNode {

	static void add(SortedMap tree, KeySequence keySequence, State contextConfiguration, int rank, State platformLocale, String commandId) {		 
		List keyStrokes = keySequence.getKeyStrokes();		
		SortedMap root = tree;
		KeyBindingNode keyBindingNode = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			keyBindingNode = (KeyBindingNode) root.get(keyStroke);
			
			if (keyBindingNode == null) {
				keyBindingNode = new KeyBindingNode();	
				root.put(keyStroke, keyBindingNode);
			}
			
			root = keyBindingNode.childMap;
		}

		if (keyBindingNode != null)
			add(keyBindingNode.contextConfigurationMap, contextConfiguration, rank, platformLocale, commandId);
	}

	static SortedMap find(SortedMap tree, KeySequence prefix) {	
		Iterator iterator = prefix.getKeyStrokes().iterator();
	
		while (iterator.hasNext()) {
			KeyBindingNode keyBindingNode = (KeyBindingNode) tree.get(iterator.next());
			
			if (keyBindingNode == null)
				return new TreeMap();
								
			tree = keyBindingNode.childMap;
		}		
		
		return tree;			
	}

	static void remove(SortedMap tree, KeySequence keySequence, State contextConfiguration, int rank, State platformLocale, String commandId) {
		List keyStrokes = keySequence.getKeyStrokes();		
		SortedMap root = tree;
		KeyBindingNode keyBindingNode = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			keyBindingNode = (KeyBindingNode) root.get(keyStroke);
			
			if (keyBindingNode == null)
				break;
			
			root = keyBindingNode.childMap;
		}

		if (keyBindingNode != null)
			remove(keyBindingNode.contextConfigurationMap, contextConfiguration, rank, platformLocale, commandId);
	}

	static void solve(SortedMap tree, State[] contextConfigurations, State[] platformLocales) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			KeyBindingNode keyBindingNode = (KeyBindingNode) iterator.next();			
			Envelope commandEnvelope = solveContextConfigurationMap(keyBindingNode.contextConfigurationMap, contextConfigurations, platformLocales);					
			keyBindingNode.commandId = commandEnvelope != null ? commandEnvelope.getId() : null;
			solve(keyBindingNode.childMap, contextConfigurations, platformLocales);								
		}		
	}

	static Map toCommandMap(SortedMap keySequenceMap) {
		Map commandMap = new HashMap();
		Iterator iterator = keySequenceMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();			
			String commandId = (String) entry.getValue();
			SortedSet keySequences = (SortedSet) commandMap.get(commandId);
			
			if (keySequences == null) {
				keySequences = new TreeSet();
				commandMap.put(commandId, keySequences);			
			}
			
			keySequences.add(keySequence);
		}	
		
		return commandMap;		
	}

	static SortedMap toKeySequenceMap(SortedMap tree, KeySequence prefix) {
		SortedMap keySequenceMap = new TreeMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();			
			KeyBindingNode keyBindingNode = (KeyBindingNode) entry.getValue();					
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			SortedMap childKeySequenceMap = toKeySequenceMap(keyBindingNode.childMap, keySequence);

			if (childKeySequenceMap.size() >= 1)
				keySequenceMap.putAll(childKeySequenceMap);
			else if (keyBindingNode.commandId != null && !keyBindingNode.commandId.equals(Util.ZERO_LENGTH_STRING))		
				keySequenceMap.put(keySequence, keyBindingNode.commandId);
		}

		return keySequenceMap;
	}

	private static void add(SortedMap contextConfigurationMap, State contextConfiguration, int rank, State platformLocale, String commandId) {			
		SortedMap rankMap = (SortedMap) contextConfigurationMap.get(contextConfiguration);
		
		if (rankMap == null) {
			rankMap = new TreeMap();	
			contextConfigurationMap.put(contextConfiguration, rankMap);
		}

		SortedMap platformLocaleMap = (SortedMap) rankMap.get(new Integer(rank));

		if (platformLocaleMap == null) {
			platformLocaleMap = new TreeMap();	
			rankMap.put(new Integer(rank), platformLocaleMap);
		}

		Set commandIds = (Set) platformLocaleMap.get(platformLocale);

		if (commandIds == null) {
			commandIds = new HashSet();	
			platformLocaleMap.put(platformLocale, commandIds);
		}

		commandIds.add(commandId);		
	}

	private static void remove(SortedMap contextConfigurationMap, State contextConfiguration, int rank, State platformLocale, String commandId) {
		SortedMap rankMap = (SortedMap) contextConfigurationMap.get(contextConfiguration);

		if (rankMap != null) {
			SortedMap platformLocaleMap = (SortedMap) rankMap.get(new Integer(rank));
			
			if (platformLocaleMap != null) {
				Set commandIds = (Set) platformLocaleMap.get(platformLocale);
				
				if (commandIds != null) {
					commandIds.remove(commandId);	
						
					if (commandIds.isEmpty()) {
						platformLocaleMap.remove(platformLocale);
								
						if (platformLocaleMap.isEmpty()) {
							rankMap.remove(new Integer(rank));
							
							if (rankMap.isEmpty())
								contextConfigurationMap.remove(contextConfiguration);
						}
					}					
				}
			}			
		}
	}
	
	private static String solveCommandIds(Set commandIds) {	
		return commandIds != null && commandIds.size() == 1 ? (String) commandIds.iterator().next() : null;
	}

	private static Envelope solvePlatformLocaleMap(SortedMap platformLocaleMap, State platformLocale) {
		int bestMatch = -1;
		String bestCommandId = null;		
		Iterator iterator = platformLocaleMap.entrySet().iterator();
		boolean match = false;

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testPlatformLocale = (State) entry.getKey();
			Set testCommandIds = (Set) entry.getValue();
			int testMatch = testPlatformLocale.match(platformLocale);

			if (testMatch >= 0) {
				match = true;
				String testCommandId = solveCommandIds(testCommandIds);

				if (testCommandId != null) {
					if (bestMatch == -1 || testMatch < bestMatch) {
						bestMatch = testMatch;
						bestCommandId = testCommandId;
					}
								
					if (bestMatch == 0)
						break;				
				}					
			}	
		}

		return match ? new Envelope(bestCommandId) : null;
	}	

	private static Envelope solvePlatformLocaleMap(SortedMap platformLocaleMap, State[] platformLocales) {
		for (int i = 0; i < platformLocales.length; i++) {
			Envelope envelope = solvePlatformLocaleMap(platformLocaleMap, platformLocales[i]);
				
			if (envelope != null)
				return envelope;
		}
		
		return null;
	}

	private static String solveRankMap(SortedMap rankMap, State[] platformLocales) {
		Iterator iterator = rankMap.values().iterator();
		
		while (iterator.hasNext()) {
			SortedMap platformLocaleMap = (SortedMap) iterator.next();
			Envelope envelope = solvePlatformLocaleMap(platformLocaleMap, platformLocales);
			
			if (envelope != null)
				return envelope.getId();
		}

		return null;
	}

	private static Envelope solveContextConfigurationMap(SortedMap contextConfigurationMap, State contextConfiguration, State[] platformLocales) {
		int bestMatch = -1;
		String bestCommandId = null;
		Iterator iterator = contextConfigurationMap.entrySet().iterator();
		boolean match = false;

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			State testContextConfiguration = (State) entry.getKey();
			SortedMap testRankMap = (SortedMap) entry.getValue();
			int testMatch = testContextConfiguration.match(contextConfiguration);	

			if (testMatch >= 0) {
				match = true;
				String testCommandId = solveRankMap(testRankMap, platformLocales);

				if (testCommandId != null) {
					if (bestMatch == -1 || testMatch < bestMatch) {
						bestMatch = testMatch;
						bestCommandId = testCommandId;
					}
								
					if (bestMatch == 0)
						break;
				}					
			}	
		}

		return match ? new Envelope(bestCommandId) : null;
	}

	private static Envelope solveContextConfigurationMap(SortedMap contextConfigurationMap, State[] contextConfigurations, State[] platformLocales) {
		for (int i = 0; i < contextConfigurations.length; i++) {
			Envelope envelope = solveContextConfigurationMap(contextConfigurationMap, contextConfigurations[i], platformLocales);
				
			if (envelope != null)
				return envelope;
		}
		
		return null;
	}

	private SortedMap childMap = new TreeMap();	
	private String commandId = null;
	private SortedMap contextConfigurationMap = new TreeMap();
	
	private KeyBindingNode() {
	}
}
