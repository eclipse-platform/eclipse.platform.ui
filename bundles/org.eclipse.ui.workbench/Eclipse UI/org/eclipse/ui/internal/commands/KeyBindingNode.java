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
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

final class KeyBindingNode {

	final static class Assignment implements Comparable {
		
		boolean hasPreferenceCommandIdInFirstKeyConfiguration;
		boolean hasPreferenceCommandIdInInheritedKeyConfiguration;
		boolean hasPluginCommandIdInFirstKeyConfiguration;
		boolean hasPluginCommandIdInInheritedKeyConfiguration;
		String preferenceCommandIdInFirstKeyConfiguration;
		String preferenceCommandIdInInheritedKeyConfiguration;
		String pluginCommandIdInFirstKeyConfiguration;
		String pluginCommandIdInInheritedKeyConfiguration;

		public int compareTo(Object object) {
			Assignment assignment = (Assignment) object;
			int compareTo = hasPreferenceCommandIdInFirstKeyConfiguration == false ? (assignment.hasPreferenceCommandIdInFirstKeyConfiguration == true ? -1 : 0) : 1;
		
			if (compareTo == 0) {
				compareTo = hasPreferenceCommandIdInInheritedKeyConfiguration == false ? (assignment.hasPreferenceCommandIdInInheritedKeyConfiguration == true ? -1 : 0) : 1;

				if (compareTo == 0) {	
					compareTo = hasPluginCommandIdInFirstKeyConfiguration == false ? (assignment.hasPluginCommandIdInFirstKeyConfiguration == true ? -1 : 0) : 1;

					if (compareTo == 0) {
						compareTo = hasPluginCommandIdInInheritedKeyConfiguration == false ? (assignment.hasPluginCommandIdInInheritedKeyConfiguration == true ? -1 : 0) : 1;

						if (compareTo == 0) {		
							compareTo = Util.compare(preferenceCommandIdInFirstKeyConfiguration, assignment.preferenceCommandIdInFirstKeyConfiguration);	
	
							if (compareTo == 0) {
								compareTo = Util.compare(preferenceCommandIdInInheritedKeyConfiguration, assignment.preferenceCommandIdInInheritedKeyConfiguration);
	
								if (compareTo == 0) {
									compareTo = Util.compare(pluginCommandIdInFirstKeyConfiguration, assignment.pluginCommandIdInFirstKeyConfiguration);	
	
									if (compareTo == 0)
										compareTo = Util.compare(pluginCommandIdInInheritedKeyConfiguration, assignment.pluginCommandIdInInheritedKeyConfiguration);
								}
							}
						}
					}
				}
			}
		
			return compareTo;	
		}
		
		public boolean equals(Object object) {
			if (!(object instanceof Assignment))
				return false;

			Assignment assignment = (Assignment) object;	
			boolean equals = true;
			equals &= hasPreferenceCommandIdInFirstKeyConfiguration == assignment.hasPreferenceCommandIdInFirstKeyConfiguration;
			equals &= hasPreferenceCommandIdInInheritedKeyConfiguration == assignment.hasPreferenceCommandIdInInheritedKeyConfiguration;
			equals &= hasPluginCommandIdInFirstKeyConfiguration == assignment.hasPluginCommandIdInFirstKeyConfiguration;
			equals &= hasPluginCommandIdInInheritedKeyConfiguration == assignment.hasPluginCommandIdInInheritedKeyConfiguration;
			equals &= preferenceCommandIdInFirstKeyConfiguration == assignment.preferenceCommandIdInFirstKeyConfiguration;
			equals &= preferenceCommandIdInInheritedKeyConfiguration == assignment.preferenceCommandIdInInheritedKeyConfiguration;
			equals &= pluginCommandIdInFirstKeyConfiguration == assignment.pluginCommandIdInFirstKeyConfiguration;
			equals &= pluginCommandIdInInheritedKeyConfiguration == assignment.pluginCommandIdInInheritedKeyConfiguration;
			return equals;
		}
		
		boolean contains(String commandId) {
			return Util.equals(commandId, preferenceCommandIdInFirstKeyConfiguration) || Util.equals(commandId, preferenceCommandIdInInheritedKeyConfiguration) || Util.equals(commandId, pluginCommandIdInFirstKeyConfiguration) || Util.equals(commandId, pluginCommandIdInInheritedKeyConfiguration);
		}
	}

	static void add(Map tree, KeySequence keySequence, String contextId, String keyConfigurationId, int rank, String platform, String locale, String commandId) {		 	
		List keyStrokes = keySequence.getKeyStrokes();		
		Map root = tree;
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
			Map keyConfigurationMap = (Map) keyBindingNode.contextMap.get(contextId);
		
			if (keyConfigurationMap == null) {
				keyConfigurationMap = new HashMap();	
				keyBindingNode.contextMap.put(contextId, keyConfigurationMap);
			}

			Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);
		
			if (rankMap == null) {
				rankMap = new HashMap();	
				keyConfigurationMap.put(keyConfigurationId, rankMap);
			}

			Map platformMap = (Map) rankMap.get(new Integer(rank));

			if (platformMap == null) {
				platformMap = new HashMap();	
				rankMap.put(new Integer(rank), platformMap);
			}

			Map localeMap = (Map) platformMap.get(platform);

			if (localeMap == null) {
				localeMap = new HashMap();	
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

	static Map find(Map tree, KeySequence prefix) {	
		Iterator iterator = prefix.getKeyStrokes().iterator();
	
		while (iterator.hasNext()) {
			KeyBindingNode keyBindingNode = (KeyBindingNode) tree.get(iterator.next());
			
			if (keyBindingNode == null)
				return new HashMap();
								
			tree = keyBindingNode.childMap;
		}		
		
		return tree;			
	}

	static Map getAssignmentsByContextIdKeySequence(Map tree, KeySequence prefix) {
		Map assignmentsByContextIdByKeySequence = new HashMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();			
			KeyBindingNode keyBindingNode = (KeyBindingNode) entry.getValue();					
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			Map childAssignmentsByContextIdByKeySequence = getAssignmentsByContextIdKeySequence(keyBindingNode.childMap, keySequence);

			if (childAssignmentsByContextIdByKeySequence.size() >= 1)
				assignmentsByContextIdByKeySequence.putAll(childAssignmentsByContextIdByKeySequence);
			
			assignmentsByContextIdByKeySequence.put(keySequence, keyBindingNode.assignmentsByContextId);
		}

		return assignmentsByContextIdByKeySequence;
	}

	static void getKeyBindingDefinitions(Map tree, KeySequence prefix, int rank, List keyBindingDefinitions) {
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();			
			KeyBindingNode keyBindingNode = (KeyBindingNode) entry.getValue();					
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			Map contextMap = keyBindingNode.contextMap;			
			Iterator iterator2 = contextMap.entrySet().iterator();
				
			while (iterator2.hasNext()) {
				Map.Entry entry2 = (Map.Entry) iterator2.next();
				String contextId = (String) entry2.getKey();			
				Map keyConfigurationMap = (Map) entry2.getValue();					
				Iterator iterator3 = keyConfigurationMap.entrySet().iterator();
					
				while (iterator3.hasNext()) {
					Map.Entry entry3 = (Map.Entry) iterator3.next();
					String keyConfigurationId = (String) entry3.getKey();			
					Map rankMap = (Map) entry3.getValue();					
					Map platformMap = (Map) rankMap.get(new Integer(rank));

					if (platformMap != null) {
						Iterator iterator4 = platformMap.entrySet().iterator();
					
						while (iterator4.hasNext()) {
							Map.Entry entry4 = (Map.Entry) iterator4.next();
							String platform = (String) entry4.getKey();			
							Map localeMap = (Map) entry4.getValue();									
							Iterator iterator5 = localeMap.entrySet().iterator();
					
							while (iterator5.hasNext()) {
								Map.Entry entry5 = (Map.Entry) iterator5.next();
								String locale = (String) entry5.getKey();			
								Set commandIds = (Set) entry5.getValue();
								Iterator iterator6 = commandIds.iterator();
								
								while (iterator6.hasNext()) {
									String commandId = (String) iterator6.next();
									keyBindingDefinitions.add(new KeyBindingDefinition(commandId, contextId, keyConfigurationId, keySequence, locale, platform, null));	
								}								
							}
						}
					}					
				}
			}
			
			getKeyBindingDefinitions(keyBindingNode.childMap, keySequence, rank, keyBindingDefinitions);
		}
	}

	static Map getKeyBindingsByCommandId(Map keySequenceMap) {
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

	static Map getMatchesByKeySequence(Map tree, KeySequence prefix) {
		Map keySequenceMap = new HashMap();
		Iterator iterator = tree.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();			
			KeyBindingNode keyBindingNode = (KeyBindingNode) entry.getValue();					
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			Map childMatchesByKeySequence = getMatchesByKeySequence(keyBindingNode.childMap, keySequence);

			if (childMatchesByKeySequence.size() >= 1)
				keySequenceMap.putAll(childMatchesByKeySequence);
			else if (keyBindingNode.match != null && keyBindingNode.match.getCommandId() != null)		
				keySequenceMap.put(keySequence, keyBindingNode.match);
		}

		return keySequenceMap;
	}

	static void remove(Map tree, KeySequence keySequence, String contextId, String keyConfigurationId, int rank, String platform, String locale) {
		List keyStrokes = keySequence.getKeyStrokes();		
		Map root = tree;
		KeyBindingNode keyBindingNode = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			keyBindingNode = (KeyBindingNode) root.get(keyStroke);
			
			if (keyBindingNode == null)
				break;
			
			root = keyBindingNode.childMap;
		}

		if (keyBindingNode != null) {
			Map keyConfigurationMap = (Map) keyBindingNode.contextMap.get(contextId);

			if (keyConfigurationMap != null) {
				Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);
	
				if (rankMap != null) {
					Map platformMap = (Map) rankMap.get(new Integer(rank));
				
					if (platformMap != null) {
						Map localeMap = (Map) platformMap.get(platform);

						if (localeMap != null) {	
							localeMap.remove(locale);
										
							if (localeMap.isEmpty()) {
								platformMap.remove(platform);
									
								if (platformMap.isEmpty()) {
									rankMap.remove(new Integer(rank));
									
									if (rankMap.isEmpty()) {
										keyConfigurationMap.remove(keyConfigurationId);

										if (keyConfigurationMap.isEmpty())
											keyBindingNode.contextMap.remove(contextId);
									}
								}					
							}
						}
					}			
				}
			}			
		}
	}

	static void remove(Map tree, KeySequence keySequence, String contextId, String keyConfigurationId, int rank, String platform, String locale, String commandId) {
		List keyStrokes = keySequence.getKeyStrokes();		
		Map root = tree;
		KeyBindingNode keyBindingNode = null;
	
		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			keyBindingNode = (KeyBindingNode) root.get(keyStroke);
			
			if (keyBindingNode == null)
				break;
			
			root = keyBindingNode.childMap;
		}

		if (keyBindingNode != null) {
			Map keyConfigurationMap = (Map) keyBindingNode.contextMap.get(contextId);

			if (keyConfigurationMap != null) {
				Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);
	
				if (rankMap != null) {
					Map platformMap = (Map) rankMap.get(new Integer(rank));
				
					if (platformMap != null) {
						Map localeMap = (Map) platformMap.get(platform);

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
												keyConfigurationMap.remove(keyConfigurationId);

												if (keyConfigurationMap.isEmpty())
													keyBindingNode.contextMap.remove(contextId);
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

	static void solve(Map tree, String[] keyConfigurationIds, String[] platforms, String[] locales) {
		for (Iterator iterator = tree.values().iterator(); iterator.hasNext();) {	
			KeyBindingNode keyBindingNode = (KeyBindingNode) iterator.next();
			keyBindingNode.assignmentsByContextId.clear();		

			for (Iterator iterator2 = keyBindingNode.contextMap.entrySet().iterator(); iterator2.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator2.next();
				String contextId = (String) entry.getKey();
				Map keyConfigurationMap = (Map) entry.getValue();
				KeyBindingNode.Assignment assignment = null;
				
				if (keyConfigurationMap != null)
					for (int keyConfiguration = 0; keyConfiguration < keyConfigurationIds.length && keyConfiguration < 0xFF; keyConfiguration++) {
						Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationIds[keyConfiguration]);
				
						if (rankMap != null)
							for (int rank = 0; rank <= 1; rank++) {
								Map platformMap = (Map) rankMap.get(new Integer(rank));

								if (platformMap != null)
									for (int platform = 0; platform < platforms.length && platform < 0xFF; platform++) {
										Map localeMap = (Map) platformMap.get(platforms[platform]);
					
										if (localeMap != null)
											for (int locale = 0; locale < locales.length && locale < 0xFF; locale++) {
												Set commandIds = (Set) localeMap.get(locales[locale]);
					
												if (commandIds != null) {
													String commandId = commandIds.size() == 1 ? (String) commandIds.iterator().next() : null;
																												
													if (assignment == null)
														assignment = new Assignment();
														
													switch (rank) {
														case 0:
															if (keyConfiguration == 0 && !assignment.hasPreferenceCommandIdInFirstKeyConfiguration) {															
																assignment.hasPreferenceCommandIdInFirstKeyConfiguration = true;
																assignment.preferenceCommandIdInFirstKeyConfiguration = commandId;
															} else if (!assignment.hasPreferenceCommandIdInInheritedKeyConfiguration) {
																assignment.hasPreferenceCommandIdInInheritedKeyConfiguration = true;
																assignment.preferenceCommandIdInInheritedKeyConfiguration = commandId;
															}
															
															break;
															
														case 1:										
															if (keyConfiguration == 0 && !assignment.hasPluginCommandIdInFirstKeyConfiguration) {															
																assignment.hasPluginCommandIdInFirstKeyConfiguration = true;
																assignment.pluginCommandIdInFirstKeyConfiguration = commandId;
															} else if (!assignment.hasPluginCommandIdInInheritedKeyConfiguration) {
																assignment.hasPluginCommandIdInInheritedKeyConfiguration = true;
																assignment.pluginCommandIdInInheritedKeyConfiguration = commandId;
															}
															
															break;
													}
												}
											}
									}								
									
							}
							
					}
	
				if (assignment != null)
					keyBindingNode.assignmentsByContextId.put(contextId, assignment);
			}

			solve(keyBindingNode.childMap, keyConfigurationIds, platforms, locales);								
		}		
	}

	static void solve(Map tree, String[] contextIds, String[] keyConfigurationIds, String[] platforms, String[] locales) {
		for (Iterator iterator = tree.values().iterator(); iterator.hasNext();) {
			KeyBindingNode keyBindingNode = (KeyBindingNode) iterator.next();
			keyBindingNode.match = null;		
			
			for (int context = 0; context < contextIds.length && context < 0xFF && keyBindingNode.match == null; context++) {
				Map keyConfigurationMap = (Map) keyBindingNode.contextMap.get(contextIds[context]);
			
				if (keyConfigurationMap != null)
					for (int keyConfiguration = 0; keyConfiguration < keyConfigurationIds.length && keyConfiguration < 0xFF && keyBindingNode.match == null; keyConfiguration++) {
						Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationIds[keyConfiguration]);
		
						if (rankMap != null) {
							for (int rank = 0; rank <= 1; rank++) {
								Map platformMap = (Map) rankMap.get(new Integer(rank));
								
								if (platformMap != null)
									for (int platform = 0; platform < platforms.length && platform < 0xFF && keyBindingNode.match == null; platform++) {
										Map localeMap = (Map) platformMap.get(platforms[platform]);
				
										if (localeMap != null)
											for (int locale = 0; locale < locales.length && locale < 0xFF && keyBindingNode.match == null; locale++) {
												Set commandIds = (Set) localeMap.get(locales[locale]);
				
												if (commandIds != null)
													keyBindingNode.match = new Match(commandIds.size() == 1 ? (String) commandIds.iterator().next() : null, (context << 24) + (keyConfiguration << 16) + (platform << 8) + locale);
											}
									}								
							}
						}
					}
			}
		
			solve(keyBindingNode.childMap, contextIds, keyConfigurationIds, platforms, locales);								
		}		
	}

	private Map assignmentsByContextId = new HashMap();
	private Map childMap = new HashMap();	
	private Map contextMap = new HashMap();
	private Match match = null;
	
	private KeyBindingNode() {
	}
}
