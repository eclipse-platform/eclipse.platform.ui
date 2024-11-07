/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
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
 *     James Blackburn (Broadcom Corp.) - Bug 294628 multiple selection
 *     Lucas Bullen (Red Hat Inc.) - Bug 530654 - Import/Export Preferences buttons visible on other dialogs
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Iterator;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.model.IContributionService;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This dialog is created and shown when 'Properties' action is performed while
 * an object is selected. It shows one or more pages registered for object's
 * type.
 */
public class PropertyDialog extends FilteredPreferenceDialog {
	private ISelection selection;

	// The id of the last page that was selected
	private static String lastPropertyId = null;

	/**
	 * Create a new property dialog.
	 *
	 * @param shell          the parent shell
	 * @param propertyPageId the property page id
	 * @param element        the adaptable element
	 * @return the property dialog
	 */
	public static PropertyDialog createDialogOn(Shell shell, final String propertyPageId, Object element) {

		PropertyPageManager pageManager = new PropertyPageManager();
		String title = "";//$NON-NLS-1$

		if (element == null) {
			return null;
		}
		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(pageManager, element);
		// testing if there are pages in the manager
		Iterator<IPreferenceNode> pages = pageManager.getElements(PreferenceManager.PRE_ORDER).iterator();
		String name = getName(element);
		if (!pages.hasNext()) {
			if ("".equals(name)) { //$NON-NLS-1$
				MessageDialog.openInformation(shell, WorkbenchMessages.PropertyDialog_messageTitle,
						WorkbenchMessages.PropertyDialog_noPropertyMessageForUnknown);
			} else {
				MessageDialog.openInformation(shell, WorkbenchMessages.PropertyDialog_messageTitle,
						NLS.bind(WorkbenchMessages.PropertyDialog_noPropertyMessage, name));
			}
			return null;
		}
		title = NLS.bind(WorkbenchMessages.PropertyDialog_propertyMessage, name);
		PropertyDialog propertyDialog = new PropertyDialog(shell, pageManager, new StructuredSelection(element));

		if (propertyPageId != null) {
			propertyDialog.setSelectedNode(propertyPageId);
		}
		propertyDialog.create();

		propertyDialog.getShell().setText(title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(propertyDialog.getShell(),
				IWorkbenchHelpContextIds.PROPERTY_DIALOG);

		return propertyDialog;

	}

	@Override
	protected void addButtonsToHelpControl(Control control) {
		// Overridden to remove import/export buttons
	}

	/**
	 * Returns the name of the given element(s). Prints at most 3 names.
	 *
	 * @param element the element / IStructuredSelection
	 * @return the name of the element
	 */
	private static String getName(Object element) {
		Object[] elements;
		if (element instanceof IStructuredSelection)
			elements = ((IStructuredSelection) element).toArray();
		else
			elements = new Object[] { element };
		StringBuilder sb = new StringBuilder();
		// Print at most 3 entries...
		for (int i = 0; i < elements.length; i++) {
			element = elements[i];
			if (i > 2) {
				sb.append(" ..."); //$NON-NLS-1$
				break;
			}
			IWorkbenchAdapter adapter = Adapters.adapt(element, IWorkbenchAdapter.class);
			if (adapter != null) {
				if (sb.length() > 0)
					sb.append(", "); //$NON-NLS-1$
				sb.append(adapter.getLabel(element));
			}
		}
		return sb.toString();
	}

	/**
	 * Create an instance of the receiver.
	 */
	public PropertyDialog(Shell parentShell, PreferenceManager mng, ISelection selection) {
		super(parentShell, mng);
		setSelection(selection);
	}

	/**
	 * Returns selection in the "Properties" action context.
	 *
	 * @return the selection
	 */
	public ISelection getSelection() {
		return selection;
	}

	/**
	 * Sets the selection that will be used to determine target object.
	 *
	 * @param newSelection the new selection
	 */
	public void setSelection(ISelection newSelection) {
		selection = newSelection;
	}

	/**
	 * Get the name of the selected item preference
	 */
	@Override
	protected String getSelectedNodePreference() {
		return lastPropertyId;
	}

	/**
	 * Get the name of the selected item preference
	 */
	@Override
	protected void setSelectedNodePreference(String pageId) {
		lastPropertyId = pageId;
	}

	/**
	 * Return the contributionType (used by the IContributionService).
	 *
	 * Override this with a more specific contribution type as required.
	 *
	 * @return a string, the contributionType
	 */
	@Override
	protected String getContributionType() {
		return IContributionService.TYPE_PROPERTY;
	}

}
