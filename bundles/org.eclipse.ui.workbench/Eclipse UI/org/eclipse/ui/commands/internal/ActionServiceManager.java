/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.commands.internal;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.IActionService;
import org.eclipse.ui.commands.IActionServiceListener;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.WorkbenchWindow;

public class ActionServiceManager {

	private IActionService workbenchWindowActionService;
	private IActionService activeWorkbenchPartActionService;
	
	private IActionServiceListener actionServiceListener = new IActionServiceListener() {
		public void actionServiceChanged(IActionService actionService) {
			ActionServiceManager.this.actionServiceChanged(actionService);
		}
	};			

	private final IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart workbenchPart) {
			partChanged();
		}
			
		public void partBroughtToTop(IWorkbenchPart workbenchPart) {
		}
			
		public void partClosed(IWorkbenchPart workbenchPart) {
			partChanged();
		}
			
		public void partDeactivated(IWorkbenchPart workbenchPart) {
			partChanged();
		}
			
		public void partOpened(IWorkbenchPart workbenchPart) {
			partChanged();
		}
	};
		
	private IWorkbenchWindow workbenchWindow;

	public ActionServiceManager(IWorkbenchWindow workbenchWindow) {
		super();
		this.workbenchWindow = workbenchWindow;	
		workbenchWindow.getPartService().addPartListener(partListener);				
		// TODO: eliminate downcast: IWorkbenchWindow needs getActionService();
		workbenchWindowActionService = ((WorkbenchWindow) workbenchWindow).getActionService();
		workbenchWindowActionService.addActionServiceListener(actionServiceListener);
		partChanged();
	}

	private void actionServiceChanged(IActionService actionService) {
		System.out.println("actionServiceChanged!");
	}
	
	private void partChanged() {
		IActionService activeWorkbenchPartActionService = null;
		IWorkbenchPage activeWorkbenchPage = workbenchWindow.getActivePage();
	 
		if (activeWorkbenchPage != null) {
			IWorkbenchPart activeWorkbenchPart = activeWorkbenchPage.getActivePart();
	
			if (activeWorkbenchPart != null) {
				IWorkbenchPartSite activeWorkbenchPartSite = activeWorkbenchPart.getSite();
			
				if (activeWorkbenchPartSite != null)
					// TODO: eliminate downcast: IWorkbenchPartSite needs getActionService();
					activeWorkbenchPartActionService = ((PartSite) activeWorkbenchPartSite).getActionService();
			}
		}
				
		if (this.activeWorkbenchPartActionService != activeWorkbenchPartActionService) {
			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.removeActionServiceListener(actionServiceListener);				
			
			this.activeWorkbenchPartActionService = activeWorkbenchPartActionService;
			
			if (this.activeWorkbenchPartActionService != null)
				this.activeWorkbenchPartActionService.addActionServiceListener(actionServiceListener);
				
			actionServiceChanged(this.activeWorkbenchPartActionService);				
		}
	}
	
	/*
	void shellChanged() {
	}
	
	final WorkbenchWindow finalWorkbenchWindow = this.workbenchWindow; 
		
	this.workbenchWindow.addPageListener(new IPageListener() {			
		public void pageActivated(IWorkbenchPage page) {
		}
			
		public void pageClosed(IWorkbenchPage page) {
		}

		public void pageOpened(IWorkbenchPage page) {
			page.addPartListener(partListener);
			update(page.getActivePart());
			Shell shell = finalWorkbenchWindow.getShell();
			shell.removeShellListener(shellListener);				
			shell.addShellListener(shellListener);				
		}
	});

	private final ShellListener shellListener = new ShellAdapter() {
		public void shellActivated(ShellEvent e) {
			shellChanged();
		}

		public void shellDeactivated(ShellEvent e) {
			shellChanged();
		}
	};
	*/
}
