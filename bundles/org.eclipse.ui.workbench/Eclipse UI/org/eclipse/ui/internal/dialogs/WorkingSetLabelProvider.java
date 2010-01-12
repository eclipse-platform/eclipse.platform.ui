/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkingSet;

public class WorkingSetLabelProvider extends LabelProvider {
    private ResourceManager images;

    /**
     * Create a new instance of the receiver.
     */
    public WorkingSetLabelProvider() {
        images = new LocalResourceManager(JFaceResources.getResources());
    }

    public void dispose() {
        images.dispose();

        super.dispose();
    }

    public Image getImage(Object object) {
        Assert.isTrue(object instanceof IWorkingSet);
        IWorkingSet workingSet = (IWorkingSet) object;
        ImageDescriptor imageDescriptor = workingSet.getImageDescriptor();

        if (imageDescriptor == null) {
			return null;
		}

        Image icon = (Image) images.get(imageDescriptor);
        return icon;
    }

    public String getText(Object object) {
        Assert.isTrue(object instanceof IWorkingSet);
        IWorkingSet workingSet = (IWorkingSet) object;
        return workingSet.getLabel();
    }
}
