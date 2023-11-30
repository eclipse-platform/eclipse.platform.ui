/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.navigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.ICommonViewerSite;

/**
 * Provides a delegate implementation of {@link ICommonViewerSite}.
 *
 * @since 3.2
 */
public class CommonViewerSiteDelegate implements ICommonViewerSite {


	private String id;
	private ISelectionProvider selectionProvider;
	private Shell shell;

	public CommonViewerSiteDelegate(String anId,  ISelectionProvider aSelectionProvider, Shell aShell) {
		Assert.isNotNull(anId);
		Assert.isNotNull(aSelectionProvider);
		Assert.isNotNull(aShell);
		id = anId;
		selectionProvider = aSelectionProvider;
		shell = aShell;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}


	@Override
	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		selectionProvider = aSelectionProvider;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
