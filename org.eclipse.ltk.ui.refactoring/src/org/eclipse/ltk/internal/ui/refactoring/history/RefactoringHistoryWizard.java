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
package org.eclipse.ltk.internal.ui.refactoring.history;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.IInitializableRefactoringComponent;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringInstanceFactory;
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ltk.ui.refactoring.history.RefactoringHistoryControlConfiguration;

/**
 * Wizard to execute the refactorings of a refactoring history sequentially.
 * 
 * @since 3.2
 */
public final class RefactoringHistoryWizard extends Wizard {

	/** The refactoring history control configuration to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The index of the currently executed refactoring */
	private int fCurrentRefactoring= 0;

	/** The error wizard page */
	private final RefactoringHistoryErrorPage fErrorPage;

	/** Are we currently in method <code>addPages</code>? */
	private boolean fInAddPages= false;

	/** The overview wizard page */
	private final RefactoringHistoryOverviewPage fOverviewPage;

	/** The preview wizard page */
	private final RefactoringHistoryPreviewPage fPreviewPage;

	/** The refactoring history to execute */
	private final RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new refactoring history wizard.
	 * 
	 * @param history
	 *            the non-empty refactoring history to execute
	 * @param configuration
	 *            the refactoring history control configuration to use
	 */
	public RefactoringHistoryWizard(final RefactoringHistory history, final RefactoringHistoryControlConfiguration configuration) {
		Assert.isNotNull(history);
		Assert.isTrue(!history.isEmpty());
		Assert.isNotNull(configuration);
		fRefactoringHistory= history;
		fControlConfiguration= configuration;
		setNeedsProgressMonitor(true);
		setWindowTitle(RefactoringUIMessages.RefactoringWizard_title);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
		fOverviewPage= new RefactoringHistoryOverviewPage(fRefactoringHistory, fControlConfiguration);
		fErrorPage= new RefactoringHistoryErrorPage();
		fPreviewPage= new RefactoringHistoryPreviewPage();
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addPage(final IWizardPage page) {
		Assert.isTrue(fInAddPages);
		super.addPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addPages() {
		Assert.isNotNull(fRefactoringHistory);
		try {
			fInAddPages= true;
			addPage(fOverviewPage);
			addPage(fErrorPage);
			addPage(fPreviewPage);
		} finally {
			fInAddPages= false;
		}
	}

	/**
	 * Checks the specified kind of conditions of the refactoring.
	 * 
	 * @param refactoring
	 *            the refactoring to check its conditions
	 * @param monitor
	 *            the progress monitor to use
	 * @param style
	 *            the condition checking style
	 * @return the resulting status
	 */
	private RefactoringStatus checkConditions(final Refactoring refactoring, final IProgressMonitor monitor, final int style) {
		RefactoringStatus status= new RefactoringStatus();
		try {
			final CheckConditionsOperation operation= new CheckConditionsOperation(refactoring, style);
			operation.run(monitor);
			status= operation.getStatus();
		} catch (CoreException exception) {
			RefactoringUIPlugin.log(exception);
			status.addFatalError(RefactoringUIMessages.RefactoringWizard_internal_error_1);
		}
		return status;
	}

	/**
	 * Returns the refactoring descriptor of the current refactoring.
	 * 
	 * @return the refactoring descriptor, or <code>null</code>
	 */
	private RefactoringDescriptorProxy getCurrentDescriptor() {
		final RefactoringDescriptorProxy[] proxies= fRefactoringHistory.getDescriptors();
		if (fCurrentRefactoring >= 0 && fCurrentRefactoring < proxies.length)
			return proxies[fCurrentRefactoring];
		return null;
	}

	/**
	 * Returns the current refactoring, in initialized state.
	 * 
	 * @param status
	 *            the refactoring status
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring
	 */
	private Refactoring getCurrentRefactoring(final RefactoringStatus status, final IProgressMonitor monitor) throws CoreException {
		final RefactoringDescriptorProxy proxy= getCurrentDescriptor();
		if (proxy != null) {
			final RefactoringDescriptor descriptor= proxy.requestDescriptor(monitor);
			if (descriptor != null && !descriptor.isUnknown()) {
				final RefactoringInstanceFactory factory= RefactoringInstanceFactory.getInstance();
				final Refactoring refactoring= factory.createRefactoring(descriptor);
				if (refactoring instanceof IInitializableRefactoringComponent) {
					final IInitializableRefactoringComponent component= (IInitializableRefactoringComponent) refactoring;
					final RefactoringArguments arguments= factory.createArguments(descriptor);
					if (arguments != null) {
						status.merge(component.initialize(arguments));
						if (!status.hasFatalError())
							return refactoring;
					}
				}
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getNextPage(final IWizardPage page) {
		if (page == fOverviewPage) {
			fCurrentRefactoring= 0;
			return getRefactoringPage();
		} else if (page == fPreviewPage) {
			fCurrentRefactoring++;
			return getRefactoringPage();
		} else if (page == fErrorPage) {
			fPreviewPage.setNextPageEnabled(isNotLastRefactoring());
			return fPreviewPage;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IWizardPage getPreviousPage(final IWizardPage page) {
		return null;
	}

	/**
	 * Returns the refactoring history.
	 * 
	 * @return the refactoring history
	 */
	public final RefactoringHistory getRefactoringHistory() {
		return fRefactoringHistory;
	}

	/**
	 * Returns the first page of a refactoring.
	 * 
	 * @return the first page, or <code>null</code>
	 */
	private IWizardPage getRefactoringPage() {
		final IWizardPage[] result= { null};
		final IRunnableWithProgress runnable= new IRunnableWithProgress() {

			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Assert.isNotNull(monitor);
				try {
					monitor.beginTask("", 100); //$NON-NLS-1$
					result[0]= null;
					final RefactoringStatus status= new RefactoringStatus();
					final Refactoring refactoring= getCurrentRefactoring(status, new SubProgressMonitor(monitor, 10));
					if (refactoring != null && status.isOK()) {
						final RefactoringDescriptorProxy descriptor= getCurrentDescriptor();
						status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 20), CheckConditionsOperation.INITIAL_CONDITONS));
						if (!status.isOK()) {
							fErrorPage.setStatus(status);
							fErrorPage.setTitle(descriptor);
							result[0]= fErrorPage;
							return;
						}
						status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 65), CheckConditionsOperation.FINAL_CONDITIONS));
						if (!status.isOK()) {
							fErrorPage.setStatus(status);
							fErrorPage.setTitle(descriptor);
							result[0]= fErrorPage;
							return;
						}
						fPreviewPage.setNextPageEnabled(isNotLastRefactoring());
						fPreviewPage.setChange(refactoring.createChange(new SubProgressMonitor(monitor, 5)));
						fPreviewPage.setTitle(descriptor);
						result[0]= fPreviewPage;
					}
				} catch (CoreException exception) {
					throw new InvocationTargetException(exception);
				} catch (OperationCanceledException exception) {
					// Do nothing
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(false, true, runnable);
		} catch (InvocationTargetException exception) {
			RefactoringUIPlugin.log(exception);
		} catch (InterruptedException exception) {
			// Do nothing
		}
		return result[0];
	}

	/**
	 * Returns whether the current refactoring is not the last one.
	 * 
	 * @return <code>true</code> if it is not the last one, <code>false</code>
	 *         otherwise
	 */
	private boolean isNotLastRefactoring() {
		return fCurrentRefactoring < fRefactoringHistory.getDescriptors().length - 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		return true;
	}
}