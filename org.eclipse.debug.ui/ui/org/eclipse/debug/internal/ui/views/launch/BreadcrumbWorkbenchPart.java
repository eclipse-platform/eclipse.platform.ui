/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech - Wind River - adapted to use in Debug view
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.launch;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;

/**
 * Fake part to used to create the breadcrumb page.
 *
 * @since 3.5
 */
class BreadcrumbWorkbenchPart implements IWorkbenchPart {

    private IWorkbenchPartSite fSite = null;

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
	public boolean equals(Object obj) {
        return (obj instanceof BreadcrumbWorkbenchPart);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Constructs a part for the given console that binds to the given
     * site
     * @param site the backing site
     */
    public BreadcrumbWorkbenchPart(IWorkbenchPartSite site) {
        fSite = site;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#addPropertyListener(org.eclipse.ui.IPropertyListener)
     */
    @Override
	public void addPropertyListener(IPropertyListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public void createPartControl(Composite parent) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    @Override
	public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getSite()
     */
    @Override
	public IWorkbenchPartSite getSite() {
        return fSite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitle()
     */
    @Override
	public String getTitle() {
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
     */
    @Override
	public Image getTitleImage() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#getTitleToolTip()
     */
    @Override
	public String getTitleToolTip() {
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#removePropertyListener(org.eclipse.ui.IPropertyListener)
     */
    @Override
	public void removePropertyListener(IPropertyListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    @Override
	public void setFocus() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
	public <T> T getAdapter(Class<T> adapter) {
        return null;
    }
}

