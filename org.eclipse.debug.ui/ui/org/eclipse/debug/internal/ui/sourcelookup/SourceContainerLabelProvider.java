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
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Label provider for source containers.
 * 
 * @since 3.0
 */
public class SourceContainerLabelProvider extends LabelProvider {
	
	private ILabelProvider fLabelProvider = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {		
		if (element instanceof ISourceContainer) {
			Image image = SourceLookupUIUtils.getSourceContainerImage(((ISourceContainer) element).getType().getId());
			if (image == null && element instanceof IAdaptable) {
				Object object = ((IAdaptable)element).getAdapter(IAdaptable.class);
				image = getWorkbenchLabelProvider().getImage(object);
			}
			if (image != null) {
				return image;
			}
		}
		return super.getImage(element);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof ISourceContainer)
			return ((ISourceContainer) element).getName();
		return super.getText(element);
	}
	
	private ILabelProvider getWorkbenchLabelProvider() {
		if (fLabelProvider == null) {
			fLabelProvider = new WorkbenchLabelProvider();
		}
		return fLabelProvider;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (fLabelProvider != null) {
			fLabelProvider.dispose();
		}
	}
}
