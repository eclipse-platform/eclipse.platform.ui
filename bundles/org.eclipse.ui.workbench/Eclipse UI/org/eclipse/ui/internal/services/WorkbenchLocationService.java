/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
public class WorkbenchLocationService implements IWorkbenchLocationService, IDisposable {

	private IEditorSite mpepSite;
	private IPageSite pageSite;
	private IWorkbenchPartSite partSite;
	private String serviceScope;
	private IWorkbench workbench;
	private IWorkbenchWindow window;
	private int level;

	public WorkbenchLocationService(String serviceScope, IWorkbench workbench, IWorkbenchWindow window,
			IWorkbenchPartSite partSite, IEditorSite mpepSite, IPageSite pageSite, int level) {
		this.mpepSite = mpepSite;
		this.pageSite = pageSite;
		this.partSite = partSite;
		this.serviceScope = serviceScope;
		this.window = window;
		this.workbench = workbench;
		this.level = level;
	}

	@Override
	public IEditorSite getMultiPageEditorSite() {
		return mpepSite;
	}

	@Override
	public IPageSite getPageSite() {
		return pageSite;
	}

	@Override
	public IWorkbenchPartSite getPartSite() {
		return partSite;
	}

	@Override
	public String getServiceScope() {
		return serviceScope;
	}

	@Override
	public IWorkbench getWorkbench() {
		return workbench;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}

	@Override
	public void dispose() {
		mpepSite = null;
		pageSite = null;
		partSite = null;
		serviceScope = null;
		workbench = null;
		window = null;
	}

	@Override
	public int getServiceLevel() {
		return level;
	}

}
