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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.swt.SWT;

public class Manager {

	private final static String LOCALE_SEPARATOR = "_"; //$NON-NLS-1$
	private final static java.util.Locale SYSTEM_LOCALE = java.util.Locale.getDefault();
	private final static String SYSTEM_PLATFORM = SWT.getPlatform();

	private static Manager instance;

	public static Manager getInstance() {
		if (instance == null)
			instance = new Manager();
			
		return instance;	
	}

	private static SortedMap buildPathMapForConfigurationMap(SortedMap configurationMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = configurationMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForConfiguration(id, configurationMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
	}

	private static SortedMap buildPathMapForScopeMap(SortedMap scopeMap) {
		SortedMap pathMap = new TreeMap();
		Iterator iterator = scopeMap.keySet().iterator();

		while (iterator.hasNext()) {
			String id = (String) iterator.next();
			
			if (id != null) {			
				Path path = pathForScope(id, scopeMap);
			
				if (path != null)
					pathMap.put(id, path);
			}			
		}

		return pathMap;		
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
							
				pathItems.add(0, id);
				id = configuration.getParent();
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
						pathItems.add(value);
				}
			}

			path = Path.create(pathItems);
		}
			
		return path;		
	}

	static Path pathForPlatform(String platform) {
		Path path = null;

		if (platform != null) {
			List pathItems = new ArrayList();				
			platform = platform.trim();
			
			if (platform.length() > 0)
				pathItems.add(platform);

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
							
				pathItems.add(0, id);
				id = scope.getParent();
			}
		
			path = Path.create(pathItems);
		}
		
		return path;			
	}	

	static Path systemLocale() {
		return SYSTEM_LOCALE != null ? pathForLocale(SYSTEM_LOCALE.toString()) : null;
	}

	static Path systemPlatform() {
		return pathForPlatform(SYSTEM_PLATFORM);
	}

	private Machine gestureMachine;	
	private Machine keyMachine;	
	
	private Manager() {
		super();
		gestureMachine = Machine.create();
		keyMachine = Machine.create();
		initializeConfigurations();		
	}

	public Machine getGestureMachine() {
		return gestureMachine;
	}

	public Machine getKeyMachine() {
		return keyMachine;
	}

	public String getKeyTextForCommand(String command)
		throws IllegalArgumentException {
		if (command == null)
			throw new IllegalArgumentException();					

		String text = null;
		Map commandMap = getKeyMachine().getCommandMap();
		SortedSet sequenceSet = (SortedSet) commandMap.get(command);
		
		if (sequenceSet != null && !sequenceSet.isEmpty())
			text = ((Sequence) sequenceSet.first()).formatKeySequence();
		
		return text != null ? text : ""; //$NON-NLS-1$
	}

	public void initializeConfigurations() {
		CoreRegistry coreRegistry = CoreRegistry.getInstance();
		LocalRegistry localRegistry = LocalRegistry.getInstance();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		try {
			coreRegistry.load();
		} catch (IOException eIO) {
		}

		try {
			localRegistry.load();
		} catch (IOException eIO) {
		}
		
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}

		List activeGestureConfigurations = new ArrayList();
		activeGestureConfigurations.addAll(coreRegistry.getActiveGestureConfigurations());
		activeGestureConfigurations.addAll(localRegistry.getActiveGestureConfigurations());
		activeGestureConfigurations.addAll(preferenceRegistry.getActiveGestureConfigurations());	
		String activeGestureConfigurationId;
			
		if (activeGestureConfigurations.size() == 0)
			activeGestureConfigurationId = ""; //$NON-NLS-1$
		else {
			ActiveConfiguration activeKeyConfiguration = (ActiveConfiguration) activeGestureConfigurations.get(activeGestureConfigurations.size() - 1);
			activeGestureConfigurationId = activeKeyConfiguration.getValue();
		}

		List activeKeyConfigurations = new ArrayList();
		activeKeyConfigurations.addAll(coreRegistry.getActiveKeyConfigurations());
		activeKeyConfigurations.addAll(localRegistry.getActiveKeyConfigurations());
		activeKeyConfigurations.addAll(preferenceRegistry.getActiveKeyConfigurations());	
		String activeKeyConfigurationId;
			
		if (activeKeyConfigurations.size() == 0)
			activeKeyConfigurationId = ""; //$NON-NLS-1$
		else {
			ActiveConfiguration activeKeyConfiguration = (ActiveConfiguration) activeKeyConfigurations.get(activeKeyConfigurations.size() - 1);
			activeKeyConfigurationId = activeKeyConfiguration.getValue();
		}

		gestureMachine.setConfiguration(activeGestureConfigurationId);
		keyMachine.setConfiguration(activeKeyConfigurationId);
		update();
	}

	public void update() {
		CoreRegistry coreRegistry = CoreRegistry.getInstance();		
		LocalRegistry localRegistry = LocalRegistry.getInstance();
		PreferenceRegistry preferenceRegistry = PreferenceRegistry.getInstance();

		try {
			coreRegistry.load();
		} catch (IOException eIO) {
		}

		try {
			localRegistry.load();
		} catch (IOException eIO) {
		}
		
		try {
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}

		SortedSet gestureBindingSet = new TreeSet();		
		gestureBindingSet.addAll(coreRegistry.getGestureBindings());
		gestureBindingSet.addAll(localRegistry.getGestureBindings());
		gestureBindingSet.addAll(preferenceRegistry.getGestureBindings());

		List gestureConfigurations = new ArrayList();
		gestureConfigurations.addAll(coreRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(localRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(preferenceRegistry.getGestureConfigurations());
		SortedMap gestureConfigurationMap = buildPathMapForConfigurationMap(Configuration.sortedMapById(gestureConfigurations));

		SortedSet keyBindingSet = new TreeSet();		
		keyBindingSet.addAll(coreRegistry.getKeyBindings());
		keyBindingSet.addAll(localRegistry.getKeyBindings());
		keyBindingSet.addAll(preferenceRegistry.getKeyBindings());

		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(coreRegistry.getKeyConfigurations());
		keyConfigurations.addAll(localRegistry.getKeyConfigurations());
		keyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		SortedMap keyConfigurationMap = buildPathMapForConfigurationMap(Configuration.sortedMapById(keyConfigurations));
		
		List scopes = new ArrayList();
		scopes.addAll(coreRegistry.getScopes());
		scopes.addAll(localRegistry.getScopes());
		scopes.addAll(preferenceRegistry.getScopes());
		SortedMap scopeMap = buildPathMapForScopeMap(Scope.sortedMapById(scopes));

		gestureMachine.setConfigurationMap(Collections.unmodifiableSortedMap(gestureConfigurationMap));
		gestureMachine.setScopeMap(Collections.unmodifiableSortedMap(scopeMap));
		gestureMachine.setBindingSet(Collections.unmodifiableSortedSet(gestureBindingSet));
		
		keyMachine.setConfigurationMap(Collections.unmodifiableSortedMap(keyConfigurationMap));
		keyMachine.setScopeMap(Collections.unmodifiableSortedMap(scopeMap));
		keyMachine.setBindingSet(Collections.unmodifiableSortedSet(keyBindingSet));
	}
}
