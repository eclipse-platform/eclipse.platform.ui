/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;

/**
 * Interface extension to <code>IActionDelegate</code> adding lifecycle methods.
 * In addition, a <code>runWithEvent</code> method that includes the triggering
 * SWT event.
 * <p>
 * An action delegate that implements this interface will have its
 * <code>runWithEvent(IAction, Event)</code> called instead of its
 * <code>run(IAction)</code> method.
 * </p><p>
 * Clients should implement this interface, in addition to
 * <code>IActionDelegate</code> or sub-interfaces, if interested in the
 * triggering event or in the lifecycle of the delegate object.
 * </p><p>
 * Clients can choose to subclass the provided abstract implementation
 * <code>org. eclipse. ui. actions. ActionDelegate</code> or implement the
 * interface directly.
 * </p>
 *
 * @see org.eclipse.ui.actions.ActionDelegate
 * @see org.eclipse.ui.IActionDelegate
 * @since 2.1
 */
public interface IActionDelegate2 extends IActionDelegate {
    /**
     * Allows the action delegate to initialize itself after being created by
     * the proxy action. This lifecycle method is called after the
     * action delegate has been created and before any other method of the
     * action delegate is called.
     * 
     * @param action the proxy action that handles the presentation portion of
     * the action.
     */
    public void init(IAction action);

    /**
     * Allows the action delegate to clean up. This lifecycle method is called
     * when the proxy action is done with this action delegate. This is the last
     * method called.
     */
    public void dispose();

    /**
     * Performs this action, passing the SWT event which triggered it. This
     * method is called by the proxy action when the action has been triggered.
     * Implement this method to do the actual work.
     * <p>
     * <b>Note:</b> This method is called instead of <code>run(IAction)</code>.
     * </p>
     *
     * @param action the action proxy that handles the presentation portion of
     * the action
     * @param event the SWT event which triggered this action being run
     * @since 2.0
     */
    public void runWithEvent(IAction action, Event event);
}
