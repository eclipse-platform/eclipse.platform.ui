/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.contexts;

import java.util.Iterator;

import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.services.INestable;

/**
 * <p>
 * A context service which delegates almost all responsibility to the parent
 * service. This service is capable of being nested inside a component that is
 * not recognized by the "source" event mechanism.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public class NestableContextService extends SlaveContextService implements
		INestable {
	/**
	 * Maintain the state of the context service.
	 */
	private boolean fActive;

	/**
	 * Construct the new nested slave context.
	 * 
	 * @param parentService
	 *            the parent context service; must not be <code>null</code>.
	 * @param defaultExpression
	 *            A default expression to use to determine viability. It's
	 *            mainly used for conflict resolution. It can be
	 *            <code>null</code>.
	 */
	public NestableContextService(IContextService parentService,
			Expression defaultExpression) {
		super(parentService, defaultExpression);
		fActive = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.contexts.SlaveContextService#doActivateContext(org.eclipse.ui.contexts.IContextActivation)
	 */
	protected IContextActivation doActivateContext(IContextActivation activation) {
		if (fActive) {
			return super.doActivateContext(activation);
		}
		fLocalActivations.put(activation, null);
		return activation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#activate()
	 */
	public void activate() {
		if (fActive) {
			return;
		}

		Iterator c = fLocalActivations.keySet().iterator();
		while (c.hasNext()) {
			IContextActivation activation = (IContextActivation) c.next();
			super.doActivateContext(activation);
		}
		fActive = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.INestable#deactivate()
	 */
	public void deactivate() {
		if (!fActive) {
			return;
		}
		deactivateContexts(fParentActivations);
		fParentActivations.clear();

		Iterator c = fLocalActivations.keySet().iterator();
		while (c.hasNext()) {
			fLocalActivations.put(c.next(), null);
		}
		fActive = false;
	}
}
