package org.eclipse.ui.internal.keybindings;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class KeyManager {

	private final static String KEY_SEQUENCE_SEPARATOR = ", "; //$NON-NLS-1$
	private final static String KEY_STROKE_SEPARATOR = " "; //$NON-NLS-1$
	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static String OR_SEPARATOR = "||"; //$NON-NLS-1$

	private static KeyManager instance;

	public static KeyManager getInstance() {
		if (instance == null)
			instance = new KeyManager();
			
		return instance;	
	}

	public static List parseKeySequences(String keys) {
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

	private static Path pathForConfiguration(String id, Map configurationMap) {
		Path path = null;

		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				Configuration configuration = (Configuration) configurationMap.get(id);
				
				if (configuration == null)
					return null;
							
				pathItems.add(0, PathItem.create(id));
				id = configuration.getParent();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}	
	
	private static Path pathForScope(String id, Map scopeMap) {
		Path path = null;
		
		if (id != null) {
			List pathItems = new ArrayList();

			while (id != null) {	
				if (pathItems.contains(id))
					return null;
							
				Scope scope = (Scope) scopeMap.get(id);
				
				if (scope == null)
					return null;
							
				pathItems.add(0, PathItem.create(id));
				id = scope.getParent();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;	
	}			
	
	private static Path pathForLocale(String locale) {
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

	private static Path pathForPlatform(String platform) {
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

	private static Path systemLocale() {
		java.util.Locale locale = java.util.Locale.getDefault();
		return locale != null ? pathForLocale(locale.toString()) : null;
	}

	private static Path systemPlatform() {
		return pathForPlatform(SWT.getPlatform());
	}
	
	private static SortedMap buildConfigurationMap(SortedMap registryConfigurationMap) {
		SortedMap configurationMap = new TreeMap();
		Iterator iterator = registryConfigurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForConfiguration(id, registryConfigurationMap);
			
				if (path != null)
					configurationMap.put(id, path);
			}			
		}

		return configurationMap;		
	}

	private static SortedMap buildScopeMap(Map registryScopeMap) {
		SortedMap scopeMap = new TreeMap();
		Iterator iterator = registryScopeMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {
				Path path = pathForScope(id, registryScopeMap);
			
				if (path != null)
					scopeMap.put(id, path);
			}
		}

		return scopeMap;		
	}

	private static List buildBindings(List definitions, SortedMap configurationMap, SortedMap scopeMap) {
		List bindings = new ArrayList();
		Iterator iterator = definitions.iterator();
		
		while (iterator.hasNext()) {
			Definition definition = (Definition) iterator.next();
			Path configuration = (Path) configurationMap.get(definition.getConfiguration());
			
			if (configuration == null)
				configuration = Path.create();
				
			Path locale = pathForLocale(definition.getLocale());
			Path platform = pathForPlatform(definition.getPlatform());
			Path scope = (Path) scopeMap.get(definition.getScope());
			
			if (scope == null)
				scope = Path.create();
				
			bindings.add(Binding.create(definition.getKeySequence(), configuration, locale, platform, scope, definition.getAction(), 
				definition.getPlugin()));			
		}
		
		return bindings;	
	}

	private static List readDefinitions(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();			
		
		IMemento[] mementos = memento.getChildren(Definition.ELEMENT);
		
		if (mementos == null)
			throw new IllegalArgumentException();
		
		List definitions = new ArrayList(mementos.length);
		
		for (int i = 0; i < mementos.length; i++)
			definitions.add(Definition.read(mementos[i]));
		
		return definitions;		
	}

	private static void writeDefinitions(IMemento memento, List definitions)
		throws IllegalArgumentException {
		if (memento == null || definitions == null)
			throw new IllegalArgumentException();
			
		Iterator iterator = definitions.iterator();
		
		while (iterator.hasNext())
			((Definition) iterator.next()).write(memento.createChild(Definition.ELEMENT)); 
	}

	private SortedMap registryConfigurationMap;
	private SortedMap registryScopeMap;
	private List registryDefinitions;	
	
	private SortedMap preferenceConfigurationMap;
	private SortedMap preferenceScopeMap;
	private List preferenceDefinitions;
	
	private SortedMap unifiedConfigurationMap;
	private SortedMap unifiedScopeMap;
	private List unifiedDefinitions;

	private SortedMap configurationMap;
	private SortedMap scopeMap;
	private List bindings;	

	private String configuration;
	private String[] scopes = new String[] { null };	

	private KeySequence mode = KeySequence.create();

	private boolean valid;
	private KeyMachine keyMachine;	
	
	private KeyManager() {
		super();
		loadRegistry();
		loadPreferences();		
	}

	public SortedMap getRegistryConfigurationMap() {
		return registryConfigurationMap;	
	}	
	
	public SortedMap getRegistryScopeMap() {
		return registryScopeMap;	
	}
	
	public List getRegistryDefinitions() {
		return registryDefinitions;	
	}

	public void loadRegistry() {		
		Registry keyRegistry = Registry.getInstance();	
		registryConfigurationMap = Collections.unmodifiableSortedMap(keyRegistry.getConfigurationMap());
		registryScopeMap = Collections.unmodifiableSortedMap(keyRegistry.getScopeMap());
		registryDefinitions = Collections.unmodifiableList(keyRegistry.getDefinitions());	
		invalidate();
	}

	public List getPreferenceDefinitions() {
		return preferenceDefinitions;
	}

	public void loadPreferences() {
		preferenceConfigurationMap = Collections.unmodifiableSortedMap(new TreeMap());
		preferenceScopeMap = Collections.unmodifiableSortedMap(new TreeMap());				
		preferenceDefinitions = Collections.EMPTY_LIST;
		
		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		String preferenceString = preferenceStore.getString("org.eclipse.ui.keys");
		
		if (preferenceString != null) {
			StringReader stringReader = new StringReader(preferenceString);

			try {
				XMLMemento xmlMemento = XMLMemento.createReadRoot(stringReader);
				IMemento memento = xmlMemento.getChild("definitions");
			
				if (memento != null) 
					preferenceDefinitions = Collections.unmodifiableList(readDefinitions(memento));
			} catch (WorkbenchException eWorkbench) {
			}
		}
		
		invalidate();
	}

	public String getConfiguration() {
		return configuration;
	}	
	
	public void setConfiguration(String configuration) {
		if (!Util.equals(this.configuration, configuration)) {
			this.configuration = configuration;
			invalidate();
		}
	}
	
	public String[] getScopes() {
		return scopes;	
	}
	
	public void setScopes(String[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length == 0)
			throw new IllegalArgumentException();
			
		if (!Util.equals(this.scopes, scopes)) {
			this.scopes = scopes;
			invalidate();
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
			invalidate();
		}	
	}
	
	private void invalidate() {
		valid = false;
		// TBD: fire changed here			
	}
			
	public KeyMachine getKeyMachine() {
		if (!valid) {
			unifiedConfigurationMap = new TreeMap();
			unifiedConfigurationMap.putAll(registryConfigurationMap);
			unifiedConfigurationMap.putAll(preferenceConfigurationMap);
			
			unifiedScopeMap = new TreeMap();
			unifiedScopeMap.putAll(registryScopeMap);
			unifiedScopeMap.putAll(preferenceScopeMap);
			
			unifiedDefinitions = new ArrayList();
			unifiedDefinitions.addAll(registryDefinitions);
			unifiedDefinitions.addAll(preferenceDefinitions);
			
			configurationMap = buildConfigurationMap(registryConfigurationMap);
			scopeMap = buildScopeMap(registryScopeMap);	
			bindings = buildBindings(unifiedDefinitions, configurationMap, scopeMap);
									
			keyMachine = KeyMachine.create();
			keyMachine.setBindings(bindings);
						
			Path configurationPath = null;
			
			if (configuration != null)
				configurationPath = (Path) configurationMap.get(configuration);
		
			if (configurationPath == null)
				configurationPath = Path.create();
		
			keyMachine.setConfiguration(configurationPath);			

			keyMachine.setLocale(systemLocale());

			keyMachine.setPlatform(systemPlatform());

			List scopePaths = new ArrayList();
	
			if (scopes != null)
				for (int i = 0; i < scopes.length; i++) {
					Path scopePath = null;
					
					if (scopes[i] != null)
						scopePath = (Path) scopeMap.get(scopes[i]);			
	
					if (scopePath != null)
						scopePaths.add(scopePath);
				}	
			
			if (scopePaths.size() == 0)
				scopePaths.add(Path.create());
				
			Path[] scopeArray = (Path[]) scopePaths.toArray(new Path[0]);			
			keyMachine.setScopes(scopeArray);

			keyMachine.setMode(mode);
			valid = true;
		}

		return keyMachine;
	}

	/*
	void savePreferences() {		
		XMLMemento xmlMemento = XMLMemento.createWriteRoot("org.eclipse.ui.keys");
		IMemento memento = xmlMemento.createChild("definitions");
		writeDefinitions(memento, preferenceDefinitions);
		StringWriter stringWriter = new StringWriter();
		String preferenceString = null;
		
		try {
			xmlMemento.save(stringWriter);
			preferenceString = stringWriter.toString();
		} catch (IOException eIO) {
		}

		IPreferenceStore preferenceStore = WorkbenchPlugin.getDefault().getPreferenceStore();
		preferenceStore.setValue("org.eclipse.ui.keys", preferenceString);
	}
	
	void setPreferenceDefinitions(List preferenceDefinitions)
		throws IllegalArgumentException {			
		if (preferenceDefinitions == null)
			throw new IllegalArgumentException();
		
		preferenceDefinitions = new ArrayList(preferenceDefinitions);
		Iterator iterator = preferenceDefinitions.iterator();
		
		while (iterator.hasNext())
			if (!(iterator.next() instanceof Definition))
				throw new IllegalArgumentException();
	
		this.preferenceDefinitions = Collections.unmodifiableList(preferenceDefinitions);
	}
	*/

	public String getTextForAction(String action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();					

		String text = null;
		SortedMap actionMap = getKeyMachine().getActionMap();		
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
					stringBuffer.append(getTextForKeySequence(matchKeySequence.getKeySequence()));
					i++;
				}
			}
	
			text = stringBuffer.toString();
		}
		
		return text;
	}

	public String getTextForKeySequence(KeySequence keySequence)
		throws IllegalArgumentException {
		if (keySequence == null)
			throw new IllegalArgumentException();		
	
	    StringBuffer stringBuffer = new StringBuffer();
		Iterator iterator = keySequence.getKeyStrokes().iterator();
		int i = 0;
		
		while (iterator.hasNext()) {					
			if (i != 0)
				stringBuffer.append(KEY_STROKE_SEPARATOR);

			KeyStroke keyStroke = (KeyStroke) iterator.next();
			int accelerator = keyStroke.getAccelerator();
			stringBuffer.append(Action.convertAccelerator(accelerator));					
			i++;
		}

		return stringBuffer.toString();
	}

	/*
	try {
		IPath path = WorkbenchPlugin.getDefault().getStateLocation();
		path = path.append("keybindings.xml");
		FileWriter fileWriter = new FileWriter(path.toFile());
		writeKeyBindingsToWriter(fileWriter, "keybindings", keyBindings);
		fileWriter.close();
	} catch (IOException eIO) {
		eIO.printStackTrace();
	}
	*/
}
