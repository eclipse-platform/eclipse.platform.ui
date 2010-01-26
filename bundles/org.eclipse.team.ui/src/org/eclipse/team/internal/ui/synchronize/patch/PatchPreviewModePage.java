/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.TeamUIMessages;

public class PatchPreviewModePage extends WizardPage {

	public final static String PATCH_PREVIEW_MODE_PAGE_NAME = "PatchPreviewModePage"; //$NON-NLS-1$
	private Button previewInDialogButton;
	private Button previewInSyncViewButton;

	public PatchPreviewModePage() {
		super(PATCH_PREVIEW_MODE_PAGE_NAME,
				TeamUIMessages.PatchPreviewModePage_title, null);
		setDescription(TeamUIMessages.PatchPreviewModePage_description);
	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		initializeDialogUnits(parent);
		createPreviewOptionArea(composite);
	}

	private void createPreviewOptionArea(Composite parent) {

		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(true); // update wizard buttons
			}
		};

		GridData gd = new GridData();

		// 1st row
		previewInDialogButton = new Button(parent, SWT.RADIO);
		previewInDialogButton
				.setText(TeamUIMessages.PatchPreviewModePage_previewInPatchWizard);
		previewInDialogButton.setLayoutData(gd);
		previewInDialogButton.addSelectionListener(selectionAdapter);
		// TODO: enable 'Next' and 'Finish'

		// 2nd row
		previewInSyncViewButton = new Button(parent, SWT.RADIO);
		previewInSyncViewButton
				.setText(TeamUIMessages.PatchPreviewModePage_previewInSynchronizeView);
		previewInSyncViewButton.setLayoutData(gd);
		previewInSyncViewButton.addSelectionListener(selectionAdapter);
		previewInSyncViewButton.setSelection(true);
	}

	public IWizardPage getNextPage() {
		if (previewInSyncViewButton.getSelection())
			return null;
		return super.getNextPage();
	}

	public boolean isPreviewInSyncViewSelected() {
		return previewInSyncViewButton.getSelection();
	}

}
