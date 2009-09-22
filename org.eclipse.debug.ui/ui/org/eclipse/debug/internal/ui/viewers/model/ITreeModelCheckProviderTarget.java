/*****************************************************************
 * Copyright (c) 2009 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation  (Bug 286310)
 *****************************************************************/
package org.eclipse.debug.internal.ui.viewers.model;

import org.eclipse.jface.viewers.TreePath;

/**
 *  This interface can be implemented by the viewer which uses the
 * {@link TreeModelLabelProvider} label provider and supports the 
 * {@link org.eclipse.swt.SWT.CHECK} style.  It allows the label provider to 
 * update the viewer with check-box information retrieved from the 
 * element-based label providers.
 * 
 * @since 3.6
 */
public interface ITreeModelCheckProviderTarget extends ITreeModelLabelProviderTarget {
    
    /**
     * Sets the element check state data.
     * 
     * @param path
     * @param checked
     * @param grayed
     */
    public void setElementChecked(TreePath path, boolean checked, boolean grayed);    

    /**
     * Retrieves the element check state.
     * 
     * @param path
     * @return checked
     */
    public boolean getElementChecked(TreePath path);    

    /**
     * Retrieves the element's check box grayed state.
     * 
     * @param path
     * @return grayed
     */
    public boolean getElementGrayed(TreePath path);    
}
