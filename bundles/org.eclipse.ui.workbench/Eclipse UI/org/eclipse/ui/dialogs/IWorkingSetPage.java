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
package org.eclipse.ui.dialogs;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkingSet;

/**
 * A working set page allows the user to edit an existing 
 * working set and create a new working set.
 * <p>
 * Clients should implement this interface and include the 
 * name of their class in an extension contributed to the 
 * workbench's working set extension point 
 * (named <code>"org.eclipse.ui.workingSets"</code>) if they 
 * want to provide a special wizard page for a particular 
 * working set element type.
 * </p>
 * <p>
 * Clients implementing this interface may subclass from 
 * org.eclipse.jface.wizard.WizardPage.
 * </p>
 *
 * @since 2.0
 */
public interface IWorkingSetPage extends IWizardPage {
    /**
     * Called when the working set wizard is closed by selecting 
     * the finish button.
     * Implementers may store the page result (new/changed working 
     * set returned in getSelection) here.
     */
    public void finish();

    /**
     * Returns the working set edited or created on the page 
     * after the wizard has closed.
     * Returns the working set that was initially set using 
     * <code>setSelection</code>if the wizard has not been 
     * closed yet.
     * Implementors should return the object set in setSelection
     * instead of making a copy and returning the changed copy.
     * 
     * @return the working set edited or created on the page.
     */
    public IWorkingSet getSelection();

    /**
     * Sets the working set edited on the page.
     * Implementors should not make a copy of this working set.
     * The passed object can be edited as is and should be 
     * returned in getSelection().
     * 
     * @param workingSet the working set edited on the page.
     */
    public void setSelection(IWorkingSet workingSet);
}
