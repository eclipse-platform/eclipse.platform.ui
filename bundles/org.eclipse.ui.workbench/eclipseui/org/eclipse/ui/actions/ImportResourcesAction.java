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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Action representing a generic import operation.
 * <p>
 * This class may be instantiated. It is not intended to be subclassed.
 * </p>
 * <p>
 * This method automatically registers listeners so that it can keep its
 * enablement state up to date. Ordinarily, the window's references to these
 * listeners will be dropped automatically when the window closes. However, if
 * the client needs to get rid of an action while the window is still open, the
 * client must call IWorkbenchAction#dispose to give the action an opportunity
 * to deregister its listeners and to perform any other cleanup.
 *
 * </p>
 * <p>
 * Note: Despite the name, an import operation can deal with things other than
 * resources; the current name was retained for historical reasons.
 * </p>
 *
 * @since 2.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ImportResourcesAction extends BaseSelectionListenerAction implements ActionFactory.IWorkbenchAction {
	/**
	 * Reference to the WorkbenchCommandAction that executes the Import Wizard.
	 */
	private ActionFactory.IWorkbenchAction action;

	/**
	 * The workbench window; or <code>null</code> if this action has been
	 * <code>dispose</code>d.
	 */
	private IWorkbenchWindow workbenchWindow;

	/**
	 * Create a new instance of this class.
	 *
	 * @param window the window
	 */
	public ImportResourcesAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.ImportResourcesAction_text);
		if (window == null) {
			throw new IllegalArgumentException();
		}

		this.workbenchWindow = window;
		action = ActionFactory.IMPORT.create(window);

		setText(action.getText());
		setToolTipText(action.getToolTipText());
		setId(action.getId());
		setActionDefinitionId(action.getActionDefinitionId());
		window.getWorkbench().getHelpSystem().setHelp(this, IWorkbenchHelpContextIds.IMPORT_ACTION);
		setImageDescriptor(action.getImageDescriptor());
	}

	/**
	 * Create a new instance of this class
	 *
	 * @param workbench the workbench
	 * @deprecated use the constructor
	 *             <code>ImportResourcesAction(IWorkbenchWindow)</code>
	 */
	@Deprecated
	public ImportResourcesAction(IWorkbench workbench) {
		this(workbench.getActiveWorkbenchWindow());
	}

	/**
	 * Invoke the Import wizards selection Wizard.
	 */
	@Override
	public void run() {
		if (workbenchWindow == null) {
			// action has been disposed
			return;
		}

		action.run();
	}

	/**
	 * Sets the current selection. In for backwards compatibility. Use
	 * selectionChanged() instead.
	 *
	 * @param selection the new selection
	 */
	@Deprecated
	public void setSelection(IStructuredSelection selection) {
		selectionChanged(selection);
	}

	@Override
	public void dispose() {
		workbenchWindow = null;
		if (action != null) {
			action.dispose();
		}
		action = null;
	}
}
