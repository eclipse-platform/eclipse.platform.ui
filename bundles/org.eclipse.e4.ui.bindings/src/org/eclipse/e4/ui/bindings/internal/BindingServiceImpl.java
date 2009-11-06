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
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.TriggerSequence;
import org.eclipse.e4.ui.bindings.keys.KeySequence;
import org.eclipse.e4.ui.bindings.keys.ParseException;

/**
 *
 */
public class BindingServiceImpl implements EBindingService {
	static final String LOOKUP_BINDING = "binding"; //$NON-NLS-1$
	static final String LOOKUP_CMD = "cmd"; //$NON-NLS-1$
	static final String BINDING_LOOKUP = "org.eclipse.e4.ui.bindings.EBindingLookup"; //$NON-NLS-1$
	static final String CMD_LOOKUP = "org.eclipse.e4.ui.bindings.ECommandLookup"; //$NON-NLS-1$
	static final String B_ID = "binding::"; //$NON-NLS-1$
	static final String P_ID = "parmCmd::"; //$NON-NLS-1$

	private IEclipseContext context;

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#activateBinding(org.eclipse.e4.ui.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand)
	 */
	public void activateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		String keys = sequence.format();
		context.set(B_ID + keys, command);

		// add mapping from command to keys
		String cmdString = command.serialize();
		ArrayList bindings = new ArrayList(3);
		String cmdBindingId = P_ID + cmdString;
		ArrayList tmp = (ArrayList) context.getLocal(cmdBindingId);
		if (tmp != null) {
			bindings.addAll(tmp);
		}
		bindings.add(sequence);
		context.set(cmdBindingId, bindings);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#deactivateBinding(org.eclipse.e4.ui.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand)
	 */
	public void deactivateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		context.remove(B_ID + sequence.format());
		// TODO remove command to key mapping
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
	public Collection getConflictsFor(TriggerSequence sequence) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getPerfectMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public ParameterizedCommand getPerfectMatch(TriggerSequence trigger) {
		return (ParameterizedCommand) context.get(BINDING_LOOKUP, lookupBinding(trigger.format()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#isPartialMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence keySequence) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getBestSequenceFor(org.eclipse.core.commands.
	 * ParameterizedCommand)
	 */
	public TriggerSequence getBestSequenceFor(ParameterizedCommand command) {
		String cmdString = command.serialize();
		ArrayList tmp = (ArrayList) context.get(CMD_LOOKUP, lookupCommand(cmdString));
		if (tmp != null && !tmp.isEmpty()) {
			return (TriggerSequence) tmp.get(0);
		}
		return null;
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
}
