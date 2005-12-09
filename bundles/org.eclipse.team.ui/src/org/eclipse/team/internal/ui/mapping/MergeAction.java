/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.operations.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Action that merges the selected files
 */
public class MergeAction extends ModelProviderAction {
	
	private final boolean overwrite;

	public MergeAction(ISynchronizePageConfiguration configuration, boolean overwrite) {
		super(null, configuration);
		this.overwrite = overwrite;
		if (overwrite)
			Utils.initAction(this, "action.overwrite."); //$NON-NLS-1$
		else
			Utils.initAction(this, "action.merge."); //$NON-NLS-1$
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
						IDiffNode[] diffs = getFileDeltas(getStructuredSelection());
						IStatus status = context.merge(diffs, overwrite, monitor);
						if (!status.isOK())
							throw new CoreException(status);
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

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		return getFileDeltas(selection).length > 0;
	}
}
