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

package org.eclipse.ui.internal.commands.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.ui.internal.commands.registry.old.Configuration;
import org.eclipse.ui.internal.commands.registry.old.Context;
import org.eclipse.ui.internal.commands.registry.old.SequenceBinding;
import org.eclipse.ui.internal.commands.util.old.Sequence;
import org.eclipse.ui.internal.commands.util.old.Util;

public final class SequenceMachine {

	public static SequenceMachine create() {
		return new SequenceMachine();
	}

	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static Locale SYSTEM_LOCALE = Locale.getDefault();
	private final static String SYSTEM_PLATFORM = SWT.getPlatform();

	static SortedMap buildPathMapForConfigurationMap(SortedMap configurationMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = configurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = getPathForConfiguration(id, configurationMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	static SortedMap buildPathMapForContextMap(SortedMap contextMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = contextMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = getPathForContext(id, contextMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	static Path getPathForConfiguration(String id, Map configurationMap) {
		Path path = null;

		if (id != null) {
			List strings = new ArrayList();

			while (id != null) {	
				if (strings.contains(id))
					return null;
							
				Configuration configuration = (Configuration) configurationMap.get(id);
				
				if (configuration == null)
					return null;
							
				strings.add(0, id);
				id = configuration.getParent();
			}
		
			path = Path.create(strings);
		}
		
		return path;			
	}

	static Path getPathForContext(String id, Map contextMap) {
		Path path = null;

		if (id != null) {
			List strings = new ArrayList();

			while (id != null) {	
				if (strings.contains(id))
					return null;
							
				Context context = (Context) contextMap.get(id);
				
				if (context == null)
					return null;
							
				strings.add(0, id);
				id = context.getParent();
			}
		
			path = Path.create(strings);
		}
		
		return path;			
	}	

	static Path getPathForLocale(String locale) {
		Path path = null;

		if (locale != null) {
			List strings = new ArrayList();				
			locale = locale.trim();
			
			if (locale.length() > 0) {
				StringTokenizer st = new StringTokenizer(locale, LOCALE_SEPARATOR);
						
				while (st.hasMoreElements()) {
					String string = ((String) st.nextElement()).trim();
					
					if (string != null)
						strings.add(string);
				}
			}

			path = Path.create(strings);
		}
			
		return path;		
	}

	static Path getPathForPlatform(String platform) {
		Path path = null;

		if (platform != null) {
			List strings = new ArrayList();				
			platform = platform.trim();
			
			if (platform.length() > 0)
				strings.add(platform);

			path = Path.create(strings);
		}
			
		return path;		
	}

	static Path getSystemLocale() {
		return SYSTEM_LOCALE != null ? getPathForLocale(SYSTEM_LOCALE.toString()) : null;
	}

	static Path getSystemPlatform() {
		return getPathForPlatform(SYSTEM_PLATFORM);
	}

	private Map commandMap;
	private Map commandMapForMode;	
	private SortedSet sequenceBindingSet;
	private String configuration;
	private SortedMap configurationMap;	
	private SortedMap sequenceMap;
	private SortedMap sequenceMapForMode;
	private Sequence mode;	
	private SortedMap contextMap;
	private String[] contexts;
	private boolean solved;
	private SortedMap tree;

	private SequenceMachine() {
		super();
		configurationMap = new TreeMap();
		contextMap = new TreeMap();
		sequenceBindingSet = new TreeSet();
		configuration = Util.ZERO_LENGTH_STRING;
		contexts = new String[] { Util.ZERO_LENGTH_STRING };
		mode = Sequence.create();	
	}

	public Map getCommandMap() {
		if (commandMap == null) {
			solve();
			commandMap = Collections.unmodifiableMap(SequenceNode.toCommandMap(getSequenceMap()));				
		}
		
		return commandMap;
	}
	
	public Map getCommandMapForMode() {
		if (commandMapForMode == null) {
			solve();
			SortedMap tree = SequenceNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();

			commandMapForMode = Collections.unmodifiableMap(SequenceNode.toCommandMap(getSequenceMapForMode()));				
		}
		
		return commandMapForMode;
	}

	public SortedSet getSequenceBindingSet() {
		return sequenceBindingSet;	
	}

	public String getConfiguration() {
		return configuration;
	}		

	public SortedMap getConfigurationMap() {
		return configurationMap;	
	}

	public Sequence getFirstSequenceForCommand(String command)
		throws IllegalArgumentException {
		if (command == null)
			throw new IllegalArgumentException();					

		SortedSet sequenceSet = (SortedSet) getCommandMap().get(command);
		
		if (sequenceSet != null && !sequenceSet.isEmpty())
			return (Sequence) sequenceSet.first();
		
		return null;
	}

	public SortedMap getSequenceMap() {
		if (sequenceMap == null) {
			solve();
			sequenceMap = Collections.unmodifiableSortedMap(SequenceNode.toSequenceMap(tree, Sequence.create()));				
		}
		
		return sequenceMap;
	}

	public SortedMap getSequenceMapForMode() {
		if (sequenceMapForMode == null) {
			solve();
			SortedMap tree = SequenceNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();
							
			sequenceMapForMode = Collections.unmodifiableSortedMap(SequenceNode.toSequenceMap(tree, mode));				
		}
		
		return sequenceMapForMode;
	}

	public Sequence getMode() {
		return mode;	
	}	

	public SortedMap getContextMap() {
		return contextMap;	
	}	
	
	public String[] getContexts() {
		return (String[]) contexts.clone();
	}		

	public boolean setBindingSet(SortedSet sequenceBindingSet)
		throws IllegalArgumentException {
		if (sequenceBindingSet == null)
			throw new IllegalArgumentException();
		
		sequenceBindingSet = new TreeSet(sequenceBindingSet);
		Iterator iterator = sequenceBindingSet.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof SequenceBinding))
				throw new IllegalArgumentException();

		if (this.sequenceBindingSet.equals(sequenceBindingSet))
			return false;
		
		this.sequenceBindingSet = Collections.unmodifiableSortedSet(sequenceBindingSet);
		invalidateTree();
		return true;
	}

	public boolean setConfiguration(String configuration) {
		if (configuration == null)
			throw new IllegalArgumentException();
			
		if (this.configuration.equals(configuration))
			return false;
		
		this.configuration = configuration;
		invalidateSolution();
		return true;
	}

	public boolean setConfigurationMap(SortedMap configurationMap)
		throws IllegalArgumentException {
		if (configurationMap == null)
			throw new IllegalArgumentException();
			
		configurationMap = new TreeMap(configurationMap);
		Iterator iterator = configurationMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Path))
				throw new IllegalArgumentException();			
		}

		if (this.configurationMap.equals(configurationMap))
			return false;
					
		this.configurationMap = Collections.unmodifiableSortedMap(configurationMap);
		invalidateTree();
		return true;
	}

	public boolean setMode(Sequence mode)
		throws IllegalArgumentException {
		if (mode == null)
			throw new IllegalArgumentException();
			
		if (this.mode.equals(mode))
			return false;
		
		this.mode = mode;
		invalidateMode();
		return true;
	}
	
	public boolean setContextMap(SortedMap contextMap)
		throws IllegalArgumentException {
		if (contextMap == null)
			throw new IllegalArgumentException();
			
		contextMap = new TreeMap(contextMap);
		Iterator iterator = contextMap.entrySet().iterator();
		
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			
			if (!(entry.getKey() instanceof String) || !(entry.getValue() instanceof Path))
				throw new IllegalArgumentException();			
		}
		
		if (this.contextMap.equals(contextMap))
			return false;
				
		this.contextMap = Collections.unmodifiableSortedMap(contextMap);
		invalidateTree();
		return true;
	}	
	
	public boolean setContexts(String[] contexts)
		throws IllegalArgumentException {
		if (contexts == null || contexts.length == 0)
			throw new IllegalArgumentException();

		contexts = (String[]) contexts.clone();
		
		for (int i = 0; i < contexts.length; i++)
			if (contexts[i] == null)
				throw new IllegalArgumentException();	
		
		if (Arrays.equals(this.contexts, contexts))
			return false;
		
		this.contexts = contexts;
		invalidateSolution();
		return true;		
	}	

	private void build() {
		if (tree == null) {		
			tree = new TreeMap();
			Iterator iterator = sequenceBindingSet.iterator();
		
			while (iterator.hasNext()) {
				SequenceBinding sequenceBinding = (SequenceBinding) iterator.next();
				Path context = (Path) contextMap.get(sequenceBinding.getContext());
		
				if (context == null)
					continue;

				Path configuration = (Path) configurationMap.get(sequenceBinding.getConfiguration());
					
				if (configuration == null)
					continue;

				List paths = new ArrayList();
				paths.add(context);
				paths.add(configuration);
				State contextConfiguration = State.create(paths);						
				paths = new ArrayList();
				paths.add(getPathForPlatform(sequenceBinding.getPlatform()));
				paths.add(getPathForLocale(sequenceBinding.getLocale()));
				State platformLocale = State.create(paths);		
				SequenceNode.add(tree, sequenceBinding, contextConfiguration, platformLocale);
			}
		}
	}

	private void invalidateMode() {
		commandMapForMode = null;
		sequenceMapForMode = null;
	}

	private void invalidateSolution() {
		solved = false;
		commandMap = null;	
		sequenceMap = null;
		invalidateMode();
	}
	
	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}
	
	private void solve() {
		if (!solved) {
			build();
			State[] contextConfigurations = new State[contexts.length];
			Path configuration = (Path) configurationMap.get(this.configuration);
			
			if (configuration == null)
				configuration = Path.create();
							
			for (int i = 0; i < contexts.length; i++) {
				Path context = (Path) contextMap.get(contexts[i]);
			
				if (context == null)
					context = Path.create();

				List paths = new ArrayList();
				paths.add(context);
				paths.add(configuration);		
				contextConfigurations[i] = State.create(paths);
			}
			
			List paths = new ArrayList();
			paths.add(getSystemPlatform());
			paths.add(getSystemLocale());
			State platformLocale = State.create(paths);			
			SequenceNode.solve(tree, contextConfigurations, new State[] { platformLocale } );
			solved = true;
		}
	}
}
