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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.search.*;

/**
 * @author dejan
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class NewSearchWizardPage extends BaseNewWizardPage {
	private static final String KEY_TITLE = "NewSearchWizardPage.title";
	private static final String KEY_DESC = "NewSearchWizardPage.desc";
	private static final String KEY_CATEGORY = "NewSearchWizardPage.category";
	private Combo categoryCombo;
	private SearchCategoryDescriptor[] descriptors;
	private SearchCategoryDescriptor descriptor;
	/**
	 * Constructor for NewFolderWizardPage.
	 * @param folder
	 */
	public NewSearchWizardPage(BookmarkFolder folder) {
		super(folder);
		setTitle(UpdateUI.getString(KEY_TITLE));
		setDescription(UpdateUI.getString(KEY_DESC));
	}

	/**
	 * @see BaseNewWizardPage#createClientControl(Composite, int)
	 */
	protected void createClientControl(Composite parent, int span) {
		Label label = new Label(parent, SWT.NULL);
		label.setText(UpdateUI.getString(KEY_CATEGORY));
		categoryCombo = new Combo(parent, SWT.READ_ONLY);
		descriptors =
			SearchCategoryRegistryReader.getDefault().getCategoryDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			categoryCombo.add(descriptors[i].getName());
		}
		categoryCombo.select(0);
		descriptor = descriptors[0];
		categoryCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				descriptor = descriptors[categoryCombo.getSelectionIndex()];
				validatePage();
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		categoryCombo.setLayoutData(gd);
		WorkbenchHelp.setHelp(parent, "org.eclipse.update.ui.NewSearchWizardPage");
	}

	protected void validatePage() {
		super.validatePage();
		if (isPageComplete())
			setPageComplete(descriptor != null);
	}

	public boolean finish() {
		//UpdateModel model = UpdateUI.getDefault().getUpdateModel();
		SearchObject search = new SearchObject(getName(), descriptor);
		addToModel(search);
		return true;
	}
}