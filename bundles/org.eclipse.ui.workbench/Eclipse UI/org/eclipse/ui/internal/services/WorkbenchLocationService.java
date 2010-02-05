/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.services.IDisposable;

/**
 * @since 3.4
 * 
 */
public class WorkbenchLocationService implements IWorkbenchLocationService,
		IDisposable {

	private IEditorSite mpepSite;
	private IPageSite pageSite;
	private IWorkbenchPartSite partSite;
	private String serviceScope;
	private IWorkbench workbench;
	private IWorkbenchWindow window;
	private int level;

	public WorkbenchLocationService(String serviceScope, IWorkbench workbench,
			IWorkbenchWindow window, IWorkbenchPartSite partSite,
			IEditorSite mpepSite, IPageSite pageSite, int level) {
		this.mpepSite = mpepSite;
		this.pageSite = pageSite;
		this.partSite = partSite;
		this.serviceScope = serviceScope;
		this.window = window;
		this.workbench = workbench;
		this.level = level;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.ui.internal.services.IWorkbenchLocationService#
	 * getMultiPageEditorSite()
	 */
	public IEditorSite getMultiPageEditorSite() {
		return mpepSite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getPageSite()
	 */
	public IPageSite getPageSite() {
		return pageSite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getPartSite()
	 */
	public IWorkbenchPartSite getPartSite() {
		return partSite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getServiceScope
	 * ()
	 */
	public String getServiceScope() {
		return serviceScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getWorkbench()
	 */
	public IWorkbench getWorkbench() {
		return workbench;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.services.IWorkbenchLocationService#getWorkbenchWindow
	 * ()
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		mpepSite = null;
		pageSite = null;
		partSite = null;
		serviceScope = null;
		workbench = null;
		window = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IWorkbenchLocationService#getServiceLevel()
	 */
	public int getServiceLevel() {
		return level;
	}

}
