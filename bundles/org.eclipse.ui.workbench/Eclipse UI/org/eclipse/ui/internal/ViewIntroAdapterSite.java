/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.Adapters;
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

    @Override
	public IActionBars getActionBars() {
        return viewSite.getActionBars();
    }

    @Override
	public <T> T getAdapter(Class<T> adapter) {
		return Adapters.adapt(viewSite, adapter);
    }

    @Override
	public String getId() {
        return descriptor.getId();
    }

    @Override
	public IKeyBindingService getKeyBindingService() {
        return viewSite.getKeyBindingService();
    }

    @Override
	public IWorkbenchPage getPage() {
        return viewSite.getPage();
    }

    @Override
	public String getPluginId() {
        return descriptor.getPluginId();
    }

    @Override
	public ISelectionProvider getSelectionProvider() {
        return viewSite.getSelectionProvider();
    }

    @Override
	public final Object getService(final Class key) {
    		return viewSite.getService(key);
    }

    @Override
	public Shell getShell() {
        return viewSite.getShell();
    }

    @Override
	public IWorkbenchWindow getWorkbenchWindow() {
        return viewSite.getWorkbenchWindow();
    }

	@Override
	public final boolean hasService(final Class key) {
		return viewSite.hasService(key);
	}

    @Override
	public void setSelectionProvider(ISelectionProvider provider) {
        viewSite.setSelectionProvider(provider);
    }

    @Override
	public String toString() {
        return viewSite.toString();
    }
}
