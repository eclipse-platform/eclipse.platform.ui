/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * A label provider for source elements.
 * 
 * @since 3.0
 */
public class SourceElementLabelProvider extends LabelProvider {
	
	private ILabelProvider fLabelProvider = null;
	
	
	
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
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		return getWorkbenchLabelProvider().getImage(element);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		if (element instanceof IResource) {
			return SourceElementWorkbenchAdapter.getQualifiedName(((IResource)element).getFullPath());
		}
		return getWorkbenchLabelProvider().getText(element);
	}
}
