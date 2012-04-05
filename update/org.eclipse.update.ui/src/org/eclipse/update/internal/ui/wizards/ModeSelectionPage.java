/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.ui.wizards;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.update.internal.operations.UpdateUtils;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.UpdateUIMessages;
import org.eclipse.update.search.UpdateSearchRequest;

/**
 */
public class ModeSelectionPage extends BannerPage implements ISearchProvider {
	private boolean updateMode=true;
	private Button updatesButton;
	private Button newFeaturesButton;
	private UpdateSearchRequest searchRequest;
	private static final String SECTION_ID = "ModeSelectionPage"; //$NON-NLS-1$
	private static final String P_NEW_FEATURES_MODE = "new-features-mode"; //$NON-NLS-1$
	
	public ModeSelectionPage(UpdateSearchRequest searchRequest) {
		super("modeSelection"); //$NON-NLS-1$
		setTitle(UpdateUIMessages.ModeSelectionPage_title); 
		setDescription(UpdateUIMessages.ModeSelectionPage_desc); 
		this.searchRequest = searchRequest;
	}
	
	public UpdateSearchRequest getSearchRequest() {
		initializeSearch();
		return searchRequest;
	}
	
	private IDialogSettings getSettings() {
		IDialogSettings master = UpdateUI.getDefault().getDialogSettings();
		IDialogSettings section = master.getSection(SECTION_ID);
		if (section==null)
			section = master.addNewSection(SECTION_ID);
		return section;
	}

	private void initializeSearch() {
		if (searchRequest!=null) return;
		searchRequest = UpdateUtils.createNewUpdatesRequest(null, false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.update.internal.ui.wizards.BannerPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		updatesButton = new Button(composite, SWT.RADIO);
		updatesButton.setText(UpdateUIMessages.ModeSelectionPage_updates); 
		updateMode = !getSettings().getBoolean(P_NEW_FEATURES_MODE);
		updatesButton.setSelection(updateMode);
		
		final Label updatesText = new Label(composite, SWT.WRAP);
		updatesText.setText(UpdateUIMessages.ModeSelectionPage_updatesText);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 100;
		updatesText.setLayoutData(gd);
		
		updatesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMode = true;
                getWizard().getContainer().updateButtons();
			}
		});
		// spacer
		new Label(composite, SWT.NULL);
		newFeaturesButton = new Button(composite, SWT.RADIO);
		newFeaturesButton.setSelection(!updateMode);
		newFeaturesButton.setText(UpdateUIMessages.ModeSelectionPage_newFeatures); 
		newFeaturesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateMode = false;
                getWizard().getContainer().updateButtons();
			}
		});		

		final Label newFeaturesText = new Label(composite, SWT.WRAP);
		newFeaturesText.setText(UpdateUIMessages.ModeSelectionPage_newFeaturesText);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 100;
		newFeaturesText.setLayoutData(gd);

		/*
		composite.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				Composite parent = (Composite)e.widget;
				Rectangle carea = parent.getClientArea();
				GridData gd = (GridData)updatesText.getLayoutData();
				gd.widthHint = carea.width; 
				gd = (GridData)newFeaturesText.getLayoutData();
				gd.widthHint = carea.width; 
			}
		});
		*/
		
		Dialog.applyDialogFont(parent);
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "org.eclipse.update.ui.ModeSelectionPage"); //$NON-NLS-1$

		return composite;
	}
	
	public void saveSettings() {
		boolean updateMode = updatesButton.getSelection();
		getSettings().put(P_NEW_FEATURES_MODE, !updateMode);
	}
	
	public boolean isUpdateMode() {
		return updateMode;
	}

	public boolean isPageComplete() {
		return updateMode && super.isPageComplete();
	}

	protected boolean isCurrentPage() {
		return super.isCurrentPage();
	}

	public boolean canFlipToNextPage() {
		return !updateMode;
	}
	
	
    
}
