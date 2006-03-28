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

package org.eclipse.ui.internal.navigator.extensions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

/**
 * @since 3.2
 *
 */
public class SkeletonLinkHelper implements ILinkHelper {

	/**
	 * The singleton instance.
	 */
	public static final ILinkHelper INSTANCE = new SkeletonLinkHelper();
	
	private SkeletonLinkHelper() {
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ILinkHelper#findSelection(org.eclipse.ui.IEditorInput)
	 */
	public IStructuredSelection findSelection(IEditorInput anInput) {
		return StructuredSelection.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ILinkHelper#activateEditor(org.eclipse.ui.IWorkbenchPage, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		// no-op
		
	}

}
