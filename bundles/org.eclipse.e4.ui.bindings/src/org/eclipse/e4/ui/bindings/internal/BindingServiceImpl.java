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
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyBinding;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;

/**
 *
 */
public class BindingServiceImpl implements EBindingService {
	static final String ACTIVE_CONTEXTS = "activeContexts"; //$NON-NLS-1$

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

	@Inject
	private IEclipseContext context;

	@Inject
	private BindingTableManager manager;

	@Inject
	private ContextManager contextManager;

	private ContextSet contextSet = ContextSet.EMPTY;

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.bindings.EBindingService#activateBinding(org.eclipse.jface.bindings.Binding
	 * )
	 */
	public void activateBinding(Binding binding) {
		String contextId = binding.getContextId();
		BindingTable table = manager.getTable(contextId);
		if (table == null) {
			System.err.println("No binding table for " + contextId); //$NON-NLS-1$
		}
		table.addBinding(binding);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.bindings.EBindingService#deactivateBinding(org.eclipse.jface.bindings.Binding
	 * )
	 */
	public void deactivateBinding(Binding binding) {
		String contextId = binding.getContextId();
		BindingTable table = manager.getTable(contextId);
		if (table == null) {
			System.err.println("No binding table for " + contextId); //$NON-NLS-1$
		}
		table.removeBinding(binding);
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
		return manager.getPerfectMatch(contextSet, trigger);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#isPartialMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence keySequence) {
		return manager.isPartialMatch(contextSet, keySequence);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getBestSequenceFor(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public TriggerSequence getBestSequenceFor(ParameterizedCommand command) {
		Binding binding = manager.getBestSequenceFor(contextSet, command);
		return binding == null ? null : binding.getTriggerSequence();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getSequencesFor(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public Collection<TriggerSequence> getSequencesFor(ParameterizedCommand command) {
		Collection<Binding> bindings = manager.getSequencesFor(contextSet, command);
		ArrayList<TriggerSequence> sequences = new ArrayList<TriggerSequence>(bindings.size());
		for (Binding binding : bindings) {
			sequences.add(binding.getTriggerSequence());
		}
		return sequences;
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
		return manager.getPartialMatches(contextSet, sequence);
	}

	/**
	 * @return the context for this service.
	 */
	public IEclipseContext getContext() {
		return context;
	}

	@Inject
	public void setContextIds(@Named(ACTIVE_CONTEXTS) @Optional Set<String> set) {
		if (set == null || set.isEmpty() || contextManager == null) {
			contextSet = ContextSet.EMPTY;
			return;
		}
		ArrayList<Context> contexts = new ArrayList<Context>();
		for (String id : set) {
			contexts.add(contextManager.getContext(id));
		}
		contextSet = manager.createContextSet(contexts);
	}
}
