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
package org.eclipse.team.internal.ui.synchronize;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.*;
import org.eclipse.ui.commands.*;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A synchronize page site for use in dialogs.
 */
public class DialogSynchronizePageSite implements ISynchronizePageSite {

	private final Shell shell;
	private ISelectionProvider selectionProvider;
	private IActionBars actionBars;
	private final boolean isModal;
	// Keybindings enabled in the dialog, these should be removed
	// when the dialog is closed.
	private List actionHandlers = new ArrayList(2);
	
	/**
	 * Create a site for use in a dialog
	 * @param shell the shell
	 * @param isModal whether the dialog is model
	 */
	public DialogSynchronizePageSite(Shell shell, boolean isModal) {
		this.shell = shell;
		this.isModal = isModal;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getShell()
	 */
	public Shell getShell() {
		return shell;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#setSelectionProvider(org.eclipse.jface.viewers.ISelectionProvider)
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		selectionProvider = provider;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getWorkbenchSite()
	 */
	public IWorkbenchSite getWorkbenchSite() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getPart()
	 */
	public IWorkbenchPart getPart() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getKeyBindingService()
	 */
	public IKeyBindingService getKeyBindingService() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#setFocus()
	 */
	public void setFocus() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getPageSettings()
	 */
	public IDialogSettings getPageSettings() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#getActionBars()
	 */
	public IActionBars getActionBars() {
		return actionBars;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.ISynchronizePageSite#isModal()
	 */
	public boolean isModal() {
		return isModal;
	}
	
	/**
	 * Create the action-bars for this site.
	 * @param toolbar the toolbar for the action bar
	 */
	public void createActionBars(final IToolBarManager toolbar) {
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
					if (actionId != null && !"".equals(actionId)) { //$NON-NLS-1$
						IHandler handler = new ActionHandler(action);
						HandlerSubmission handlerSubmission = new HandlerSubmission(null, shell, null, actionId, handler, Priority.MEDIUM);
						PlatformUI.getWorkbench().getCommandSupport().addHandlerSubmission(handlerSubmission);
						actionHandlers.add(handlerSubmission);
					}
				}

				public void updateActionBars() {
				}
				public IServiceLocator getServiceLocator() {
					return null;
				}
			};
		}
	}
	
	/**
	 * Cleanup when the dialog is closed
	 */
	public void dispose() {
		IWorkbenchCommandSupport cm = PlatformUI.getWorkbench().getCommandSupport();
		for (Iterator it = actionHandlers.iterator(); it.hasNext();) {
			HandlerSubmission handler = (HandlerSubmission) it.next();
			cm.removeHandlerSubmission(handler);
		}
	}
}