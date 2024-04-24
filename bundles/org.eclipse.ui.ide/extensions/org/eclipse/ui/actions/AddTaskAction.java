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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.views.markers.internal.DialogTaskProperties;

/**
 * Standard action for adding a task to the currently selected file
 * resource(s).
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @since 2.1
 * @noextend This class is not intended to be subclassed by clients.
 */
public class AddTaskAction extends SelectionListenerAction {
	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".AddTaskAction";//$NON-NLS-1$

	/**
	 * The IShellProvider in which to show any dialogs.
	 */
	private IShellProvider shellProvider;

	/**
	 * Creates a new instance of the receiver.
	 *
	 * @param shell shell to use to show any dialogs
	 * @deprecated See {@link #AddTaskAction(IShellProvider)}
	 */
	@Deprecated
	public AddTaskAction(final Shell shell) {
		super(IDEWorkbenchMessages.AddTaskLabel);
		Assert.isNotNull(shell);
		this.shellProvider = () -> shell;
		initAction();
	}

	/**
	 * Creates a new instance of the receiver.
	 *
	 * @param provider
	 *            the IShellProvider to show any dialogs
	 * @since 3.4
	 */
	public AddTaskAction(IShellProvider provider) {
		super(IDEWorkbenchMessages.AddTaskLabel);
		Assert.isNotNull(provider);
		shellProvider = provider;
		initAction();
	}

	/**
	 *  Initializes the workbench
	 */
	private void initAction() {
		setId(ID);
		setToolTipText(IDEWorkbenchMessages.AddTaskToolTip);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.ADD_TASK_ACTION);
	}

	private IResource getElement(IStructuredSelection selection) {
		if (selection.size() != 1) {
			return null;
		}

		Object element = selection.getFirstElement();
		IResource resource = Adapters.adapt(element, IResource.class);

		if (resource != null && resource instanceof IProject) {
			IProject project = (IProject) resource;
			if (!project.isOpen()) {
				resource = null;
			}
		}
		return resource;
	}

	@Override
	public void run() {
		IResource resource = getElement(getStructuredSelection());
		if (resource != null) {
			DialogTaskProperties dialog = new DialogTaskProperties(
					shellProvider.getShell());
			dialog.setResource(resource);
			dialog.open();
		}
	}

	/**
	 * The <code>AddTaskAction</code> implementation of this
	 * <code>SelectionListenerAction</code> method enables the action only
	 * if the selection contains a single resource and the resource is
	 * not a closed project.
	 *
	 * @param selection the selection to update the enabled state for
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		return super.updateSelection(selection)
				&& getElement(selection) != null;
	}
}
