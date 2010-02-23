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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.WorkbenchPlugin;

/*
 * A page used as a filler for nodes in the preference tree
 * for which no page is suppplied.
 */
public class EmptyPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {
    protected Control createContents(Composite parent) {
        return new Composite(parent, SWT.NULL);
    }

    /**
     * Hook method to get a page specific preference store. Reimplement this
     * method if a page don't want to use its parent's preference store.
     */
    protected IPreferenceStore doGetPreferenceStore() {
        return WorkbenchPlugin.getDefault().getPreferenceStore();
    }

    /**
     * @see IWorkbenchPreferencePage
     */
    public void init(IWorkbench workbench) {
    }
}
