/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     vogella GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.actions;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.part.ResourceTransfer;

/**
 * Standard action for cutting the currently selected resources to the clipboard.
 *
 * @since 3.10
 */
/*package*/class CutAction extends SelectionListenerAction {

	/**
	 * The id of this action.
	 */
	public static final String ID = PlatformUI.PLUGIN_ID + ".CutAction"; //$NON-NLS-1$

	/**
	 * Resources that were cut. Non-null when the last clipboard operation was a
	 * cut, {@code null} when it was a copy or after paste has consumed the cut.
	 */
	static IResource[] cutResources;

	/**
	 * The shell in which to show any dialogs.
	 */
	private final Shell shell;

	/**
	 * System clipboard
	 */
	private final Clipboard clipboard;

	/**
	 * Associated paste action. May be <code>null</code>
	 */
	private PasteAction pasteAction;

	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 * @param clipboard a platform clipboard
	 */
	public CutAction(Shell shell, Clipboard clipboard) {
		super(WorkbenchNavigatorMessages.CutAction_Cut);
		Assert.isNotNull(shell);
		Assert.isNotNull(clipboard);
		this.shell = shell;
		this.clipboard = clipboard;
		setToolTipText(WorkbenchNavigatorMessages.CutAction_Cut_selected_resource_s_);
		setId(CutAction.ID);
		setActionDefinitionId(IWorkbenchCommandConstants.EDIT_CUT);
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT));
		setDisabledImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED));
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this, "CutHelpId"); //$NON-NLS-1$
	}

	/**
	 * Creates a new action.
	 *
	 * @param shell the shell for any dialogs
	 * @param clipboard a platform clipboard
	 * @param pasteAction a paste action
	 */
	public CutAction(Shell shell, Clipboard clipboard, PasteAction pasteAction) {
		this(shell, clipboard);
		this.pasteAction = pasteAction;
	}

	@Override
	public void run() {
		List<? extends IResource> selectedResources = getSelectedResources();
		IResource[] resources = selectedResources.toArray(new IResource[selectedResources.size()]);

		// Get the file names and a string representation
		final int length = resources.length;
		int actualLength = 0;
		String[] fileNames = new String[length];
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < length; i++) {
			IPath location = resources[i].getLocation();
			if (location != null) {
				fileNames[actualLength++] = location.toOSString();
			}
			if (i > 0) {
				buf.append("\n"); //$NON-NLS-1$
			}
			buf.append(resources[i].getName());
		}
		if (actualLength < length) {
			String[] tempFileNames = fileNames;
			fileNames = new String[actualLength];
			System.arraycopy(tempFileNames, 0, fileNames, 0, actualLength);
		}
		setClipboard(resources, fileNames, buf.toString());

		cutResources = resources;

		if (pasteAction != null && pasteAction.getStructuredSelection() != null) {
			pasteAction.selectionChanged(pasteAction.getStructuredSelection());
		}
	}

	private void setClipboard(IResource[] resources, String[] fileNames, String names) {
		try {
			if (fileNames.length > 0) {
				clipboard.setContents(new Object[] { resources, fileNames, names },
						new Transfer[] { ResourceTransfer.getInstance(), FileTransfer.getInstance(),
								TextTransfer.getInstance() });
			} else {
				clipboard.setContents(new Object[] { resources, names },
						new Transfer[] { ResourceTransfer.getInstance(), TextTransfer.getInstance() });
			}
		} catch (SWTError e) {
			if (e.code != DND.ERROR_CANNOT_SET_CLIPBOARD) {
				throw e;
			}
			if (MessageDialog.openQuestion(shell, "Problem with cut title", "Problem with cut.")) { //$NON-NLS-1$ //$NON-NLS-2$
				setClipboard(resources, fileNames, names);
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		if (getSelectedNonResources().size() > 0) {
			return false;
		}

		List<? extends IResource> selectedResources = getSelectedResources();
		if (selectedResources.isEmpty()) {
			return false;
		}

		boolean projSelected = selectionIsOfType(IResource.PROJECT);
		boolean fileFoldersSelected = selectionIsOfType(IResource.FILE | IResource.FOLDER);
		if (!projSelected && !fileFoldersSelected) {
			return false;
		}

		// selection must be homogeneous
		if (projSelected && fileFoldersSelected) {
			return false;
		}

		// must have a common parent
		IContainer firstParent = selectedResources.get(0).getParent();
		if (firstParent == null) {
			return false;
		}

		for (IResource currentResource : selectedResources) {
			if (!currentResource.getParent().equals(firstParent)) {
				return false;
			}
			// resource location must exist
			if (currentResource.getLocationURI() == null) {
				return false;
			}
		}
		return true;
	}

}
