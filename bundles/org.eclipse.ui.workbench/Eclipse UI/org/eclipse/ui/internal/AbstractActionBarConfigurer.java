/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Abstract base implementation of an IActionBarConfigurer.
 * 
 * @since 3.0
 */
public abstract class AbstractActionBarConfigurer implements
        IActionBarConfigurer {

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IActionBarConfigurer
     */
    public abstract IStatusLineManager getStatusLineManager();

    /* (non-javadoc)
     * @see org.eclipse.ui.application.IActionBarConfigurer
     */
    public abstract IMenuManager getMenuManager();

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.application.IActionBarConfigurer#getCoolBarManager()
     */
    public abstract ICoolBarManager getCoolBarManager();

}