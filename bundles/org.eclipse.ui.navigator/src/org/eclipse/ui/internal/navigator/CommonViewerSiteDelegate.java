/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 *
 */
public class CommonViewerSiteDelegate implements ICommonViewerSite {
	
	
	private String id; 
	private ISelectionProvider selectionProvider; 
	private Shell shell;

	/**
	 * 
	 * @param anId
	 * @param aSelectionProvider
	 * @param aShell
	 */
	public CommonViewerSiteDelegate(String anId,  ISelectionProvider aSelectionProvider, Shell aShell) {
		Assert.isNotNull(anId);
		Assert.isNotNull(aSelectionProvider);
		Assert.isNotNull(aShell);
		id = anId;
		selectionProvider = aSelectionProvider;		
		shell = aShell;
	} 

	public String getId() {
		return id;
	} 

	public Shell getShell() {
		return shell;
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}  


	public void setSelectionProvider(ISelectionProvider aSelectionProvider) {
		selectionProvider = aSelectionProvider;
	}

	public Object getAdapter(Class adapter) { 
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

}
