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
package org.eclipse.ui.tests.api;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetPage;

/**
 * Tests the org.eclipse.ui.workingSets extension point.
 */
public class MockWorkingSetPage extends WizardPage implements IWorkingSetPage {
    private IWorkingSet workingSet;

    /**
     * Creates a new instance of the receiver.
     */
    public MockWorkingSetPage() {
        super(
                "MockWorkingSetPage", "Test Working Set", ImageDescriptor.getMissingImageDescriptor()); //$NON-NLS-1$ $NON-NLS-2$
    }

    /**
     * Overrides method in WizardPage.
     * 
     * @see org.eclipse.jface.wizard.WizardPage#createControl(Composite)
     */
    @Override
	public void createControl(Composite parent) {
    }

    /**
     * Implements IWorkingSetPage.
     * 
     * @see org.eclipse.ui.dialogs.IWorkingSetPage#getSelection()
     */
    @Override
	public IWorkingSet getSelection() {
        return workingSet;
    }

    /**
     * Implements IWorkingSetPage.
     * 
     * @see org.eclipse.ui.dialogs.IWorkingSetPage#setSelection(IWorkingSet)
     */
    @Override
	public void setSelection(IWorkingSet workingSet) {
    }

    /**
     * @see org.eclipse.ui.dialogs.IWorkingSetPage#finish()
     */
    @Override
	public void finish() {
    }

}
