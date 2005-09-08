/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.core.refactoring.history;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.text.Assert;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.RefactoringCoreMessages;

/**
 * Operation that, when run, executes a refactoring session.
 * <p>
 * The operation should be executed via the run method offered by
 * <code>IWorkspace</code> to achieve proper delta batching.
 * </p>
 * <p>
 * Note: this class is not intended to be extended outside of the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class PerformRefactoringSessionOperation implements IWorkspaceRunnable {

	/** The refactoring session descriptor */
	private final RefactoringSessionDescriptor fSessionDescriptor;

	/** The status of the session */
	private RefactoringStatus fSessionStatus= new RefactoringStatus();

	/**
	 * Creates a new perform refactoring session operation.
	 * 
	 * @param descriptor
	 *            the refactoring session descriptor
	 */
	public PerformRefactoringSessionOperation(final RefactoringSessionDescriptor descriptor) {
		Assert.isNotNull(descriptor);
		fSessionDescriptor= descriptor;
	}

	/**
	 * Returns the status of the session which has been executed. Guaranteed not
	 * to be <code>null</code>.
	 * 
	 * @return the status of the session
	 */
	public RefactoringStatus getSessionStatus() {
		return fSessionStatus;
	}

	/*
	 * @see org.eclipse.core.resources.IWorkspaceRunnable#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(final IProgressMonitor monitor) throws CoreException {
		fSessionStatus= new RefactoringStatus();
		final RefactoringDescriptor[] descriptors= fSessionDescriptor.getRefactorings();
		monitor.beginTask(RefactoringCoreMessages.PerformRefactoringSessionOperation_perform_refactoring_session, descriptors.length);
		final ScriptableRefactoringFactory factory= ScriptableRefactoringFactory.getInstance();
		try {
			for (int index= 0; index < descriptors.length && !fSessionStatus.hasFatalError(); index++) {
				boolean execute= false;
				final RefactoringDescriptor descriptor= descriptors[index];
				final Refactoring refactoring= factory.createRefactoring(descriptor);
				if (refactoring instanceof IInitializableRefactoring) {
					final IInitializableRefactoring extended= (IInitializableRefactoring) refactoring;
					final RefactoringArguments arguments= factory.createArguments(descriptor);
					if (arguments != null && extended.initialize(arguments))
						execute= true;
				} else
					execute= false;
				if (execute)
					ResourcesPlugin.getWorkspace().run(new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS), new SubProgressMonitor(monitor, 1));
			}
		} finally {
			monitor.done();
		}
	}
}
