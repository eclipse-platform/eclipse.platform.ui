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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.operations.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Action contributed by the {@link ModelSynchronizeParticipant} that 
 * will mark a file as merged.
 */
public class MarkAsMergedAction extends ModelProviderAction {

	public MarkAsMergedAction(ISynchronizePageConfiguration configuration) {
		super(null, configuration);
		Utils.initAction(this, "action.markAsMerged."); //$NON-NLS-1$
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
						final IDiffNode[] deltas = getFileDeltas(getStructuredSelection());
						ISchedulingRule rule = getMergeRule(context, deltas);
						context.run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor) throws CoreException {
								markAsMerged(deltas, context, monitor);
							}
						
						}, rule, IResource.NONE, monitor);
						
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}

				private ISchedulingRule getMergeRule(IMergeContext context, IDiffNode[] deltas) {
					ISchedulingRule result = null;
					for (int i = 0; i < deltas.length; i++) {
						IDiffNode node = deltas[i];
						ISchedulingRule rule = context.getMergeRule(node);
						if (result == null) {
							result = rule;
						} else {
							result = MultiRule.combine(result, rule);
						}
					}
					return result;
				}

				private void markAsMerged(IDiffNode[] deltas, final IMergeContext context, IProgressMonitor monitor) throws CoreException {
					context.markAsMerged(deltas, false, monitor);
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
