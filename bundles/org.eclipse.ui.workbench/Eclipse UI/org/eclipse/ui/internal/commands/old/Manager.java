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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.ui.internal.util.Util;

public class Manager {

	private static Manager instance;

	public static Manager getInstance() {
		if (instance == null)
			instance = new Manager();
			
		return instance;	
	}

	private CoreRegistry coreRegistry;
	private LocalRegistry localRegistry;
	private PreferenceRegistry preferenceRegistry;
	private SequenceMachine gestureMachine;	
	private SequenceMachine keyMachine;	
	
	private Manager() {
		super();		
		coreRegistry = CoreRegistry.getInstance();		
		localRegistry = LocalRegistry.getInstance();
		preferenceRegistry = PreferenceRegistry.getInstance();		
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
			text = GestureSupport.formatSequence(sequence, true);
			
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

		List contexts = new ArrayList();
		contexts.addAll(coreRegistry.getContexts());
		contexts.addAll(localRegistry.getContexts());
		contexts.addAll(preferenceRegistry.getContexts());
		SortedMap contextMap = SequenceMachine.buildPathMapForContextMap(Context.sortedMapById(contexts));

		SortedSet gestureBindingSet = new TreeSet();		
		gestureBindingSet.addAll(coreRegistry.getGestureBindings());
		gestureBindingSet.addAll(localRegistry.getGestureBindings());
		gestureBindingSet.addAll(preferenceRegistry.getGestureBindings());
		Manager.validateSequenceBindings(gestureBindingSet);
		
		List gestureConfigurations = new ArrayList();
		gestureConfigurations.addAll(coreRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(localRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(preferenceRegistry.getGestureConfigurations());
		SortedMap gestureConfigurationMap = SequenceMachine.buildPathMapForConfigurationMap(Configuration.sortedMapById(gestureConfigurations));

		SortedSet keyBindingSet = new TreeSet();		
		keyBindingSet.addAll(coreRegistry.getKeyBindings());
		keyBindingSet.addAll(localRegistry.getKeyBindings());
		keyBindingSet.addAll(preferenceRegistry.getKeyBindings());
		Manager.validateSequenceBindings(keyBindingSet);

		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(coreRegistry.getKeyConfigurations());
		keyConfigurations.addAll(localRegistry.getKeyConfigurations());
		keyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		SortedMap keyConfigurationMap = SequenceMachine.buildPathMapForConfigurationMap(Configuration.sortedMapById(keyConfigurations));

		gestureMachine.setConfiguration(activeGestureConfigurationId);
		gestureMachine.setConfigurationMap(Collections.unmodifiableSortedMap(gestureConfigurationMap));
		gestureMachine.setContextMap(Collections.unmodifiableSortedMap(contextMap));
		gestureMachine.setBindingSet(Collections.unmodifiableSortedSet(gestureBindingSet));

		keyMachine.setConfiguration(activeKeyConfigurationId);	
		keyMachine.setConfigurationMap(Collections.unmodifiableSortedMap(keyConfigurationMap));
		keyMachine.setContextMap(Collections.unmodifiableSortedMap(contextMap));
		keyMachine.setBindingSet(Collections.unmodifiableSortedSet(keyBindingSet));
	}

	static void validateSequenceBindings(Collection sequenceBindings) {
		Iterator iterator = sequenceBindings.iterator();
		
		while (iterator.hasNext()) {
			SequenceBinding sequenceBinding = (SequenceBinding) iterator.next();
			
			if (!SequenceUtil.validateSequence(sequenceBinding.getSequence()))
				iterator.remove();
		}
	}
}
