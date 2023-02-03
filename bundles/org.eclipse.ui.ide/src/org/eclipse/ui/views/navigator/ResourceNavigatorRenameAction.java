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
package org.eclipse.ui.views.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.RenameResourceAction;

/**
 * The ResourceNavigatorRenameAction is the rename action used by the
 * ResourceNavigator that also allows updating after rename.
 *
 * @since 2.0
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noreference This class is not intended to be referenced by clients.
 *
 *              Planned to be deleted, please see Bug
 *              https://bugs.eclipse.org/bugs/show_bug.cgi?id=549953
 *
 * @deprecated as of 3.5, use the Common Navigator Framework classes instead
 */
@Deprecated(forRemoval = true)
public class ResourceNavigatorRenameAction extends RenameResourceAction {
	private TreeViewer viewer;

	/**
	 * Create a ResourceNavigatorRenameAction and use the tree of the supplied
	 * viewer for editing.
	 *
	 * @param shell      Shell
	 * @param treeViewer TreeViewer
	 */
	public ResourceNavigatorRenameAction(Shell shell, TreeViewer treeViewer) {
		super(shell, treeViewer.getTree());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				INavigatorHelpContextIds.RESOURCE_NAVIGATOR_RENAME_ACTION);
		this.viewer = treeViewer;
	}

	@Override
	protected void runWithNewPath(IPath path, IResource resource) {
		IWorkspaceRoot root = resource.getProject().getWorkspace().getRoot();
		super.runWithNewPath(path, resource);
		if (this.viewer != null) {
			IResource newResource = root.findMember(path);
			if (newResource != null) {
				this.viewer.setSelection(new StructuredSelection(newResource), true);
			}
		}
	}

	/**
	 * Handle the key release
	 */
	public void handleKeyReleased(KeyEvent event) {
		if (event.keyCode == SWT.F2 && event.stateMask == 0 && isEnabled()) {
			run();
		}
	}
}
