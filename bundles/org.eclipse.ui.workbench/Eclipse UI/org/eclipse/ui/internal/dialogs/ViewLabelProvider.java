/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.Category;
import org.eclipse.ui.internal.registry.IViewDescriptor;
import org.eclipse.ui.internal.registry.ViewDescriptor;

public class ViewLabelProvider extends LabelProvider {
	private HashMap images;
Image cacheImage(ImageDescriptor desc) {
	if (images == null)
		images = new HashMap(21);
	Image image = (Image) images.get(desc);
	if (image == null) {
		image = desc.createImage();
		images.put(desc, image);
	}
	return image;
}
/* (non-Javadoc)
 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
 */
public void dispose() {
	if (images != null) {
		for (Iterator i = images.values().iterator(); i.hasNext();) {
			((Image) i.next()).dispose();
		}
		images = null;
	}
	super.dispose();
}
/* (non-Javadoc)
 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
 */
public Image getImage(Object element) {
	if (element instanceof IViewDescriptor) {
		ImageDescriptor desc = ((IViewDescriptor)element).getImageDescriptor();
		if (desc != null)
			return cacheImage(desc);
	} else if (element instanceof Category) {
		ImageDescriptor desc = WorkbenchImages.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
		return cacheImage(desc);
	}
	return null;
}
/* (non-Javadoc)
 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
 */
public String getText(Object element) {
	String label = WorkbenchMessages.getString("ViewLabel.unknown"); //$NON-NLS-1$
	if (element instanceof Category)
		label = ((Category)element).getLabel();
	else if (element instanceof IViewDescriptor)
		label = ((ViewDescriptor)element).getLabel();
	return DialogUtil.removeAccel(label);
}
}
