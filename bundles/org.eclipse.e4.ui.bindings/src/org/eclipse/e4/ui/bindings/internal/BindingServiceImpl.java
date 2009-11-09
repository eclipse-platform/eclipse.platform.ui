/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
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
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.Trigger;
import org.eclipse.e4.ui.bindings.TriggerSequence;
import org.eclipse.e4.ui.bindings.keys.IKeyLookup;
import org.eclipse.e4.ui.bindings.keys.KeyLookupFactory;
import org.eclipse.e4.ui.bindings.keys.KeySequence;
import org.eclipse.e4.ui.bindings.keys.KeyStroke;
import org.eclipse.e4.ui.bindings.keys.ParseException;

/**
 *
 */
public class BindingServiceImpl implements EBindingService {

	static final class Binding {
		public TriggerSequence sequence;
		public ParameterizedCommand command;
		private String[] prefixes;
		private String bindingId;
		private String commandId;
		private int hashCode = -1;

		public Binding(TriggerSequence s, ParameterizedCommand c) {
			sequence = s;
			command = c;
		}

		String[] getPrefixes() {
			if (prefixes == null) {
				TriggerSequence[] prefs = sequence.getPrefixes();
				prefixes = new String[prefs.length - 1];
				for (int i = 1; i < prefs.length; i++) {
					prefixes[i - 1] = B_SEQ + prefs[i];
				}
			}
			return prefixes;
		}

		String getBindingId() {
			if (bindingId == null) {
				bindingId = B_ID + sequence.format();
			}
			return bindingId;
		}

		String getCommandId() {
			if (commandId == null) {
				commandId = P_ID + command.serialize();
			}
			return commandId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Binding)) {
				return false;
			}
			Binding b = (Binding) o;
			return sequence.equals(b.sequence) && command.equals(b.command);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			if (hashCode == -1) {
				hashCode = sequence.hashCode() * 81 + command.hashCode();
			}
			return hashCode;
		}
	}

	static final Comparator<Binding> BEST_SEQUENCE = new Comparator<Binding>() {
		public int compare(Binding o1, Binding o2) {
			/*
			 * Check to see which has the least number of triggers in the trigger sequence.
			 */
			final Trigger[] bestTriggers = o1.sequence.getTriggers();
			final Trigger[] currentTriggers = o2.sequence.getTriggers();
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
			return o1.sequence.format().length() - o2.sequence.format().length();
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
	};

	static final String LOOKUP_BINDING = "binding"; //$NON-NLS-1$
	static final String LOOKUP_CMD = "cmd"; //$NON-NLS-1$
	static final String BINDING_LOOKUP = "org.eclipse.e4.ui.bindings.EBindingLookup"; //$NON-NLS-1$
	static final String BINDING_PREFIX_LOOKUP = "org.eclipse.e4.ui.bindings.EBindingPrefixLookup"; //$NON-NLS-1$
	static final String CMD_LOOKUP = "org.eclipse.e4.ui.bindings.ECommandLookup"; //$NON-NLS-1$
	static final String CMD_SEQ_LOOKUP = "org.eclipse.e4.ui.bindings.ECommandSequenceLookup"; //$NON-NLS-1$
	static final String LOOKUP_PARTIAL_MATCH = "org.eclipse.e4.ui.bindings.EPartialMatchLookup"; //$NON-NLS-1$
	static final String B_ID = "binding::"; //$NON-NLS-1$
	static final String B_SEQ = "bindSeq::"; //$NON-NLS-1$
	static final String P_ID = "parmCmd::"; //$NON-NLS-1$

	private IEclipseContext context;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#activateBinding(org.eclipse.e4.ui.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand)
	 */
	public void activateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		Binding binding = new Binding(sequence, command);
		context.set(binding.getBindingId(), binding);

		// add mapping from command to keys
		addLocalArray(binding.getCommandId(), binding);

		// deal with partial bindings
		String[] prefixes = binding.getPrefixes();
		for (int i = 0; i < prefixes.length; i++) {
			addLocalArray(prefixes[i], binding);
		}
	}

	private void addLocalArray(String id, Binding binding) {
		ArrayList<Binding> bindings = new ArrayList<Binding>(3);
		ArrayList<Binding> tmp = (ArrayList<Binding>) context.getLocal(id);
		if (tmp != null) {
			bindings.addAll(tmp);
		}
		bindings.add(binding);
		Collections.sort(bindings, BEST_SEQUENCE);
		context.set(id, bindings);
	}

	private void removeLocalArray(String id, Binding binding) {
		ArrayList<Binding> tmp = (ArrayList<Binding>) context.getLocal(id);
		if (tmp.size() < 2) {
			context.remove(id);
		} else {
			tmp.remove(binding);
			context.set(id, new ArrayList<Binding>(tmp));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#deactivateBinding(org.eclipse.e4.ui.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand)
	 */
	public void deactivateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		Binding binding = new Binding(sequence, command);
		context.remove(binding.getBindingId());

		// remove the command to trigger bindings
		removeLocalArray(binding.getCommandId(), binding);

		// deal with removing the partial binding
		String[] prefixes = binding.getPrefixes();
		for (int i = 0; i < prefixes.length; i++) {
			removeLocalArray(prefixes[i], binding);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.bindings.EBindingService#createSequence(java.lang.String)
	 */
	public TriggerSequence createSequence(String sequence) {
		try {
			return KeySequence.getInstance(sequence);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getConflictsFor(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public Collection<ParameterizedCommand> getConflictsFor(TriggerSequence sequence) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getPerfectMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public ParameterizedCommand getPerfectMatch(TriggerSequence trigger) {
		Binding binding = (Binding) context.get(BINDING_LOOKUP, lookupBinding(trigger.format()));
		return binding == null ? null : binding.command;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#isPartialMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence keySequence) {
		return context.get(BINDING_PREFIX_LOOKUP, lookupSequence(keySequence.format())) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getBestSequenceFor(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public TriggerSequence getBestSequenceFor(ParameterizedCommand command) {
		String cmdString = command.serialize();
		ArrayList<Binding> tmp = (ArrayList<Binding>) context.get(CMD_LOOKUP,
				lookupCommand(cmdString));
		if (tmp != null && !tmp.isEmpty()) {
			return tmp.get(0).sequence;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getSequencesFor(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public Collection<TriggerSequence> getSequencesFor(ParameterizedCommand command) {
		String cmdString = command.serialize();
		return (Collection<TriggerSequence>) context.get(CMD_SEQ_LOOKUP, lookupCommand(cmdString));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#isPerfectMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public boolean isPerfectMatch(TriggerSequence sequence) {
		return getPerfectMatch(sequence) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getPartialMatches(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public Collection<ParameterizedCommand> getPartialMatches(TriggerSequence sequence) {
		return (Collection<ParameterizedCommand>) context.get(LOOKUP_PARTIAL_MATCH,
				lookupSequence(sequence.format()));
	}

	/**
	 * @param c
	 */
	@Inject
	public void setContext(IEclipseContext c) {
		context = c;
	}

	/**
	 * @return the context for this service.
	 */
	public IEclipseContext getContext() {
		return context;
	}

	private Object[] lookupBinding(String bindingId) {
		return new Object[] { B_ID + bindingId };
	}

	private Object[] lookupCommand(String cmdString) {
		return new Object[] { P_ID + cmdString };
	}

	private Object[] lookupSequence(String sequence) {
		return new Object[] { B_SEQ + sequence };
	}
}
