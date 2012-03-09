/*****************************************************************
 * Copyright (c) 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Texas Instruments - View action override (Bug 344023)
 *****************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import org.eclipse.jface.action.IAction;


/**
 * An interface that allows an implementation to provide (contribute) its own
 * action which is used to override an action for the same action id.
 * 
 * @since 3.7
 */
public interface IViewActionOverride {
	/**
	 * Get action for a given presentation context and action id. Implementation
	 * class can use presentation context to figure out the view part or view
	 * model (IVMProvider) which wants to provide (contribute) an action. Once
	 * the view part or view model is known, the dedicated action for the view
	 * can be figured out by the implementation, view model, or some other
	 * classes.
	 * @param presentationContext presentation context
	 * @param actionID action id
	 * @return action or null
	 */
	public IAction getAction(IPresentationContext presentationContext, String actionID);
}
