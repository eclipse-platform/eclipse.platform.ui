/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
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
package org.eclipse.ltk.internal.ui.refactoring.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.jface.action.Action;

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

	@Override
	public boolean isEnabled() {
		if (fProxies != null && fProxies.length > 0 && fContext instanceof IMergeContext) {
			for (RefactoringDescriptorProxy fproxy : fProxies) {
				if (fproxy instanceof RefactoringDescriptorSynchronizationProxy) {
					final RefactoringDescriptorSynchronizationProxy proxy= (RefactoringDescriptorSynchronizationProxy) fproxy;
					if (proxy.getDirection() == IThreeWayDiff.INCOMING)
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (fProxies != null) {
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, true, monitor -> {
					try {
						monitor.beginTask("", fProxies.length + 100); //$NON-NLS-1$
						final RefactoringHistoryService service= RefactoringHistoryService.getInstance();
						for (RefactoringDescriptorProxy proxy : fProxies) {
							service.addRefactoringDescriptor(proxy, new SubProgressMonitor(monitor, 1));
						}
					} finally {
						monitor.done();
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