/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.activities.IActivity;
import org.eclipse.ui.activities.ICategory;
import org.eclipse.ui.activities.NotDefinedException;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * Provides labels for elements drawn from <code>IActivityManagers</code>.  
 * Ie:  <code>IActivity</code> and <code>ICategory</code> objects.
 * 
 * @since 3.0
 */
public class ActivityCategoryLabelProvider extends LabelProvider {

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element instanceof ICategory) {
            return WorkbenchImages
                    .getImage(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY_CATEGORY);
        } else {
            return WorkbenchImages
                    .getImage(IWorkbenchGraphicConstants.IMG_OBJ_ACTIVITY);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof IActivity) {
            IActivity activity = (IActivity) element;
            try {
                return activity.getName();
            } catch (NotDefinedException e) {
                return activity.getId();
            }
        } else if (element instanceof ICategory) {
            ICategory category = ((ICategory) element);
            try {
                return category.getName();
            } catch (NotDefinedException e) {
                return category.getId();
            }
        }
        return super.getText(element);
    }
}