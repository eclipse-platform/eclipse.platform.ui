/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.IWorkbenchAdapter;

import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * This dialog is created and shown when 'Properties' action is performed while
 * an object is selected. It shows one or more pages registered for object's
 * type.
 */
public class PropertyDialog extends FilteredPreferenceDialog {
    private ISelection selection;

    //The id of the last page that was selected
    private static String lastPropertyId = null;

    
    public static PropertyDialog createDialogOn(Shell shell,final String propertyPageId, IAdaptable element){

		PropertyPageManager pageManager = new PropertyPageManager();
		String title = "";//$NON-NLS-1$
		
		if (element == null)
			return null;
		// load pages for the selection
		// fill the manager with contributions from the matching contributors
		PropertyPageContributorManager.getManager().contribute(pageManager,
				element);
		// testing if there are pages in the manager
		Iterator pages = pageManager.getElements(PreferenceManager.PRE_ORDER)
				.iterator();
		String name = getName(element);
		if (!pages.hasNext()) {
			MessageDialog
					.openInformation(
							shell,
							WorkbenchMessages
									.getString("PropertyDialog.messageTitle"), //$NON-NLS-1$
							WorkbenchMessages
									.format(
											"PropertyDialog.noPropertyMessage", new Object[] { name })); //$NON-NLS-1$
			return null;
		}
		title = WorkbenchMessages.format(
				"PropertyDialog.propertyMessage", new Object[] { name }); //$NON-NLS-1$
		PropertyDialog propertyDialog = new PropertyDialog(shell, pageManager,
				new StructuredSelection(element));

		if (propertyPageId != null)
			propertyDialog.setSelectedNode(propertyPageId);
		propertyDialog.create();

		propertyDialog.getShell().setText(title);
		WorkbenchHelp.setHelp(propertyDialog.getShell(),
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
	private static String getName(IAdaptable element) {
		IWorkbenchAdapter adapter = (IWorkbenchAdapter) element
				.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) 
			return adapter.getLabel(element);
		return "";//$NON-NLS-1$
	}
	
    /**
     * Create an instance of the receiver.
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
     */
    public ISelection getSelection() {
        return selection;
    }

    /**
     * Sets the selection that will be used to determine target object.
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
    
    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dialogs.FilteredPreferenceDialog#getGroups()
	 */
	protected WorkbenchPreferenceGroup[] getGroups() {
		//There is no grouping in properties
		return new WorkbenchPreferenceGroup[0]; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferenceDialog#getCurrentPage()
	 */
	public IPreferencePage getCurrentPage() {
		return super.getCurrentPage();
	}
}