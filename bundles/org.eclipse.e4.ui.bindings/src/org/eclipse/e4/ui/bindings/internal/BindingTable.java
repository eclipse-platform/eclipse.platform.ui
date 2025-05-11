/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.bindings.internal.ContextSet.CComp;
import org.eclipse.e4.ui.model.application.MApplication;
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
	private static int compareSchemes(String[] activeSchemeIds, final String schemeId1,
			final String schemeId2) {
		if (activeSchemeIds == null || activeSchemeIds.length == 0) {
			return 0;
		}
		if (!schemeId2.equals(schemeId1)) {
			for (final String schemePointer : activeSchemeIds) {
				if (schemeId2.equals(schemePointer)) {
					return 1;
				} else if (schemeId1.equals(schemePointer)) {
					return -1;
				}
			}
		}
		return 0;
	}

	static class BindingComparator implements Comparator<Binding> {
		private String[] activeSchemeIds;
		private ContextManager contextManager;

		public void setActiveSchemes(String[] activeSchemeIds) {
			this.activeSchemeIds = activeSchemeIds;
		}

		public String[] getActiveSchemes() {
			return this.activeSchemeIds;
		}

		public void setContextManager(ContextManager contextManager) {
			this.contextManager = contextManager;
		}

		@Override
		public int compare(Binding o1, Binding o2) {
			int rc = compareSchemes(activeSchemeIds, o1.getSchemeId(), o2.getSchemeId());
			if (rc != 0) {
				return rc;
			}

			/*
			 * Check to see which has the deeper context. The deeper context is the one that
			 * is preferred.
			 */
			if (contextManager != null) {
				CComp cComp = new ContextSet.CComp(contextManager);
				int contextDepth = cComp.compare(contextManager.getContext(o2.getContextId()),
						contextManager.getContext(o1.getContextId())); // deeper is better
				if (contextDepth != 0) {
					return contextDepth;
				}
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
			for (final Trigger trigger : triggers) {
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
	private ArrayList<Binding> bindings = new ArrayList<>();
	private Map<TriggerSequence, Binding> bindingsByTrigger = new HashMap<>();
	private Map<ParameterizedCommand, ArrayList<Binding>> bindingsByCommand = new HashMap<>();
	private Map<TriggerSequence, ArrayList<Binding>> bindingsByPrefix = new HashMap<>();
	private Map<TriggerSequence, ArrayList<Binding>> conflicts = new HashMap<>();
	private Map<TriggerSequence, ArrayList<Binding>> orderedBindingsByTrigger = new HashMap<>();
	private final Map<Binding, Boolean> activeBindings = new HashMap<>();

	private IContributionFactory contributionFactory;

	private MApplication application;

	public BindingTable(Context context, MApplication application) {
		tableId = context;
		this.application = application;
	}

	public Context getTableId() {
		return tableId;
	}

	public String getId() {
		return tableId.getId();
	}

	public Collection<Binding> getConflicts() {
		Collection<Binding> conflictsList = new ArrayList<>();
		for (ArrayList<Binding> conflictsForTrigger : conflicts.values()) {
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
		ArrayList<Binding> bindingList = orderedBindingsByTrigger.get(binding.getTriggerSequence());
		Binding possibleConflict = bindingsByTrigger.get(binding.getTriggerSequence());
		if (bindingList == null || bindingList.isEmpty()) {
			if (possibleConflict != null) {
				if (bindingList == null) {
					bindingList = new ArrayList<>();
					orderedBindingsByTrigger.put(binding.getTriggerSequence(), bindingList);
				}
				bindingList.add(binding);
				bindingList.add(possibleConflict);
				bindingList.sort(BEST_SEQUENCE);
			}
		} else {
			bindingList.add(binding);
			bindingList.sort(BEST_SEQUENCE);
		}

		if (possibleConflict != null && bindingList != null && !bindingList.isEmpty()
				&& bindingList.get(0) != possibleConflict) {
			removeBindingSimple(possibleConflict);
			possibleConflict = null;
		}

		evaluateOrderedBindings(binding.getTriggerSequence(), binding);
	}

	private void addBindingSimple(Binding binding) {
		bindings.add(binding);
		bindingsByTrigger.put(binding.getTriggerSequence(), binding);

		ArrayList<Binding> sequences = bindingsByCommand.get(binding.getParameterizedCommand());
		if (sequences == null) {
			sequences = new ArrayList<>();
			bindingsByCommand.put(binding.getParameterizedCommand(), sequences);
		}
		sequences.add(binding);
		sequences.sort(BEST_SEQUENCE);

		TriggerSequence[] prefs = binding.getTriggerSequence().getPrefixes();
		for (int i = 1; i < prefs.length; i++) {
			ArrayList<Binding> bindings = bindingsByPrefix.get(prefs[i]);
			if (bindings == null) {
				bindings = new ArrayList<>();
				bindingsByPrefix.put(prefs[i], bindings);
			}
			bindings.add(binding);
		}
	}

	private void removeBindingSimple(Binding binding) {
		bindings.remove(binding);
		bindingsByTrigger.remove(binding.getTriggerSequence());
		ArrayList<Binding> sequences = bindingsByCommand.get(binding.getParameterizedCommand());

		if (sequences != null) {
			sequences.remove(binding);
		}
		TriggerSequence[] prefs = binding.getTriggerSequence().getPrefixes();
		for (int i = 1; i < prefs.length; i++) {
			ArrayList<Binding> bindings = bindingsByPrefix.get(prefs[i]);
			if (bindings != null) {
				bindings.remove(binding);
			}
		}
	}

	public void removeBinding(Binding binding) {
		if (!getId().equals(binding.getContextId())) {
			throw new IllegalArgumentException("Binding context " + binding.getContextId() //$NON-NLS-1$
					+ " does not match " + getId()); //$NON-NLS-1$
		}
		ArrayList<Binding> bindingList = orderedBindingsByTrigger.get(binding.getTriggerSequence());
		Binding possibleConflict = bindingsByTrigger.get(binding.getTriggerSequence());
		if (possibleConflict == binding) {
			removeBindingSimple(binding);
			if (bindingList != null) {
				bindingList.remove(binding);
				if (bindingList.isEmpty()) {
					orderedBindingsByTrigger.remove(binding.getTriggerSequence());
				} else {
					evaluateOrderedBindings(binding.getTriggerSequence(), null);
				}
			}
		} else if (bindingList != null) {
			bindingList.remove(binding);
			if (bindingList.isEmpty()) {
				orderedBindingsByTrigger.remove(binding.getTriggerSequence());
			} else {
				evaluateOrderedBindings(binding.getTriggerSequence(), null);
			}
		}
		activeBindings.remove(binding);
	}

	private void evaluateOrderedBindings(TriggerSequence sequence, Binding binding) {
		ArrayList<Binding> bindingList = orderedBindingsByTrigger.get(sequence);

		// calculate binding to be used or any conflicts
		if (bindingList != null) {
			if (bindingList.isEmpty()) {
				orderedBindingsByTrigger.remove(sequence);
			} else if (bindingList.size() > 1) {
				Binding msb = bindingList.get(0);
				Binding lsb = bindingList.get(1);
				int rc = compareSchemes(BEST_SEQUENCE.getActiveSchemes(), msb.getSchemeId(),
						lsb.getSchemeId());
				if (rc == 0) {
					ArrayList<Binding> conflictList = conflicts.get(sequence);
					if (conflictList == null) {
						conflictList = new ArrayList<>();
						conflicts.put(sequence, conflictList);
					} else {
						conflictList.clear();
					}
					Iterator<Binding> i = bindingList.iterator();
					Binding prev = i.next();
					conflictList.add(prev);
					while (i.hasNext() && rc == 0) {
						Binding next = i.next();
						rc = compareSchemes(BEST_SEQUENCE.getActiveSchemes(), prev.getSchemeId(),
								next.getSchemeId());
						if (rc == 0) {
							conflictList.add(next);
						}
						prev = next;
					}
				} else {
					conflicts.remove(sequence);
					if (bindingsByTrigger.get(sequence) == null) {
						addBindingSimple(msb);
					}
				}
			} else {
				if (bindingsByTrigger.get(sequence) == null) {
					addBindingSimple(bindingList.get(0));
				}
				orderedBindingsByTrigger.remove(sequence);
			}
		} else if (binding != null) {
			conflicts.remove(sequence);
			if (bindingsByTrigger.get(sequence) == null) {
				addBindingSimple(binding);
			}
		}
	}

	public Binding getPerfectMatch(TriggerSequence trigger) {
		Binding binding = bindingsByTrigger.get(trigger);
		if (isActive(binding)) {
			return binding;
		}
		return null;
	}

	public Binding getBestSequenceFor(ParameterizedCommand command) {
		ArrayList<Binding> sequences = bindingsByCommand.get(command);
		if (sequences != null) {
			for (Binding binding : sequences) {
				if (isActive(binding)) {
					return binding;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Collection<Binding> getSequencesFor(ParameterizedCommand command) {
		ArrayList<Binding> triggers = bindingsByCommand.get(command);
		return (Collection<Binding>) (triggers == null ? Collections.emptyList() : getActive(triggers));
	}

	public Collection<Binding> getPartialMatches(TriggerSequence sequence) {
		return getActive(bindingsByPrefix.get(sequence));
	}

	public boolean isPartialMatch(TriggerSequence seq) {
		ArrayList<Binding> values = bindingsByPrefix.get(seq);
		return values != null && !getActive(values).isEmpty();
	}

	public Collection<Binding> getBindings() {
		return Collections.unmodifiableCollection(getActive(bindings));
	}

	List<Binding> getActive(List<Binding> bindings) {
		return bindings == null ? null : bindings.stream().filter(b -> isActive(b)).toList();
	}

	private boolean isActive(final Binding binding) {
		if (binding == null) {
			return false;
		}
		Boolean cachedValue = activeBindings.get(binding);
		if (cachedValue != null) {
			return cachedValue;
		}
		ParameterizedCommand command = binding.getParameterizedCommand();
		if (command == null) {
			// Binding without command is "unbound", so can't be active
			// We don't cache in case command will be added in preferences
			return false;
		}
		String identifierId = command.getId();
		if (contributionFactory == null && application != null) {
			contributionFactory = application.getContext().get(IContributionFactory.class);
		}
		if (contributionFactory == null) {
			// Something went wrong, let assume binding is active
			return true;
		}
		boolean currentValue = contributionFactory.isEnabled(identifierId);
		activeBindings.put(binding, currentValue);
		return currentValue;
	}

	public void activitiesChanged() {
		activeBindings.clear();
	}
}
