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
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class Manager {

	private static Manager instance;

	public static Manager getInstance() {
		if (instance == null)
			instance = new Manager();
			
		return instance;	
	}

	private SequenceMachine gestureMachine;	
	private SequenceMachine keyMachine;	
	
	private Manager() {
		super();
		gestureMachine = SequenceMachine.create();
		keyMachine = SequenceMachine.create();
		reset();		
	}

	public SequenceMachine getGestureMachine() {
		return gestureMachine;
	}

	public SequenceMachine getKeyMachine() {
		return keyMachine;
	}

	public String getGestureTextForCommand(String command)
		throws IllegalArgumentException {
		String text = null;
		Sequence sequence = getGestureMachine().getFirstSequenceForCommand(command);
		
		if (sequence != null)
			text = GestureSupport.formatSequence(sequence);
			
		return text != null ? text : Util.ZERO_LENGTH_STRING;
	}

	public String getKeyTextForCommand(String command)
		throws IllegalArgumentException {
		String text = null;
		Sequence sequence = getKeyMachine().getFirstSequenceForCommand(command);
		
		if (sequence != null)
			text = KeySupport.formatSequence(sequence, true);
			
		return text != null ? text : Util.ZERO_LENGTH_STRING;
	}

	public void reset() {
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
			activeGestureConfigurationId = Util.ZERO_LENGTH_STRING;
		else {
			ActiveConfiguration activeGestureConfiguration = (ActiveConfiguration) activeGestureConfigurations.get(activeGestureConfigurations.size() - 1);
			activeGestureConfigurationId = activeGestureConfiguration.getValue();
		}

		List activeKeyConfigurations = new ArrayList();
		activeKeyConfigurations.addAll(coreRegistry.getActiveKeyConfigurations());
		activeKeyConfigurations.addAll(localRegistry.getActiveKeyConfigurations());
		activeKeyConfigurations.addAll(preferenceRegistry.getActiveKeyConfigurations());	
		String activeKeyConfigurationId;
			
		if (activeKeyConfigurations.size() == 0)
			activeKeyConfigurationId = Util.ZERO_LENGTH_STRING;
		else {
			ActiveConfiguration activeKeyConfiguration = (ActiveConfiguration) activeKeyConfigurations.get(activeKeyConfigurations.size() - 1);
			activeKeyConfigurationId = activeKeyConfiguration.getValue();
		}

		SortedSet gestureBindingSet = new TreeSet();		
		gestureBindingSet.addAll(coreRegistry.getGestureBindings());
		gestureBindingSet.addAll(localRegistry.getGestureBindings());
		gestureBindingSet.addAll(preferenceRegistry.getGestureBindings());

		List gestureConfigurations = new ArrayList();
		gestureConfigurations.addAll(coreRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(localRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(preferenceRegistry.getGestureConfigurations());
		SortedMap gestureConfigurationMap = SequenceMachine.buildPathMapForConfigurationMap(Configuration.sortedMapById(gestureConfigurations));

		SortedSet keyBindingSet = new TreeSet();		
		keyBindingSet.addAll(coreRegistry.getKeyBindings());
		keyBindingSet.addAll(localRegistry.getKeyBindings());
		keyBindingSet.addAll(preferenceRegistry.getKeyBindings());

		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(coreRegistry.getKeyConfigurations());
		keyConfigurations.addAll(localRegistry.getKeyConfigurations());
		keyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		SortedMap keyConfigurationMap = SequenceMachine.buildPathMapForConfigurationMap(Configuration.sortedMapById(keyConfigurations));
		
		List scopes = new ArrayList();
		scopes.addAll(coreRegistry.getScopes());
		scopes.addAll(localRegistry.getScopes());
		scopes.addAll(preferenceRegistry.getScopes());
		SortedMap scopeMap = SequenceMachine.buildPathMapForScopeMap(Scope.sortedMapById(scopes));

		gestureMachine.setConfiguration(activeGestureConfigurationId);
		gestureMachine.setConfigurationMap(Collections.unmodifiableSortedMap(gestureConfigurationMap));
		gestureMachine.setScopeMap(Collections.unmodifiableSortedMap(scopeMap));
		gestureMachine.setBindingSet(Collections.unmodifiableSortedSet(gestureBindingSet));

		keyMachine.setConfiguration(activeKeyConfigurationId);	
		keyMachine.setConfigurationMap(Collections.unmodifiableSortedMap(keyConfigurationMap));
		keyMachine.setScopeMap(Collections.unmodifiableSortedMap(scopeMap));
		keyMachine.setBindingSet(Collections.unmodifiableSortedSet(keyBindingSet));
	}
}
