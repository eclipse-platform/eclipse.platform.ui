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

import org.eclipse.debug.internal.core.sourcelookup.ISourceContainer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Label provider for source containers.
 * 
 * @since 3.0
 */
public class SourceContainerLabelProvider extends LabelProvider {
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {		
		if (element instanceof ISourceContainer)
			return SourceLookupUIUtils.getSourceContainerImage(((ISourceContainer) element).getType().getId());
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
	
}
