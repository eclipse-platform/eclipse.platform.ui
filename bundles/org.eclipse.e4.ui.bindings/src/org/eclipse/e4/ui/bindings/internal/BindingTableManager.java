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
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.TriggerSequence;

/**
 * manage tables of bindings that can be used to look up commands from keys.
 */
public class BindingTableManager {
	@Inject
	private IEclipseContext eclipseContext;

	HashSet<String> definedTables = new HashSet<String>();

	public void addTable(BindingTable table) {
		String contextId = table.getId();
		if (eclipseContext.containsKey(contextId)) {
			return; // it's already there
			//			throw new IllegalArgumentException("Already contains table " + contextId); //$NON-NLS-1$
		}
		eclipseContext.set(contextId, table);
		definedTables.add(contextId);
	}

	public void removeTable(BindingTable table) {
		String contextId = table.getId();
		if (!eclipseContext.containsKey(contextId)) {
			throw new IllegalArgumentException("Does not contains table " + contextId); //$NON-NLS-1$
		}
		eclipseContext.remove(contextId);
		definedTables.remove(contextId);
	}

	public BindingTable getTable(String id) {
		return (BindingTable) eclipseContext.get(id);
	}

	// we're just going through each binding table, and returning a
	// flat list of bindings here
	public Collection<Binding> getActiveBindings() {
		ArrayList<Binding> bindings = new ArrayList<Binding>();
		for (String id : definedTables) {
			Object obj = eclipseContext.get(id);
			if (obj instanceof BindingTable) {
				bindings.addAll(((BindingTable) obj).getBindings());
			}
		}
		return bindings;
	}

	public ContextSet createContextSet(Collection<Context> contexts) {
		return new ContextSet(contexts);
	}

	public Binding getPerfectMatch(ContextSet contextSet, TriggerSequence triggerSequence) {
		Binding result = null;
		List<Context> contexts = contextSet.getContexts();
		ListIterator<Context> it = contexts.listIterator(contexts.size());
		while (it.hasPrevious() && result == null) {
			Context c = it.previous();
			BindingTable table = getTable(c.getId());
			if (table != null) {
				result = table.getPerfectMatch(triggerSequence);
			}
		}
		return result;
	}

	public Binding getBestSequenceFor(ContextSet contextSet,
			ParameterizedCommand parameterizedCommand) {
		ArrayList<Binding> bindings = (ArrayList<Binding>) getSequencesFor(contextSet,
				parameterizedCommand);
		if (bindings.size() == 0) {
			return null;
		}
		return bindings.get(0);
	}

	public Collection<Binding> getSequencesFor(ContextSet contextSet,
			ParameterizedCommand parameterizedCommand) {
		ArrayList<Binding> bindings = new ArrayList<Binding>();
		List<Context> contexts = contextSet.getContexts();
		ListIterator<Context> it = contexts.listIterator(contexts.size());
		while (it.hasPrevious()) {
			Context c = it.previous();
			BindingTable table = getTable(c.getId());
			if (table != null) {
				Collection<Binding> sequences = table.getSequencesFor(parameterizedCommand);
				if (sequences != null) {
					bindings.addAll(sequences);
				}
			}
		}
		Collections.sort(bindings, BindingTable.BEST_SEQUENCE);
		return bindings;
	}

	public boolean isPartialMatch(ContextSet contextSet, TriggerSequence sequence) {
		List<Context> contexts = contextSet.getContexts();
		ListIterator<Context> it = contexts.listIterator(contexts.size());
		while (it.hasPrevious()) {
			Context c = it.previous();
			BindingTable table = getTable(c.getId());
			if (table != null) {
				if (table.isPartialMatch(sequence)) {
					return true;
				}
			}
		}
		return false;
	}

	public Collection<Binding> getPartialMatches(ContextSet contextSet, TriggerSequence sequence) {
		ArrayList<Binding> bindings = new ArrayList<Binding>();
		List<Context> contexts = contextSet.getContexts();
		ListIterator<Context> it = contexts.listIterator(contexts.size());
		while (it.hasPrevious()) {
			Context c = it.previous();
			BindingTable table = getTable(c.getId());
			if (table != null) {
				Collection<Binding> partialMatches = table.getPartialMatches(sequence);
				if (partialMatches != null) {
					bindings.addAll(partialMatches);
				}
			}
		}
		return bindings;
	}
}
