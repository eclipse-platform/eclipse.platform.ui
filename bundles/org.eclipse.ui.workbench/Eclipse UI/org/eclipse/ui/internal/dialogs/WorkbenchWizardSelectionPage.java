/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.AdaptableList;

/**
 * Page for selecting a wizard from a group of available wizards.
 */
public abstract class WorkbenchWizardSelectionPage extends WizardSelectionPage {

    // variables
    protected IWorkbench workbench;

    protected AdaptableList wizardElements;

    public TableViewer wizardSelectionViewer;

    protected IStructuredSelection currentResourceSelection;
    
    protected String triggerPointId;

    /**
     *	Create an instance of this class
     */
    public WorkbenchWizardSelectionPage(String name, IWorkbench aWorkbench,
            IStructuredSelection currentSelection, AdaptableList elements, 
            String triggerPointId) {
        super(name);
        this.wizardElements = elements;
        this.currentResourceSelection = currentSelection;
        this.workbench = aWorkbench;
        this.triggerPointId = triggerPointId;
        setTitle(WorkbenchMessages.Select);
    }

    /**
     *	Answer the wizard object corresponding to the passed id, or null
     *	if such an object could not be found
     *
     *	@return WizardElement
     *	@param searchId the id to search on
     */
    protected WorkbenchWizardElement findWizard(String searchId) {
        Object[] children = wizardElements.getChildren();
        for (int i = 0; i < children.length; ++i) {
            WorkbenchWizardElement currentWizard = (WorkbenchWizardElement) children[i];
            if (currentWizard.getId().equals(searchId)) {
				return currentWizard;
			}
        }

        return null;
    }

    public IStructuredSelection getCurrentResourceSelection() {
        return currentResourceSelection;
    }

    public IWorkbench getWorkbench() {
        return this.workbench;
    }

    /**
     *	Specify the passed wizard node as being selected, meaning that if
     *	it's non-null then the wizard to be displayed when the user next
     *	presses the Next button should be determined by asking the passed
     *	node.
     *
     *	@param node org.eclipse.jface.wizards.IWizardNode
     */
    public void selectWizardNode(IWizardNode node) {
        setSelectedNode(node);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizardPage#getNextPage()
     */
    public IWizardPage getNextPage() { 
        ITriggerPoint triggerPoint = getWorkbench().getActivitySupport()
        .getTriggerPointManager().getTriggerPoint(triggerPointId);
        if (triggerPoint == null || WorkbenchActivityHelper.allowUseOf(triggerPoint, getSelectedNode())) {
			return super.getNextPage();
		}
        return null;
    }
}
