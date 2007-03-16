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
package org.eclipse.ui.help;

import org.eclipse.swt.events.HelpEvent;

/**
 * A content computer is used to dynamically calculate help support contexts at the
 * time the user requests help.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * @deprecated nested contexts are no longer supported by the help support system
 * 
 */
public interface IContextComputer {
    /**
     * Computes contexts for the help system.
     *
     * @param event the help event which triggered this request for help
     * @return a mixed-type array of context ids (type <code>String</code>)
     *   and/or help contexts (type <code>IContext</code>)
     * @see org.eclipse.help.IContext
     */
    public Object[] computeContexts(HelpEvent event);

    /**
     * Returns the local contexts for this context computer.
     * <p>
     * Typically this method is called by other instances of
     * <code>IContextComputer</code> in their <code>computeContexts</code>
     * method.
     * </p>
     * <p>
     * The important concept here is that the value returned by 
     * <code>computeContexts</code> represents the complete help
     * contexts and is passed directly to the help support system.
     * </p>
     * <p>
     * However the value returned by this method represents the
     * only the contexts for the particular control with which this
     * <code>IContextComputer</code> is associated.
     * </p> 
     * @param event the help event which triggered this request for help
     * @return a mixed-type array of context ids (type <code>String</code>)
     *   and/or help contexts (type <code>IContext</code>)
     * @see org.eclipse.help.IContext
     */
    public Object[] getLocalContexts(HelpEvent event);
}
