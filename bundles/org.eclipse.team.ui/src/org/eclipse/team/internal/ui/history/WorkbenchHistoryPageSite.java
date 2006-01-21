/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.IPageSite;

public class WorkbenchHistoryPageSite implements IHistoryPageSite {
	
	IWorkbenchPart part;
	IPageSite site;
	
	public WorkbenchHistoryPageSite(IWorkbenchPart part, IPageSite site) {
		this.part = part;
		this.site = site;
	}
	
	public IWorkbenchPartSite getWorkbenchPartSite() {
		return part.getSite();
	}

	public IWorkbenchPart getPart() {
		return part;
	}

	public Shell getShell() {
		return part.getSite().getShell();
	}

	public ISelectionProvider getSelectionProvider() {
		return site.getSelectionProvider();
	}

	public void setSelectionProvider(ISelectionProvider provider) {
		site.setSelectionProvider(provider);

	}

	public IKeyBindingService getKeyBindingService() {
		return part.getSite().getKeyBindingService();
	}

	public void setFocus() {
		part.getSite().getPage().activate(part);
	}

	public IActionBars getActionBars() {
		return site.getActionBars();
	}
	

	public boolean isModal() {
		return false;
	}


}
