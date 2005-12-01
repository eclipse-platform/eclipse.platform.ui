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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IInitializableRefactoringComponent;
import org.eclipse.ltk.core.refactoring.IRefactoringInstanceCreator;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformMultipleRefactoringsOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.IErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.IPreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.UIPerformChangeOperation;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryErrorPage;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryOverviewPage;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryPreviewPage;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardContainer;
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
 * Clients must ensure that the calling thread holds the workspace lock.
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

	/** Preference key for the show apply preference */
	private static final String PREFERENCE_DO_NOT_SHOW_APPLY_ERROR= RefactoringUIPlugin.getPluginId() + ".do.not.show.apply.refactoring"; //$NON-NLS-1$;

	/** Preference key for the show skip preference */
	private static final String PREFERENCE_DO_NOT_SHOW_SKIP= RefactoringUIPlugin.getPluginId() + ".do.not.show.skip.refactoring"; //$NON-NLS-1$

	/** Preference key for the warn finish preference */
	private static final String PREFERENCE_DO_NOT_WARN_FINISH= RefactoringUIPlugin.getPluginId() + ".do.not.warn.finish.wizard"; //$NON-NLS-1$;

	/**
	 * The status code representing an interrupted operation.
	 * <p>
	 * This constant is NOT official API. It is used by the refactoring UI
	 * plug-in to recognize a status of interrupted operations.
	 * </p>
	 */
	public static final int STATUS_CODE_INTERRUPTED= 10003;

	/** Has the about to perform history event already been fired? */
	private boolean fAboutToPerformFired= false;

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
	 * Hook method which is called before the first refactoring of the history
	 * is executed. This method may be called from non-UI threads.
	 * <p>
	 * This method is guaranteed to be called exactly once during the lifetime
	 * of a refactoring history wizard. Subclasses may reimplement this method
	 * to perform any special processing.
	 * </p>
	 * <p>
	 * Returning a status of severity {@link RefactoringStatus#FATAL} will
	 * terminate the execution of the refactorings.
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor to use
	 * 
	 * @return a status describing the outcome of the operation
	 */
	protected RefactoringStatus aboutToPerformHistory(final IProgressMonitor monitor) {
		Assert.isNotNull(monitor);
		return new RefactoringStatus();
	}

	/**
	 * Hook method which is called before the a refactoring of the history is
	 * executed. The refactoring itself is in an uninitialized state at the time
	 * of the method call. The default implementation initializes the
	 * refactoring based on the refactoring arguments stored in the descriptor.
	 * This method may be called from non-UI threads.
	 * <p>
	 * Subclasses may extend this method to perform any special processing.
	 * </p>
	 * <p>
	 * Returning a status of severity {@link RefactoringStatus#FATAL} will
	 * terminate the execution of the refactorings.
	 * </p>
	 * 
	 * @param refactoring
	 *            the refactoring about to be executed
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param monitor
	 *            the progress monitor to use
	 * @return a status describing the outcome of the initialization
	 */
	protected RefactoringStatus aboutToPerformRefactoring(final Refactoring refactoring, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) {
		Assert.isNotNull(refactoring);
		Assert.isNotNull(descriptor);
		final RefactoringStatus status= new RefactoringStatus();
		if (refactoring instanceof IInitializableRefactoringComponent) {
			final IInitializableRefactoringComponent component= (IInitializableRefactoringComponent) refactoring;
			final RefactoringArguments arguments= RefactoringCore.getRefactoringInstanceCreator().createArguments(descriptor);
			if (arguments != null)
				status.merge(component.initialize(arguments));
		}
		return status;
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
	 * <p>
	 * Clients may reimplement this method to add custom wizard pages in front
	 * of the wizard.
	 * </p>
	 */
	protected void addUserDefinedPages() {
		Assert.isTrue(fInAddPages);

		// Do not add any as default
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canFinish() {
		return getContainer().getCurrentPage() != fErrorPage;
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
	 * @throws OperationCanceledException
	 *             if the operation has been cancelled
	 * @return the resulting status
	 */
	private RefactoringStatus checkConditions(final Refactoring refactoring, final IProgressMonitor monitor, final int style) throws OperationCanceledException {
		Assert.isNotNull(refactoring);
		Assert.isNotNull(monitor);
		final RefactoringStatus status= new RefactoringStatus();
		try {
			final CheckConditionsOperation operation= new CheckConditionsOperation(refactoring, style);
			operation.run(monitor);
			status.merge(operation.getStatus());
		} catch (CoreException exception) {
			RefactoringUIPlugin.log(exception);
			status.addFatalError(RefactoringUIMessages.RefactoringWizard_internal_error_1);
		}
		return status;
	}

	/**
	 * Creates the change for the specified refactoring.
	 * 
	 * @param refactoring
	 *            the refactoring
	 * @param monitor
	 *            the progress monitor
	 * @return the created change
	 * @throws OperationCanceledException
	 *             if the operation has been cancelled
	 * @throws CoreException
	 *             if an error occurs while creating the change
	 */
	private Change createChange(final Refactoring refactoring, final IProgressMonitor monitor) throws OperationCanceledException, CoreException {
		Assert.isNotNull(refactoring);
		Assert.isNotNull(monitor);
		final CreateChangeOperation operation= new CreateChangeOperation(refactoring);
		operation.run(monitor);
		return operation.getChange();
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		Platform.run(new ISafeRunnable() {

			public void handleException(final Throwable exception) {
				RefactoringUIPlugin.log(exception);
			}

			public final void run() throws Exception {
				if (fAboutToPerformFired) {
					final RefactoringStatusEntry entry= historyPerformed(new NullProgressMonitor()).getEntryWithHighestSeverity();
					if (entry != null)
						RefactoringUIPlugin.log(new Status(entry.getSeverity(), entry.getPluginId(), entry.getCode(), entry.getMessage(), null));
				}
			}
		});
		super.dispose();
	}

	/**
	 * Fires the about to perform history event.
	 * 
	 * @param monitor
	 *            the progress monitor to use
	 * 
	 * @return a status describing the outcome of the operation
	 */
	private RefactoringStatus fireAboutToPerformHistory(final IProgressMonitor monitor) {
		final RefactoringStatus status= new RefactoringStatus();

		Platform.run(new ISafeRunnable() {

			public void handleException(final Throwable exception) {
				status.addFatalError(exception.getLocalizedMessage());
			}

			public final void run() throws Exception {
				status.merge(aboutToPerformHistory(monitor));
			}
		});

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
	 * Returns the refactoring associated with the current refactoring
	 * descriptor, in initialized state.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * 
	 * @param status
	 *            the refactoring status
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring, or <code>null</code>
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring
	 */
	private Refactoring getCurrentRefactoring(final RefactoringDescriptor descriptor, final RefactoringStatus status, final IProgressMonitor monitor) throws CoreException {
		final IRefactoringInstanceCreator factory= RefactoringCore.getRefactoringInstanceCreator();
		final Refactoring refactoring= factory.createRefactoring(descriptor);
		if (refactoring != null) {
			status.merge(aboutToPerformRefactoring(refactoring, descriptor, monitor));
			if (!status.hasFatalError())
				return refactoring;
		}
		return null;
	}

	/**
	 * Returns the error wizard page.
	 * 
	 * @return the error wizard page
	 */
	public final IErrorWizardPage getErrorPage() {
		return fErrorPage;
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
			final RefactoringStatus status= fErrorPage.getStatus();
			final IWizardContainer wizard= getContainer();
			if (status.hasFatalError()) {
				final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
				String message= null;
				String key= null;
				if (!RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error_title.equals(fErrorPage.getTitle())) {
					message= NLS.bind(RefactoringUIMessages.RefactoringHistoryWizard_fatal_error_message, fErrorPage.getTitle());
					key= PREFERENCE_DO_NOT_SHOW_SKIP;
				} else {
					message= RefactoringUIMessages.RefactoringHistoryWizard_error_applying_changes;
					key= PREFERENCE_DO_NOT_SHOW_APPLY_ERROR;
				}
				if (!store.getBoolean(key)) {
					final MessageDialogWithToggle dialog= MessageDialogWithToggle.openWarning(getShell(), wizard.getShell().getText(), message, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null);
					store.setValue(key, dialog.getToggleState());
				}
				fCurrentRefactoring++;
				return getRefactoringPage();
			}
			final Refactoring refactoring= fErrorPage.getRefactoring();
			if (refactoring != null) {
				final IRunnableWithProgress runnable= new IRunnableWithProgress() {

					public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						Assert.isNotNull(monitor);
						try {
							fPreviewPage.setRefactoring(refactoring);
							final Change change= createChange(refactoring, monitor);
							getShell().getDisplay().syncExec(new Runnable() {

								public final void run() {
									fPreviewPage.setChange(change);
								}
							});
						} catch (CoreException exception) {
							throw new InvocationTargetException(exception);
						} catch (OperationCanceledException exception) {
							throw new InterruptedException(exception.getLocalizedMessage());
						} finally {
							monitor.done();
						}
					}
				};
				try {
					wizard.run(true, false, runnable);
				} catch (InvocationTargetException exception) {
					final Throwable throwable= exception.getTargetException();
					if (throwable != null) {
						fErrorPage.setStatus(RefactoringStatus.createFatalErrorStatus(throwable.getLocalizedMessage()));
						return fErrorPage;
					}
				} catch (InterruptedException exception) {
					return fErrorPage;
				}
			} else {
				fPreviewPage.setRefactoring(null);
				fPreviewPage.setChange(null);
			}
			final RefactoringDescriptorProxy descriptor= getCurrentDescriptor();
			if (descriptor != null)
				fPreviewPage.setTitle(descriptor);
			else
				fPreviewPage.setTitle(RefactoringUIMessages.PreviewWizardPage_changes);
			fPreviewPage.setStatus(status);
			fPreviewPage.setNextPageDisabled(isLastRefactoring());
			return fPreviewPage;
		}
		return super.getNextPage(page);
	}

	/**
	 * Returns the preview wizard page.
	 * 
	 * @return the preview wizard page
	 */
	public final IPreviewWizardPage getPreviewPage() {
		return fPreviewPage;
	}

	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getPreviousPage(final IWizardPage page) {
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
	 * Returns the first page of a refactoring.
	 * 
	 * @return the first page, or <code>null</code>
	 */
	private IWizardPage getRefactoringPage() {
		final IWizardPage[] result= { null};
		final RefactoringStatus status= new RefactoringStatus();
		final IWizardContainer wizard= getContainer();
		final IRunnableWithProgress runnable= new IRunnableWithProgress() {

			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Assert.isNotNull(monitor);
				try {
					monitor.beginTask(RefactoringUIMessages.RefactoringHistoryWizard_preparing_refactoring, 220);
					result[0]= null;
					if (!fAboutToPerformFired) {
						try {
							status.merge(fireAboutToPerformHistory(new SubProgressMonitor(monitor, 50, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
						} finally {
							fAboutToPerformFired= true;
						}
					}
					final boolean last= isLastRefactoring();
					final boolean fatal= status.hasFatalError();
					final RefactoringDescriptorProxy proxy= getCurrentDescriptor();
					preparePreviewPage(status, proxy, last);
					prepareErrorPage(status, proxy, fatal, last || fatal);
					fErrorPage.setRefactoring(null);
					if (!status.isOK()) {
						result[0]= fErrorPage;
					} else if (proxy != null) {
						final IRefactoringHistoryService service= RefactoringCore.getRefactoringHistoryService();
						try {
							service.connect();
							final RefactoringDescriptor descriptor= proxy.requestDescriptor(new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							if (descriptor != null && !descriptor.isUnknown()) {
								final Refactoring refactoring= getCurrentRefactoring(descriptor, status, new SubProgressMonitor(monitor, 60, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
								if (refactoring != null && status.isOK()) {
									fPreviewPage.setRefactoring(refactoring);
									fErrorPage.setRefactoring(refactoring);
									status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), CheckConditionsOperation.INITIAL_CONDITONS));
									if (!status.isOK()) {
										prepareErrorPage(status, proxy, fatal, last);
										result[0]= fErrorPage;
									} else {
										status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 65, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), CheckConditionsOperation.FINAL_CONDITIONS));
										if (!status.isOK()) {
											prepareErrorPage(status, proxy, fatal, last);
											result[0]= fErrorPage;
										} else {
											final Change change= createChange(refactoring, new SubProgressMonitor(monitor, 5, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
											getShell().getDisplay().syncExec(new Runnable() {

												public final void run() {
													fPreviewPage.setChange(change);
												}
											});
											result[0]= fPreviewPage;
										}
									}
								}
							} else {
								status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringUIMessages.RefactoringHistoryWizard_error_resolving_refactoring));
								prepareErrorPage(status, proxy, fatal, last);
								result[0]= fErrorPage;
							}
						} finally {
							service.disconnect();
						}
					} else {
						prepareErrorPage(status, proxy, fatal, last);
						result[0]= fErrorPage;
					}
				} catch (CoreException exception) {
					throw new InvocationTargetException(exception);
				} catch (OperationCanceledException exception) {
					throw new InterruptedException(exception.getLocalizedMessage());
				} finally {
					monitor.done();
				}
			}
		};
		try {
			wizard.run(true, false, runnable);
		} catch (InvocationTargetException exception) {
			final Throwable throwable= exception.getTargetException();
			if (throwable != null) {
				fErrorPage.setStatus(RefactoringStatus.createFatalErrorStatus(throwable.getLocalizedMessage()));
				result[0]= fErrorPage;
			}
		} catch (InterruptedException exception) {
			// Stay on same page
			result[0]= null;
		}
		return result[0];
	}

	/**
	 * Hook method which is called when all refactorings of the history have
	 * been executed. This method may be called from non-UI threads.
	 * <p>
	 * This method is guaranteed to be called exactly once during the lifetime
	 * of a refactoring history wizard. It is not guaranteed that the user
	 * interface has not already been disposed of.
	 * </p>
	 * <p>
	 * Subclasses may reimplement this method to perform any special processing.
	 * </p>
	 * 
	 * @param monitor
	 *            the progress monitor to use
	 * 
	 * @return a status describing the outcome of the operation
	 */
	protected RefactoringStatus historyPerformed(final IProgressMonitor monitor) {
		Assert.isNotNull(monitor);
		return new RefactoringStatus();
	}

	/**
	 * Is the current refactoring the last one?
	 * 
	 * @return <code>true</code> if it is the last one, <code>false</code>
	 *         otherwise
	 */
	private boolean isLastRefactoring() {
		return fCurrentRefactoring >= getRefactoringDescriptors().length - 1;
	}

	/**
	 * Is the current refactoring the second last one?
	 * 
	 * @return <code>true</code> if it is the second last one,
	 *         <code>false</code> otherwise
	 */
	private boolean isSecondLastRefactoring() {
		return fCurrentRefactoring >= getRefactoringDescriptors().length - 2;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		final IWizardContainer wizard= getContainer();
		final RefactoringStatus status= new RefactoringStatus();
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptors();
		final List list= new ArrayList(proxies.length);
		for (int index= fCurrentRefactoring; index < proxies.length; index++)
			list.add(proxies[index]);
		final RefactoringDescriptorProxy[] descriptors= new RefactoringDescriptorProxy[list.size()];
		list.toArray(descriptors);
		final boolean last= isLastRefactoring();
		if (!last) {
			final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
			if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_FINISH)) {
				final MessageDialogWithToggle dialog= MessageDialogWithToggle.openWarning(getShell(), wizard.getShell().getText(), RefactoringUIMessages.RefactoringHistoryWizard_warning_finish, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null);
				store.setValue(PREFERENCE_DO_NOT_WARN_FINISH, dialog.getToggleState());
			}
			final PerformMultipleRefactoringsOperation operation= new PerformMultipleRefactoringsOperation(new RefactoringHistoryImplementation(descriptors)) {

				protected RefactoringStatus aboutToPerformRefactoring(final Refactoring refactoring, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) {
					final RefactoringStatus[] result= { new RefactoringStatus()};
					Platform.run(new ISafeRunnable() {

						public void handleException(final Throwable exception) {
							RefactoringUIPlugin.log(exception);
						}

						public final void run() throws Exception {
							result[0]= RefactoringHistoryWizard.this.aboutToPerformRefactoring(refactoring, descriptor, monitor);
						}
					});
					return result[0];
				}

				protected void refactoringPerformed(final Refactoring refactoring, final IProgressMonitor monitor) {
					Platform.run(new ISafeRunnable() {

						public void handleException(final Throwable exception) {
							RefactoringUIPlugin.log(exception);
						}

						public final void run() throws Exception {
							RefactoringHistoryWizard.this.refactoringPerformed(refactoring, monitor);
						}
					});
				}

				public void run(final IProgressMonitor monitor) throws CoreException {
					try {
						monitor.beginTask(RefactoringUIMessages.RefactoringHistoryWizard_preparing_refactorings, 100);
						if (!fAboutToPerformFired) {
							try {
								status.merge(fireAboutToPerformHistory(new SubProgressMonitor(monitor, 20)));
							} finally {
								fAboutToPerformFired= true;
							}
						}
						if (!status.isOK())
							throw new CoreException(new Status(status.getSeverity(), RefactoringUIPlugin.getPluginId(), 0, null, null));
						super.run(new SubProgressMonitor(monitor, 80));
					} finally {
						monitor.done();
					}
				}
			};
			try {
				wizard.run(true, true, new WorkbenchRunnableAdapter(operation, ResourcesPlugin.getWorkspace().getRoot()));
			} catch (InvocationTargetException exception) {
				final Throwable throwable= exception.getTargetException();
				if (throwable != null) {
					final String message= throwable.getLocalizedMessage();
					if (message != null && !"".equals(message)) //$NON-NLS-1$
						status.merge(RefactoringStatus.createFatalErrorStatus(message));
					fErrorPage.setStatus(status);
					fErrorPage.setNextPageDisabled(status.hasFatalError());
					fErrorPage.setTitle(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error_title);
					fErrorPage.setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error);
					wizard.showPage(fErrorPage);
					return false;
				}
			} catch (InterruptedException exception) {
				// Just close wizard
			}
		} else if (wizard.getCurrentPage() == fPreviewPage) {
			final Refactoring refactoring= fPreviewPage.getRefactoring();
			final Change change= fPreviewPage.getChange();
			if (refactoring != null && change != null) {
				status.merge(performPreviewChange(change, refactoring));
				if (!status.isOK()) {
					final RefactoringStatusEntry entry= status.getEntryWithHighestSeverity();
					if (entry.getSeverity() == RefactoringStatus.INFO && entry.getCode() == RefactoringHistoryWizard.STATUS_CODE_INTERRUPTED)
						return false;
					fErrorPage.setStatus(status);
					fErrorPage.setNextPageDisabled(true);
					fErrorPage.setTitle(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error_title);
					fErrorPage.setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_apply_error);
					wizard.showPage(fErrorPage);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Performs the change previously displayed in the preview.
	 * <p>
	 * This method is NOT official API. It is used by the refactoring UI plug-in
	 * to apply changes to the workspace.
	 * </p>
	 * 
	 * @param change
	 *            the change displayed in the preview
	 * @param refactoring
	 *            the associated refactoring
	 * @return the status of the operation, already handled by the user
	 */
	public final RefactoringStatus performPreviewChange(final Change change, final Refactoring refactoring) {
		Assert.isNotNull(change);
		Assert.isNotNull(refactoring);
		final UIPerformChangeOperation operation= new UIPerformChangeOperation(getShell().getDisplay(), change, getContainer()) {

			public void run(final IProgressMonitor monitor) throws CoreException {
				try {
					monitor.beginTask(RefactoringUIMessages.RefactoringHistoryWizard_preparing_changes, 12);
					super.run(new SubProgressMonitor(monitor, 10));
					refactoringPerformed(refactoring, new SubProgressMonitor(monitor, 2));
				} finally {
					monitor.done();
				}
			}
		};
		final RefactoringStatus status= performPreviewChange(operation, refactoring);
		if (status.isOK())
			status.merge(operation.getValidationStatus());
		return status;
	}

	/**
	 * Performs the change previously displayed in the preview using the
	 * specified change operation.
	 * 
	 * @param operation
	 *            the change operation
	 * @param refactoring
	 *            the associated refactoring
	 * @return the status of the operation, already handled by the user
	 */
	private RefactoringStatus performPreviewChange(final PerformChangeOperation operation, final Refactoring refactoring) {
		Assert.isNotNull(operation);
		Assert.isNotNull(refactoring);
		operation.setUndoManager(RefactoringCore.getUndoManager(), refactoring.getName());
		final IWizardContainer wizard= getContainer();
		final Shell shell= wizard.getShell();
		try {
			wizard.run(false, true, new WorkbenchRunnableAdapter(operation, ResourcesPlugin.getWorkspace().getRoot()));
		} catch (InvocationTargetException exception) {
			final Throwable throwable= exception.getTargetException();
			if (operation.changeExecutionFailed()) {
				final Change change= operation.getChange();
				final ChangeExceptionHandler handler= new ChangeExceptionHandler(shell, refactoring);
				if (throwable instanceof RuntimeException)
					handler.handle(change, (RuntimeException) throwable);
				else if (throwable instanceof CoreException)
					handler.handle(change, (CoreException) throwable);
			}
			ExceptionHandler.handle(exception, shell, RefactoringUIMessages.RefactoringWizard_refactoring, RefactoringUIMessages.RefactoringWizard_unexpected_exception_1);
		} catch (InterruptedException exception) {
			return RefactoringStatus.create(new Status(IStatus.INFO, RefactoringUIPlugin.getPluginId(), STATUS_CODE_INTERRUPTED, exception.getLocalizedMessage(), exception));
		} finally {
			fPreviewPage.setNextPageDisabled(isSecondLastRefactoring());
			getContainer().updateButtons();
		}
		return new RefactoringStatus();
	}

	/**
	 * Prepares the error page to be displayed.
	 * 
	 * @param status
	 *            the refactoring status
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param fatal
	 *            <code>true</code> if the error is fatal, <code>false</code>
	 *            otherwise
	 * @param disabled
	 *            <code>true</code> if the next page is disabled,
	 *            <code>false</code> otherwise
	 */
	private void prepareErrorPage(final RefactoringStatus status, final RefactoringDescriptorProxy descriptor, final boolean fatal, final boolean disabled) {
		getShell().getDisplay().syncExec(new Runnable() {

			public final void run() {
				fErrorPage.setTitle(descriptor);
				fErrorPage.setNextPageDisabled(disabled);
				fErrorPage.setPageComplete(!fatal);
				fErrorPage.setStatus(null);
				fErrorPage.setStatus(status);
			}
		});
	}

	/**
	 * Prepares the preview page to be displayed.
	 * 
	 * @param status
	 *            the refactoring status
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param disabled
	 *            <code>true</code> if the next page is disabled,
	 *            <code>false</code> otherwise
	 */
	private void preparePreviewPage(final RefactoringStatus status, final RefactoringDescriptorProxy descriptor, final boolean disabled) {
		getShell().getDisplay().syncExec(new Runnable() {

			public final void run() {
				fPreviewPage.setTitle(descriptor);
				fPreviewPage.setNextPageDisabled(disabled);
				fPreviewPage.setPageComplete(!disabled);
				fPreviewPage.setStatus(status);
			}
		});
	}

	/**
	 * Hook method which is called when the specified refactoring has been
	 * performed. This method may be called from non-UI threads.
	 * <p>
	 * Subclasses may reimplement this method to perform any special processing.
	 * </p>
	 * <p>
	 * Returning a status of severity {@link RefactoringStatus#FATAL} will
	 * terminate the execution of the refactorings.
	 * </p>
	 * 
	 * @param refactoring
	 *            the refactoring which has been performed
	 * @param monitor
	 *            the progress monitor to use
	 * @return a status describing the outcome of the operation
	 */
	protected RefactoringStatus refactoringPerformed(final Refactoring refactoring, final IProgressMonitor monitor) {
		Assert.isNotNull(refactoring);
		Assert.isNotNull(monitor);
		return new RefactoringStatus();
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
	public final void setConfiguration(final RefactoringHistoryControlConfiguration configuration) {
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
	public final void setInput(final RefactoringHistory history) {
		Assert.isNotNull(history);
		fRefactoringHistory= history;
	}
}