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
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.mapping.ISaveableCompareModel;
import org.eclipse.team.ui.mapping.SynchronizationOperation;
import org.eclipse.team.ui.operations.ModelMergeOperation;
import org.eclipse.team.ui.operations.ModelSynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.ui.PlatformUI;

/**
 * Action that performs an optimistic merge
 */
public class MergeIncomingChangesAction extends ModelProviderAction {

	public MergeIncomingChangesAction(ISynchronizePageConfiguration configuration) {
		super(null, configuration);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		try {
			handleBufferChange();
		} catch (InvocationTargetException e) {
			handle(e);
			return;
		} catch (InterruptedException e) {
			// Cancelled so return
			return;
		}
		final IMergeContext context = (IMergeContext)((ModelSynchronizeParticipant)getConfiguration().getParticipant()).getContext();
		try {
			new SynchronizationOperation(getConfiguration(), getContext().getScope().getMappings()) {
				public void execute(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					new ModelMergeOperation(getPart(), ((ModelSynchronizeParticipant)getConfiguration().getParticipant()).getScopeManager()) {
						public boolean isPreviewRequested() {
							return false;
						}
						protected void initializeContext(IProgressMonitor monitor) throws CoreException {
							// Context is already initialized
						}
						protected ISynchronizationContext getContext() {
							return context;
						}
					}.run(monitor);
				}
				protected boolean canRunAsJob() {
					return true;
				}
			}.run();
		} catch (InvocationTargetException e) {
			Utils.handle(e);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	private void handle(Throwable throwable) {
		Utils.handle(throwable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.mapping.ModelProviderAction#isEnabledForSelection(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	protected boolean isEnabledForSelection(IStructuredSelection selection) {
		// This action is always enabled
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ModelProviderAction#getDiffFilter()
	 */
	protected FastDiffFilter getDiffFilter() {
		return new FastDiffFilter() {
			public boolean select(IDiff node) {
				if (node instanceof IThreeWayDiff) {
					IThreeWayDiff twd = (IThreeWayDiff) node;
					if (twd.getDirection() == IThreeWayDiff.CONFLICTING || twd.getDirection() == IThreeWayDiff.INCOMING) {
						return true;
					}
				}
				return false;
			}
		};
	}
	
	protected void handleBufferChange() throws InvocationTargetException, InterruptedException {
		final ISaveableCompareModel currentBuffer = getActiveBuffer();
		if (currentBuffer != null && currentBuffer.isDirty()) {
			PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {	
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					try {
						handleBufferChange(getConfiguration().getSite().getShell(), null, currentBuffer, true, monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		}
		setActiveBuffer(null);
	}
	
}
