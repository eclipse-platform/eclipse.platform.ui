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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

public abstract class MockWorkbenchPart extends MockPart implements
        IWorkbenchPart {

    private IWorkbenchPartSite site;

    private String title;

    public MockWorkbenchPart() {
        super();
    }

    public void setSite(IWorkbenchPartSite site) {
        this.site = site;
        site.setSelectionProvider(selectionProvider);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.MockPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public void createPartControl(Composite parent) {
        super.createPartControl(parent);
		parent.setLayout(new GridLayout());
		
        Label label = new Label(parent, SWT.NONE);
        label.setText(title);
    }

    @Override
	public IWorkbenchPartSite getSite() {
        return site;
    }

    /**
     * @see IWorkbenchPart#getTitle()
     */
    @Override
	public String getTitle() {
        return title;
    }

    /**
     * @see IWorkbenchPart#getTitleToolTip()
     */
    @Override
	public String getTitleToolTip() {
        return title;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.api.MockPart#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    @Override
	public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        // TODO Auto-generated method stub
        super.setInitializationData(config, propertyName, data);
        title = config.getAttribute("name");
    }

    protected void setSiteInitialized() {
        setSiteInitialized(getSite().getKeyBindingService() != null
                & getSite().getPage() != null
                & getSite().getSelectionProvider() != null
                & getSite().getWorkbenchWindow() != null
                & testActionBars(getActionBars()));
    }

    /**
     * @param actionBars
     * @return
     */
    private boolean testActionBars(IActionBars bars) {
        return bars != null && bars.getMenuManager() != null
                && bars.getToolBarManager() != null
                && bars.getStatusLineManager() != null;

    }

    protected abstract IActionBars getActionBars();
}
