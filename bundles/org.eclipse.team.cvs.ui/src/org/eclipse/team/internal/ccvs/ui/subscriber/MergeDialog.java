/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.sync.SyncResourceSet;

/**
 * This dialog is presented to the user when non-mergable conflicts 
 * existed in a merge update.
 */
public class MergeDialog extends SyncResourceSetDetailsDialog {

	/**
	 * @param parentShell
	 * @param dialogTitle
	 * @param syncSet
	 */
	public MergeDialog(Shell parentShell, SyncResourceSet syncSet) {
		super(parentShell, "Overwrite Unmergable Resources?", syncSet);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.DetailsDialog#createMainDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected void createMainDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		// TODO: set F1 help
		//WorkbenchHelp.setHelp(composite, IHelpContextIds.ADD_TO_VERSION_CONTROL_DIALOG);
		
		createWrappingLabel(composite, "All mergable resources have been updated. However, some non-mergable resources remain. Should these resources be updated, ignoring any local changes?");
	}

}
