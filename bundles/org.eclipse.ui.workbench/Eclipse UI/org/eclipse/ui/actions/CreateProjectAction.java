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
package org.eclipse.ui.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.MultiStepWizardDialog;
import org.eclipse.ui.internal.dialogs.NewProjectWizard;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.registry.Capability;
import org.eclipse.ui.internal.registry.CapabilityRegistry;
import org.eclipse.ui.internal.registry.ICategory;

/**
 * Standard action for launching the new project creation
 * wizard.
 * <p>
 * This class may be instantiated and subclassed by clients.
 * </p>
 * @deprecated This new experimental API is being temporary
 * 		deprecated for release 2.0  New project creation should
 * 		continue to make use of NewProjectAction.
 */
public class CreateProjectAction extends Action {
	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

	/**
	 * The workbench window this action will run in
	 */
	private IWorkbenchWindow window;

	/**
	 * The suggested name for the new project
	 */
	private String initialProjectName;
	
	/**
	 * The suggested capabilities for the new project
	 */
	private Capability[] initialProjectCapabilities;
	
	/**
	 * The suggested categories to be selected
	 */
	private ICategory[] initialSelectedCategories;

	/**
	 * Creates a new action for launching the new project
	 * selection wizard.
	 *
	 * @param window the workbench window to query the current
	 * 		selection and shell for opening the wizard.
	 */
	public CreateProjectAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("CreateProjectAction.text")); //$NON-NLS-1$
		Assert.isNotNull(window);
		this.window = window;
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_HOVER));
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
		setToolTipText(WorkbenchMessages.getString("CreateProjectAction.toolTip"));	 //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.NEW_ACTION);
	}

	/**
	 * Returns the selection to initialized the wizard with
	 */
	protected IStructuredSelection getInitialSelection() {
		ISelection selection = window.getSelectionService().getSelection();
		IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
		if (selection instanceof IStructuredSelection)
			selectionToPass = (IStructuredSelection) selection;
		return selectionToPass;
	}
	
	/**
	 * Sets the initial categories to be selected. Ignores
	 * any IDs which do not represent valid categories.
	 * 
	 * @param ids initial category ids to select
	 */
	public void setInitialSelectedCategories(String[] ids) {
		if (ids == null || ids.length == 0)
			initialSelectedCategories = null;
		else {
			CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
			ArrayList results = new ArrayList(ids.length);
			for (int i = 0; i < ids.length; i++) {
				ICategory cat = reg.findCategory(ids[i]);
				if (cat != null)
					results.add(cat);
			}
			if (results.isEmpty())
				initialSelectedCategories = null;
			else {
				initialSelectedCategories = new ICategory[results.size()];
				results.toArray(initialSelectedCategories);
			}
		}
	}
	
	/**
	 * Sets the initial project capabilities to be selected.
	 * Ignores any IDs which do not represent valid capabilities.
	 * 
	 * @param ids initial project capability ids to select
	 */
	public void setInitialProjectCapabilities(String[] ids) {
		if (ids == null || ids.length == 0)
			initialProjectCapabilities = null;
		else {
			CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
			ArrayList results = new ArrayList(ids.length);
			for (int i = 0; i < ids.length; i++) {
				Capability cap = reg.findCapability(ids[i]);
				if (cap != null && cap.isValid())
					results.add(cap);
			}
			if (results.isEmpty())
				initialProjectCapabilities = null;
			else {
				initialProjectCapabilities = new Capability[results.size()];
				results.toArray(initialProjectCapabilities);
			}
		}
	}
	
	/**
	 * Sets the initial project name. Leading and trailing
	 * spaces in the name are ignored.
	 * 
	 * @param name initial project name
	 */
	public void setInitialProjectName(String name) {
		if (name == null)
			initialProjectName = null;
		else
			initialProjectName = name.trim();
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		// Create a new project wizard 
		NewProjectWizard wizard = new NewProjectWizard();
		wizard.init(window.getWorkbench(), getInitialSelection());
		wizard.setInitialProjectName(initialProjectName);
		wizard.setInitialProjectCapabilities(initialProjectCapabilities);
		wizard.setInitialSelectedCategories(initialSelectedCategories);
		
		// Create a wizard dialog.
		WizardDialog dialog = new MultiStepWizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.NEW_PROJECT_WIZARD);
	
		// Open the wizard.
		dialog.open();
	}
}
