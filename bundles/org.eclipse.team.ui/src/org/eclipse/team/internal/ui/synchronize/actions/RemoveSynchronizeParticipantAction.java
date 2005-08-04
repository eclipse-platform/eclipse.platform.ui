/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.PlatformUI;

/**
 * Action to remove the given participant from the synchronize manager.
 * @since 3.0 
 */
public class RemoveSynchronizeParticipantAction extends Action {

	private final ISynchronizeView view;
	private boolean removeAll;

	public RemoveSynchronizeParticipantAction(ISynchronizeView view, boolean removeAll) {
		this.view = view;
		this.removeAll = removeAll;
		if (removeAll) {
			Utils.initAction(this, "action.removeAllPage."); //$NON-NLS-1$
		} else {
			Utils.initAction(this, "action.removePage."); //$NON-NLS-1$
		}
	}

	public void run() {
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					if (removeAll) {
						removeAll();
					} else {
						removeCurrent();
					}
				}
			});
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Cancelled. Just ignore
		}
	}

	private void removeCurrent() {
		final ISynchronizeParticipant participant = view.getParticipant();
		if (participant != null) {
			if (participant.isPinned()) {
				final boolean[] bail = new boolean[] { false };
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						bail[0] = !MessageDialog.openQuestion(
								view.getSite().getShell(), 
								TeamUIMessages.RemoveSynchronizeParticipantAction_0,  
								TeamUIMessages.RemoveSynchronizeParticipantAction_1); 

					}
				});
				if (bail[0]) {
					return;
				}
			}
			TeamUI.getSynchronizeManager().removeSynchronizeParticipants(new ISynchronizeParticipant[]{participant});
		}
	}

	private void removeAll() {
		ISynchronizeManager manager = TeamUI.getSynchronizeManager();
		ISynchronizeParticipantReference[] refs = manager.getSynchronizeParticipants();
		ArrayList removals = new ArrayList();
		for (int i = 0; i < refs.length; i++) {
			ISynchronizeParticipantReference reference = refs[i];
			ISynchronizeParticipant p;
			try {
				p = reference.getParticipant();
				if (! p.isPinned())
					removals.add(p);
			} catch (TeamException e) {
				// keep going
			}
		}
		manager.removeSynchronizeParticipants((ISynchronizeParticipant[]) removals.toArray(new ISynchronizeParticipant[removals.size()]));
	}
}
