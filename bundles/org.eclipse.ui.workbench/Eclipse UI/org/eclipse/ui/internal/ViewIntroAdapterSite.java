/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.intro.IntroDescriptor;
import org.eclipse.ui.intro.IIntroSite;

/**
 * Simple <code>IIntroSite</code> that wraps a <code>IViewSite</code>.  For use in conjunction with 
 * <code>ViewIntroAdapterPart</code>.
 * 
 * @since 3.0
 */
final class ViewIntroAdapterSite implements IIntroSite {
    private IntroDescriptor descriptor;

    private IViewSite viewSite;

    public ViewIntroAdapterSite(IViewSite viewSite, IntroDescriptor descriptor) {
        this.viewSite = viewSite;
        this.descriptor = descriptor;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.intro.IIntroSite#getActionBars()
     */
    public IActionBars getActionBars() {
        return viewSite.getActionBars();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        return viewSite.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getId()
     */
    public String getId() {
        return descriptor.getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getKeyBindingService()
     */
    public IKeyBindingService getKeyBindingService() {
        return viewSite.getKeyBindingService();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getPage()
     */
    public IWorkbenchPage getPage() {
        return viewSite.getPage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPartSite#getPluginId()
     */
    public String getPluginId() {
        return descriptor.getPluginId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getSelectionProvider()
     */
    public ISelectionProvider getSelectionProvider() {
        return viewSite.getSelectionProvider();
    }
    
    public final Object getService(final Class key) {
    		return viewSite.getService(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getShell()
     */
    public Shell getShell() {
        return viewSite.getShell();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#getWorkbenchWindow()
     */
    public IWorkbenchWindow getWorkbenchWindow() {
        return viewSite.getWorkbenchWindow();
    }

	public final boolean hasService(final Class key) {
		return viewSite.hasService(key);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
     */
    public void setSelectionProvider(ISelectionProvider provider) {
        viewSite.setSelectionProvider(provider);
    }

	/* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return viewSite.toString();
    }
}
