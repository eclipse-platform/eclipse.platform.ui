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
package org.eclipse.team.internal.ui.target;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.team.internal.core.target.Site;
import org.eclipse.team.internal.core.target.TargetManager;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.wizards.TeamWizardPage;

/**
 * This page allows the user to save the settings used by Target sites to a file.
 */
public class ExportTargetSiteMainPage extends TeamWizardPage {
	private List selectedSites = new ArrayList();
	private Text fileText;
	private String fileName = ""; //$NON-NLS-1$
	private Table table;
	private Button browseButton;

	/**
	 * Constructor for ExportTargetSiteMainPage.
	 * @param pageName The name of the page.
	 */
	public ExportTargetSiteMainPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor for ExportTargetSiteMainPage.
	 * @param pageName The name of the page
	 * @param title The title of the page
	 * @param titleImage The image for the page
	 */
	public ExportTargetSiteMainPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);
		createLabel(composite, Policy.bind("ExportTargetSiteMainPage.Select_Sites")); //$NON-NLS-1$
		
		//TODO: add F1 help.

		table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.setLayout(new TableLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = 300;
		table.setLayoutData(data);
		table.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (event.detail == SWT.CHECK) {
					TableItem item = (TableItem) event.item;
					if (item.getChecked()) {
						selectedSites.add(item.getData());
					} else {
						selectedSites.remove(item.getData());
					}
					updateEnablement();
				}
			}
		});
		createLabel(composite, Policy.bind("ExportTargetSiteMainPage.Target_Site_Filename")); //$NON-NLS-1$

		Composite inner = new Composite(composite, SWT.NULL);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		inner.setLayout(layout);

		createLabel(inner, Policy.bind("ExportTargetSiteMainPage.File_name")); //$NON-NLS-1$
		fileText = createTextField(inner);
		if (fileName != null)
			fileText.setText(fileName);
		fileText.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				fileName = fileText.getText();
				updateEnablement();
			}
		});

		browseButton = new Button(inner, SWT.PUSH);
		browseButton.setText(Policy.bind("ExportTargetSiteMainPage.Browse")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, browseButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		browseButton.setLayoutData(data);
		browseButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				FileDialog d = new FileDialog(getShell(), SWT.SAVE);
				d.setFilterExtensions(new String[] { "*.tsf" }); //$NON-NLS-1$
				d.setFilterNames(new String[] { Policy.bind("ExportTargetSiteMainPage.Target_Site_Files")}); //$NON-NLS-1$
				d.setFileName(Policy.bind("ExportTargetSiteMainPage.default")); //$NON-NLS-1$
				d.setFilterPath(new File(".").getAbsolutePath()); //$NON-NLS-1$
				String f = d.open();
				if (f != null) {
					fileText.setText(f);
					fileName = f;
				}
			}
		});

		initializeSites();
		setControl(composite);
		updateEnablement();
	}

	public void setSelectedSites(Site[] selectedSites) {
		this.selectedSites.addAll(Arrays.asList(selectedSites));
	}

	public void setFileName(String file) {
		if (file != null) {
			this.fileName = file;
		}
	}

	private void initializeSites() {
		List siteList = new ArrayList();
		Site[] workspaceSites = TargetManager.getSites();
		for (int i = 0; i < workspaceSites.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setData(workspaceSites[i]);
			item.setText(workspaceSites[i].getDisplayName());
		}
	}

	/**
	 * Returns the fileName.
	 * @return String
	 */
	public String getFileName() {
		return fileName;
	}

	public Site[] getSelectedSites() {
		return (Site[]) selectedSites.toArray(new Site[selectedSites.size()]);
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fileText.setFocus();
		}
	}

	private void updateEnablement() {
		boolean complete;
		if (selectedSites.size() == 0) {
			setMessage(null);
			complete = false;
		} else if (fileName.length() == 0) {
			setMessage(null);
			complete = false;
		} else {
			File f = new File(fileName);
			if (f.isDirectory()) {
				setMessage(Policy.bind("ExportTargetSiteMainPage.folder_specified"), ERROR); //$NON-NLS-1$
				complete = false;
			} else {
				complete = true;
			}
		}
		if (complete) {
			setMessage(null);
		}
		setPageComplete(complete);
	}

}
