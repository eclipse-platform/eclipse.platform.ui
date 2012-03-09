/*****************************************************************
 * Copyright (c) 2012 Texas Instruments and others
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
 * Action provider allows a debug model to override the standard actions in the 
 * variables view.  The client should return this provider as an adapter to the 
 * input element of the variables view.
 * 
 * @since 3.8
 */
public interface IViewActionProvider {
	/**
	 * Get action for a given presentation context and action id.  Implementation
	 * should return an action implementation appropriate for given view and action ID.
	 * The implementation may register itself as listener to presentation context 
	 * to determine when to dispose the returned action.
	 * @param presentationContext presentation context
	 * @param actionID action id
	 * @return action or null 
	 */
	public IAction getAction(IPresentationContext presentationContext, String actionID);
}
