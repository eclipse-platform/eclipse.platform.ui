/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.expressions.Expression;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.services.INestable;

/**
 * <p>
 * A handler service which delegates almost all responsibility to the parent
 * service. It is capable of being nested inside a component that is not
 * recognizable by the "sources" event mechanism. This means that the handlers
 * must be activated and deactivated manually.
 * </p>
 * <p>
 * This class is not intended for use outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public final class NestableHandlerService extends SlaveHandlerService implements
		INestable {

	/**
	 * Whether the component with which this service is associated is active.
	 */
	private boolean active = false;

	/**
	 * Constructs a new instance.
	 * 
	 * @param parentHandlerService
	 *            The parent handler service for this slave; must not be
	 *            <code>null</code>.
	 * @param defaultExpression
	 *            The default expression to use if none is provided. This is
	 *            primarily used for conflict resolution. This value may be
	 *            <code>null</code>.
	 */
	public NestableHandlerService(final IHandlerService parentHandlerService,
			final Expression defaultExpression) {
		super(parentHandlerService, defaultExpression);
	}

	public final void activate() {
		if (active) {
			return;
		}

		Object[] localActivations = localActivationsToParentActivations.keySet().toArray();
		for (int i = 0; i < localActivations.length; i++) {
			final Object object = localActivations[i];
			if (object instanceof IHandlerActivation) {
				final IHandlerActivation localActivation = (IHandlerActivation) object;
				// Ignore activations that have been cleared since the copy
				// was made.
				if (localActivationsToParentActivations.containsKey(localActivation))
					super.doActivation(localActivation);
			}
		}
		active = true;
	}

	protected final IHandlerActivation doActivation(
			final IHandlerActivation localActivation) {
		if (active) {
			return super.doActivation(localActivation);
		} 
		localActivationsToParentActivations.put(localActivation, null);
		return localActivation;
	}

	public final void deactivate() {
		if (!active) {
			return;
		}

		deactivateHandlers(parentActivations);
		parentActivations.clear();

		Object[] localActivations = localActivationsToParentActivations.keySet().toArray();
		for (int i = 0; i < localActivations.length; i++) {
			final Object object = localActivations[i];
			if (localActivationsToParentActivations.containsKey(object))
				localActivationsToParentActivations.put(object, null);
		}

		active = false;
	}
}

