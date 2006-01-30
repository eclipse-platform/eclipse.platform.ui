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

import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.history.IHistoryPageSite;
import org.eclipse.ui.*;
import org.eclipse.ui.services.IServiceLocator;

public class DialogHistoryPageSite implements IHistoryPageSite {

	private final IWorkbenchPartSite site;
	private ISelectionProvider selectionProvider;
	private IActionBars actionBars;
	private final boolean isModal;
	
	public DialogHistoryPageSite(IWorkbenchPartSite site, boolean isModal) {
		this.site = site;
		this.isModal = isModal;
	}
	
	public IWorkbenchPartSite getWorkbenchPartSite() {
		return site;
	}

	public IWorkbenchPart getPart() {
		return site.getPart();
	}

	public Shell getShell() {
		return site.getShell();
	}

	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}

	public void setSelectionProvider(ISelectionProvider provider) {
		this.selectionProvider = provider;
	}

	public IKeyBindingService getKeyBindingService() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public IActionBars getActionBars() {
		return actionBars;
	}

	public void setToolBarManager(ToolBarManager toolBarManager) {
		if (actionBars == null){
			createActionBars(toolBarManager);
		}
	}
	
	/**
	 * Create the action-bars for this site.
	 * @param toolbar the toolbar for the action bar
	 */
	private void createActionBars(final IToolBarManager toolbar) {
		if (actionBars == null) {
			actionBars = new IActionBars() {
				public void clearGlobalActionHandlers() {
				}
				public IAction getGlobalActionHandler(String actionId) {
					return null;
				}
				public IMenuManager getMenuManager() {
					return null;
				}
				public IStatusLineManager getStatusLineManager() {
					return null;
				}
				public IToolBarManager getToolBarManager() {
					return toolbar;
				}
				public void setGlobalActionHandler(String actionId, IAction action) {
					/*if (actionId != null && !"".equals(actionId)) { //$NON-NLS-1$
						IHandler handler = new ActionHandler(action);
						HandlerSubmission handlerSubmission = new HandlerSubmission(null, shell, null, actionId, handler, Priority.MEDIUM);
						PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(handlerSubmission);
						actionHandlers.add(handlerSubmission);
					}*/
				}

				public void updateActionBars() {
				}
				public IServiceLocator getServiceLocator() {
					return null;
				}
			};
		}
	}
	

}
