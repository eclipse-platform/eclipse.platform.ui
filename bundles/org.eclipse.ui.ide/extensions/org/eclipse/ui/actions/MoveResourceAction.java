/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.ltk.ui.refactoring.resource.MoveResourcesWizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Standard action for moving the currently selected resources elsewhere
 * in the workspace. All resources being moved as a group must be siblings.
 * <p>
 * As of 3.4 this action uses the LTK aware undoable operations.  The standard
 * undoable operations are still available.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class MoveResourceAction extends CopyResourceAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".MoveResourceAction"; //$NON-NLS-1$

    /**
     * Keep a list of destinations so that any required update can be done after the
     * move.
     */
    protected List destinations;

    /**
     * Creates a new action.
     *
     * @param shell the shell for any dialogs
     */
    public MoveResourceAction(Shell shell) {
        super(shell, IDEWorkbenchMessages.MoveResourceAction_text);
        setToolTipText(IDEWorkbenchMessages.MoveResourceAction_toolTip);
        setId(MoveResourceAction.ID);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.MOVE_RESOURCE_ACTION);
    }

    /**
     * Returns the destination resources for the resources that have been moved so far.
     *
     * @return list of destination <code>IResource</code>s
     */
    protected List getDestinations() {
        return destinations;
    }

    /* (non-Javadoc)
     * Overrides method in CopyResourceAction
     */
    protected IResource[] getResources(List resourceList) {
        ReadOnlyStateChecker checker = new ReadOnlyStateChecker(getShell(),
                IDEWorkbenchMessages.MoveResourceAction_title,
                IDEWorkbenchMessages.MoveResourceAction_checkMoveMessage);
        return checker.checkReadOnlyResources(super.getResources(resourceList));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.actions.CopyResourceAction#run()
     */
    public void run() {
		List resourcesList = getSelectedResources();
		if (resourcesList.isEmpty()) {
			return;
		}
		IResource[] resources = (IResource[]) resourcesList
				.toArray(new IResource[resourcesList.size()]);

		MoveResourcesWizard refactoringWizard = new MoveResourcesWizard(
				resources);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(
				refactoringWizard);
		try {
			op.run(getShell(), IDEWorkbenchMessages.MoveResourceAction_title);
		} catch (InterruptedException e) {
			StatusManager
					.getManager()
					.handle(
							new Status(
									IStatus.ERROR,
									IDEWorkbenchPlugin.IDE_WORKBENCH,
									NLS
											.bind(
													IDEWorkbenchMessages.MoveProjectAction_internalError,
													e.getMessage()), e));
		}
	}
}
