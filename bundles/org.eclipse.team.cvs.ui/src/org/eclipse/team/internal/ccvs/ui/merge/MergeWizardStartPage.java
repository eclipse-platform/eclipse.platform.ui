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
package org.eclipse.team.internal.ccvs.ui.merge;


import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagConfigurationDialog;
import org.eclipse.team.internal.ccvs.ui.wizards.CVSWizardPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class MergeWizardStartPage extends CVSWizardPage {
	TableViewer table;
	CVSTag result;
	IProject project;
	
	private static final int TABLE_HEIGHT_HINT = 350;
	private static final int TABLE_WIDTH_HINT = 100;
	
	/**
	 * MergeWizardStartPage constructor.
	 * 
	 * @param pageName  the name of the page
	 * @param title  the title of the page
	 * @param titleImage  the image for the page
	 */
	public MergeWizardStartPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
		setDescription(Policy.bind("MergeWizardStartPage.description")); //$NON-NLS-1$
	}
	protected TableViewer createTable(Composite parent) {
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = TABLE_HEIGHT_HINT;
		data.widthHint = TABLE_WIDTH_HINT;
		table.setLayoutData(data);
		TableLayout layout = new TableLayout();
		layout.addColumnData(new ColumnWeightData(100, true));
		table.setLayout(layout);
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
	
		return new TableViewer(table);
	}
	/*
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.MERGE_START_PAGE);
		
		table = createTable(composite);
		table.setContentProvider(new WorkbenchContentProvider());
		table.setLabelProvider(new WorkbenchLabelProvider());
		table.setSorter(new ViewerSorter() {
			public int compare(Viewer v, Object o1, Object o2) {
				int result = super.compare(v, o1, o2);
				if (o1 instanceof TagElement && o2 instanceof TagElement) {
					return -result;
				}
				return result;
			}
		});
		table.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)table.getSelection();
				if(!selection.isEmpty()) {
					TagElement element = (TagElement)((IStructuredSelection)table.getSelection()).getFirstElement();
					if(element!=null) {
						result = element.getTag();
						setPageComplete(true);
					}
				}
			}
		});
		table.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				getContainer().showPage(getNextPage());
			}
		});

		Runnable afterRefresh = new Runnable() {
			public void run() {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						table.refresh();
						setPageComplete(! table.getSelection().isEmpty());
					}
				});
			}
		};
		
		Runnable afterConfigure = new Runnable() {
			public void run() {
				getShell().getDisplay().syncExec(new Runnable() {
					public void run() {
						initialize();
					}
				});
			}
		};

		setControl(composite);
		TagConfigurationDialog.createTagDefinitionButtons(getShell(), composite, new ICVSFolder[] {CVSWorkspaceRoot.getCVSFolderFor(project)}, 
														  convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT), 
														  convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH),
														  afterRefresh, afterConfigure);
		Dialog.applyDialogFont(parent);
		initialize();
		setPageComplete(false);
	}
	private void initialize() {
		ICVSFolder cvsProject = CVSWorkspaceRoot.getCVSFolderFor(project);
		table.setInput(new TagRootElement(cvsProject, CVSTag.VERSION));
	}
	public void setProject(IProject project) {
		this.project = project;
	}
	public CVSTag getTag() {
		return result;
	}
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			table.getControl().setFocus();
		}
	}
}
