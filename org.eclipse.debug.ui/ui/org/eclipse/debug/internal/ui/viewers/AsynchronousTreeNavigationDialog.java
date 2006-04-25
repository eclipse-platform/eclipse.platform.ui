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

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * A dialog used to navigate to an element in an asynchronous
 * tree viewer. Must be created on corresponding label provider.
 * 
 * @since 3.2
 *
 */
public class AsynchronousTreeNavigationDialog extends ElementListSelectionDialog {
	
	/**
	 * Constructs a dialog to navigate to an element in the given viewer.
	 * 
	 * @param viewer
	 * @param provider
	 */
	public AsynchronousTreeNavigationDialog(Shell shell, ILabelProvider provider, Object[] elements) {
		super(shell, provider);
		setElements(elements);
		setMultipleSelection(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.ElementListSelectionDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control comp = super.createDialogArea(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(comp, IDebugHelpContextIds.FIND_ELEMENT_DIALOG);
		return comp;
	}
	
}
