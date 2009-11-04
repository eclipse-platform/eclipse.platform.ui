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

import java.util.Collection;
import javax.inject.Inject;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.bindings.EBindingService;
import org.eclipse.e4.ui.bindings.TriggerSequence;
import org.eclipse.e4.ui.bindings.keys.KeySequence;
import org.eclipse.e4.ui.bindings.keys.ParseException;

/**
 *
 */
public class BindingServiceImpl implements EBindingService {
	static final String LOOKUP_BINDING = "binding"; //$NON-NLS-1$
	static final BindingLookupFinction LOOKUP_INSTANCE = new BindingLookupFinction();
	static final String B_ID = "binding::"; //$NON-NLS-1$

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
		if (context.get(keys) == null) {
			addFunction(keys);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#deactivateBinding(org.eclipse.e4.ui.bindings.
	 * TriggerSequence, org.eclipse.core.commands.ParameterizedCommand)
	 */
	public void deactivateBinding(TriggerSequence sequence, ParameterizedCommand command) {
		context.remove(B_ID + sequence.format());
	}

	/**
	 * @param sequence
	 */
	private void addFunction(String keys) {
		IEclipseContext root = (IEclipseContext) context.get(IContextConstants.ROOT_CONTEXT);
		if (root != null) {
			root.set(keys, LOOKUP_INSTANCE);
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
			// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#getPerfectMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public ParameterizedCommand getPerfectMatch(TriggerSequence trigger) {
		return (ParameterizedCommand) context
				.get(trigger.format(), lookupBinding(trigger.format()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.e4.ui.bindings.EBindingService#isPartialMatch(org.eclipse.e4.ui.bindings.
	 * TriggerSequence)
	 */
	public boolean isPartialMatch(TriggerSequence keySequence) {
		// TODO Auto-generated method stub
		return false;
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

	@Inject
	public void setContext(IEclipseContext c) {
		context = c;
	}

	public IEclipseContext getContext() {
		return context;
	}

	private Object[] lookupBinding(String bindingId) {
		return new Object[] { LOOKUP_BINDING, B_ID + bindingId };
	}
}
