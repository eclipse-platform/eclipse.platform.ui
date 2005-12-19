/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.operations.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Action that performs an optimistic merge
 */
public class MergeIncomingChangesAction extends ModelProviderAction {

	public MergeIncomingChangesAction(ISynchronizePageConfiguration configuration) {
		super(null, configuration);
		Utils.initAction(this, "action.mergeAll."); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		final IMergeContext context = (IMergeContext)((ModelSynchronizeParticipant)getConfiguration().getParticipant()).getContext();
		try {
			new ModelProviderOperation(getConfiguration()) {
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					try {
						if (performMerge(context, monitor)) {
							promptForNoChanges();
						} else {
							promptForMergeFailure();
						}
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			}.run();
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	protected void promptForMergeFailure() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getConfiguration().getSite().getShell(), TeamUIMessages.MergeIncomingChangesAction_0, TeamUIMessages.MergeIncomingChangesAction_1);
			};
		});
	}

	private void promptForNoChanges() {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openInformation(getConfiguration().getSite().getShell(), TeamUIMessages.MergeIncomingChangesAction_2, TeamUIMessages.MergeIncomingChangesAction_3);
			};
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		// This action is always enabled
		return true;
	}
	

}
