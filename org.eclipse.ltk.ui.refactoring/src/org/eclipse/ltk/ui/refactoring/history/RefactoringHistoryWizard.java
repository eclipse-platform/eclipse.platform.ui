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
package org.eclipse.ltk.ui.refactoring.history;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.IInitializableRefactoringComponent;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringInstanceFactory;
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryErrorPage;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryOverviewPage;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryPreviewPage;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;

import org.eclipse.osgi.util.NLS;

/**
 * A default implementation of a refactoring history wizard. Refactoring history
 * wizards are used to execute the refactorings described by a refactoring
 * history. A refactoring history wizard differs from a normal wizard in the
 * following characteristics:
 * <ul>
 * <li>A refactoring wizard consists of 0 .. n user defined pages, one error
 * page to present the outcome of a refactoring's condition checking and one
 * preview page to present a preview of the workspace changes.</li>
 * <li> Refactorings are applied to the workspace as soon as a preview has been
 * accepted. The execution of a refactoring history triggers a series of error
 * pages and preview pages. Within this sequence of pages, going back is not
 * supported anymore. </li>
 * </ul>
 * <p>
 * A refactoring history wizard is usually opened using the {@link WizardDialog}.
 * </p>
 * <p>
 * Note: this class is intended to be extended by clients.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @see org.eclipse.ltk.core.refactoring.Refactoring
 * @see org.eclipse.ltk.core.refactoring.history.RefactoringHistory
 * 
 * @since 3.2
 */
public class RefactoringHistoryWizard extends Wizard {

	/** Preference key for the prompt skip refactoring preference */
	private static final String PREFERENCE_PROMPT_SKIP_REFACTORING= RefactoringUIPlugin.getPluginId() + ".prompt.skip.refactoring"; //$NON-NLS-1$

	/** The refactoring history control configuration to use */
	private RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The index of the currently executed refactoring */
	private int fCurrentRefactoring= 0;

	/** The refactoring descriptor proxies, or <code>null</code> */
	private RefactoringDescriptorProxy[] fDescriptorProxies= null;

	/** The error wizard page */
	private final RefactoringHistoryErrorPage fErrorPage;

	/** Are we currently in method <code>addPages</code>? */
	private boolean fInAddPages= false;

	/** The description of the overview page */
	private final String fOverviewDescription;

	/** The overview wizard page */
	private RefactoringHistoryOverviewPage fOverviewPage;

	/** The title of the overview page */
	private final String fOverviewTitle;

	/** The preview wizard page */
	private final RefactoringHistoryPreviewPage fPreviewPage;

	/** The refactoring history to execute */
	private RefactoringHistory fRefactoringHistory;

	/**
	 * Creates a new refactoring history wizard.
	 * <p>
	 * Clients must ensure that the refactoring history and the refactoring
	 * history control configuration are set before opening the wizard in a
	 * dialog.
	 * </p>
	 * 
	 * @param caption
	 *            the caption of the wizard window
	 * @param title
	 *            the title of the overview page
	 * @param description
	 *            the description of the overview page
	 */
	public RefactoringHistoryWizard(final String caption, final String title, final String description) {
		Assert.isNotNull(caption);
		Assert.isNotNull(title);
		Assert.isNotNull(description);
		fOverviewTitle= title;
		fOverviewDescription= description;
		fErrorPage= new RefactoringHistoryErrorPage();
		fPreviewPage= new RefactoringHistoryPreviewPage();
		setNeedsProgressMonitor(true);
		setWindowTitle(caption);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Clients must contribute their wizard pages by reimplementing
	 * {@link #addUserDefinedPages()}.
	 */
	public final void addPage(final IWizardPage page) {
		Assert.isTrue(fInAddPages);
		super.addPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	public final void addPages() {
		try {
			fInAddPages= true;
			addUserDefinedPages();
			Assert.isNotNull(fRefactoringHistory);
			Assert.isNotNull(fControlConfiguration);
			fOverviewPage= new RefactoringHistoryOverviewPage(fRefactoringHistory, fOverviewTitle, fOverviewDescription, fControlConfiguration);
			addPage(fOverviewPage);
			addPage(fErrorPage);
			addPage(fPreviewPage);
		} finally {
			fInAddPages= false;
		}
	}

	/**
	 * Adds user defined wizard pages in front of the wizard.
	 */
	protected void addUserDefinedPages() {
		Assert.isTrue(fInAddPages);

		// Do not add any as default
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
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptors();
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
			final IRefactoringHistoryService service= RefactoringCore.getRefactoringHistoryService();
			try {
				service.connect();
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
			} finally {
				service.disconnect();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public final IWizardPage getNextPage(final IWizardPage page) {
		if (page == fOverviewPage) {
			fCurrentRefactoring= 0;
			return getRefactoringPage();
		} else if (page == fPreviewPage) {
			fCurrentRefactoring++;
			return getRefactoringPage();
		} else if (page == fErrorPage) {
			final Refactoring refactoring= fErrorPage.getRefactoring();
			final RefactoringStatus status= fErrorPage.getStatus();
			if (status != null && status.hasFatalError()) {
				final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
				if (store.getBoolean(PREFERENCE_PROMPT_SKIP_REFACTORING))
					MessageDialogWithToggle.openWarning(getShell(), RefactoringUIMessages.ChangeExceptionHandler_refactoring, NLS.bind(RefactoringUIMessages.RefactoringHistoryWizard_fatal_error_message, fErrorPage.getTitle()), RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, store, PREFERENCE_PROMPT_SKIP_REFACTORING);
				fCurrentRefactoring++;
				return getRefactoringPage();
			}
			if (refactoring != null) {
				final IRunnableWithProgress runnable= new IRunnableWithProgress() {

					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						Assert.isNotNull(monitor);
						try {
							fPreviewPage.setChange(refactoring.createChange(monitor));
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
					getContainer().run(false, false, runnable);
				} catch (InvocationTargetException exception) {
					RefactoringUIPlugin.log(exception);
				} catch (InterruptedException exception) {
					// Do nothing
				}
			}
			fPreviewPage.setNextPageEnabled(isNotLastRefactoring());
			return fPreviewPage;
		}
		return super.getNextPage(page);
	}

	/**
	 * {@inheritDoc}
	 */
	public final IWizardPage getPreviousPage(final IWizardPage page) {
		if (page == fErrorPage || page == fPreviewPage)
			return null;
		return super.getPreviousPage(page);
	}

	/**
	 * Returns the refactoring descriptors in their order of execution.
	 * 
	 * @return the refactoring descriptors
	 */
	private RefactoringDescriptorProxy[] getRefactoringDescriptors() {
		if (fDescriptorProxies == null) {
			final RefactoringDescriptorProxy[] proxies= fRefactoringHistory.getDescriptors();
			final RefactoringDescriptorProxy[] result= new RefactoringDescriptorProxy[proxies.length];
			System.arraycopy(proxies, 0, result, 0, proxies.length);
			Arrays.sort(result, new Comparator() {

				public final int compare(final Object first, final Object second) {
					final RefactoringDescriptorProxy predecessor= (RefactoringDescriptorProxy) first;
					final RefactoringDescriptorProxy successor= (RefactoringDescriptorProxy) second;
					return (int) (predecessor.getTimeStamp() - successor.getTimeStamp());
				}
			});
			fDescriptorProxies= result;
		}
		return fDescriptorProxies;
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
					final RefactoringDescriptorProxy descriptor= getCurrentDescriptor();
					fErrorPage.setTitle(descriptor);
					fErrorPage.setLastRefactoring(!isNotLastRefactoring());
					final Refactoring refactoring= getCurrentRefactoring(status, new SubProgressMonitor(monitor, 10));
					if (refactoring != null && status.isOK()) {
						status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 20), CheckConditionsOperation.INITIAL_CONDITONS));
						if (!status.isOK()) {
							fErrorPage.setRefactoring(refactoring);
							fErrorPage.setStatus(status);
							result[0]= fErrorPage;
							return;
						}
						status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 65), CheckConditionsOperation.FINAL_CONDITIONS));
						if (!status.isOK()) {
							fErrorPage.setRefactoring(refactoring);
							fErrorPage.setStatus(status);
							result[0]= fErrorPage;
							return;
						}
						fPreviewPage.setNextPageEnabled(isNotLastRefactoring());
						fPreviewPage.setChange(refactoring.createChange(new SubProgressMonitor(monitor, 5)));
						fPreviewPage.setTitle(descriptor);
						result[0]= fPreviewPage;
					} else {
						fErrorPage.setStatus(status);
						result[0]= fErrorPage;
						return;
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
			getContainer().run(false, false, runnable);
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
		return fCurrentRefactoring < getRefactoringDescriptors().length - 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		return true;
	}

	/**
	 * Sets the refactoring history control configuration.
	 * <p>
	 * This method must be called before opening the wizard in a dialog.
	 * </p>
	 * 
	 * @param configuration
	 *            the configuration to set
	 */
	public final void setControlConfiguration(final RefactoringHistoryControlConfiguration configuration) {
		Assert.isNotNull(configuration);
		fControlConfiguration= configuration;
	}

	/**
	 * Sets the refactoring history.
	 * <p>
	 * This method must be called before opening the wizard in a dialog.
	 * </p>
	 * 
	 * @param history
	 *            the refactoring history
	 */
	public final void setRefactoringHistory(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
	}
}