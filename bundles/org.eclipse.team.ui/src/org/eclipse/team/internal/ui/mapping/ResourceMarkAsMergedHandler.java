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
package org.eclipse.team.internal.ui.mapping;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.ui.mapping.MergeActionHandler;
import org.eclipse.team.ui.mapping.SynchronizationOperation;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class ResourceMarkAsMergedHandler extends MergeActionHandler {

	private ResourceModelProviderOperation operation;


	public ResourceMarkAsMergedHandler(ISynchronizePageConfiguration configuration) {
		super(configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.MergeActionHandler#getOperation()
	 */
	protected synchronized SynchronizationOperation getOperation() {
		if (operation == null) {
			operation = new ResourceModelProviderOperation(getConfiguration(),
						getStructuredSelection().toArray()) {
				/* (non-Javadoc)
				 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public void execute(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					try {
						final IMergeContext context = (IMergeContext) getContext();
						final IDiffNode[] deltas = getFileDeltas(getElements());
						ISchedulingRule rule = getMergeRule(context, deltas);
						context.run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor monitor)
									throws CoreException {
								markAsMerged(deltas, context, monitor);
							}
	
						}, rule, IResource.NONE, monitor);
	
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
	
				private ISchedulingRule getMergeRule(IMergeContext context,
						IDiffNode[] deltas) {
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
	
				private void markAsMerged(IDiffNode[] deltas,
						final IMergeContext context, IProgressMonitor monitor)
						throws CoreException {
					context.markAsMerged(deltas, false, monitor);
				}
	
				/* (non-Javadoc)
				 * @see org.eclipse.team.internal.ui.mapping.ResourceModelProviderOperation#getDiffFilter()
				 */
				protected FastDiffFilter getDiffFilter() {
					return new FastDiffFilter() {
						public boolean select(IDiffNode node) {
							if (node instanceof IThreeWayDiff) {
								IThreeWayDiff twd = (IThreeWayDiff) node;
								if (twd.getDirection() == IThreeWayDiff.CONFLICTING
										|| twd.getDirection() == IThreeWayDiff.INCOMING) {
									return true;
								}
							}
							return false;
						}
					};
				}
			};
		}
		return operation;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.MergeActionHandler#updateEnablement(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected void updateEnablement(IStructuredSelection selection) {
		synchronized (this) {
			operation = null;
		}
		super.updateEnablement(selection);
	}
	
}
