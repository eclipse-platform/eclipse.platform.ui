package org.eclipse.ui.internal.dialogs;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * Second page for the new project creation wizard. This page
 * collects the capabilities of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardNewProjectCapabilityPage("wizardNewProjectCapabilityPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Choose project's capabilities.");
 * </pre>
 * </p>
 */
public class WorkingSetTypePage extends WizardPage {
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	
	private TableViewer typesListViewer;
		
	/**
	 * Creates a new project capabilities wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public WorkingSetTypePage(IWizard wizard) {
		super("workingSetTypeSelectionPage");	//$NON-NLS-1$
		setWizard(wizard);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		Composite composite= new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

	//	WorkbenchHelp.setHelp(composite, IHelpContextIds.CREATION_WIZARD_PAGE_CONTEXT);
	
		Label typesLabel = new Label(composite, SWT.NONE);
		typesLabel.setText(WorkbenchMessages.getString("WorkingSetTypePage.typesLabel"));	//$NON-NLS-1$
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		typesLabel.setLayoutData(data);

		typesListViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		typesListViewer.getTable().setLayoutData(data);

//		typesListViewer.setLabelProvider(labelProvider);
//		typesListViewer.setContentProvider(contentProvider);
//		typesListViewer.setSorter(new WorkbenchViewerSorter());
		
		typesListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});
		createContent();
		setPageComplete(false);
	}
	private void createContent() {
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		WorkingSetDescriptor[] descriptors = registry.getWorkingSetDescriptors();
		Table table = (Table) typesListViewer.getControl();
		
		for (int i = 0; i < descriptors.length; i++) {
			TableItem tableItem = new TableItem(table, SWT.NULL);
			tableItem.setText(descriptors[i].getName());
			tableItem.setData(descriptors[i]);
		}
	}
	public String getSelection() {
		ISelection selection = typesListViewer.getSelection();
		boolean hasSelection = selection != null && selection.isEmpty() == false;
				
		if (hasSelection && selection instanceof IStructuredSelection) {
			WorkingSetDescriptor workingSetDescriptor = (WorkingSetDescriptor) ((IStructuredSelection) selection).getFirstElement();
			return workingSetDescriptor.getId();
		}
		return null;
	}
	/**
	 * Called when the selection has changed.
	 */
	private void handleSelectionChanged() {
		ISelection selection = typesListViewer.getSelection();
		boolean hasSelection = selection != null && selection.isEmpty() == false;
		
		setPageComplete(hasSelection);
	}
}
