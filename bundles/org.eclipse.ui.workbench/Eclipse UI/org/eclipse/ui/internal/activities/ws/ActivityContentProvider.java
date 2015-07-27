/*******************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.activities.ws;

import java.util.Collection;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.activities.IActivityManager;

/**
 * @since 3.0
 */
public class ActivityContentProvider implements IStructuredContentProvider {

    /**
     * @since 3.0
     */
    public ActivityContentProvider() {
    }

    @Override
	public void dispose() {
    }

    @Override
	public Object[] getElements(Object inputElement) {
        Object[] activities = new Object[0];
        if (inputElement instanceof IActivityManager) {
            activities = ((IActivityManager) inputElement)
                    .getDefinedActivityIds().toArray();
        } else if (inputElement instanceof Collection) {
            activities = ((Collection) inputElement).toArray();
        }
        return activities;
    }

    @Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
