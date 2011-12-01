/*****************************************************************
 * Copyright (c) 2009, 2011 Texas Instruments and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Chuong (Texas Instruments) - Initial API and implementation (Bug 286310)
 *****************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

/**
 * Label update which allows the label provider to set the checked element state.
 * The label provider can use the presentation context to determine whether the 
 * viewer is showing item check boxes. 
 * 
 * @since 3.6
 */
public interface ICheckUpdate extends ILabelUpdate {

    /**
     * Property of the presentation context which indicates that the viewer 
     * has the check box style.
     */
    public static final String PROP_CHECK = "org.eclipse.debug.ui.check";  //$NON-NLS-1$
    
    /**
     * Sets the check state of the tree node.
     * 
     * @param checked Whether element should be checked.
     * @param grayed Whether element should be grayed out.
     */
    public void setChecked(boolean checked, boolean grayed);
    
}
