/************************************************************************
Copyright (c) 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

package org.eclipse.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

/**
 * Interface extension to <code>IActionDelegate</code> adding lifecycle methods
 * and including the triggering SWT event in the <code>runWithEvent</code>
 * method.
 * <p>
 * An action delegate that implements this interface will have its
 * <code>runWithEvent(IAction, Event)</code> called instead of the
 * <code>run(IAction)</code> method.
 * </p><p>
 * Clients should implement this interface, in addition to
 * <code>IActionDelegate</code> or sub-interfaces, if interested in the
 * triggering event or in the lifecycle of the delegate object.
 * </p>
 *
 * @see org.eclipse.ui.IActionDelegate
 * @since 2.1
 */
public interface IActionDelegate2 extends IActionDelegate {
	/**
	 * Allows the delegate to initialize itself after being created by the proxy
	 * action. This lifecycle method is called after the delegate has been
	 * created and before any other method of the delegate is called.
	 * 
	 * @param action the proxy action that handles the presentation portion of
	 * the action.
	 */
	public void init(IAction action);
	
	/**
	 * Allows the delegate to clean up. This lifecycle method is called when the
	 * proxy action is done with this delegate. This is the last method called.
	 */
	public void dispose();
	
	/**
	 * Performs this action, passing the SWT event which triggered it. This
	 * method is called when the delegating action has been triggered. Implement
	 * this method to do the actual work.
	 * <p>
	 * <b>Note:</b> This method is called instead of <code>run(IAction)</code>.
	 *
	 * @param action the action proxy that handles the presentation portion of
	 * the action
	 * @param event the SWT event which triggered this action being run
	 * @since 2.0
	 */
	public void runWithEvent(IAction action, Event event);
}
