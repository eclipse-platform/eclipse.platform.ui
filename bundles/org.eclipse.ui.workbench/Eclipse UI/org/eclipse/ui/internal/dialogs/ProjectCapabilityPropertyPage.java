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
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.ProjectCapabilitySelectionGroup;
import org.eclipse.ui.internal.registry.*;

/**
 * A property page for IProject resources to view and edit the
 * capabilities assigned to the project.
 */
public class ProjectCapabilityPropertyPage extends PropertyPage {
	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;
	
	private IProject project;
	private ProjectCapabilitySelectionGroup capabilityGroup;

	/**
	 * Creates a new ProjectCapabilityPropertyPage.
	 */
	public ProjectCapabilityPropertyPage() {
		super();
	}
	
	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	protected Control createContents(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.PROJECT_CAPABILITY_PROPERTY_PAGE);
		noDefaultAndApplyButton();
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		
		String instructions;
		if (reg.hasCapabilities())
			instructions = WorkbenchMessages.getString("ProjectCapabilityPropertyPage.chooseCapabilities"); //$NON-NLS-1$
		else
			instructions = WorkbenchMessages.getString("ProjectCapabilityPropertyPage.noCapabilities"); //$NON-NLS-1$
		Label label = new Label(parent, SWT.LEFT);
		label.setFont(parent.getFont());
		label.setText(instructions);
		
		Capability[] caps = reg.getProjectCapabilities(getProject());
		Capability[] disabledCaps = reg.getProjectDisabledCapabilities(getProject());
		ICategory[] cats = new ICategory[0];
		capabilityGroup = new ProjectCapabilitySelectionGroup(cats, caps, disabledCaps, reg);
		return capabilityGroup.createContents(parent);
	}
	
	/**
	 * Returns the project which this property page applies to
	 * 
	 * @return IProject the project for this property page
	 */
	/* package */ IProject getProject() {
		if (project == null)
			project = (IProject) getElement().getAdapter(IResource.class);
			
		return project;
	}

	/* (non-Javadoc)
	 * Method declared on PreferencePage
	 */
	public boolean performOk() {
		// Avoid doing any work if no changes were made.
		if (!capabilityGroup.getCapabilitiesModified())
			return true;

		// Validate the requested changes are ok
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		Capability[] caps = capabilityGroup.getSelectedCapabilities();
		IStatus status = reg.validateCapabilities(caps);
		if (!status.isOK()) {
			ErrorDialog.openError(
				getShell(),
				WorkbenchMessages.getString("ProjectCapabilityPropertyPage.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("ProjectCapabilityPropertyPage.invalidSelection"), //$NON-NLS-1$
				status);
			return true;
		}

		// Get the current set of nature ids on the project
		String[] natureIds;
		try {
			natureIds = getProject().getDescription().getNatureIds();
			natureIds = getProject().getWorkspace().sortNatureSet(natureIds);
		} catch (CoreException e) {
			ErrorDialog.openError(
				getShell(),
				WorkbenchMessages.getString("ProjectCapabilityPropertyPage.errorTitle"), //$NON-NLS-1$
				WorkbenchMessages.getString("ProjectCapabilityPropertyPage.internalError"), //$NON-NLS-1$
				e.getStatus());
			return true;
		}
		
		// Keep only the nature ids whose capability is selected
		ArrayList keepIds = new ArrayList();
		ArrayList removeCaps = new ArrayList();
		for (int i = 0; i < natureIds.length; i++) {
			boolean isRemoved = true;
			String id = natureIds[i];
			for (int j = 0; j < caps.length; j++) {
				if (id.equals(caps[j].getNatureId())) {
					keepIds.add(id);
					isRemoved = false;
					break;
				}
			}
			if (isRemoved)
				removeCaps.add(reg.getCapabilityForNature(id));
		}
		
		// Collect the capabilities to add
		ArrayList newCaps = new ArrayList();
		for (int i = 0; i < caps.length; i++) {
			boolean isNew = true;
			Capability cap = caps[i];
			for (int j = 0; j < natureIds.length; j++) {
				if (natureIds[j].equals(cap.getNatureId())) {
					isNew = false;
					break;
				}
			}
			if (isNew)
				newCaps.add(cap);
		}

		// Launch the step wizard if needed		
		if (newCaps.size() > 0 || removeCaps.size() > 0) {
			Capability[] newCapabilities = new Capability[newCaps.size()];
			newCaps.toArray(newCapabilities);
			
			Capability[] removeCapabilities = new Capability[removeCaps.size()];
			removeCaps.toArray(removeCapabilities);
			
			UpdateProjectCapabilityWizard wizard =
				new UpdateProjectCapabilityWizard(getProject(), newCapabilities, removeCapabilities);
			
			MultiStepWizardDialog dialog = new MultiStepWizardDialog(getShell(), wizard);
			dialog.create();
			dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
			WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.UPDATE_CAPABILITY_WIZARD);
			dialog.open();
		}
		
		return true;
	}
}
