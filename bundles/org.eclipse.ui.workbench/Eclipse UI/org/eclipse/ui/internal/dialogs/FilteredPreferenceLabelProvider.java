/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.preference.PreferenceLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.activities.WorkbenchActivityHelper;


/**
 * Adds colouring support for filtered items.
 * 
 * @since 3.0
 */
public class FilteredPreferenceLabelProvider extends PreferenceLabelProvider implements IColorProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        if (WorkbenchActivityHelper.filterItem(element)) {
            return WorkbenchActivityHelper.getFilterColor();
        }
        return null;
    }    
    
}
