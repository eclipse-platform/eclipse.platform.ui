/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * A dialog used to navigate to an element in an asynchronous
 * tree viewer. Must be created on corresponding label provider.
 * 
 * @since 3.2
 *
 */
public class AsynchronousTreeNavigationDialog extends ElementListSelectionDialog {
	
	private AsynchronousTreeNavigationModel fLabelProvider = null;
	private AsynchronousTreeViewer fViewer = null;
	
	/**
	 * Constructs a dialog to navigate to an element in the given viewer.
	 * 
	 * @param viewer
	 * @param provider
	 */
	public AsynchronousTreeNavigationDialog(AsynchronousTreeNavigationModel provider) {
		super(provider.getViewer().getControl().getShell(), provider);
		fLabelProvider = provider;
		fViewer = provider.getViewer();
		setElements(fLabelProvider.getElements());
		setMultipleSelection(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#okPressed()
	 */
	protected void okPressed() {
		Object[] elements = getSelectedElements();
		if (elements.length == 1) {
			TreeItem item = fLabelProvider.getItem(elements[0]);
			TreePath treePath = fViewer.getTreePath(item);
			fViewer.setSelection(new TreeSelection(treePath), true, true);
		}
		super.okPressed();
	}
	
	

	
}
