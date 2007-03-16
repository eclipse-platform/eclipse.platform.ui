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
package org.eclipse.ui.internal.registry;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * An action set is responsible for the creation of actions.
 * The end user may add these actions to a workbench page menu and tool bar
 * if they are deemed useful to the particular task at hand.
 * <p>
 * [Issue: This interface is not exposed in API, but time may
 * demonstrate that it should be.  For the short term leave it be.
 * In the long term its use should be re-evaluated. ]
 * </p>
 * <p>
 * In the current workbench implementation the desktop provides the
 * only implementation of this class in PluginActionSet.  So, it may
 * be useful to phase this interface out at some point.  PluginActionSet
 * provides a lazy load strategy, where the actions are declared in
 * XML and represented at runtime by a PluginAction.  
 * </p>
 */
public interface IActionSet {
    /**
     * Disposes of this action set.
     * <p>
     * Implementation should remove any references to the window and action bars 
     * created in the <code>init</code>.
     * </p>
     * <p>
     * [Issue: Should this say: "...should remove anything they contributed
     *  in <code>init</code>? Or is most of the withdrawal done automatically?
     * ]
     * </p>
     */
    public void dispose();

    /**
     * Initializes this action set, which is expected to add it actions as required
     * to the given workbench window and action bars.
     *
     * @param window the workbench window
     * @param bars the action bars
     */
    public void init(IWorkbenchWindow window, IActionBars bars);
}
