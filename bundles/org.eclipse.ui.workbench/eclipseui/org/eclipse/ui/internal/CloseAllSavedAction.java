/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Closes all editors except ones with unsaved changes.
 */
public class CloseAllSavedAction extends PageEventAction implements IPropertyListener {

	/**
	 * List of parts (element type: <code>IWorkbenchPart</code>) against which this
	 * class has outstanding property listeners registered.
	 */
	private List<IWorkbenchPart> partsWithListeners = new ArrayList<>(1);

	/**
	 * Create an instance of this class.
	 *
	 * @param window the window
	 */
	public CloseAllSavedAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.CloseAllSavedAction_text, window);
		setToolTipText(WorkbenchMessages.CloseAllSavedAction_toolTip);
		// @issue Should create a ID in IWorkbenchActionConstants when it becames API?
		setId("closeAllSaved"); //$NON-NLS-1$
		updateState();
		window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.CLOSE_ALL_SAVED_ACTION);
		setActionDefinitionId("org.eclipse.ui.file.closeAllSaved"); //$NON-NLS-1$
	}

	@Override
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		updateState();
	}

	@Override
	public void pageClosed(IWorkbenchPage page) {
		super.pageClosed(page);
		updateState();
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		super.partClosed(part);
		if (part instanceof IEditorPart) {
			part.removePropertyListener(this);
			partsWithListeners.remove(part);
			updateState();
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		super.partOpened(part);
		if (part instanceof IEditorPart) {
			part.addPropertyListener(this);
			partsWithListeners.add(part);
			updateState();
		}
	}

	@Override
	public void propertyChanged(Object source, int propID) {
		if (source instanceof IEditorPart) {
			if (propID == IEditorPart.PROP_DIRTY) {
				updateState();
			}
		}
	}

	@Override
	public void run() {
		if (getWorkbenchWindow() == null) {
			// action has been dispose
			return;
		}
		IWorkbenchPage page = getActivePage();
		if (page != null) {
			((WorkbenchPage) page).closeAllSavedEditors();
		}
	}

	/**
	 * Enable the action if there at least one editor open.
	 */
	private void updateState() {
		IWorkbenchPage page = getActivePage();
		if (page == null) {
			setEnabled(false);
			return;
		}
		IEditorReference editors[] = page.getEditorReferences();
		for (IEditorReference editor : editors) {
			if (!editor.isDirty()) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}

	@Override
	public void dispose() {
		super.dispose();
		for (IWorkbenchPart part : partsWithListeners) {
			part.removePropertyListener(this);
		}
		partsWithListeners.clear();
	}

}
