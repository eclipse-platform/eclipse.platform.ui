/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetLabelProvider extends LabelProvider {
    private Map icons;

    /**
     * Create a new instance of the receiver.
     */
    public WorkingSetLabelProvider() {
        icons = new Hashtable();
    }

    public void dispose() {
        Iterator iterator = icons.values().iterator();

        while (iterator.hasNext()) {
            Image icon = (Image) iterator.next();
            icon.dispose();
        }
        super.dispose();
    }

    public Image getImage(Object object) {
        Assert.isTrue(object instanceof IWorkingSet);
        IWorkingSet workingSet = (IWorkingSet) object;
        ImageDescriptor imageDescriptor = workingSet.getImage();

        if (imageDescriptor == null)
            return null;

        Image icon = (Image) icons.get(imageDescriptor);
        if (icon == null) {
            icon = imageDescriptor.createImage();
            icons.put(imageDescriptor, icon);
        }
        return icon;
    }

    public String getText(Object object) {
        Assert.isTrue(object instanceof IWorkingSet);
        IWorkingSet workingSet = (IWorkingSet) object;
        return workingSet.getLabel();
    }
}