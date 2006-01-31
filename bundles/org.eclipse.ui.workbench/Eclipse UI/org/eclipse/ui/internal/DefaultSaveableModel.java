/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISaveableModel;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPart2;

/**
 * A default {@link ISaveableModel} implementation that wrappers a regular
 * workbench part (one that does not itself adapt to ISaveableModel).
 * 
 * @since 3.2
 */
public class DefaultSaveableModel implements ISaveableModel {

	private IWorkbenchPart part;

	/**
	 * Creates a new DefaultSaveableModel.
	 * 
	 * @param part
	 *            the part represented by this model
	 */
	public DefaultSaveableModel(IWorkbenchPart part) {
		this.part = part;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveableModel#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		if (part instanceof ISaveablePart) {
			ISaveablePart saveable = (ISaveablePart) part;
			saveable.doSave(monitor);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveableModel#getName()
	 */
	public String getName() {
		if (part instanceof IWorkbenchPart2) {
			return ((IWorkbenchPart2) part).getPartName();
		}
		return part.getTitle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveableModel#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		Image image = part.getTitleImage();
		if (image == null)
			return null;
		return ImageDescriptor.createFromImage(image);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveableModel#getToolTipText()
	 */
	public String getToolTipText() {
		return part.getTitleToolTip();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.ISaveableModel#isDirty()
	 */
	public boolean isDirty() {
		if (part instanceof ISaveablePart) {
			return ((ISaveablePart) part).isDirty();
		}
		return false;
	}

}
