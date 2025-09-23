/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * An operation which delegates its work to a runnable that modifies the
 * workspace.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class WorkspaceModifyDelegatingOperation extends
		WorkspaceModifyOperation {

	/**
	 * The runnable to delegate work to at execution time.
	 */
	private final IRunnableWithProgress content;

	/**
	 * Creates a new operation which will delegate its work to the given
	 * runnable using the provided scheduling rule.
	 *
	 * @param content
	 *            the runnable to delegate to when this operation is executed
	 * @param rule
	 *            The ISchedulingRule to use or <code>null</code>.
	 */
	public WorkspaceModifyDelegatingOperation(IRunnableWithProgress content,
			ISchedulingRule rule) {
		super(rule);
		this.content = content;
	}

	/**
	 * Creates a new operation which will delegate its work to the given
	 * runnable. Schedule using the supplied s
	 *
	 * @param content
	 *            the runnable to delegate to when this operation is executed
	 */
	public WorkspaceModifyDelegatingOperation(IRunnableWithProgress content) {
		super();
		this.content = content;
	}

	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InterruptedException {
		try {
			content.run(monitor);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				throw (CoreException) e.getTargetException();
			}
			if (e.getTargetException() instanceof RuntimeException) {
				throw (RuntimeException) e.getTargetException();
			}
			if (e.getTargetException() instanceof Error) {
				throw (Error) e.getTargetException();
			}
			IDEWorkbenchPlugin.log(e.getTargetException().getMessage(), e.getTargetException());
		}
	}
}
