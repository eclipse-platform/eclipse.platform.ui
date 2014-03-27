/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Benjamin Muskalla  - bug 77710
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430603
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.HashMap;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;
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
public class ViewLabelProvider extends ColumnLabelProvider {
    private HashMap<ImageDescriptor, Image> images;
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

	@Override
	protected void initialize(ColumnViewer viewer, ViewerColumn column) {
		super.initialize(viewer, column);
	}

	Image cacheImage(ImageDescriptor desc) {
        if (images == null) {
			images = new HashMap<ImageDescriptor, Image>(21);
		}
        Image image = images.get(desc);
        if (image == null) {
            image = desc.createImage();
            images.put(desc, image);
        }
        return image;
    }

    @Override
	public void dispose() {
        if (images != null) {
			for (Image i : images.values()) {
				i.dispose();
			}
            images = null;
        }
        super.dispose();
    }

    @Override
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

    @Override
	public String getText(Object element) {
        String label = WorkbenchMessages.ViewLabel_unknown;
        if (element instanceof IViewCategory) {
			label = ((IViewCategory) element).getLabel();
		} else if (element instanceof IViewDescriptor) {
			label = ((IViewDescriptor) element).getLabel();
		}
        return DialogUtil.removeAccel(label);
    }

	@Override
	public Color getBackground(Object element) {
		return null;
	}

	@Override
	public Color getForeground(Object element) {
		if (element instanceof IViewDescriptor) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				if (activePage
						.findViewReference(((IViewDescriptor) element).getId()) != null) {
					return dimmedForeground;
				}
			}
		}
		return null;
	}
}
