/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * A wizard node represents a "potential" wizard. Wizard nodes
 * are used by wizard selection pages to allow the user to pick
 * from several available nested wizards.
 * <p>
 * <b>Subclasses</b> simply need to override method <code>createWizard()</code>,
 * which is responsible for creating an instance of the wizard it represents
 * AND ensuring that this wizard is the "right" type of wizard (e.g.-
 * New, Import, etc.).</p>
 */
public abstract class WorkbenchWizardNode implements IWizardNode,
        IPluginContribution {
    protected WorkbenchWizardSelectionPage parentWizardPage;

    protected IWizard wizard;

    protected IWizardDescriptor wizardElement;

    /**
     * Creates a <code>WorkbenchWizardNode</code> that holds onto a wizard
     * element.  The wizard element provides information on how to create
     * the wizard supplied by the ISV's extension.
     * 
     * @param aWizardPage the wizard page
     * @param element the wizard descriptor
     */
    public WorkbenchWizardNode(WorkbenchWizardSelectionPage aWizardPage,
    		IWizardDescriptor element) {
        super();
        this.parentWizardPage = aWizardPage;
        this.wizardElement = element;
    }

    /**
     * Returns the wizard represented by this wizard node.  <b>Subclasses</b>
     * must override this method.
     * 
     * @return the wizard object
     * @throws CoreException 
     */
    public abstract IWorkbenchWizard createWizard() throws CoreException;

    /* (non-Javadoc)
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

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
     */
    public Point getExtent() {
        return new Point(-1, -1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getLocalId()
     */
    public String getLocalId() {
    	IPluginContribution contribution = (IPluginContribution) Util.getAdapter(wizardElement,
				IPluginContribution.class);
		if (contribution != null) {
			return contribution.getLocalId();
		}
		return wizardElement.getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPluginContribution#getPluginId()
     */
    public String getPluginId() {
       	IPluginContribution contribution = (IPluginContribution) Util.getAdapter(wizardElement,
				IPluginContribution.class);
		if (contribution != null) {
			return contribution.getPluginId();
		}
		return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#getWizard()
     */
    public IWizard getWizard() {
        if (wizard != null) {
			return wizard; // we've already created it
		}

        final IWorkbenchWizard[] workbenchWizard = new IWorkbenchWizard[1];
        final IStatus statuses[] = new IStatus[1];
        // Start busy indicator.
        BusyIndicator.showWhile(parentWizardPage.getShell().getDisplay(),
                new Runnable() {
                    public void run() {
                        SafeRunner.run(new SafeRunnable() {
                            /**
                             * Add the exception details to status is one happens.
                             */
                            public void handleException(Throwable e) {
                               	IPluginContribution contribution = (IPluginContribution) Util.getAdapter(wizardElement, IPluginContribution.class);
                                statuses[0] = new Status(
                                        IStatus.ERROR,
                                        contribution != null ? contribution.getPluginId() : WorkbenchPlugin.PI_WORKBENCH,
                                        IStatus.OK,
                                        WorkbenchMessages.WorkbenchWizard_errorMessage,
                                        e);
                            }

                            public void run() {
                                try {
                                    workbenchWizard[0] = createWizard();
                                    // create instance of target wizard
                                } catch (CoreException e) {
                                	IPluginContribution contribution = (IPluginContribution) Util.getAdapter(wizardElement, IPluginContribution.class);
                                	statuses[0] = new Status(
                                            IStatus.ERROR,
                                            contribution != null ? contribution.getPluginId() : WorkbenchPlugin.PI_WORKBENCH,
                                            IStatus.OK,
                                            WorkbenchMessages.WorkbenchWizard_errorMessage,
                                            e);
                                }
                            }
                        });
                    }
                });

        if (statuses[0] != null) {
            parentWizardPage
					.setErrorMessage(WorkbenchMessages.WorkbenchWizard_errorMessage);
			StatusAdapter statusAdapter = new StatusAdapter(statuses[0]);
			statusAdapter.addAdapter(Shell.class, parentWizardPage.getShell());
			statusAdapter.setProperty(StatusAdapter.TITLE_PROPERTY,
					WorkbenchMessages.WorkbenchWizard_errorTitle);
			StatusManager.getManager()
					.handle(statusAdapter, StatusManager.SHOW);
			return null;
        }

        IStructuredSelection currentSelection = getCurrentResourceSelection();

        //Get the adapted version of the selection that works for the
        //wizard node
        currentSelection = wizardElement.adaptedSelection(currentSelection);

        workbenchWizard[0].init(getWorkbench(), currentSelection);

        wizard = workbenchWizard[0];
        return wizard;
    }

    /**
     * Returns the wizard element.
     * 
     * @return the wizard descriptor
     */
    public IWizardDescriptor getWizardElement() {
        return wizardElement;
    }

    /**
     * Returns the current workbench.
     */
    protected IWorkbench getWorkbench() {
        return parentWizardPage.getWorkbench();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
     */
    public boolean isContentCreated() {
        return wizard != null;
    }
}
