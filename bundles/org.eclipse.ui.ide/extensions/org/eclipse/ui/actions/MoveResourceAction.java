/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.actions.LTKLauncher;

/**
 * Standard action for moving the currently selected resources elsewhere
 * in the workspace. All resources being moved as a group must be siblings.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class MoveResourceAction extends CopyResourceAction {

	/**
	 * The id of this action.
	 */
	@SuppressWarnings("hiding")
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
	 *
	 * @deprecated {@link #MoveResourceAction(IShellProvider)}
	 */
	@Deprecated
	public MoveResourceAction(Shell shell) {
		super(shell, IDEWorkbenchMessages.MoveResourceAction_text);
		initAction();
	}

	/**
	 * Creates a new action.
	 *
	 * @param provider the shell for any dialogs.
	 * @since 3.4
	 */
	public MoveResourceAction(IShellProvider provider){
		super(provider, IDEWorkbenchMessages.MoveResourceAction_text);
		initAction();
	}

	/**
	 * Initializes the workbench
	 */
	private void initAction(){
		setToolTipText(IDEWorkbenchMessages.MoveResourceAction_toolTip);
		setId(MoveResourceAction.ID);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.MOVE_RESOURCE_ACTION);
	}

	@Override
	protected CopyFilesAndFoldersOperation createOperation() {
		return new MoveFilesAndFoldersOperation(getShell());
	}

	/**
	 * Returns the destination resources for the resources that have been moved so far.
	 *
	 * @return list of destination <code>IResource</code>s
	 */
	protected List getDestinations() {
		return destinations;
	}

	@Override
	protected IResource[] getResources(List resourceList) {
		ReadOnlyStateChecker checker = new ReadOnlyStateChecker(getShell(),
				IDEWorkbenchMessages.MoveResourceAction_title,
				IDEWorkbenchMessages.MoveResourceAction_checkMoveMessage);
		return checker.checkReadOnlyResources(super.getResources(resourceList));
	}

	@Override
	protected void runOperation(IResource[] resources, IContainer destination) {
		//Initialize the destinations
		destinations = new ArrayList();
		IResource[] copiedResources = operation.copyResources(resources,
				destination);

		for (IResource copiedResource : copiedResources) {
			destinations.add(destination.getFullPath().append(
					copiedResource.getName()));
		}
	}

	@Override
	public void run() {
		if (LTKLauncher.openMoveWizard(getStructuredSelection())) {
			return;
		}
		super.run();
	}
}
