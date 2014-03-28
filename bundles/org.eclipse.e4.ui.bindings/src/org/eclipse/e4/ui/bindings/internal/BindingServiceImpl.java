/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
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
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
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

	final static String ACTIVE_CONTEXTS = "activeContexts"; //$NON-NLS-1$
	final static String USER_TYPE = "user"; //$NON-NLS-1$

	@Inject
	private IEclipseContext context;

	@Inject
	private BindingTableManager manager;

	@Inject
	private ContextManager contextManager;

	private ContextSet contextSet = ContextSet.EMPTY;

	@Override
	public Binding createBinding(TriggerSequence sequence, ParameterizedCommand command,
			String contextId, Map<String, String> attributes) {

		String schemeId = DEFAULT_SCHEME_ID;
		String locale = null;
		String platform = null;
		int bindingType = Binding.SYSTEM;

		if (sequence != null && !sequence.isEmpty() && contextId != null) {
			if (attributes != null) {
				String tmp = attributes.get(SCHEME_ID_ATTR_TAG);
				if (tmp != null && tmp.length() > 0) {
					schemeId = tmp;
				}
				locale = attributes.get(LOCALE_ATTR_TAG);
				platform = attributes.get(PLATFORM_ATTR_TAG);
				if (USER_TYPE.equals(attributes.get(TYPE_ATTR_TAG))) {
					bindingType = Binding.USER;
				}
			}
			return new KeyBinding((KeySequence) sequence, command, schemeId, contextId, locale,
					platform, null, bindingType);
		}
		return null;
	}

	@Override
	public void activateBinding(Binding binding) {
		String contextId = binding.getContextId();
		BindingTable table = manager.getTable(contextId);
		if (table == null) {
			return;
		}
		table.addBinding(binding);
	}

	@Override
	public void deactivateBinding(Binding binding) {
		String contextId = binding.getContextId();
		BindingTable table = manager.getTable(contextId);
		if (table == null) {
			//System.err.println("No binding table for " + contextId); //$NON-NLS-1$
			return;
		}
		table.removeBinding(binding);
	}

	@Override
	public TriggerSequence createSequence(String sequence) {
		try {
			return KeySequence.getInstance(sequence);
		} catch (ParseException e) {
			// should probably log
		}
		return null;
	}

	@Override
	public Collection<Binding> getConflictsFor(TriggerSequence sequence) {
		return manager.getConflictsFor(contextSet, sequence);
	}

	@Override
	public Collection<Binding> getAllConflicts() {
		return manager.getAllConflicts();
	}

	@Override
	public Binding getPerfectMatch(TriggerSequence trigger) {
		return manager.getPerfectMatch(contextSet, trigger);
	}

	@Override
	public boolean isPartialMatch(TriggerSequence keySequence) {
		return manager.isPartialMatch(contextSet, keySequence);
	}

	@Override
	public TriggerSequence getBestSequenceFor(ParameterizedCommand command) {
		Binding binding = manager.getBestSequenceFor(contextSet, command);
		return binding == null ? null : binding.getTriggerSequence();
	}

	@Override
	public Collection<TriggerSequence> getSequencesFor(ParameterizedCommand command) {
		Collection<Binding> bindings = manager.getSequencesFor(contextSet, command);
		ArrayList<TriggerSequence> sequences = new ArrayList<TriggerSequence>(bindings.size());
		for (Binding binding : bindings) {
			sequences.add(binding.getTriggerSequence());
		}
		return sequences;
	}

	@Override
	public Collection<Binding> getBindingsFor(ParameterizedCommand command) {
		return manager.getBindingsFor(contextSet, command);
	}

	@Override
	public boolean isPerfectMatch(TriggerSequence sequence) {
		return getPerfectMatch(sequence) != null;
	}

	@Override
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

	@Override
	public Collection<Binding> getActiveBindings() {
		return manager.getActiveBindings();
	}

}
