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
package org.eclipse.ui.internal.activities.ui;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.LabelProvider;

import org.eclipse.ui.activities.ActivityNotDefinedException;
import org.eclipse.ui.activities.IActivity;

/**
 * @since 3.0
 */
public class ActivityLabelProvider extends LabelProvider {

    /**
     * Create a new instance of the receiver.
     * @since 3.0
     */
    public ActivityLabelProvider() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        IActivity activity = ((IActivity)element);
        try {
            return activity.getName();
        }
        catch (ActivityNotDefinedException e) {
            return activity.getId();
        }
    }
}
