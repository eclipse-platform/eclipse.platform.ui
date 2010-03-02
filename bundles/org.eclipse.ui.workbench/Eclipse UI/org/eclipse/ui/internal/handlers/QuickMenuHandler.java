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

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.QuickMenuCreator;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.progress.UIJob;

/**
 * Support for a command based QuickMenuAction that can pop up a
 * menu-contribution based context menu.
 * <p>
 * This is experimental and should not be moved.
 * </p>
 * 
 * @since 3.4
 */
public class QuickMenuHandler extends AbstractHandler implements IMenuListener2 {
	private QuickMenuCreator creator = new QuickMenuCreator() {

		protected void fillMenu(IMenuManager menu) {
			if (!(menu instanceof ContributionManager)) {
				return;
			}
			IMenuService service = (IMenuService) PlatformUI.getWorkbench()
					.getService(IMenuService.class);
			service.populateContributionManager((ContributionManager) menu,
					locationURI);
			menu.addMenuListener(QuickMenuHandler.this);
		}

	};

	private String locationURI;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		locationURI = event.getParameter("org.eclipse.ui.window.quickMenu.uri"); //$NON-NLS-1$
		if (locationURI == null) {
			throw new ExecutionException("locatorURI must not be null"); //$NON-NLS-1$
		}
		creator.createMenu();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#dispose()
	 */
	public void dispose() {
		if (creator != null) {
			creator.dispose();
			creator = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuListener2#menuAboutToHide(org.eclipse.jface.action.IMenuManager)
	 */
	public void menuAboutToHide(final IMenuManager managerM) {
		new UIJob("quickMenuCleanup") { //$NON-NLS-1$

			public IStatus runInUIThread(IProgressMonitor monitor) {
				IMenuService service = (IMenuService) PlatformUI.getWorkbench()
						.getService(IMenuService.class);
				service.releaseContributions((ContributionManager) managerM);
				return Status.OK_STATUS;
			}

		}.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
	 */
	public void menuAboutToShow(IMenuManager manager) {
		// no-op
	}
}
