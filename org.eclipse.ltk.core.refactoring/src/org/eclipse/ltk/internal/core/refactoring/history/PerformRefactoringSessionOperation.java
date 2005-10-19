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

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringSessionDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.Assert;
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
public final class PerformRefactoringSessionOperation implements IWorkspaceRunnable {

	/** Refactoring history listener */
	private class RefactoringHistoryListener extends RefactoringHistoryAdapter {

		/*
		 * @see org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryAdapter#refactoringPerformed(org.eclipse.ltk.internal.core.refactoring.history.IRefactoringHistory,org.eclipse.ltk.core.refactoring.RefactoringDescriptor)
		 */
		public final void refactoringPerformed(final IRefactoringHistory history, final RefactoringDescriptor descriptor) {
			Assert.isNotNull(descriptor);
			if (!descriptor.isUnknown())
				fCurrentDescriptor= descriptor;
			else
				fCurrentDescriptor= null;
		}
	}

	/**
	 * The descriptor of the currently executed refactoring, or
	 * <code>null</code>
	 */
	private RefactoringDescriptor fCurrentDescriptor= null;

	/** The refactoring history listener, or <code>null</code> */
	private IRefactoringHistoryListener fHistoryListener= null;

	/** The refactoring descriptors */
	private final RefactoringDescriptor[] fRefactoringDescriptors;

	/** The status of the session */
	private RefactoringStatus fSessionStatus= new RefactoringStatus();

	/**
	 * Creates a new perform refactoring session operation.
	 * 
	 * @param descriptors
	 *            the refactoring descriptors
	 */
	public PerformRefactoringSessionOperation(final RefactoringDescriptor[] descriptors) {
		Assert.isNotNull(descriptors);
		fRefactoringDescriptors= descriptors;
	}

	/**
	 * Creates a new perform refactoring session operation.
	 * 
	 * @param descriptor
	 *            the refactoring session descriptor
	 */
	public PerformRefactoringSessionOperation(final RefactoringSessionDescriptor descriptor) {
		this(descriptor.getRefactorings());
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
		monitor.beginTask(RefactoringCoreMessages.PerformRefactoringSessionOperation_perform_refactoring_session, fRefactoringDescriptors.length);
		final RefactoringSessionFactory factory= RefactoringSessionFactory.getInstance();
		try {
			fHistoryListener= new RefactoringHistoryListener();
			RefactoringCore.getRefactoringHistory().addHistoryListener(fHistoryListener);
			for (int index= 0; index < fRefactoringDescriptors.length && !fSessionStatus.hasFatalError(); index++) {
				boolean execute= false;
				final RefactoringDescriptor descriptor= fRefactoringDescriptors[index];
				if (!descriptor.isUnknown()) {
					final Refactoring refactoring= factory.createRefactoring(descriptor);
					if (refactoring instanceof IInitializableRefactoringComponent) {
						final IInitializableRefactoringComponent object= (IInitializableRefactoringComponent) refactoring;
						final RefactoringArguments arguments= factory.createArguments(descriptor);
						if (arguments != null) {
							final RefactoringStatus status= object.initialize(arguments);
							if (!status.hasFatalError())
								execute= true;
							else {
								fSessionStatus.merge(status);
								break;
							}
						}
					}
					if (execute) {
						final PerformRefactoringOperation operation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
						ResourcesPlugin.getWorkspace().run(operation, new SubProgressMonitor(monitor, 1));
						if (fCurrentDescriptor != null && !fCurrentDescriptor.isUnknown())
							RefactoringHistory.getInstance().setDependency(fCurrentDescriptor, descriptor);
						fSessionStatus.merge(operation.getConditionStatus());
						if (!fSessionStatus.hasFatalError())
							fSessionStatus.merge(operation.getValidationStatus());
					}
				}
			}
		} finally {
			fCurrentDescriptor= null;
			if (fHistoryListener != null) {
				RefactoringCore.getRefactoringHistory().removeHistoryListener(fHistoryListener);
				fHistoryListener= null;
			}
			monitor.done();
		}
	}
}
