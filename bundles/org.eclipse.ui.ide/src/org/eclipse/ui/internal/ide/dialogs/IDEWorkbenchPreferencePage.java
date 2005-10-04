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
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferencePage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * The IDE workbench main preference page.
 * 
 *Note: want IDE settings to appear in main Workbench preference page (via subclassing),
 *   however the superclass, WorkbenchPreferencePage, is internal
 */
public class IDEWorkbenchPreferencePage extends WorkbenchPreferencePage
        implements IWorkbenchPreferencePage {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage
     */
    protected Control createContents(Composite parent) {

    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IWorkbenchHelpContextIds.WORKBENCH_PREFERENCE_PAGE);

        Composite composite = createComposite(parent);

        createShowUserDialogPref(composite);
        createStickyCyclePref(composite);
        createHeapStatusPref(composite);
        
        createOpenModeGroup(composite);

        applyDialogFont(composite);

        return composite;
    }

     /**
     * Returns the IDE preference store.
     * @return the preference store.
     */
    protected IPreferenceStore getIDEPreferenceStore() {
        return IDEWorkbenchPlugin.getDefault().getPreferenceStore();
    }

    /**
     * The default button has been pressed.
     */
    protected void performDefaults() {
		super.performDefaults();
    }

    /**
     * The user has pressed Ok. Store/apply this page's values appropriately.
     */
    public boolean performOk() {
        return super.performOk();
    }
   
}
