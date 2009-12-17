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
package org.eclipse.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;

/**
 * Abstract base implementation of <code>IActionDelegate</code> and
 * <code>IActionDelegate2</code> for a client delegate action.
 * <p>
 * Subclasses should reimplement <code>runWithEvent</code> or <code>run</code>
 * methods to do the action's work, and may reimplement
 * <code>selectionChanged</code> to react to selection changes in the workbench.
 * </p>
 */
public abstract class ActionDelegate implements IActionDelegate2 {
    /**
     * The <code>ActionDelegate</code> implementation of this
     * <code>IActionDelegate</code> method does nothing. Subclasses may
     * reimplement.
     * <p>
     * <b>Note:</b> This method is not called directly by the proxy action. Only
     * by the default implementation of <code>runWithEvent</code> of this
     * abstract class.
     */
    public void run(IAction action) {
    }

    /**
     * The <code>ActionDelegate</code> implementation of this
     * <code>IActionDelegate</code> method does nothing. Subclasses may
     * reimplement.
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }

    /**
     * The <code>ActionDelegate</code> implementation of this
     * <code>IActionDelegate2</code> method does nothing. Subclasses may
     * reimplement.
     */
    public void init(IAction action) {
    }

    /**
     * The <code>ActionDelegate</code> implementation of this
     * <code>IActionDelegate2</code> method does nothing. Subclasses may
     * reimplement.
     */
    public void dispose() {
    }

    /**
     * The <code>ActionDelegate</code> implementation of this
     * <code>IActionDelegate2</code> method redirects to the <code>run</code>
     * method. Subclasses may reimplement.
     */
    public void runWithEvent(IAction action, Event event) {
        run(action);
    }
}
