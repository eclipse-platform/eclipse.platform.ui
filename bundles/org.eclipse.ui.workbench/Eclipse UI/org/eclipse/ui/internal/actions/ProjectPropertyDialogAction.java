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
package org.eclipse.ui.internal.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.PartEventAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.dialogs.PropertyDialog;
import org.eclipse.ui.internal.dialogs.PropertyPageContributorManager;
import org.eclipse.ui.internal.dialogs.PropertyPageManager;
import org.eclipse.ui.model.IWorkbenchAdapter;
/**
 * Implementation for the action Property on the Project menu.
 */
public class ProjectPropertyDialogAction extends PartEventAction implements INullSelectionListener {
	private IWorkbenchWindow window;
	
public ProjectPropertyDialogAction(IWorkbenchWindow window) {
	super(new String());
	this.window = window;
	setText(WorkbenchMessages.getString("PropertyDialog.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("PropertyDialog.toolTip")); //$NON-NLS-1$
	window.getSelectionService().addSelectionListener(this);
}
/**
 * Returns the label for the specified adaptable.
 */
private String getName(IAdaptable element) {
	IWorkbenchAdapter adapter = (IWorkbenchAdapter)element.getAdapter(IWorkbenchAdapter.class);
	if (adapter != null) {
		return adapter.getLabel(element);
	} else {
		return "";//$NON-NLS-1$
	}
}
/**
 * Opens the project properties dialog.
 */
public void run() {
	IProject project = getProject();
	if(project == null)
		return;
		
	PropertyPageManager pageManager = new PropertyPageManager();
	String title = "";//$NON-NLS-1$

	// load pages for the selection
	// fill the manager with contributions from the matching contributors
	PropertyPageContributorManager.getManager().contribute(pageManager, project);
	
	// testing if there are pages in the manager
	Iterator pages = pageManager.getElements(PreferenceManager.PRE_ORDER).iterator();
	String name = getName(project);
	if (!pages.hasNext()) {
		MessageDialog.openInformation(
			window.getShell(),
			WorkbenchMessages.getString("PropertyDialog.messageTitle"), //$NON-NLS-1$
			WorkbenchMessages.format("PropertyDialog.noPropertyMessage", new Object[] {name})); //$NON-NLS-1$
		return;
	} else
		title = WorkbenchMessages.format("PropertyDialog.propertyMessage", new Object[] {name}); //$NON-NLS-1$

	PropertyDialog propertyDialog = new PropertyDialog(window.getShell(), pageManager, new StructuredSelection(project)); 
	propertyDialog.create();
	propertyDialog.getShell().setText(title);
	WorkbenchHelp.setHelp(propertyDialog.getShell(), IHelpContextIds.PROPERTY_DIALOG);
	propertyDialog.open();
}
/**
 * Update the enablement state when a the selection changes.
 */
public void selectionChanged(IWorkbenchPart part, ISelection sel) {
	setEnabled(getProject() != null);
}
/**
 * Update the enablement state when a new part is activated.
 */
public void partActivated(IWorkbenchPart part) {
	super.partActivated(part);
	setEnabled(getProject() != null);
}
/**
 * Returns a project from the selection of the active part.
 */
private IProject getProject() {
	IWorkbenchPart part = getActivePart();
	Object selection = null;
	if(part instanceof IEditorPart) {
		selection = ((IEditorPart)part).getEditorInput();
	} else {
		ISelection sel = window.getSelectionService().getSelection();
		if((sel != null) && (sel instanceof IStructuredSelection))
			selection = ((IStructuredSelection)sel).getFirstElement();		
	}
	if(selection == null)
		return null;
	if(!(selection instanceof IAdaptable))
		return null;
	IResource resource = (IResource)((IAdaptable)selection).getAdapter(IResource.class);
	if(resource == null)
		return null;
	return resource.getProject();	
}
}
