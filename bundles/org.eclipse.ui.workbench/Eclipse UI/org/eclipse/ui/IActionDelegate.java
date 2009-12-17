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
import org.eclipse.jface.viewers.ISelection;

/**
 * Interface for actions contributed via an extension point.
 * <p>
 * This interface should be implemented by clients who need to contribute actions
 * via an extension point. The workbench will generate a <b>proxy action</b> 
 * object on behalf of the plug-in to avoid having to activate the plug-in until 
 * the user needs it. If the action is performed the workbench will load the class 
 * that implements this interface and create what is called an <b>action 
 * delegate</b> object. Then the request, and all subsequent ones, are
 * forwarded through the proxy action to the action delegate, which does the
 * real work. 
 * </p><p>
 * The proxy action is the one that appears in the UI, so the action delegate 
 * will need to talk to the proxy action in order to keep up an appropriate 
 * appearance. Once the action delegate has been created, it will be
 * notified of all selection changes, allowing it to enable or disable the 
 * proxy action appropriately.
 * </p><p>
 * An action delegate cannot be consulted about selection changes before the
 * action is performed because it does not exist.  For this reason, control of
 * the action's enable state should also be exercised through simple XML rules
 * contained in the extension.  These rules allow enable state control before
 * the action delegate's plug-in is loaded.
 * </p><p>
 * Clients can choose to subclass the provided abstract implementation
 * <code>org.eclipse.ui.actions.ActionDelegate</code> or implement the
 * interface directly.
 * </p>
 * 
 * @see org.eclipse.ui.actions.ActionDelegate
 * @see org.eclipse.ui.IActionDelegate2
 */
public interface IActionDelegate {
    /**
     * Performs this action.
     * <p>
     * This method is called by the proxy action when the action has been
     * triggered. Implement this method to do the actual work.
     * </p><p>
     * <b>Note:</b> If the action delegate also implements
     * <code>IActionDelegate2</code>, then this method is not invoked but
     * instead the <code>runWithEvent(IAction, Event)</code> method is called.
     * </p>
     *
     * @param action the action proxy that handles the presentation portion of the
     *   action
     */
    public void run(IAction action);

    /**
     * Notifies this action delegate that the selection in the workbench has changed.
     * <p>
     * Implementers can use this opportunity to change the availability of the
     * action or to modify other presentation properties.
     * </p><p>
     * When the selection changes, the action enablement state is updated based on
     * the criteria specified in the plugin.xml file. Then the delegate is notified
     * of the selection change regardless of whether the enablement criteria in the
     * plugin.xml file is met.
     * </p>
     *
     * @param action the action proxy that handles presentation portion of 
     * 		the action
     * @param selection the current selection, or <code>null</code> if there
     * 		is no selection.
     */
    public void selectionChanged(IAction action, ISelection selection);
}
