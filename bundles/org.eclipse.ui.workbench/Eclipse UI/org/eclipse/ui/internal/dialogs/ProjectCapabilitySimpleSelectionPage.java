package org.eclipse.ui.internal.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font should be
 *       activated and used by other components.
*********************************************************************/
import java.util.*;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ICapabilityInstallWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

public class ProjectCapabilitySimpleSelectionPage extends WizardPage {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private IProject project;
	private TreeViewer viewer;
	private CapabilityRegistry reg;
	private Capability chosenCapability;
	private HashMap mapCapToWizard = new HashMap();

	/**
	 * Creates an instance of this page.
	 */
	public ProjectCapabilitySimpleSelectionPage(String pageName, IWorkbench workbench, IStructuredSelection selection, IProject project) {
		super(pageName);
		this.workbench = workbench;
		this.selection = selection;
		this.project = project;
		this.reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage
	 */
	public void createControl(Composite parent) {
		Font font = parent.getFont();
		Composite topContainer = new Composite(parent, SWT.NONE);
		topContainer.setLayout(new GridLayout());
		topContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(topContainer, SWT.LEFT);
		label.setText(WorkbenchMessages.getString("ProjectCapabilitySelectionGroup.capabilities")); //$NON-NLS-1$
		GridData data = new GridData();
		data.verticalAlignment = SWT.TOP;
		label.setLayoutData(data);
		label.setFont(font);
		
		viewer = new TreeViewer(topContainer, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.getTree().setFont(font);
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setContentProvider(getContentProvider());
		viewer.setInput(reg);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				chosenCapability = null;
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (sel != null && !sel.isEmpty()) {
						Object result = sel.getFirstElement();
						if (result instanceof Capability) {
							chosenCapability = (Capability) result;
						}
					}
				}
				
				setPageComplete(validateChosenCapability());
			}
		});
		
		setControl(topContainer);
	}
	
	/**
	 * Returns the content provider for the viewer
	 */
	private IContentProvider getContentProvider() {
		return new WorkbenchContentProvider() {
			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof CapabilityRegistry) {
					ArrayList cats = reg.getUsedCategories();
					if (reg.getMiscCategory() != null)
						cats.add(reg.getMiscCategory());
					return cats.toArray();
				}
				if (parentElement instanceof ICategory)
					return ((ICategory)parentElement).getChildren(parentElement);
				return null;
			}
			
			public boolean hasChildren(Object element) {
				if (element instanceof CapabilityRegistry)
					return true;
				if (element instanceof ICategory)
					return ((ICategory)element).hasElements();
				return false;
			}
		};
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizardPage.
	 */
	public IWizardPage getNextPage() {
		if (chosenCapability == null)
			return null;
			
		final IWizard[] wizard = new IWizard[1];
		wizard[0] = (IWizard)mapCapToWizard.get(chosenCapability);
		if (wizard[0] == null) {
			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
				public void run() {
					ICapabilityInstallWizard wiz;
					wiz = chosenCapability.getInstallWizard();
					if (wiz != null) {
						wiz.init(workbench, selection, project);
						wiz.addPages();
						mapCapToWizard.put(chosenCapability, wiz);
						wizard[0] = wiz;
					}
				}
			});
		}
		
		if (wizard[0] == null)
			return null;
			
		IWizardPage page = wizard[0].getStartingPage();
		wizard[0] = null;
		return page;
	}
	
	private boolean validateChosenCapability() {
		if (chosenCapability == null) {
			setErrorMessage(null);
			return false;
		}

		Capability[] caps = reg.getProjectCapabilities(project);
		List existingCaps = Arrays.asList(caps);
		if (existingCaps.contains(chosenCapability)) {
			setErrorMessage(WorkbenchMessages.getString("ProjectCapabilitySimpleSelectionPage.capabilityExist")); //$NON-NLS-1$
			return false;
		}

		String[] ids = reg.getPrerequisiteIds(chosenCapability);
		Capability[] prereqs = reg.findCapabilities(ids);
		for (int i = 0; i < prereqs.length; i++) {
			if (prereqs[i] == null) {
				setErrorMessage(WorkbenchMessages.format("ProjectCapabilitySimpleSelectionPage.capabilityMissing", new Object[] {ids[i]})); //$NON-NLS-1$
				return false;
			}
			if (!existingCaps.contains(prereqs[i])) {
				setErrorMessage(WorkbenchMessages.format("ProjectCapabilitySimpleSelectionPage.capabilityRequired", new Object[] {prereqs[i].getName()})); //$NON-NLS-1$
				return false;
			}
		}
		
		ids = reg.getMembershipSetIds(chosenCapability);
		List idsList = Arrays.asList(ids);
		for (int i = 0; i < caps.length; i++) {
			String[] setIds = reg.getMembershipSetIds(caps[i]);
			for (int j = 0; j < setIds.length; j++) {
				if (idsList.contains(setIds[j])) {
					setErrorMessage(WorkbenchMessages.format("ProjectCapabilitySimpleSelectionPage.capabilitySet", new Object[] {caps[i].getName()})); //$NON-NLS-1$
					return false;
				}
			}
		}
		
		Capability[] newCaps = new Capability[caps.length + 1];
		System.arraycopy(caps, 0, newCaps, 0, caps.length);
		newCaps[caps.length] = chosenCapability;
		IStatus status = reg.validateCapabilities(newCaps);
		if (!status.isOK()) {
			setErrorMessage(status.getMessage());
			return false;
		}
		
		setErrorMessage(null);
		return true;
	}
}
