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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.ui.internal.commands.registry.IKeyBindingDefinition;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

final class KeyBindingMachine {
		
	private String[] activeContextIds;
	private String[] activeKeyConfigurationIds;
	private String[] activeLocales;
	private String[] activePlatforms;
	private SortedSet[] keyBindings;
	private Map keyBindingsByCommandId;
	private Map keyBindingsByCommandIdForMode;	
	private SortedMap matchesByKeySequence;
	private SortedMap matchesByKeySequenceForMode;
	private KeySequence mode;	
	private boolean solved;
	private SortedMap tree;

	KeyBindingMachine() {
		activeContextIds = new String[0];
		activeKeyConfigurationIds = new String[0];
		activeLocales = new String[0];
		activePlatforms = new String[0];
		keyBindings = new SortedSet[] { new TreeSet(), new TreeSet() };
		mode = KeySequence.getInstance();	
	}

	String[] getActiveContextIds() {
		return (String[]) activeContextIds.clone();
	}		

	String[] getActiveKeyConfigurationIds() {
		return (String[]) activeKeyConfigurationIds.clone();
	}		
	
	String[] getActiveLocales() {
		return (String[]) activeLocales.clone();
	}

	String[] getActivePlatforms() {
		return (String[]) activePlatforms.clone();
	}

	Map getKeyBindingsByCommandId() {
		if (keyBindingsByCommandId == null) {
			solve();
			keyBindingsByCommandId = Collections.unmodifiableMap(KeyBindingNode.getKeyBindingsByCommandId(getMatchesByKeySequence()));				
		}
		
		return keyBindingsByCommandId;
	}
	
	Map getKeyBindingsByCommandIdForMode() {
		if (keyBindingsByCommandIdForMode == null) {
			solve();
			SortedMap tree = KeyBindingNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();

			keyBindingsByCommandIdForMode = Collections.unmodifiableMap(KeyBindingNode.getKeyBindingsByCommandId(getMatchesByKeySequenceForMode()));				
		}
		
		return keyBindingsByCommandIdForMode;
	}

	SortedSet getKeyBindings0() {
		return keyBindings[0];	
	}

	SortedSet getKeyBindings1() {
		return keyBindings[1];	
	}

	SortedMap getMatchesByKeySequence() {
		if (matchesByKeySequence == null) {
			solve();
			matchesByKeySequence = Collections.unmodifiableSortedMap(KeyBindingNode.getMatchesByKeySequence(tree, KeySequence.getInstance()));				
		}
		
		return matchesByKeySequence;
	}

	SortedMap getMatchesByKeySequenceForMode() {
		if (matchesByKeySequenceForMode == null) {
			solve();
			SortedMap tree = KeyBindingNode.find(this.tree, mode);
	
			if (tree == null)
				tree = new TreeMap();
							
			matchesByKeySequenceForMode = Collections.unmodifiableSortedMap(KeyBindingNode.getMatchesByKeySequence(tree, mode));				
		}
		
		return matchesByKeySequenceForMode;
	}

	KeySequence getMode() {
		return mode;	
	}
	
	boolean setActiveContextIds(String[] activeContextIds) {
		if (activeContextIds == null || activeContextIds.length == 0)
			throw new NullPointerException();

		activeContextIds = (String[]) activeContextIds.clone();
		
		for (int i = 0; i < activeContextIds.length; i++)
			if (activeContextIds[i] == null)
				throw new IllegalArgumentException();	
		
		if (!Arrays.equals(this.activeContextIds, activeContextIds)) {
			this.activeContextIds = activeContextIds;
			invalidateSolution();
			return true;		
		}
			
		return false;		
	}

	boolean setActiveKeyConfigurationIds(String[] activeKeyConfigurationIds) {
		if (activeKeyConfigurationIds == null || activeKeyConfigurationIds.length == 0)
			throw new NullPointerException();

		activeKeyConfigurationIds = (String[]) activeKeyConfigurationIds.clone();
		
		for (int i = 0; i < activeKeyConfigurationIds.length; i++)
			if (activeKeyConfigurationIds[i] == null)
				throw new IllegalArgumentException();	
		
		if (!Arrays.equals(this.activeKeyConfigurationIds, activeKeyConfigurationIds)) {
			this.activeKeyConfigurationIds = activeKeyConfigurationIds;
			invalidateSolution();
			return true;		
		}
			
		return false;		
	}
	
	boolean setActiveLocales(String[] activeLocales) {
		if (activeLocales == null || activeLocales.length == 0)
			throw new NullPointerException();

		activeLocales = (String[]) activeLocales.clone();
		
		for (int i = 0; i < activeLocales.length; i++)
			if (activeLocales[i] == null)
				throw new IllegalArgumentException();	
		
		if (!Arrays.equals(this.activeLocales, activeLocales)) {
			this.activeLocales = activeLocales;
			invalidateSolution();
			return true;		
		}
			
		return false;		
	}	

	boolean setActivePlatforms(String[] activePlatforms) {
		if (activePlatforms == null || activePlatforms.length == 0)
			throw new NullPointerException();

		activePlatforms = (String[]) activePlatforms.clone();
		
		for (int i = 0; i < activePlatforms.length; i++)
			if (activePlatforms[i] == null)
				throw new IllegalArgumentException();	
		
		if (!Arrays.equals(this.activePlatforms, activePlatforms)) {
			this.activePlatforms = activePlatforms;
			invalidateSolution();
			return true;		
		}
			
		return false;		
	}	

	boolean setKeyBindings0(SortedSet keyBindings0) {
		keyBindings0 = Util.safeCopy(keyBindings0, IKeyBindingDefinition.class);
		
		if (!this.keyBindings[0].equals(keyBindings0)) {
			this.keyBindings[0] = keyBindings0;
			invalidateTree();
			return true;
		}			
			
		return false;		
	}

	boolean setKeyBindings1(SortedSet keyBindings1) {
		keyBindings1 = Util.safeCopy(keyBindings1, IKeyBindingDefinition.class);
		
		if (!this.keyBindings[1].equals(keyBindings1)) {
			this.keyBindings[1] = keyBindings1;
			invalidateTree();
			return true;
		}			
			
		return false;		
	}

	boolean setMode(KeySequence mode) {
		if (mode == null)
			throw new NullPointerException();
			
		if (!this.mode.equals(mode)) {
			this.mode = mode;
			invalidateMode();
			return true;
		}

		return false;		
	}
	
	private void build() {
		if (tree == null) {
			tree = new TreeMap();
		
			for (int i = 0; i < keyBindings.length; i++) {		
				Iterator iterator = keyBindings[i].iterator();
			
				while (iterator.hasNext()) {
					IKeyBindingDefinition keyBindingDefinition = (IKeyBindingDefinition) iterator.next();
					KeyBindingNode.add(tree, keyBindingDefinition.getKeySequence(), keyBindingDefinition.getContextId(), keyBindingDefinition.getKeyConfigurationId(), i, keyBindingDefinition.getPlatform(), keyBindingDefinition.getLocale(), keyBindingDefinition.getCommandId());
				}
			}
		}
	}

	private void invalidateMode() {
		keyBindingsByCommandIdForMode = null;
		matchesByKeySequenceForMode = null;
	}

	private void invalidateSolution() {
		solved = false;
		keyBindingsByCommandId = null;	
		matchesByKeySequence = null;
		invalidateMode();
	}
	
	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}

	private void solve() {
		if (!solved) {
			build();		
			KeyBindingNode.solve(tree, activeContextIds, activeKeyConfigurationIds, activePlatforms, activeLocales);
			solved = true;
		}
	}
}
