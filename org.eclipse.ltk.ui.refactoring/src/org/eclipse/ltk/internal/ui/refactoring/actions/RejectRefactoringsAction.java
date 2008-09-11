/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.model.ModelMessages;
import org.eclipse.ltk.internal.ui.refactoring.model.RefactoringDescriptorSynchronizationProxy;

/**
 * Action to reject a pending refactoring and to just store it in the history.
 *
 * @since 3.2
 */
public final class RejectRefactoringsAction extends Action {

	/** The synchronization context to use */
	private final ISynchronizationContext fContext;

	/** The refactoring descriptor proxies, or <code>null</code> */
	private RefactoringDescriptorProxy[] fProxies= null;

	/**
	 * Creates a new reject refactorings action.
	 *
	 * @param context
	 *            the synchronization context
	 */
	public RejectRefactoringsAction(final ISynchronizationContext context) {
		Assert.isNotNull(context);
		fContext= context;
		setText(ModelMessages.RejectRefactoringsAction_title);
		setToolTipText(ModelMessages.RejectRefactoringsAction_tool_tip);
		setDescription(ModelMessages.RejectRefactoringsAction_description);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isEnabled() {
		if (fProxies != null && fProxies.length > 0 && fContext instanceof IMergeContext) {
			for (int index= 0; index < fProxies.length; index++) {
				if (fProxies[index] instanceof RefactoringDescriptorSynchronizationProxy) {
					final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) fProxies[index];
					if (proxy.getDirection() == IThreeWayDiff.INCOMING)
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void run() {
		if (fProxies != null) {
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, true, new IRunnableWithProgress() {

					public final void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							monitor.beginTask("", fProxies.length + 100); //$NON-NLS-1$
							final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
							for (int index= 0; index < fProxies.length; index++)
								service.addRefactoringDescriptor(fProxies[index], new SubProgressMonitor(monitor, 1));
						} finally {
							monitor.done();
						}
					}
				});
			} catch (InvocationTargetException exception) {
				RefactoringUIPlugin.log(exception);
			} catch (InterruptedException exception) {
				// Do nothing
			}
		}
	}

	/**
	 * Sets the refactoring descriptor proxies to accept.
	 *
	 * @param proxies
	 *            the refactoring descriptor proxies
	 */
	public void setRefactoringDescriptors(final RefactoringDescriptorProxy[] proxies) {
		Assert.isNotNull(proxies);
		fProxies= proxies;
	}
}