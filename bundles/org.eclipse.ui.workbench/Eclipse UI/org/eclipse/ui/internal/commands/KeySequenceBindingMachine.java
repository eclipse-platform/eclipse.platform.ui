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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.keys.KeySequence;

final class KeySequenceBindingMachine {

	private String[] activeActivityIds;
	private String[] activeKeyConfigurationIds;
	private String[] activeLocales;
	private String[] activePlatforms;
	private List[] keySequenceBindings;
	private Map keySequenceBindingsByCommandId;
	private Map matchesByKeySequence;
	private boolean solved;
	private SortedMap tree;

	KeySequenceBindingMachine() {
		activeActivityIds = new String[0];
		activeKeyConfigurationIds = new String[0];
		activeLocales = new String[0];
		activePlatforms = new String[0];
		keySequenceBindings = new List[] { new ArrayList(), new ArrayList()};
	}

	String[] getActiveActivityIds() {
		return (String[]) activeActivityIds.clone();
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

	List getKeySequenceBindings0() {
		return keySequenceBindings[0];
	}

	List getKeySequenceBindings1() {
		return keySequenceBindings[1];
	}

	Map getKeySequenceBindingsByCommandId() {
		if (keySequenceBindingsByCommandId == null) {
			validateSolution();
			keySequenceBindingsByCommandId =
				Collections.unmodifiableMap(
					KeySequenceBindingNode.getKeySequenceBindingsByCommandId(
						getMatchesByKeySequence()));
		}

		return keySequenceBindingsByCommandId;
	}

	Map getMatchesByKeySequence() {
		if (matchesByKeySequence == null) {
			validateSolution();
			matchesByKeySequence =
				Collections.unmodifiableMap(
					KeySequenceBindingNode.getMatchesByKeySequence(
						tree,
						KeySequence.getInstance()));
		}

		return matchesByKeySequence;
	}

	private void invalidateSolution() {
		solved = false;
		keySequenceBindingsByCommandId = null;
		matchesByKeySequence = null;
	}

	private void invalidateTree() {
		tree = null;
		invalidateSolution();
	}

	boolean setActiveActivityIds(String[] activeActivityIds) {
		if (activeActivityIds == null)
			throw new NullPointerException();

		activeActivityIds = (String[]) activeActivityIds.clone();

		if (!Arrays.equals(this.activeActivityIds, activeActivityIds)) {
			this.activeActivityIds = activeActivityIds;
			invalidateSolution();
			return true;
		}

		return false;
	}

	boolean setActiveKeyConfigurationIds(String[] activeKeyConfigurationIds) {
		if (activeKeyConfigurationIds == null)
			throw new NullPointerException();

		activeKeyConfigurationIds =
			(String[]) activeKeyConfigurationIds.clone();

		if (!Arrays
			.equals(
				this.activeKeyConfigurationIds,
				activeKeyConfigurationIds)) {
			this.activeKeyConfigurationIds = activeKeyConfigurationIds;
			invalidateSolution();
			return true;
		}

		return false;
	}

	boolean setActiveLocales(String[] activeLocales) {
		if (activeLocales == null)
			throw new NullPointerException();

		activeLocales = (String[]) activeLocales.clone();

		if (!Arrays.equals(this.activeLocales, activeLocales)) {
			this.activeLocales = activeLocales;
			invalidateSolution();
			return true;
		}

		return false;
	}

	boolean setActivePlatforms(String[] activePlatforms) {
		if (activePlatforms == null)
			throw new NullPointerException();

		activePlatforms = (String[]) activePlatforms.clone();

		if (!Arrays.equals(this.activePlatforms, activePlatforms)) {
			this.activePlatforms = activePlatforms;
			invalidateSolution();
			return true;
		}

		return false;
	}

	boolean setKeySequenceBindings0(List keySequenceBindings0) {
		keySequenceBindings0 =
			Util.safeCopy(
				keySequenceBindings0,
				IKeySequenceBindingDefinition.class);

		if (!this.keySequenceBindings[0].equals(keySequenceBindings0)) {
			this.keySequenceBindings[0] = keySequenceBindings0;
			invalidateTree();
			return true;
		}

		return false;
	}

	boolean setKeySequenceBindings1(List keySequenceBindings1) {
		keySequenceBindings1 =
			Util.safeCopy(
				keySequenceBindings1,
				IKeySequenceBindingDefinition.class);

		if (!this.keySequenceBindings[1].equals(keySequenceBindings1)) {
			this.keySequenceBindings[1] = keySequenceBindings1;
			invalidateTree();
			return true;
		}

		return false;
	}

	private void validateSolution() {
		if (!solved) {
			validateTree();
			KeySequenceBindingNode.solve(
				tree,
				activeActivityIds,
				activeKeyConfigurationIds,
				activePlatforms,
				activeLocales);
			solved = true;
		}
	}

	private void validateTree() {
		if (tree == null) {
			tree = new TreeMap();

			for (int i = 0; i < keySequenceBindings.length; i++) {
				Iterator iterator = keySequenceBindings[i].iterator();

				while (iterator.hasNext()) {
					IKeySequenceBindingDefinition keySequenceBindingDefinition =
						(IKeySequenceBindingDefinition) iterator.next();
					KeySequenceBindingNode.add(
						tree,
						keySequenceBindingDefinition.getKeySequence(),
						keySequenceBindingDefinition.getActivityId(),
						keySequenceBindingDefinition.getKeyConfigurationId(),
						i,
						keySequenceBindingDefinition.getPlatform(),
						keySequenceBindingDefinition.getLocale(),
						keySequenceBindingDefinition.getCommandId());
				}
			}
		}
	}
}
