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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;

import org.eclipse.ui.internal.util.Util;

final class KeySequenceBindingNode {

	final static class Assignment implements Comparable {
		boolean hasPluginCommandIdInFirstKeyConfiguration;
		boolean hasPluginCommandIdInInheritedKeyConfiguration;

		boolean hasPreferenceCommandIdInFirstKeyConfiguration;
		boolean hasPreferenceCommandIdInInheritedKeyConfiguration;
		String pluginCommandIdInFirstKeyConfiguration;
		String pluginCommandIdInInheritedKeyConfiguration;
		String preferenceCommandIdInFirstKeyConfiguration;
		String preferenceCommandIdInInheritedKeyConfiguration;
		boolean allowedInDialogs;

		public int compareTo(Object object) {
			Assignment castedObject = (Assignment) object;
			int compareTo =
				hasPreferenceCommandIdInFirstKeyConfiguration == false
					? (castedObject.hasPreferenceCommandIdInFirstKeyConfiguration
						== true
						? -1
						: 0)
					: 1;

			if (compareTo == 0) {
				compareTo =
					hasPreferenceCommandIdInInheritedKeyConfiguration == false
						? (castedObject
							.hasPreferenceCommandIdInInheritedKeyConfiguration
							== true
							? -1
							: 0)
						: 1;

				if (compareTo == 0) {
					compareTo =
						hasPluginCommandIdInFirstKeyConfiguration == false
							? (castedObject
								.hasPluginCommandIdInFirstKeyConfiguration
								== true
								? -1
								: 0)
							: 1;

					if (compareTo == 0) {
						compareTo =
							hasPluginCommandIdInInheritedKeyConfiguration
								== false
								? (castedObject
									.hasPluginCommandIdInInheritedKeyConfiguration
									== true
									? -1
									: 0)
								: 1;

						if (compareTo == 0) {
							compareTo =
								Util.compare(
									preferenceCommandIdInFirstKeyConfiguration,
									castedObject
										.preferenceCommandIdInFirstKeyConfiguration);

							if (compareTo == 0) {
								compareTo =
									Util.compare(
										preferenceCommandIdInInheritedKeyConfiguration,
										castedObject
											.preferenceCommandIdInInheritedKeyConfiguration);

								if (compareTo == 0) {
									compareTo =
										Util.compare(
											pluginCommandIdInFirstKeyConfiguration,
											castedObject
												.pluginCommandIdInFirstKeyConfiguration);

									if (compareTo == 0) {
										compareTo =
											Util.compare(
												pluginCommandIdInInheritedKeyConfiguration,
												castedObject
													.pluginCommandIdInInheritedKeyConfiguration);
										
										if (compareTo == 0) {
										    if (allowedInDialogs && !castedObject.allowedInDialogs) {
										        compareTo = 1;
										    } else if (!allowedInDialogs && castedObject.allowedInDialogs) {
										        compareTo = -1;
										    }
										}
									}
								}
							}
						}
					}
				}
			}

			return compareTo;
		}

		boolean contains(String commandId) {
			return Util.equals(
				commandId,
				preferenceCommandIdInFirstKeyConfiguration)
				|| Util.equals(
					commandId,
					preferenceCommandIdInInheritedKeyConfiguration)
				|| Util.equals(commandId, pluginCommandIdInFirstKeyConfiguration)
				|| Util.equals(
					commandId,
					pluginCommandIdInInheritedKeyConfiguration);
		}

		public boolean equals(Object object) {
			if (!(object instanceof Assignment))
				return false;

			Assignment castedObject = (Assignment) object;
			boolean equals = true;
			equals &= hasPreferenceCommandIdInFirstKeyConfiguration
				== castedObject.hasPreferenceCommandIdInFirstKeyConfiguration;
			equals &= hasPreferenceCommandIdInInheritedKeyConfiguration
				== castedObject.hasPreferenceCommandIdInInheritedKeyConfiguration;
			equals &= hasPluginCommandIdInFirstKeyConfiguration
				== castedObject.hasPluginCommandIdInFirstKeyConfiguration;
			equals &= hasPluginCommandIdInInheritedKeyConfiguration
				== castedObject.hasPluginCommandIdInInheritedKeyConfiguration;
			equals &= preferenceCommandIdInFirstKeyConfiguration
				== castedObject.preferenceCommandIdInFirstKeyConfiguration;
			equals &= preferenceCommandIdInInheritedKeyConfiguration
				== castedObject.preferenceCommandIdInInheritedKeyConfiguration;
			equals &= pluginCommandIdInFirstKeyConfiguration
				== castedObject.pluginCommandIdInFirstKeyConfiguration;
			equals &= pluginCommandIdInInheritedKeyConfiguration
				== castedObject.pluginCommandIdInInheritedKeyConfiguration;
			equals &= (allowedInDialogs == castedObject.allowedInDialogs);
			return equals;
		}
	}

	static void add(
		Map keyStrokeNodeByKeyStrokeMap,
		KeySequence keySequence,
		String contextId,
		String keyConfigurationId,
		int rank,
		String platform,
		String locale,
		String commandId,
		boolean allowInDialogs) {
		List keyStrokes = keySequence.getKeyStrokes();
		Map root = keyStrokeNodeByKeyStrokeMap;
		KeySequenceBindingNode keySequenceBindingNode = null;

		for (int i = 0; i < keyStrokes.size(); i++) {
			KeyStroke keyStroke = (KeyStroke) keyStrokes.get(i);
			keySequenceBindingNode =
				(KeySequenceBindingNode) root.get(keyStroke);

			if (keySequenceBindingNode == null) {
				keySequenceBindingNode = new KeySequenceBindingNode();
				root.put(keyStroke, keySequenceBindingNode);
			}

			root = keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
		}

		if (keySequenceBindingNode != null)
			keySequenceBindingNode.add(
				contextId,
				keyConfigurationId,
				rank,
				platform,
				locale,
				commandId,
				allowInDialogs);
	}

	static Map find(Map keyStrokeNodeByKeyStrokeMap, KeySequence keySequence) {
		Iterator iterator = keySequence.getKeyStrokes().iterator();
		KeySequenceBindingNode keySequenceBindingNode = null;

		while (iterator.hasNext()) {
			keySequenceBindingNode =
				(KeySequenceBindingNode) keyStrokeNodeByKeyStrokeMap.get(
					iterator.next());

			if (keySequenceBindingNode == null)
				return null;

			keyStrokeNodeByKeyStrokeMap =
				keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
		}

		return keyStrokeNodeByKeyStrokeMap;
	}

	static Map getAssignmentsByContextIdKeySequence(
		Map keyStrokeNodeByKeyStrokeMap,
		KeySequence prefix) {
		Map assignmentsByContextIdByKeySequence = new HashMap();
		Iterator iterator = keyStrokeNodeByKeyStrokeMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			KeySequenceBindingNode keySequenceBindingNode =
				(KeySequenceBindingNode) entry.getValue();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			Map childAssignmentsByContextIdByKeySequence =
				getAssignmentsByContextIdKeySequence(
					keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
					keySequence);

			if (childAssignmentsByContextIdByKeySequence.size() >= 1)
				assignmentsByContextIdByKeySequence.putAll(
					childAssignmentsByContextIdByKeySequence);

			assignmentsByContextIdByKeySequence.put(
				keySequence,
				keySequenceBindingNode.assignmentsByContextId);
		}

		return assignmentsByContextIdByKeySequence;
	}

	static void getKeySequenceBindingDefinitions(
		Map keyStrokeNodeByKeyStrokeMap,
		KeySequence prefix,
		int rank,
		List keySequenceBindingDefinitions) {
		Iterator iterator = keyStrokeNodeByKeyStrokeMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			KeySequenceBindingNode keySequenceBindingNode =
				(KeySequenceBindingNode) entry.getValue();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			Map contextMap = keySequenceBindingNode.contextMap;
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
							Iterator iterator5 =
								localeMap.entrySet().iterator();

							while (iterator5.hasNext()) {
								Map.Entry entry5 = (Map.Entry) iterator5.next();
								String locale = (String) entry5.getKey();
								Map commandIdMap = (Map) entry5.getValue();
								Iterator iterator6 = commandIdMap.entrySet().iterator();

								while (iterator6.hasNext()) {
									Map.Entry entry6 = (Map.Entry) iterator6.next();
									String commandId = (String) entry6.getKey();
									Boolean allowInDialogs = (Boolean) entry6.getValue();
									
									keySequenceBindingDefinitions.add(
										new KeySequenceBindingDefinition(
											contextId,
											commandId,
											keyConfigurationId,
											keySequence,
											locale,
											platform,
											null,
											allowInDialogs.booleanValue()));
								}
							}
						}
					}
				}
			}

			getKeySequenceBindingDefinitions(
				keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
				keySequence,
				rank,
				keySequenceBindingDefinitions);
		}
	}

	static Map getKeySequenceBindingsByCommandId(Map keySequenceMap) {
		Map commandMap = new HashMap();
		Iterator iterator = keySequenceMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeySequence keySequence = (KeySequence) entry.getKey();
			Match match = (Match) entry.getValue();
			String commandId = match.getCommandId();
			int value = match.getValue();
			SortedSet keySequenceBindings =
				(SortedSet) commandMap.get(commandId);

			if (keySequenceBindings == null) {
				keySequenceBindings = new TreeSet();
				commandMap.put(commandId, keySequenceBindings);
			}

			keySequenceBindings.add(new KeySequenceBinding(keySequence, value));
		}

		return commandMap;
	}

	static Map getMatchesByKeySequence(
		Map keyStrokeNodeByKeyStrokeMap,
		KeySequence prefix) {
		Map keySequenceMap = new HashMap();
		Iterator iterator = keyStrokeNodeByKeyStrokeMap.entrySet().iterator();

		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			KeyStroke keyStroke = (KeyStroke) entry.getKey();
			KeySequenceBindingNode keySequenceBindingNode =
				(KeySequenceBindingNode) entry.getValue();
			List keyStrokes = new ArrayList(prefix.getKeyStrokes());
			keyStrokes.add(keyStroke);
			KeySequence keySequence = KeySequence.getInstance(keyStrokes);
			Map childMatchesByKeySequence =
				getMatchesByKeySequence(
					keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
					keySequence);

			if (childMatchesByKeySequence.size() >= 1)
				keySequenceMap.putAll(childMatchesByKeySequence);
			else if (
				keySequenceBindingNode.match != null
					&& keySequenceBindingNode.match.getCommandId() != null)
				keySequenceMap.put(keySequence, keySequenceBindingNode.match);
		}

		return keySequenceMap;
	}

	static void remove(
		Map keyStrokeNodeByKeyStrokeMap,
		KeySequence keySequence,
		String contextId,
		String keyConfigurationId,
		int rank,
		String platform,
		String locale) {
		Iterator iterator = keySequence.getKeyStrokes().iterator();
		KeySequenceBindingNode keySequenceBindingNode = null;

		while (iterator.hasNext()) {
			keySequenceBindingNode =
				(KeySequenceBindingNode) keyStrokeNodeByKeyStrokeMap.get(
					iterator.next());

			if (keySequenceBindingNode == null)
				return;

			keyStrokeNodeByKeyStrokeMap =
				keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
		}

		keySequenceBindingNode.remove(
			contextId,
			keyConfigurationId,
			rank,
			platform,
			locale);
	}

	static void remove(
		Map keyStrokeNodeByKeyStrokeMap,
		KeySequence keySequence,
		String contextId,
		String keyConfigurationId,
		int rank,
		String platform,
		String locale,
		String commandId) {
		Iterator iterator = keySequence.getKeyStrokes().iterator();
		KeySequenceBindingNode keySequenceBindingNode = null;

		while (iterator.hasNext()) {
			keySequenceBindingNode =
				(KeySequenceBindingNode) keyStrokeNodeByKeyStrokeMap.get(
					iterator.next());

			if (keySequenceBindingNode == null)
				return;

			keyStrokeNodeByKeyStrokeMap =
				keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap;
		}

		keySequenceBindingNode.remove(
			contextId,
			keyConfigurationId,
			rank,
			platform,
			locale,
			commandId);
	}

	static void solve(
		Map keyStrokeNodeByKeyStrokeMap,
		String[] keyConfigurationIds,
		String[] platforms,
		String[] locales) {
		for (Iterator iterator =
			keyStrokeNodeByKeyStrokeMap.values().iterator();
			iterator.hasNext();
			) {
			KeySequenceBindingNode keySequenceBindingNode =
				(KeySequenceBindingNode) iterator.next();
			keySequenceBindingNode.solveAssignmentsByContextId(
				keyConfigurationIds,
				platforms,
				locales);
			solve(
				keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
				keyConfigurationIds,
				platforms,
				locales);
		}
	}

	static void solve(
		Map keyStrokeNodeByKeyStrokeMap,
		String[] contextIds,
		String[] keyConfigurationIds,
		String[] platforms,
		String[] locales) {
		for (Iterator iterator =
			keyStrokeNodeByKeyStrokeMap.values().iterator();
			iterator.hasNext();
			) {
			KeySequenceBindingNode keySequenceBindingNode =
				(KeySequenceBindingNode) iterator.next();
			keySequenceBindingNode.solveMatch(
				contextIds,
				keyConfigurationIds,
				platforms,
				locales);
			solve(
				keySequenceBindingNode.childKeyStrokeNodeByKeyStrokeMap,
				contextIds,
				keyConfigurationIds,
				platforms,
				locales);
		}
	}
	private Map contextMap = new HashMap();

	private Map assignmentsByContextId = new HashMap();
	private Map childKeyStrokeNodeByKeyStrokeMap = new HashMap();
	private Match match = null;

	private KeySequenceBindingNode() {
	    // Do nothing
	}

	private void add(
		String contextId,
		String keyConfigurationId,
		int rank,
		String platform,
		String locale,
		String commandId,
		boolean allowInDialogs) {
		Map keyConfigurationMap = (Map) contextMap.get(contextId);

		if (keyConfigurationMap == null) {
			keyConfigurationMap = new HashMap();
			contextMap.put(contextId, keyConfigurationMap);
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

		Map commandIdMap = (Map) localeMap.get(locale);

		if (commandIdMap == null) {
			commandIdMap = new HashMap();
			localeMap.put(locale, commandIdMap);
		}

		commandIdMap.put(commandId, allowInDialogs ? Boolean.TRUE : Boolean.FALSE);
	}

	private void remove(
		String contextId,
		String keyConfigurationId,
		int rank,
		String platform,
		String locale) {
		Map keyConfigurationMap = (Map) contextMap.get(contextId);

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
									keyConfigurationMap.remove(
										keyConfigurationId);

									if (keyConfigurationMap.isEmpty())
										contextMap.remove(contextId);
								}
							}
						}
					}
				}
			}
		}
	}

	private void remove(
		String contextId,
		String keyConfigurationId,
		int rank,
		String platform,
		String locale,
		String commandId) {
		Map keyConfigurationMap = (Map) contextMap.get(contextId);

		if (keyConfigurationMap != null) {
			Map rankMap = (Map) keyConfigurationMap.get(keyConfigurationId);

			if (rankMap != null) {
				Map platformMap = (Map) rankMap.get(new Integer(rank));

				if (platformMap != null) {
					Map localeMap = (Map) platformMap.get(platform);

					if (localeMap != null) {
						Map commandIdMap = (Map) localeMap.get(locale);

						if (commandIdMap != null) {
							commandIdMap.remove(commandId);

							if (commandIdMap.isEmpty()) {
								localeMap.remove(locale);

								if (localeMap.isEmpty()) {
									platformMap.remove(platform);

									if (platformMap.isEmpty()) {
										rankMap.remove(new Integer(rank));

										if (rankMap.isEmpty()) {
											keyConfigurationMap.remove(
												keyConfigurationId);

											if (keyConfigurationMap.isEmpty())
												contextMap.remove(contextId);
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

	private void solveAssignmentsByContextId(
		String[] keyConfigurationIds,
		String[] platforms,
		String[] locales) {
		assignmentsByContextId.clear();

		for (Iterator iterator = contextMap.entrySet().iterator();
			iterator.hasNext();
			) {
			Map.Entry entry = (Map.Entry) iterator.next();
			String contextId = (String) entry.getKey();
			Map keyConfigurationMap = (Map) entry.getValue();
			KeySequenceBindingNode.Assignment assignment = null;

			if (keyConfigurationMap != null)
				for (int keyConfiguration = 0;
					keyConfiguration < keyConfigurationIds.length
						&& keyConfiguration < 0xFF;
					keyConfiguration++) {
					Map rankMap =
						(Map) keyConfigurationMap.get(
							keyConfigurationIds[keyConfiguration]);

					if (rankMap != null)
						for (int rank = 0; rank <= 1; rank++) {
							Map platformMap =
								(Map) rankMap.get(new Integer(rank));

							if (platformMap != null)
								for (int platform = 0;
									platform < platforms.length
										&& platform < 0xFF;
									platform++) {
									Map localeMap =
										(Map) platformMap.get(
											platforms[platform]);

									if (localeMap != null)
										for (int locale = 0;
											locale < locales.length
												&& locale < 0xFF;
											locale++) {
											Map commandIdMap =
												(Map) localeMap.get(
													locales[locale]);

											if (commandIdMap != null) {
												String commandId =
													commandIdMap.size() == 1
														? (String) commandIdMap
															.keySet().iterator()
															.next()
														: null;

												if (assignment == null)
													assignment =
														new Assignment();
												
												if (commandIdMap.size() == 1) {
												    assignment.allowedInDialogs = ((Boolean) commandIdMap.values().iterator().next()).booleanValue();
												}

												switch (rank) {
													case 0 :
														if (keyConfiguration
															== 0
															&& !assignment
																.hasPreferenceCommandIdInFirstKeyConfiguration) {
															assignment
																.hasPreferenceCommandIdInFirstKeyConfiguration =
																true;
															assignment
																.preferenceCommandIdInFirstKeyConfiguration =
																commandId;
														} else if (
															!assignment
																.hasPreferenceCommandIdInInheritedKeyConfiguration) {
															assignment
																.hasPreferenceCommandIdInInheritedKeyConfiguration =
																true;
															assignment
																.preferenceCommandIdInInheritedKeyConfiguration =
																commandId;
														}

														break;

													case 1 :
														if (keyConfiguration
															== 0
															&& !assignment
																.hasPluginCommandIdInFirstKeyConfiguration) {
															assignment
																.hasPluginCommandIdInFirstKeyConfiguration =
																true;
															assignment
																.pluginCommandIdInFirstKeyConfiguration =
																commandId;
														} else if (
															!assignment
																.hasPluginCommandIdInInheritedKeyConfiguration) {
															assignment
																.hasPluginCommandIdInInheritedKeyConfiguration =
																true;
															assignment
																.pluginCommandIdInInheritedKeyConfiguration =
																commandId;
														}

														break;
												}
											}
										}
								}

						}

				}

			if (assignment != null)
				assignmentsByContextId.put(contextId, assignment);
		}
	}

	private void solveMatch(
		String[] contextIds,
		String[] keyConfigurationIds,
		String[] platforms,
		String[] locales) {
		match = null;

		for (int context = 0;
			context < contextIds.length && context < 0xFF && match == null;
			context++) {
			Map keyConfigurationMap =
				(Map) contextMap.get(contextIds[context]);

			if (keyConfigurationMap != null)
				for (int keyConfiguration = 0;
					keyConfiguration < keyConfigurationIds.length
						&& keyConfiguration < 0xFF
						&& match == null;
					keyConfiguration++) {
					Map rankMap =
						(Map) keyConfigurationMap.get(
							keyConfigurationIds[keyConfiguration]);

					if (rankMap != null) {
						for (int rank = 0; rank <= 1; rank++) {
							Map platformMap =
								(Map) rankMap.get(new Integer(rank));

							if (platformMap != null)
								for (int platform = 0;
									platform < platforms.length
										&& platform < 0xFF
										&& match == null;
									platform++) {
									Map localeMap =
										(Map) platformMap.get(
											platforms[platform]);

									if (localeMap != null)
										for (int locale = 0;
											locale < locales.length
												&& locale < 0xFF
												&& match == null;
											locale++) {
											Map commandIdMap =
												(Map) localeMap.get(
													locales[locale]);

											if (commandIdMap != null)
												match =
													new Match(
														commandIdMap.size() == 1
															? (String) commandIdMap
																.keySet().iterator()
																.next()
															: null,
														(context << 24)
															+ (keyConfiguration
																<< 16)
															+ (platform << 8)
															+ locale);
										}
								}
						}
					}
				}
		}
	}
}
