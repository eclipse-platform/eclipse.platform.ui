package org.eclipse.ui.internal.keybindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.Accelerator;
import org.eclipse.ui.internal.registry.AcceleratorConfiguration;
import org.eclipse.ui.internal.registry.AcceleratorRegistry;
import org.eclipse.ui.internal.registry.AcceleratorScope;
import org.eclipse.ui.internal.registry.AcceleratorSet;

public final class KeyBindingManager {

	private final static String KEY_SEQUENCE_SEPARATOR = ", ";
	private final static String KEY_STROKE_SEPARATOR = " ";

	private static KeyBindingManager instance;

	public static KeyBindingManager getInstance() {
		if (instance == null)
			instance = new KeyBindingManager();
			
		return instance;	
	}
	
	private SortedMap configurationMap = new TreeMap();
	private SortedMap scopeMap = new TreeMap();

	private List bindings = new ArrayList();
	private SortedMap tree = new TreeMap();
	
	private Configuration configuration = Configuration.create();
	private Locale locale = Locale.system();
	private Platform platform = Platform.system();
	private Scope[] scopes = new Scope[] { Scope.create() };

	private KeySequence mode = KeySequence.create();
	private SortedMap actionKeySequenceSetMap;
	private SortedMap keySequenceActionMap;
	private SortedMap actionKeySequenceSetMapForMode;
	private SortedMap keySequenceActionMapForMode;
	private SortedSet keyStrokeSetForMode;

	private KeyBindingManager() {
		super();				
		AcceleratorRegistry acceleratorRegistry = 
			WorkbenchPlugin.getDefault().getAcceleratorRegistry();				
		AcceleratorConfiguration[] acceleratorConfigurations = 
			acceleratorRegistry.getConfigurations();
			
		for (int i = 0; i < acceleratorConfigurations.length; i++) {
			AcceleratorConfiguration acceleratorConfiguration = 
				acceleratorConfigurations[i];
			String id = acceleratorConfiguration.getId();
			String initialId = id;
			List elements = new ArrayList();				
			acceleratorConfiguration = 
				acceleratorConfiguration.getParentConfiguration();
				
			while (acceleratorConfiguration != null) {
				elements.add(0, Element.create(id));
				id = acceleratorConfiguration.getId();
				acceleratorConfiguration = 
					acceleratorConfiguration.getParentConfiguration();
			}
					
			configurationMap.put(initialId, 
				Configuration.create(Path.create(elements)));
		}		
	
		AcceleratorScope[] acceleratorScopes = acceleratorRegistry.getScopes();				
				
		for (int i = 0; i < acceleratorScopes.length; i++) {
			AcceleratorScope acceleratorScope = acceleratorScopes[i];
			String id = acceleratorScope.getId();
			String initialId = id;
			List elements = new ArrayList();				
			acceleratorScope = acceleratorScope.getParentScope();
			
			while (acceleratorScope != null) {
				elements.add(0, Element.create(id));
				id = acceleratorScope.getId();
				acceleratorScope = acceleratorScope.getParentScope();
			}
						
			scopeMap.put(initialId, 
				Scope.create(Path.create(elements)));
		}		
		
		List acceleratorSets = acceleratorRegistry.getAcceleratorSets();
		
		for (Iterator iterator = acceleratorSets.iterator(); 
			iterator.hasNext();) {
			AcceleratorSet acceleratorSet = (AcceleratorSet) iterator.next();		
			Accelerator[] accelerators = acceleratorSet.getAccelerators();

			String configurationId = acceleratorSet.getConfigurationId();
			Configuration configuration = 
				(Configuration) configurationMap.get(configurationId);
			
			String scopeId = acceleratorSet.getScopeId();	
			Scope scope = (Scope) scopeMap.get(scopeId);
			
			if (configuration != null && scope != null) {					
				for (int i = 0; i < accelerators.length; i++) {
					Accelerator accelerator = accelerators[i];
					int[][] a = accelerator.getAccelerators();
					String id = accelerator.getId();					
					String localeString = accelerator.getLocale();		
					List localeElements = new ArrayList();	
					
					if (!localeString.equals(Accelerator.DEFAULT_LOCALE)) {
						StringTokenizer st = 
							new StringTokenizer(localeString, "_");
						
						while (st.hasMoreElements()) {
							String element = ((String) st.nextElement()).trim();
							
							if (element.length() > 0) {							
								localeElements.add(element);
							}							
						}					
					}

					Locale locale = Locale.create(Path.create(localeElements));
					String platformString = accelerator.getPlatform();
					List platformElements = new ArrayList();	
					
					if (!platformString.equals(Accelerator.DEFAULT_PLATFORM))
						platformElements.add(platformString);
										
					Platform platform = 
						Platform.create(Path.create(platformElements));
					State state = 
						State.create(configuration, locale, platform, scope); 			
					
					// TBD:
					Contributor contributor = Contributor.create("");
					Action action = Action.create(id); 
												
					for (int j = 0; j < a.length; j++) {
						int[] b = a[j];						
						List strokes = new ArrayList();
						
						for (int k = 0; k < b.length; k++) {
							strokes.add(KeyStroke.create(b[k]));	
						}
						
						KeySequence sequence = KeySequence.create(strokes);
						Node.addToTree(tree, KeyBinding.create(sequence, state, 
							contributor, action));
					}												
				}			
			}
		}					
		
		// TBD: add all custom bindings here..
		// don't forget to add them: Node.addToTree(tree, binding);
				
		/*
		try {
			FileWriter fileWriter = new FileWriter("c:\\bindings.xml");
			KeyBinding.writeBindingsToWriter(fileWriter, KeyBinding.ROOT, 
				Node.toBindings(tree));
		} catch (IOException eIO) {
		}
		*/
		
		solve();
	}
	
	public Configuration getConfigurationForId(String id) {
		return (Configuration) configurationMap.get(id);	
	}

	public Scope getScopeForId(String id) {
		return (Scope) scopeMap.get(id);	
	}
	
	public Configuration getConfiguration() {
		return configuration;	
	}

	public void setConfiguration(Configuration configuration)
		throws IllegalArgumentException {
		if (configuration == null)
			throw new IllegalArgumentException();
			
		if (!this.configuration.equals(configuration)) {
			this.configuration = configuration;
			solve();
		}
	}

	public Scope[] getScopes() {
		Scope[] scopes = new Scope[this.scopes.length];
		System.arraycopy(this.scopes, 0, scopes, 0, this.scopes.length);		
		return scopes;
	}	
	
	public void setScopes(Scope[] scopes)
		throws IllegalArgumentException {
		if (scopes == null || scopes.length < 1)
			throw new IllegalArgumentException();

		Scope[] scopesCopy = new Scope[scopes.length];
		System.arraycopy(scopes, 0, scopesCopy, 0, scopes.length);
		
		if (!Arrays.equals(this.scopes, scopesCopy)) {
			this.scopes = scopesCopy;
			solve();
		}		
	}

	public KeySequence getMode() {
		return mode;	
	}	
	
	public void setMode(KeySequence mode)
		throws IllegalArgumentException {
		if (mode == null)
			throw new IllegalArgumentException();
			
		this.mode = mode;
		invalidateForMode();
	}

	public SortedMap getActionKeySequenceSetMap() {
		if (actionKeySequenceSetMap == null) {				
			actionKeySequenceSetMap = new TreeMap();
			SortedMap keySequenceActionMap = getKeySequenceActionMap();
			Iterator iterator = keySequenceActionMap.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				Action action = (Action) entry.getValue();		
				SortedSet keySequenceSet = 
					(SortedSet) actionKeySequenceSetMap.get(action);
				
				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionKeySequenceSetMap.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(keySequence);
			}
		}
				
		return actionKeySequenceSetMap;		
	}

	public SortedMap getKeySequenceActionMap() {
		if (keySequenceActionMap == null) {
			if (tree != null)
				keySequenceActionMap = Collections.unmodifiableSortedMap(
					Node.toKeySequenceActionMap(tree));
		
			//if (keySequenceActionMapForMode == null)
			//	keySequenceActionMapForMode = new TreeMap();
		}
		
		return keySequenceActionMap;
	}
	
	public SortedMap getActionKeySequenceSetMapForMode() {
		if (actionKeySequenceSetMapForMode == null) {
			actionKeySequenceSetMapForMode = new TreeMap();
			SortedMap keySequenceActionMap = getKeySequenceActionMapForMode();
			Iterator iterator = keySequenceActionMap.entrySet().iterator();
	
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry) iterator.next();
				KeySequence keySequence = (KeySequence) entry.getKey();
				Action action = (Action) entry.getValue();		
				SortedSet keySequenceSet = 
					(SortedSet) actionKeySequenceSetMapForMode.get(action);
				
				if (keySequenceSet == null) {
					keySequenceSet = new TreeSet();
					actionKeySequenceSetMapForMode.put(action, keySequenceSet);
				}
				
				keySequenceSet.add(keySequence);
			}
		}
					
		return actionKeySequenceSetMapForMode;			
	}		
	
	public SortedMap getKeySequenceActionMapForMode() {
		if (keySequenceActionMapForMode == null) {
			if (tree != null) {
				SortedMap tree = Node.find(this.tree, mode);
			
				if (tree != null)	
					keySequenceActionMapForMode = Collections.unmodifiableSortedMap(
						Node.toKeySequenceActionMap(tree));
			}
			
			//if (keySequenceActionMapForMode == null)
			//	keySequenceActionMapForMode = new TreeMap();
		}
		
		return keySequenceActionMapForMode;
	}

	public SortedSet getStrokeSetForMode() {
		if (keyStrokeSetForMode == null) {
			keyStrokeSetForMode = new TreeSet();
			SortedMap keySequenceActionMapForMode = 
				getKeySequenceActionMapForMode();
			Iterator iterator = keySequenceActionMapForMode.keySet().iterator();
			
			while (iterator.hasNext()) {
				KeySequence keySequence = (KeySequence) iterator.next();			
				List keyStrokes = keySequence.getKeyStrokes();			
				
				if (keyStrokes.size() >= 1)
					keyStrokeSetForMode.add(keyStrokes.get(0));
			}
		}
		
		return keyStrokeSetForMode;			
	}

	public String getAcceleratorTextForAction(String action)
		throws IllegalArgumentException {
		if (action == null)
			throw new IllegalArgumentException();					

		SortedMap actionSequenceMap = getActionKeySequenceSetMap();		
		SortedSet keySequenceSet = 
			(SortedSet) actionSequenceMap.get(Action.create(action));
		
		if (keySequenceSet == null)
			return null;
		else {
			Iterator iterator = keySequenceSet.iterator();
	    	StringBuffer stringBuffer = new StringBuffer();
			int i = 0;
			
			while (iterator.hasNext()) {
				if (i != 0)
					stringBuffer.append(KEY_SEQUENCE_SEPARATOR);

				KeySequence keySequence = (KeySequence) iterator.next();	
				Iterator iterator2 = keySequence.getKeyStrokes().iterator();
				int j = 0;
				
				while (iterator2.hasNext()) {					
					if (j != 0)
						stringBuffer.append(KEY_STROKE_SEPARATOR);

					KeyStroke keyStroke = (KeyStroke) iterator2.next();
					int accelerator = keyStroke.getAccelerator();
					stringBuffer.append(
						org.eclipse.jface.action.Action.convertAccelerator(
						accelerator));					
					j++;
				}

				i++;
			}
	
			return stringBuffer.toString();
		}
	}
	
	private void invalidate() {
		actionKeySequenceSetMap = null;
		keySequenceActionMap = null;
		invalidateForMode();
	}
	
	private void invalidateForMode() {
		actionKeySequenceSetMapForMode = null;
		keySequenceActionMapForMode = null;
		keyStrokeSetForMode = null;		
	}
	
	private void solve() {
		State[] states = new State[scopes.length];
			
		for (int i = 0; i < scopes.length; i++) {
			states[i] = State.create(configuration, locale, platform, 
				scopes[i]);
		}
		
		Node.solveTree(tree, states);
		invalidate();
	}
}
