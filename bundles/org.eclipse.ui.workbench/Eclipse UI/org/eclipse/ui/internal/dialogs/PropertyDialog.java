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

import java.util.Iterator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.util.Util;
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
	 * @param shell
	 *            the parent shell
	 * @param propertyPageId
	 *            the property page id
	 * @param element
	 *            the adaptable element
	 * @return the property dialog
	 */
	public static PropertyDialog createDialogOn(Shell shell,
			final String propertyPageId, Object element) {

		PropertyPageManager pageManager = new PropertyPageManager();
		String title = "";//$NON-NLS-1$

		if (element == null) {
			return null;
		}
		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(pageManager,
				element);
		// testing if there are pages in the manager
		Iterator pages = pageManager.getElements(PreferenceManager.PRE_ORDER)
				.iterator();
		String name = getName(element);
		if (!pages.hasNext()) {
			MessageDialog.openInformation(shell,
					WorkbenchMessages.PropertyDialog_messageTitle, NLS.bind(
							WorkbenchMessages.PropertyDialog_noPropertyMessage,
							name));
			return null;
		}
		title = NLS
				.bind(WorkbenchMessages.PropertyDialog_propertyMessage, name);
		PropertyDialog propertyDialog = new PropertyDialog(shell, pageManager,
				new StructuredSelection(element));

		if (propertyPageId != null) {
			propertyDialog.setSelectedNode(propertyPageId);
		}
		propertyDialog.create();

		propertyDialog.getShell().setText(title);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(
				propertyDialog.getShell(),
				IWorkbenchHelpContextIds.PROPERTY_DIALOG);

		return propertyDialog;

	}

	/**
	 * Returns the name of the given element.
	 * 
	 * @param element
	 *            the element
	 * @return the name of the element
	 */
	private static String getName(Object element) {
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)Util.getAdapter(element, IWorkbenchAdapter.class);
		if (adapter != null) {
			return adapter.getLabel(element);
		}
		return "";//$NON-NLS-1$
	}

	/**
	 * Create an instance of the receiver.
	 * 
	 * @param parentShell
	 * @param mng
	 * @param selection
	 */
	public PropertyDialog(Shell parentShell, PreferenceManager mng,
			ISelection selection) {
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
	 * @param newSelection
	 *            the new selection
	 */
	public void setSelection(ISelection newSelection) {
		selection = newSelection;
	}

	/**
	 * Get the name of the selected item preference
	 */
	protected String getSelectedNodePreference() {
		return lastPropertyId;
	}

	/**
	 * Get the name of the selected item preference
	 */
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
	protected String getContributionType() {
		return IContributionService.TYPE_PROPERTY;
	}

	
	
}
