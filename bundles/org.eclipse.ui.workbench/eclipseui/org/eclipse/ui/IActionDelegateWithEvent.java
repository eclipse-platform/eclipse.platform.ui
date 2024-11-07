/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

/**
 * This interface is a mixin interface for action delegates, adding the ability
 * to examine the triggering SWT event when it is run. If an action delegate
 * implements this interface, then <code>runWithEvent(IAction, Event)</code> is
 * called instead of <code>run(IAction)</code>.
 * <p>
 * Clients should implement this interface, in addition to
 * <code>IActionDelegate</code> (or subinterface), if they need to examine the
 * triggering event. Otherwise, they should simply implement
 * <code>IActionDelegate</code> (or subinterface).
 * </p>
 *
 * @since 2.0
 * @deprecated Use org.eclipse.ui.IActionDelegate2 instead.
 */
@Deprecated
public interface IActionDelegateWithEvent {

	/**
	 * Performs this action, passing the SWT event which triggered it.
	 * <p>
	 * This method is called when the delegating action has been triggered.
	 * Implement this method to do the actual work. If an action delegate implements
	 * this interface, this method is called instead of <code>run(IAction)</code>.
	 * </p>
	 *
	 * @param action the action proxy that handles the presentation portion of the
	 *               action
	 * @param event  the SWT event which triggered this action being run
	 * @since 2.0
	 * @deprecated Use org.eclipse.ui.IActionDelegate2 instead.
	 */
	@Deprecated
	void runWithEvent(IAction action, Event event);

}
