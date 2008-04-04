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

import org.eclipse.jface.action.ICoolBarManager;

/**
 * Interface extention to <code>IActionBars</code> that provides an additional
 * cool bar manager.
 * 
 * @since 3.0 
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IActionBars2 extends IActionBars {

    /**
     * Returns the cool bar manager.
     * <p>
     * Note: Clients who add or remove items from the returned cool bar manager are
     * responsible for calling <code>updateActionBars</code> so that the changes
     * can be propagated throughout the workbench.
     * </p>
     *
     * @return the cool bar manager.
     */
    public ICoolBarManager getCoolBarManager();

}
