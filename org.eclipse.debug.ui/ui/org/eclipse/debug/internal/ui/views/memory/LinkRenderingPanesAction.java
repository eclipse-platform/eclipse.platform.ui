/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingSynchronizationService;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action to link/unlink rendering view panes
 */
public class LinkRenderingPanesAction implements IViewActionDelegate {

	IMemoryRenderingSite fRenderingSite;
	private MemoryViewSynchronizationService fMemSyncService;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init(IViewPart view) {

		if (view instanceof IMemoryRenderingSite) {
			fRenderingSite = (IMemoryRenderingSite) view;

			IMemoryRenderingSynchronizationService syncService = fRenderingSite.getSynchronizationService();

			if (syncService instanceof MemoryViewSynchronizationService)
				fMemSyncService = (MemoryViewSynchronizationService) syncService;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {

		if (fMemSyncService == null)
			return;

		fMemSyncService.setEnabled(!fMemSyncService.isEnabled());
		updateActionState(action);
	}

	/**
	 * @param action
	 */
	private void updateActionState(IAction action) {

		if (fMemSyncService == null)
			return;

		if (fMemSyncService.isEnabled())
			action.setChecked(true);
		else
			action.setChecked(false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action
	 * .IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		updateActionState(action);
	}
}
