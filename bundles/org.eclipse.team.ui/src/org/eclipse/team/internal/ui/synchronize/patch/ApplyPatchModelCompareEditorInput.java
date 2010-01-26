/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize.patch;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.internal.ui.mapping.ModelCompareEditorInput;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
import org.eclipse.ui.IWorkbenchPage;

public class ApplyPatchModelCompareEditorInput extends ModelCompareEditorInput {

	public ApplyPatchModelCompareEditorInput(
			ModelSynchronizeParticipant participant, ICompareInput input,
			IWorkbenchPage page,
			ISynchronizePageConfiguration synchronizeConfiguration) {
		super(participant, input, page, synchronizeConfiguration);
	}

	protected void handleMenuAboutToShow(IMenuManager manager) {
		// add nothing for now, but when bug 300221 is fixed add 'Merge' only
		/*
		StructuredSelection selection = new StructuredSelection(((IResourceProvider)getCompareInput()).getResource());

		final ResourceMergeHandler mergeHandler = new ResourceMergeHandler(getSynchronizeConfiguration(), false);
		mergeHandler.updateEnablement(selection);
		Action mergeAction = new Action(TeamUIMessages.ModelCompareEditorInput_1) {
			public void run() {
				try {
					mergeHandler.execute(new ExecutionEvent());
				} catch (ExecutionException e) {
					TeamUIPlugin.log(IStatus.ERROR, e.getMessage(), e);
				}
			}
		};
		Utils.initAction(mergeAction, "action.merge."); //$NON-NLS-1$
		mergeAction.setEnabled(mergeAction.isEnabled());

		manager.insertAfter(IWorkbenchActionConstants.MB_ADDITIONS, new Separator("merge")); //$NON-NLS-1$
		manager.insertAfter("merge", mergeAction); //$NON-NLS-1$
		*/
	}
}
