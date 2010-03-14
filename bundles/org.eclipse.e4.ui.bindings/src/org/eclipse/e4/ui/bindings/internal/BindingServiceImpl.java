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
import java.util.HashMap;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;

/**
 *
 */
public class BindingServiceImpl implements EBindingService {
	static HashMap<Binding, String[]> prefixCache = new HashMap<Binding, String[]>();

	static String[] getPrefixes(Binding b) {
		String[] prefixes = prefixCache.get(b);
		if (prefixes == null) {
			TriggerSequence[] prefs = b.getTriggerSequence().getPrefixes();
			prefixes = new String[prefs.length - 1];
			prefixCache.put(b, prefixes);
			for (int i = 1; i < prefs.length; i++) {
				prefixes[i - 1] = B_SEQ + prefs[i];
			}
		}
		return prefixes;
	}

	static String getBindingId(Binding b) {
		return B_ID + b.getTriggerSequence().format();
	}

	static String getCommandId(Binding b) {
		return P_ID + b.getParameterizedCommand().serialize();
	}

	static final Comparator<Binding> BEST_SEQUENCE = new Comparator<Binding>() {
		public int compare(Binding o1, Binding o2) {
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
	 * @see org.eclipse.e4.ui.bindings.EBindingService#createBinding(org.eclipse.jface.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand, java.lang.String,
	 * java.lang.String)
	 */
	public Binding createBinding(TriggerSequence sequence, ParameterizedCommand command,
			String schemeId, String contextId) {
		return new KeyBinding((KeySequence) sequence, command, schemeId, contextId, null, null,
				null, Binding.SYSTEM);
	}

	private Binding createDefaultBinding(TriggerSequence sequence, ParameterizedCommand command) {
		return createBinding(sequence, command, "org.eclipse.ui.defaultAcceleratorConfiguration", //$NON-NLS-1$
				"org.eclipse.ui.context.window"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#activateBinding(org.eclipse.e4.ui.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand)
	 */
	public Binding activateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		Binding binding = createDefaultBinding(sequence, command);

		activateBinding(binding);
		return binding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.bindings.EBindingService#activateBinding(org.eclipse.jface.bindings.Binding
	 * )
	 */
	public void activateBinding(Binding binding) {
		context.set(getBindingId(binding), binding);
		// add mapping from command to keys
		addLocalArray(getCommandId(binding), binding);

		// deal with partial bindings
		String[] prefixes = getPrefixes(binding);
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
	public Binding deactivateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		Binding binding = createDefaultBinding(sequence, command);
		Binding oldBinding = (Binding) context.get(getBindingId(binding));
		deactivateBinding(binding);

		return oldBinding;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.bindings.EBindingService#deactivateBinding(org.eclipse.jface.bindings.Binding
	 * )
	 */
	public void deactivateBinding(Binding binding) {
		context.remove(getBindingId(binding));

		// remove the command to trigger bindings
		removeLocalArray(getCommandId(binding), binding);

		// deal with removing the partial binding
		String[] prefixes = getPrefixes(binding);
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
	public Collection<Binding> getConflictsFor(TriggerSequence sequence) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getPerfectMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public Binding getPerfectMatch(TriggerSequence trigger) {
		return (Binding) context.get(BINDING_LOOKUP, lookupBinding(trigger.format()));
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
			return tmp.get(0).getTriggerSequence();
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
	public Collection<Binding> getPartialMatches(TriggerSequence sequence) {
		return (Collection<Binding>) context.get(LOOKUP_PARTIAL_MATCH, lookupSequence(sequence
				.format()));
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
