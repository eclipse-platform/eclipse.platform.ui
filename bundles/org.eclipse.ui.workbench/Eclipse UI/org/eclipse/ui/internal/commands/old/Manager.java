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
	private PreferenceRegistry preferenceRegistry;
	private SequenceMachine keyMachine;	
	
	private Manager() {
		super();		
		coreRegistry = CoreRegistry.getInstance();		
		preferenceRegistry = PreferenceRegistry.getInstance();		
		keyMachine = SequenceMachine.create();
		reset();		
	}

	public SequenceMachine getKeyMachine() {
		return keyMachine;
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
			preferenceRegistry.load();
		} catch (IOException eIO) {
		}
		
		List activeGestureConfigurations = new ArrayList();
		activeGestureConfigurations.addAll(coreRegistry.getActiveGestureConfigurations());
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
		contexts.addAll(preferenceRegistry.getContexts());
		SortedMap contextMap = SequenceMachine.buildPathMapForContextMap(Context.sortedMapById(contexts));

		SortedSet gestureBindingSet = new TreeSet();		
		gestureBindingSet.addAll(coreRegistry.getGestureBindings());
		gestureBindingSet.addAll(preferenceRegistry.getGestureBindings());
		Manager.validateSequenceBindings(gestureBindingSet);
		
		List gestureConfigurations = new ArrayList();
		gestureConfigurations.addAll(coreRegistry.getGestureConfigurations());
		gestureConfigurations.addAll(preferenceRegistry.getGestureConfigurations());
		SortedMap gestureConfigurationMap = SequenceMachine.buildPathMapForConfigurationMap(Configuration.sortedMapById(gestureConfigurations));

		SortedSet keyBindingSet = new TreeSet();		
		keyBindingSet.addAll(coreRegistry.getKeyBindings());
		keyBindingSet.addAll(preferenceRegistry.getKeyBindings());
		Manager.validateSequenceBindings(keyBindingSet);

		List keyConfigurations = new ArrayList();
		keyConfigurations.addAll(coreRegistry.getKeyConfigurations());
		keyConfigurations.addAll(preferenceRegistry.getKeyConfigurations());
		SortedMap keyConfigurationMap = SequenceMachine.buildPathMapForConfigurationMap(Configuration.sortedMapById(keyConfigurations));

		keyMachine.setConfiguration(activeKeyConfigurationId);	
		keyMachine.setConfigurationMap(Collections.unmodifiableSortedMap(keyConfigurationMap));
		keyMachine.setContextMap(Collections.unmodifiableSortedMap(contextMap));
		keyMachine.setBindingSet(Collections.unmodifiableSortedSet(keyBindingSet));
	}

	static void validateSequenceBindings(Collection sequenceBindings) {
		Iterator iterator = sequenceBindings.iterator();
		
		while (iterator.hasNext()) {
			SequenceBinding sequenceBinding = (SequenceBinding) iterator.next();
			
			if (!validateSequence(sequenceBinding.getSequence()))
				iterator.remove();
		}
	}
	
	static boolean validateSequence(Sequence sequence) {
		List strokes = sequence.getStrokes();
		int size = strokes.size();
			
		if (size == 0)
			return false;
		else 
			for (int i = 0; i < size; i++) {
				Stroke stroke = (Stroke) strokes.get(i);	
	
				if (!validateStroke(stroke))
					return false;
			}
			
		return true;
	}

	static boolean validateStroke(Stroke stroke) {
		return stroke.getValue() != 0;
	}
}
