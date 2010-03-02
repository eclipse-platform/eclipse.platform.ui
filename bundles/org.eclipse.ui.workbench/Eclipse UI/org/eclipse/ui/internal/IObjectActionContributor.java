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
package org.eclipse.ui.internal;

import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This interface must be implemented in order to contribute
 * to context (pop-up) menu for an object. Classes
 * that implement this interface must register
 * with the popup menu manager.
 */
public interface IObjectActionContributor extends IObjectContributor {
    /**
     * Implement this method to add actions that deal with the currently
     * selected object or objects. Actions should be added to the
     * provided menu object. Current selection can be obtained from
     * the given selection provider.
     * 
     * @return <code>true</code> if any contributions were made, and <code>false</code> otherwise.
     */
    public boolean contributeObjectActions(IWorkbenchPart part,
            IMenuManager menu, ISelectionProvider selProv,
            List actionIdOverrides);

    /**
     * Implement this method to add menus that deal with the currently
     * selected object or objects. Menus should be added to the
     * provided menu object. Current selection can be obtained from
     * the given selection provider.
     * 
     * @return <code>true</code> if any contributions were made, and <code>false</code> otherwise.
     */
    public boolean contributeObjectMenus(IMenuManager menu,
            ISelectionProvider selProv);

    /**
     * Contribute to the list the action identifiers from other contributions that 
     * this contribution wants to override. Actions of these identifiers will
     * not be contributed.
     */
    public void contributeObjectActionIdOverrides(List actionIdOverrides);
}
