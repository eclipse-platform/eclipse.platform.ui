/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.wizards;


import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.*;

/**
 * First wizard page for importing a project into a CVS repository.
 * This page prompts the user to select an existing repo or create a new one.
 * If the user selected an existing repo, then getLocation() will return it.
 */
public class RepositorySelectionPage extends CVSWizardPage {
	private TableViewer table;
	private Button useExistingRepo;
	private Button useNewRepo;
	
	private ICVSRepositoryLocation result;
	
	String extendedDescription;
	
	/**
	 * RepositorySelectionPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public RepositorySelectionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	protected TableViewer createTable(Composite parent, int span) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL);
		data.horizontalSpan = span;
		data.widthHint = 200;
		table.setLayoutData(data);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, true));
		table.setLayout(layout);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
	
		return new TableViewer(table);
	}
	/**
	 * Creates the UI part of the page.
	 * 
	 * @param parent  the parent of the created widgets
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1, false);
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SHARING_SELECT_REPOSITORY_PAGE);
		if (extendedDescription == null) {
			extendedDescription = Policy.bind("RepositorySelectionPage.description"); //$NON-NLS-1$
		}
		createWrappingLabel(composite, extendedDescription, 0 /* indent */, 1 /* columns */); 
		
		useNewRepo = createRadioButton(composite, Policy.bind("RepositorySelectionPage.useNew"), 1); //$NON-NLS-1$
		
		useExistingRepo = createRadioButton(composite, Policy.bind("RepositorySelectionPage.useExisting"), 1); //$NON-NLS-1$
		table = createTable(composite, 1);
		table.setContentProvider(new WorkbenchContentProvider());
		table.setLabelProvider(new WorkbenchLabelProvider());
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				result = (ICVSRepositoryLocation)((IStructuredSelection)table.getSelection()).getFirstElement();
				setPageComplete(true);
			}
		});
		table.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				getContainer().showPage(getNextPage());
			}
		});
		
		useExistingRepo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (useNewRepo.getSelection()) {
					table.getTable().setEnabled(false);
					result = null;
				} else {
					table.getTable().setEnabled(true);
					result = (ICVSRepositoryLocation)((IStructuredSelection)table.getSelection()).getFirstElement();
				}
				setPageComplete(true);
			}
		});

		setControl(composite);

		initializeValues();
        Dialog.applyDialogFont(parent);
	}
	/**
	 * Initializes states of the controls.
	 */
	private void initializeValues() {
		ICVSRepositoryLocation[] locations = CVSUIPlugin.getPlugin().getRepositoryManager().getKnownRepositoryLocations();
		AdaptableList input = new AdaptableList(locations);
		table.setInput(input);
		if (locations.length == 0) {
			useNewRepo.setSelection(true);	
		} else {
			useExistingRepo.setSelection(true);	
			table.setSelection(new StructuredSelection(locations[0]));
		}
	}
	
	public ICVSRepositoryLocation getLocation() {
		return result;
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			useExistingRepo.setFocus();
		}
	}
	
	public void setExtendedDescription(String extendedDescription) {
		this.extendedDescription = extendedDescription;
	}
}
