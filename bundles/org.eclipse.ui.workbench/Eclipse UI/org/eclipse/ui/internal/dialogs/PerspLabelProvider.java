/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;

public class PerspLabelProvider extends LabelProvider {
	private HashMap imageCache = new HashMap(11);
	private boolean markDefault = true;
	
	public PerspLabelProvider() {
		super();
	}
	
	public PerspLabelProvider(boolean markDefault) {
		super();
		this.markDefault = markDefault;
	}
	
	public void dispose() {
		for (Iterator i = imageCache.values().iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		imageCache.clear();
	}
	
	public Image getImage(Object element) {
		if (element instanceof IPerspectiveDescriptor) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor) element;
			ImageDescriptor imageDescriptor = desc.getImageDescriptor();
			if (imageDescriptor == null) {
				imageDescriptor = WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_DEF_PERSPECTIVE_HOVER);
			}
			Image image = (Image) imageCache.get(imageDescriptor);
			if (image == null) {
				image = imageDescriptor.createImage();
				imageCache.put(imageDescriptor, image);
			}
			return image;
		}
		return null;
	}
	
	public String getText(Object element) {
		if (element instanceof IPerspectiveDescriptor) {
			IPerspectiveDescriptor desc = (IPerspectiveDescriptor) element;
			String label = desc.getLabel();
			if (markDefault) {
				String def = PlatformUI.getWorkbench().getPerspectiveRegistry().getDefaultPerspective();
				if (desc.getId().equals(def))
					label = WorkbenchMessages.format("PerspectivesPreference.defaultLabel", new Object[] { label }); //$NON-NLS-1$
			}
			return label;
		}
		return WorkbenchMessages.getString("PerspectiveLabelProvider.unknown"); //$NON-NLS-1$
	}
}
