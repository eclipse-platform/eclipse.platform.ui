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
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.ui.*;



public class ShowActivitiesDialog extends Dialog {
	TableViewer activitiesViewer;

	/**
	 * @param parentShell
	 */
	public ShowActivitiesDialog(Shell parentShell) {
		super(parentShell);
		setShellStyle(SWT.RESIZE | SWT.MIN | SWT.MAX);
	}
	
	public void create() {
		super.create();
		applyDialogFont(buttonBar);
		getButton(IDialogConstants.OK_ID).setFocus();
	}

	protected Control createDialogArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		container.setLayoutData(gd);
		createActivitiesViewer(container);
		Dialog.applyDialogFont(container);
		return container;
	}

	protected Control createActivitiesViewer(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = gridLayout.marginWidth = 0;
		composite.setLayout(gridLayout);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		composite.setLayoutData(gd);

		
		Label label = new Label(composite, SWT.NONE);
		label.setText(UpdateUI.getString("ShowActivitiesDialog.label")); //$NON-NLS-1$
		activitiesViewer = CurrentActivitiesTableViewer.createViewer(composite);

		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(8, 20, false));
		layout.addColumnData(new ColumnWeightData(50, 160, true));
		layout.addColumnData(new ColumnWeightData(50, 183, true));
		layout.addColumnData(new ColumnWeightData(50, 100, true));

		activitiesViewer.getTable().setLayout(layout);
		try {
			activitiesViewer.setInput(SiteManager.getLocalSite().getCurrentConfiguration());
			getShell().setText(UpdateUI.getFormattedMessage("ShowActivitiesDialog.title",SiteManager.getLocalSite().getCurrentConfiguration().getCreationDate().toString()));
		} catch (CoreException e) {
		}
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK button only by default
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

	
}
