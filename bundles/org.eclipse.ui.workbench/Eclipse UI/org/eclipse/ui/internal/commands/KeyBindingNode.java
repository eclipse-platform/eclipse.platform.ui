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

	static void add(SortedMap tree, KeySequence keySequence, String context, String configuration, int rank, String platform, String locale, String commandId) {		 
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

		if (keyBindingNode != null) {
			SortedMap configurationMap = (SortedMap) keyBindingNode.contextMap.get(context);
		
			if (configurationMap == null) {
				configurationMap = new TreeMap();	
				keyBindingNode.contextMap.put(context, configurationMap);
			}

			SortedMap rankMap = (SortedMap) configurationMap.get(configuration);
		
			if (rankMap == null) {
				rankMap = new TreeMap();	
				configurationMap.put(configuration, rankMap);
			}

			SortedMap platformMap = (SortedMap) rankMap.get(new Integer(rank));

			if (platformMap == null) {
				platformMap = new TreeMap();	
				rankMap.put(new Integer(rank), platformMap);
			}

			SortedMap localeMap = (SortedMap) platformMap.get(platform);

			if (localeMap == null) {
				localeMap = new TreeMap();	
				platformMap.put(platform, localeMap);
			}

			Set commandIds = (Set) localeMap.get(locale);

			if (commandIds == null) {
				commandIds = new HashSet();	
				localeMap.put(locale, commandIds);
			}

			commandIds.add(commandId);					
		}
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

	static Map getKeyBindingsByCommandId(SortedMap keySequenceMap) {
		Map commandMap = new HashMap();
		Iterator iterator = keySequenceMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();			
			Match match = (Match) entry.getValue();
			String commandId = match.getCommandId();
			int value = match.getValue();
			SortedSet keyBindings = (SortedSet) commandMap.get(commandId);
			
			if (keyBindings == null) {
				keyBindings = new TreeSet();
				commandMap.put(commandId, keyBindings);			
			}
			
			keyBindings.add(new KeyBinding(keySequence, value));
		}	
		
		return commandMap;		
	}

	static SortedMap getMatchesByKeySequence(SortedMap tree, KeySequence prefix) {
		SortedMap keySequenceMap = new TreeMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();			
			KeyBindingNode keyBindingNode = (KeyBindingNode) entry.getValue();					
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			SortedMap childMatchesByKeySequence = getMatchesByKeySequence(keyBindingNode.childMap, keySequence);

			if (childMatchesByKeySequence.size() >= 1)
				keySequenceMap.putAll(childMatchesByKeySequence);
			else if (keyBindingNode.match != null && !keyBindingNode.match.getCommandId().equals(Util.ZERO_LENGTH_STRING))		
				keySequenceMap.put(keySequence, keyBindingNode.match);
		}

		return keySequenceMap;
	}

	static void remove(SortedMap tree, KeySequence keySequence, String context, String configuration, int rank, String platform, String locale, String commandId) {
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

		if (keyBindingNode != null) {
			SortedMap configurationMap = (SortedMap) keyBindingNode.contextMap.get(context);

			if (configurationMap != null) {
				SortedMap rankMap = (SortedMap) configurationMap.get(configuration);
	
				if (rankMap != null) {
					SortedMap platformMap = (SortedMap) rankMap.get(new Integer(rank));
				
					if (platformMap != null) {
						SortedMap localeMap = (SortedMap) platformMap.get(platform);

						if (localeMap != null) {	
							Set commandIds = (Set) localeMap.get(locale);
					
							if (commandIds != null) {
								commandIds.remove(commandId);	
								
								if (commandIds.isEmpty()) {
									localeMap.remove(locale);
										
									if (localeMap.isEmpty()) {
										platformMap.remove(platform);
									
										if (platformMap.isEmpty()) {
											rankMap.remove(new Integer(rank));
									
											if (rankMap.isEmpty()) {
												configurationMap.remove(configuration);

												if (configurationMap.isEmpty())
													keyBindingNode.contextMap.remove(context);
											}
										}
									}
								}					
							}
						}
					}			
				}
			}			
		}
	}

	static void solve(SortedMap tree, String[] contexts, String[] configurations, String[] platforms, String[] locales) {
		Iterator iterator = tree.values().iterator();	
		
		while (iterator.hasNext()) {
			KeyBindingNode keyBindingNode = (KeyBindingNode) iterator.next();
			keyBindingNode.match = null;		
			
			for (int i = 0; i < contexts.length && i < 0xFF && keyBindingNode.match == null; i++) {
				SortedMap configurationMap = (SortedMap) keyBindingNode.contextMap.get(contexts[i]);
			
				if (configurationMap != null)
					for (int j = 0; j < configurations.length && j < 0xFF && keyBindingNode.match == null; j++) {
						SortedMap rankMap = (SortedMap) configurationMap.get(configurations[j]);
			
						if (rankMap != null) {
							Iterator iterator2 = rankMap.values().iterator();
		
							while (iterator2.hasNext()) {
								SortedMap platformMap = (SortedMap) iterator2.next();
								
								if (platformMap != null)
									for (int k = 0; k < platforms.length && k < 0xFF && keyBindingNode.match == null; k++) {
										SortedMap localeMap = (SortedMap) platformMap.get(platforms[k]);
				
										if (localeMap != null)
											for (int l = 0; l < locales.length && l < 0xFF && keyBindingNode.match == null; l++) {
												Set commandIds = (Set) localeMap.get(locales[l]);
				
												if (commandIds != null)
													keyBindingNode.match = new Match(commandIds.size() == 1 ? (String) commandIds.iterator().next() : null, (i << 24) + (j << 16) + (k << 8) + l);
											}
									}								
							}
						}
					}
			}
		
			solve(keyBindingNode.childMap, contexts, configurations, platforms, locales);								
		}		
	}

	private SortedMap childMap = new TreeMap();	
	private SortedMap contextMap = new TreeMap();
	private Match match = null;
	
	private KeyBindingNode() {
	}
}
