/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.breadcrumb;

import org.eclipse.jface.viewers.ISelection;

/**
 * Interface allowing breadcrumb drop-down implementors to communicate with their
 * containing breadcrumb.   
 * 
 * @since 3.5
 */
public interface IBreadcrumbDropDownSite {
    
    /**
     * Notifies the breadcrumb that the given selection was made in the drop-down
     * viewer.
     * @param selection Selection to set to breadcrumb.
     */
    public void notifySelection(ISelection selection);
    
    /**
     * Notifies the breadcrumb that the drop-down viewer should be closed.
     */
    public void close();
    
    /**
     * Notifies the breadcrumb that the drop-down viewer's contents have 
     * changed and viewer shell should be adjusted for the new size.
     */
    public void updateSize();
}
