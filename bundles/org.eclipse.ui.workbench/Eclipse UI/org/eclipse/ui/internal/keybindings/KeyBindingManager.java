/*
Copyright (c) 2000, 2001, 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

package org.eclipse.ui.internal.keybindings;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.Accelerator;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.AcceleratorScope;
import org.eclipse.ui.internal.registry.AcceleratorSet;

public final class KeyBindingManager {

	private final static String KEY_SEQUENCE_SEPARATOR = ", "; //$NON-NLS-1$
	private final static String KEY_STROKE_SEPARATOR = " "; //$NON-NLS-1$
	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static String OR_SEPARATOR = "||"; //$NON-NLS-1$
	private final static String ZERO_LENGTH_STRING = ""; //$NON-NLS-1$

	private static KeyBindingManager instance;

	public static KeyBindingManager getInstance() {
		if (instance == null)
			instance = new KeyBindingManager();
			
		return instance;	
	}
	
	private static SortedMap loadConfigurationMap() {
		AcceleratorRegistry acceleratorRegistry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();	
		return buildConfigurationMap(acceleratorRegistry.getAcceleratorConfigurations());	
	}
	
	private static SortedMap buildConfigurationMap(Map acceleratorConfigurationMap) {
		SortedMap configurationMap = new TreeMap();
		Iterator iterator = acceleratorConfigurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForConfigurationId(id, acceleratorConfigurationMap);
			
				if (path != null)
					configurationMap.put(id, path);
			}			
		}

		return configurationMap;		
	}

	private static Path pathForConfigurationId(String id, Map acceleratorConfigurationsMap) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				AcceleratorConfiguration acceleratorConfiguration = (AcceleratorConfiguration) acceleratorConfigurationsMap.get(id);
				
				if (acceleratorConfiguration == null)
					return null;
							
				pathItems.add(0, PathItem.create(id));
				id = acceleratorConfiguration.getParentId();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}	

	private static SortedMap loadScopeMap() {
		AcceleratorRegistry acceleratorRegistry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();	
		return buildScopeMap(acceleratorRegistry.getAcceleratorScopes());	
	}
	
	private static SortedMap buildScopeMap(Map acceleratorScopeMap) {
		SortedMap scopeMap = new TreeMap();
		Iterator iterator = acceleratorScopeMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {
				Path path = pathForScopeId(id, acceleratorScopeMap);
			
				if (path != null)
					scopeMap.put(id, path);
			}
		}

		return scopeMap;		
	}

	private static Path pathForScopeId(String id, Map acceleratorScopeMap) {
		Path path = null;
		
		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				AcceleratorScope acceleratorScope = (AcceleratorScope) acceleratorScopeMap.get(id);
				
				if (acceleratorScope == null)
					return null;
							
				pathItems.add(0, PathItem.create(id));
				id = acceleratorScope.getParentId();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;	
	}	

	static Path pathForLocale(String locale) {
		Path path = null;

		if (locale != null) {
			List pathItems = new ArrayList();				
			locale = locale.trim();
			
			if (locale.length() > 0) {
				StringTokenizer st = new StringTokenizer(locale, LOCALE_SEPARATOR);
						
				while (st.hasMoreElements()) {
					String value = ((String) st.nextElement()).trim();
					
					if (value != null)
						pathItems.add(PathItem.create(value));
				}
			}

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	private static Path systemLocale() {
		java.util.Locale locale = java.util.Locale.getDefault();
		return locale != null ? pathForLocale(locale.toString()) : null;
	}

	static Path pathForPlatform(String platform) {
		Path path = null;

		if (platform != null) {
			List pathItems = new ArrayList();				
			platform = platform.trim();
			
			if (platform.length() > 0)
				pathItems.add(PathItem.create(platform));

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	private static Path systemPlatform() {
		return pathForPlatform(SWT.getPlatform());
	}
	
	private static List loadKeyBindings() {
		List keyBindings = new ArrayList();
		AcceleratorRegistry acceleratorRegistry = WorkbenchPlugin.getDefault().getAcceleratorRegistry();	
		List acceleratorSets = acceleratorRegistry.getAcceleratorSets();
		Iterator iterator = acceleratorSets.iterator();
		
		while (iterator.hasNext()) {
			AcceleratorSet acceleratorSet = (AcceleratorSet) iterator.next();
			String configuration = acceleratorSet.getAcceleratorConfigurationId();
			
			if (configuration == null)
				configuration = ZERO_LENGTH_STRING;
			
			String scope = acceleratorSet.getAcceleratorScopeId();

			if (scope == null)
				scope = ZERO_LENGTH_STRING;

			String plugin = acceleratorSet.getPluginId();			
			List accelerators = acceleratorSet.getAccelerators();
			Iterator iterator2 = accelerators.iterator();
			
			while (iterator2.hasNext()) {
				Accelerator accelerator = (Accelerator) iterator2.next();				
				List keySequences = parseKeySequences(accelerator.getKey());
			
				if (keySequences == null)
					continue;

				String action = accelerator.getId();									
				String locale = accelerator.getLocale();
			
				if (locale == null)
					locale = ZERO_LENGTH_STRING;
				
				String platform = accelerator.getPlatform();				

				if (platform == null)
					platform = ZERO_LENGTH_STRING;
				
				Iterator iterator3 = keySequences.iterator();
				
				while (iterator3.hasNext()) {
					KeySequence keySequence = (KeySequence) iterator3.next();
							
					if (keySequence.getKeyStrokes().size() >= 1)						
						keyBindings.add(KeyBinding.create(keySequence, action, configuration, locale, platform, plugin, scope));
				}
			}			
		}
		
		return keyBindings;
	}
	
	private static List parseKeySequences(String keys) {
		List keySequences = null;
		
		if (keys != null) {
			keySequences = new ArrayList();
			StringTokenizer orTokenizer = new StringTokenizer(keys, OR_SEPARATOR); 
			
			while (orTokenizer.hasMoreTokens()) {
				List keyStrokes = new ArrayList();
				StringTokenizer spaceTokenizer = new StringTokenizer(orTokenizer.nextToken());
				
				while (spaceTokenizer.hasMoreTokens()) {
					int accelerator = org.eclipse.jface.action.Action.convertAccelerator(spaceTokenizer.nextToken());
					
					if (accelerator != 0)
						keyStrokes.add(KeyStroke.create(accelerator));
				}
				
				if (keyStrokes.size() >= 1)
					keySequences.add(KeySequence.create(keyStrokes));		
			}
		}

		return keySequences;
	}

	private static List readKeyBindingsFromReader(Reader reader)
		throws IOException {
		try {
			XMLMemento xmlMemento = XMLMemento.createReadRoot(reader);
			return readKeyBindings(xmlMemento);
		} catch (WorkbenchException eWorkbench) {
			throw new IOException();	
		}
	}

	private static void writeKeyBindingsToWriter(Writer writer, String root, List keyBindings)
		throws IOException {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(root);
		writeKeyBindings(xmlMemento, keyBindings);
		xmlMemento.save(writer);
	}

	private static List readKeyBindings(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			
		
		IMemento[] mementos = memento.getChildren(KeyBinding.ELEMENT);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List keyBindings = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			keyBindings.add(KeyBinding.read(mementos[i]));
		
		return keyBindings;		
	}

	private static void writeKeyBindings(IMemento memento, List keyBindings)
		throws IllegalArgumentException {
		if (memento == null || keyBindings == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext())
			((KeyBinding) iterator.next()).write(memento.createChild(KeyBinding.ELEMENT)); 
	}

	private static void filterAction(List keyBindings, Set actionSet, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !actionSet.contains(keyBinding.getAction()))
				iterator.remove();
		}
	}

	private static void filterConfiguration(List keyBindings, Set configurationSet, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !configurationSet.contains(keyBinding.getConfiguration()))
				iterator.remove();
		}
	}

	private static void filterLocale(List keyBindings, Set localeSet, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !localeSet.contains(keyBinding.getLocale()))
				iterator.remove();
		}
	}

	private static void filterPlatform(List keyBindings, Set platformSet, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !platformSet.contains(keyBinding.getPlatform()))
				iterator.remove();
		}
	}

	private static void filterPlugin(List keyBindings, Set pluginSet, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !pluginSet.contains(keyBinding.getPlugin()))
				iterator.remove();
		}
	}

	private static void filterScope(List keyBindings, Set scopeSet, boolean exclusive) {
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext()) {
			KeyBinding keyBinding = (KeyBinding) iterator.next();
			
			if (exclusive ^ !scopeSet.contains(keyBinding.getScope()))
				iterator.remove();
		}
	}

	private static SortedMap buildTree(List keyBindings, SortedMap configurationMap, SortedMap scopeMap) {
		SortedMap tree = new TreeMap();
		Iterator iterator = keyBindings.iterator();
		
		while (iterator.hasNext())
			Node.addToTree(tree, (KeyBinding) iterator.next(), configurationMap, scopeMap);

		return tree;
	}			

	private SortedMap configurationMap;
	private SortedMap scopeMap;		
	private List keyBindings1;	
	private List keyBindings2;
	private Path configuration;
	private Path locale;
	private Path platform;
	private Path[] scopes;
	private KeySequence mode;
	private SortedMap tree;
	private boolean solved;
	private SortedMap keySequenceMap;
	private SortedMap keySequenceMapForMode;
	private SortedSet keyStrokeSetForMode;
	private SortedMap actionMap;
	private SortedMap actionMapForMode;

	private KeyBindingManager() {
		super();
		configurationMap = Collections.unmodifiableSortedMap(loadConfigurationMap());
		scopeMap = Collections.unmodifiableSortedMap(loadScopeMap());
		keyBindings1 = Collections.EMPTY_LIST;
		keyBindings2 = Collections.EMPTY_LIST;
		configuration = Path.create();
		locale = systemLocale();
		platform = systemPlatform();
		scopes = new Path[] { Path.create() };
		mode = KeySequence.create();	
		
		// TBD: should this move?
		List keyBindings = loadKeyBindings();
		setKeyBindings1(keyBindings);

		try {
			IPath path = WorkbenchPlugin.getDefault().getStateLocation();
			path = path.append("keybindings.xml");
			FileWriter fileWriter = new FileWriter(path.toFile());
			writeKeyBindingsToWriter(fileWriter, "keybindings", keyBindings);
			fileWriter.close();
		} catch (IOException eIO) {
			eIO.printStackTrace();
		}
	}

	public Path getConfigurationForId(String id) {
		return (Path) configurationMap.get(id);	
	}

	public Path getScopeForId(String id) {
		return (Path) scopeMap.get(id);	
	}

	public List getKeyBindings1() {
		return keyBindings1;	
	}

	public void setKeyBindings1(List keyBindings1)
		throws IllegalArgumentException {
		if (keyBindings1 == null)
			throw new IllegalArgumentException();
		
		keyBindings1 = Collections.unmodifiableList(new ArrayList(keyBindings1));
		Iterator iterator = keyBindings1.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof KeyBinding))
				throw new IllegalArgumentException();

		if (!this.keyBindings1.equals(keyBindings1)) {
			this.keyBindings1 = keyBindings1;
			invalidateTree();
		}
	}
	
	public List getKeyBindings2() {
		return keyBindings2;	
	}

	public void setKeyBindings2(List keyBindings2)
		throws IllegalArgumentException {
		if (keyBindings2 == null)
			throw new IllegalArgumentException();
		
		keyBindings2 = Collections.unmodifiableList(new ArrayList(keyBindings2));
		Iterator iterator = keyBindings2.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof KeyBinding))
				throw new IllegalArgumentException();
				
		if (!this.keyBindings2.equals(keyBindings2)) {
			this.keyBindings2 = keyBindings2;
			invalidateTree();
		}
	}

	public Path getConfiguration() {
		return configuration;	
	}

	public void setConfiguration(Path configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
			
		if (!this.configuration.equals(configuration)) {
			this.configuration = configuration;
			invalidateSolution();
		}
	}
	
	public Path[] getScopes() {
		return (Path[]) scopes.clone();
	}	
	
	public void setScopes(Path[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();

		scopes = (Path[]) scopes.clone();
		
		if (!Arrays.equals(this.scopes, scopes)) {
			this.scopes = scopes;
			invalidateSolution();
		}		
	}	

	public KeySequence getMode() {
		return mode;	
	}	
	
	public void setMode(KeySequence mode)
		throws IllegalArgumentException {
		if (mode == null)
			throw new IllegalArgumentException();
			
		if (!this.mode.equals(mode)) {
			this.mode = mode;
			invalidateModalMaps();
		}
	}

	public SortedMap getKeySequenceMap() {
		if (keySequenceMap == null) {
			solve();
			keySequenceMap = Collections.unmodifiableSortedMap(Node.toKeySequenceMap(tree));
		}
		
		return keySequenceMap;
	}

	public SortedMap getKeySequenceMapForMode() {
		if (keySequenceMapForMode == null) {
			solve();
			SortedMap tree = Node.find(this.tree, mode);
			
			if (tree != null)
				keySequenceMapForMode = Collections.unmodifiableSortedMap(Node.toKeySequenceMap(tree));
			else
				keySequenceMapForMode = Collections.unmodifiableSortedMap(new TreeMap());			
		}
		
		return keySequenceMapForMode;
	}

	public SortedSet getKeyStrokeSetForMode() {
		if (keyStrokeSetForMode == null) {
			keyStrokeSetForMode = new TreeSet();
			SortedMap keySequenceMapForMode = getKeySequenceMapForMode();
			Iterator iterator = keySequenceMapForMode.keySet().iterator();
			
			while (iterator.hasNext()) {
				KeySequence keySequence = (KeySequence) iterator.next();			
				List keyStrokes = keySequence.getKeyStrokes();			
				
				if (keyStrokes.size() >= 1)
					keyStrokeSetForMode.add(keyStrokes.get(0));
			}
		}
		
		return keyStrokeSetForMode;			
	}

	public SortedMap getActionMap() {
		if (actionMap == null) {
			actionMap = new TreeMap();
			SortedMap keySequenceMap = getKeySequenceMap();
			Iterator iterator = keySequenceMap.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				MatchAction matchAction = (MatchAction) entry.getValue();		
				Match match = matchAction.getMatch();
				String action = matchAction.getAction();				
				SortedSet keySequenceSet = (SortedSet) actionMap.get(action);
				
				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionMap.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(MatchKeySequence.create(match, keySequence));
			}
		}
				
		return actionMap;		
	}
	
	public SortedMap getActionMapForMode() {
		if (actionMapForMode == null) {
			actionMapForMode = new TreeMap();
			SortedMap keySequenceMapForMode = getKeySequenceMapForMode();
			Iterator iterator = keySequenceMapForMode.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				MatchAction matchAction = (MatchAction) entry.getValue();		
				Match match = matchAction.getMatch();
				String action = matchAction.getAction();				
				SortedSet keySequenceSet = (SortedSet) actionMapForMode.get(action);				

				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionMapForMode.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(MatchKeySequence.create(match, keySequence));
			}
		}
					
		return actionMapForMode;			
	}		
	
	public String getTextForAction(String action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();					

		String text = null;
		SortedMap actionMap = getActionMap();		
		SortedSet keySequenceSet = (SortedSet) actionMap.get(action);
		
		if (keySequenceSet != null) {
			Iterator iterator = keySequenceSet.iterator();
	    	StringBuffer stringBuffer = new StringBuffer();
			int value = -1;
			int i = 0;
			
			while (iterator.hasNext()) {
				if (i != 0)
					stringBuffer.append(KEY_SEQUENCE_SEPARATOR);

				MatchKeySequence matchKeySequence = (MatchKeySequence) iterator.next();	
				Match match = matchKeySequence.getMatch();

				if (value == -1)
					value = match.getValue();
				
				if (value == match.getValue()) {
					KeySequence keySequence = matchKeySequence.getKeySequence();					
					Iterator iterator2 = keySequence.getKeyStrokes().iterator();
					int j = 0;
					
					while (iterator2.hasNext()) {					
						if (j != 0)
							stringBuffer.append(KEY_STROKE_SEPARATOR);
	
						KeyStroke keyStroke = (KeyStroke) iterator2.next();
						int accelerator = keyStroke.getAccelerator();
						stringBuffer.append(Action.convertAccelerator(accelerator));					
						j++;
					}
	
					i++;
				}
			}
	
			text = stringBuffer.toString();
		}
		
		return text;
	}
	
	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}
	
	private void invalidateSolution() {
		solved = false;
		invalidateMaps();
	}
	
	private void invalidateMaps() {			
		keySequenceMap = null;
		actionMap = null;
		invalidateModalMaps();
	}
	
	private void invalidateModalMaps() {
		keySequenceMapForMode = null;
		keyStrokeSetForMode = null;		
		actionMapForMode = null;
	}
	
	private void build() {
		if (tree == null) {
			ArrayList keyBindings = new ArrayList(keyBindings1);
			keyBindings.addAll(keyBindings2);				
			tree = buildTree(keyBindings, configurationMap, scopeMap);					
		}
	}
	
	private void solve() {
		if (!solved) {
			build();
			State[] states = new State[scopes.length];
				
			for (int i = 0; i < scopes.length; i++) {
				List paths = new ArrayList();
				paths.add(scopes[i]);			
				paths.add(configuration);
				paths.add(platform);
				paths.add(locale);							
				states[i] = State.create(paths);
			}
			
			Node.solveTree(tree, states);
			solved = true;
		}
	}
}
