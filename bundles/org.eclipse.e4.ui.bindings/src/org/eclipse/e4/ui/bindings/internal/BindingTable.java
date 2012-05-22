/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.bindings.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeyStroke;

/**
 * manage tables of bindings that can be used to look up commands from keys.
 */
public class BindingTable {
	static class BindingComparator implements Comparator<Binding> {
		private String[] activeSchemeIds;

		private final int compareSchemes(final String schemeId1, final String schemeId2) {
			if (activeSchemeIds == null || activeSchemeIds.length == 0) {
				return 0;
			}
			if (!schemeId2.equals(schemeId1)) {
				for (int i = 0; i < activeSchemeIds.length; i++) {
					final String schemePointer = activeSchemeIds[i];
					if (schemeId2.equals(schemePointer)) {
						return 1;
					} else if (schemeId1.equals(schemePointer)) {
						return -1;
					}
				}
			}
			return 0;
		}

		public void setActiveSchemes(String[] activeSchemeIds) {
			this.activeSchemeIds = activeSchemeIds;
		}

		public int compare(Binding o1, Binding o2) {
			int rc = compareSchemes(o1.getSchemeId(), o2.getSchemeId());
			if (rc != 0) {
				return rc;
			}

			/*
			 * Check to see which has the least number of triggers in the trigger sequence.
			 */
			final Trigger[] bestTriggers = o1.getTriggerSequence().getTriggers();
			final Trigger[] currentTriggers = o2.getTriggerSequence().getTriggers();
			int compareTo = bestTriggers.length - currentTriggers.length;
			if (compareTo != 0) {
				return compareTo;
			}

			/*
			 * Compare the number of keys pressed in each trigger sequence. Some types of keys count
			 * less than others (i.e., some types of modifiers keys are less likely to be chosen).
			 */
			compareTo = countStrokes(bestTriggers) - countStrokes(currentTriggers);
			if (compareTo != 0) {
				return compareTo;
			}

			// If this is still a tie, then just chose the shortest text.
			return o1.getTriggerSequence().format().length()
					- o2.getTriggerSequence().format().length();
		}

		private final int countStrokes(final Trigger[] triggers) {
			int strokeCount = triggers.length;
			for (int i = 0; i < triggers.length; i++) {
				final Trigger trigger = triggers[i];
				if (trigger instanceof KeyStroke) {
					final KeyStroke keyStroke = (KeyStroke) trigger;
					final int modifierKeys = keyStroke.getModifierKeys();
					final IKeyLookup lookup = KeyLookupFactory.getDefault();
					if ((modifierKeys & lookup.getAlt()) != 0) {
						strokeCount += 8;
					}
					if ((modifierKeys & lookup.getCtrl()) != 0) {
						strokeCount += 2;
					}
					if ((modifierKeys & lookup.getShift()) != 0) {
						strokeCount += 4;
					}
					if ((modifierKeys & lookup.getCommand()) != 0) {
						strokeCount += 2;
					}
				} else {
					strokeCount += 99;
				}
			}

			return strokeCount;
		}
	}

	public static final BindingComparator BEST_SEQUENCE = new BindingComparator();

	private Context tableId;
	private ArrayList<Binding> bindings = new ArrayList<Binding>();
	private Map<TriggerSequence, Binding> bindingsByTrigger = new HashMap<TriggerSequence, Binding>();
	private Map<ParameterizedCommand, ArrayList<Binding>> bindingsByCommand = new HashMap<ParameterizedCommand, ArrayList<Binding>>();
	private Map<TriggerSequence, ArrayList<Binding>> bindingsByPrefix = new HashMap<TriggerSequence, ArrayList<Binding>>();
	private Map<TriggerSequence, ArrayList<Binding>> conflicts = new HashMap<TriggerSequence, ArrayList<Binding>>();

	/**
	 * @param context
	 */
	public BindingTable(Context context) {
		tableId = context;
	}

	public Context getTableId() {
		return tableId;
	}

	public String getId() {
		return tableId.getId();
	}

	public Collection<Binding> getConflicts() {
		Collection<Binding> conflictsList = new ArrayList<Binding>();
		for (TriggerSequence key : conflicts.keySet()) {
			ArrayList<Binding> conflictsForTrigger = conflicts.get(key);
			if (conflictsForTrigger != null) {
				conflictsList.addAll(conflictsForTrigger);
			}
		}
		return conflictsList;
	}

	// checks both the active bindings and conflicts list
	public Collection<Binding> getConflictsFor(TriggerSequence triggerSequence) {
		return conflicts.get(triggerSequence);
	}

	public void addBinding(Binding binding) {
		if (!getId().equals(binding.getContextId())) {
			throw new IllegalArgumentException("Binding context " + binding.getContextId() //$NON-NLS-1$
					+ " does not match " + getId()); //$NON-NLS-1$
		}

		Binding conflict;
		ArrayList<Binding> conflictsList;
		boolean isConflict = false;

		// if this binding conflicts with one other active binding
		if (bindingsByTrigger.containsKey(binding.getTriggerSequence())) {
			// remove the active binding and put it in the conflicts map
			conflict = bindingsByTrigger.get(binding.getTriggerSequence());
			removeBinding(conflict);
			conflictsList = new ArrayList<Binding>();
			conflictsList.add(conflict);
			conflicts.put(binding.getTriggerSequence(), conflictsList);
			isConflict = true;
		}
		// if this trigger is already in the conflicts map
		if (conflicts.containsKey(binding.getTriggerSequence())
				&& conflicts.get(binding.getTriggerSequence()).size() > 0) {

			// add this binding to the conflicts map
			conflictsList = conflicts.get(binding.getTriggerSequence());
			if (!conflictsList.contains(binding)) {
				conflictsList.add(binding);
			}
			isConflict = true;
		}

		// if there are no conflicts, then add to the table
		if (!isConflict) {
			bindings.add(binding);
			bindingsByTrigger.put(binding.getTriggerSequence(), binding);

			ArrayList<Binding> sequences = bindingsByCommand.get(binding.getParameterizedCommand());
			if (sequences == null) {
				sequences = new ArrayList<Binding>();
				bindingsByCommand.put(binding.getParameterizedCommand(), sequences);
			}
			sequences.add(binding);
			Collections.sort(sequences, BEST_SEQUENCE);

			TriggerSequence[] prefs = binding.getTriggerSequence().getPrefixes();
			for (int i = 1; i < prefs.length; i++) {
				ArrayList<Binding> bindings = bindingsByPrefix.get(prefs[i]);
				if (bindings == null) {
					bindings = new ArrayList<Binding>();
					bindingsByPrefix.put(prefs[i], bindings);
				}
				bindings.add(binding);
			}
		}
	}

	public void removeBinding(Binding binding) {
		if (!getId().equals(binding.getContextId())) {
			throw new IllegalArgumentException("Binding context " + binding.getContextId() //$NON-NLS-1$
					+ " does not match " + getId()); //$NON-NLS-1$
		}
		ArrayList<Binding> conflictBindings = conflicts.get(binding.getTriggerSequence());

		// if this binding is in the conflicts map, then remove it
		if (!bindingsByTrigger.containsKey(binding.getTriggerSequence())
				&& conflictBindings != null) {

			conflictBindings.remove(binding);

			// if there is only one binding left in the list, then it's not really a conflict
			// binding anymore and can be re-added to the binding table
			if (conflictBindings.size() == 1) {
				Binding bindingToReAdd = conflictBindings.remove(0);
				addBinding(bindingToReAdd);
			}

		} else {
			bindings.remove(binding);
			bindingsByTrigger.remove(binding.getTriggerSequence());
			ArrayList<Binding> sequences = bindingsByCommand.get(binding.getParameterizedCommand());

			if (sequences != null) {
				sequences.remove(binding);
			}
			TriggerSequence[] prefs = binding.getTriggerSequence().getPrefixes();
			for (int i = 1; i < prefs.length; i++) {
				ArrayList<Binding> bindings = bindingsByPrefix.get(prefs[i]);
				bindings.remove(binding);
			}
		}
	}

	public Binding getPerfectMatch(TriggerSequence trigger) {
		return bindingsByTrigger.get(trigger);
	}

	public Binding getBestSequenceFor(ParameterizedCommand command) {
		ArrayList<Binding> sequences = bindingsByCommand.get(command);
		if (sequences != null && sequences.size() > 0) {
			return sequences.get(0);
		}
		return null;
	}

	public Collection<Binding> getSequencesFor(ParameterizedCommand command) {
		ArrayList<Binding> triggers = bindingsByCommand.get(command);
		return (Collection<Binding>) (triggers == null ? Collections.EMPTY_LIST : triggers.clone());
	}

	public Collection<Binding> getPartialMatches(TriggerSequence sequence) {
		return bindingsByPrefix.get(sequence);
	}

	public boolean isPartialMatch(TriggerSequence seq) {
		return bindingsByPrefix.get(seq) != null;
	}

	public Collection<Binding> getBindings() {
		return Collections.unmodifiableCollection(bindings);
	}

}
