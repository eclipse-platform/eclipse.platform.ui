/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla  - bug 77710
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.views.IViewCategory;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Provides labels for view children.
 */
public class ViewLabelProvider extends LabelProvider implements IColorProvider {
    private HashMap images;
	private final IWorkbenchWindow window;
	private final Color dimmedForeground;

    /**
	 * @param window the workbench window
     * @param dimmedForeground the dimmed foreground color to use for views that are already open
	 */
	public ViewLabelProvider(IWorkbenchWindow window, Color dimmedForeground) {
		this.window = window;
		this.dimmedForeground = dimmedForeground;
	}

	Image cacheImage(ImageDescriptor desc) {
        if (images == null) {
			images = new HashMap(21);
		}
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
            ImageDescriptor desc = ((IViewDescriptor) element)
                    .getImageDescriptor();
            if (desc != null) {
				return cacheImage(desc);
			}
        } else if (element instanceof IViewCategory) {
            ImageDescriptor desc = WorkbenchImages
                    .getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER);
            return cacheImage(desc);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        String label = WorkbenchMessages.ViewLabel_unknown;
        if (element instanceof IViewCategory) {
			label = ((IViewCategory) element).getLabel();
		} else if (element instanceof IViewDescriptor) {
			label = ((IViewDescriptor) element).getLabel();
		}
        return DialogUtil.removeAccel(label);
    }

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		if (element instanceof IViewDescriptor) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				if (activePage
						.findView(((IViewDescriptor) element).getId()) != null) {
					return dimmedForeground;
				}
			}
		}
		return null;
	}
}
