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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A wizard node represents a "potential" wizard. Wizard nodes
 * are used by wizard selection pages to allow the user to pick
 * from several available nested wizards.
 * <p>
 * <b>Subclasses</b> simply need to overide method <code>createWizard()</code>,
 * which is responsible for creating an instance of the wizard it represents
 * AND ensuring that this wizard is the "right" type of wizard (eg.-
 * New, Import, etc.).</p>
 */
public abstract class WorkbenchWizardNode implements IWizardNode {
	protected WorkbenchWizardElement wizardElement;
	protected IWizard wizard;
	protected WorkbenchWizardSelectionPage parentWizardPage;
/**
 * Creates a <code>WorkbenchWizardNode</code> that holds onto a wizard
 * element.  The wizard element provides information on how to create
 * the wizard supplied by the ISV's extension.
 */
public WorkbenchWizardNode(WorkbenchWizardSelectionPage aWizardPage, WorkbenchWizardElement element) {
	super(); 
	this.parentWizardPage = aWizardPage;
	this.wizardElement = element;
}

/**
 * Returns the wizard represented by this wizard node.  <b>Subclasses</b>
 * must override this method.
 */
public abstract IWorkbenchWizard createWizard() throws CoreException;
/**
 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
 */
public void dispose() {
	// Do nothing since the wizard wasn't created via reflection.
}
/**
 * Returns the current resource selection that is being given to the wizard.
 */
protected IStructuredSelection getCurrentResourceSelection() {
	return parentWizardPage.getCurrentResourceSelection();
}
/**
 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
 */
public Point getExtent() {
	return new Point(-1,-1);
}
/**
 * @see org.eclipse.jface.wizards.IWizardNode#getWizard()
 */
public IWizard getWizard() {
	if (wizard != null)
		return wizard;						// we've already created it

	final IWorkbenchWizard[] workbenchWizard = new IWorkbenchWizard[1];
	final IStatus statuses[] = new IStatus[1];
	// Start busy indicator.
	BusyIndicator.showWhile(parentWizardPage.getShell().getDisplay(), new Runnable() {
		public void run() {
			Platform.run(new SafeRunnable() {
				public void run() {
					try {
						workbenchWizard[0] = createWizard();		// create instance of target wizard
					} catch (CoreException e) {
						statuses[0] = e.getStatus();
					}
				}
				
				/**
				 * Add the exception details to status is one happens.
		 		 */
				public void handleException(Throwable e) {
					statuses[0] = 
						new Status(
							IStatus.ERROR, 
							wizardElement.getConfigurationElement().getDeclaringExtension().getUniqueIdentifier(),
		  					IStatus.OK, 
		  					e.getMessage(), 
		  					e);	
				}
			});
		}
	});

	if (statuses[0] != null) {
		parentWizardPage.setErrorMessage(WorkbenchMessages.getString("WorkbenchWizard.errorMessage")); //$NON-NLS-1$
		ErrorDialog.openError(
			parentWizardPage.getShell(),
			WorkbenchMessages.getString("WorkbenchWizard.errorTitle"), //$NON-NLS-1$
			WorkbenchMessages.getString("WorkbenchWizard.errorMessage"), //$NON-NLS-1$
			statuses[0]);
		return null;
	}

	IStructuredSelection currentSelection = getCurrentResourceSelection();
	
	//Get the adapted version of the selection that works for the
	//wizard node
	currentSelection = wizardElement.adaptedSelection(currentSelection);
		
	workbenchWizard[0].init(getWorkbench(),currentSelection);

	wizard = workbenchWizard[0];
	return wizard;
}
/**
 * Returns the wizard element.
 */
public WorkbenchWizardElement getWizardElement() {
	return wizardElement;
}
/**
 * Returns the current workbench.
 */
protected IWorkbench getWorkbench() {
	return parentWizardPage.getWorkbench();
}
/**
 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
 */
public boolean isContentCreated() {
	return wizard != null;
}
}
