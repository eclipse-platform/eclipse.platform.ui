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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.IInitializableRefactoringComponent;
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
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringInstanceFactory;
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
	 * <p>
	 * Clients may extend this method to add custom wizard pages in front of the
	 * wizard.
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
		Assert.isNotNull(status);
		Assert.isNotNull(monitor);
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
					final MessageDialogWithToggle dialog= MessageDialogWithToggle.openWarning(getShell(), getContainer().getShell().getText(), message, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null);
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
							fPreviewPage.setChange(createChange(refactoring, monitor));
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
					getContainer().run(false, false, runnable);
				} catch (InvocationTargetException exception) {
					final Throwable throwable= exception.getTargetException();
					if (throwable != null) {
						RefactoringUIPlugin.log(throwable);
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
		final IWizardPage[] result= { null };
		final IRunnableWithProgress runnable= new IRunnableWithProgress() {

			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				Assert.isNotNull(monitor);
				try {
					monitor.beginTask("", 100); //$NON-NLS-1$
					result[0]= null;
					final RefactoringStatus status= new RefactoringStatus();
					final RefactoringDescriptorProxy descriptor= getCurrentDescriptor();
					if (descriptor != null) {
						final boolean last= isLastRefactoring();
						fPreviewPage.setTitle(descriptor);
						fPreviewPage.setNextPageDisabled(last);
						fPreviewPage.setPageComplete(!last);
						fErrorPage.setTitle(descriptor);
						fErrorPage.setNextPageDisabled(last);
						fErrorPage.setPageComplete(true);
						final Refactoring refactoring= getCurrentRefactoring(status, new SubProgressMonitor(monitor, 10));
						if (refactoring != null && status.isOK()) {
							fPreviewPage.setRefactoring(refactoring);
							fErrorPage.setRefactoring(refactoring);
							status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 20), CheckConditionsOperation.INITIAL_CONDITONS));
							if (!status.isOK())
								result[0]= fErrorPage;
							else {
								status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 65), CheckConditionsOperation.FINAL_CONDITIONS));
								if (!status.isOK())
									result[0]= fErrorPage;
								else {
									fPreviewPage.setChange(createChange(refactoring, new SubProgressMonitor(monitor, 5)));
									result[0]= fPreviewPage;
								}
							}
						} else {
							fErrorPage.setRefactoring(null);
							fErrorPage.setStatus(status);
							result[0]= fErrorPage;
						}
					}
					fPreviewPage.setStatus(status);
					fErrorPage.setStatus(status);
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
			getContainer().run(false, false, runnable);
		} catch (InvocationTargetException exception) {
			final Throwable throwable= exception.getTargetException();
			if (throwable != null) {
				RefactoringUIPlugin.log(throwable);
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
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptors();
		final List list= new ArrayList(proxies.length);
		for (int index= fCurrentRefactoring; index < proxies.length; index++)
			list.add(proxies[index]);
		final RefactoringDescriptorProxy[] descriptors= new RefactoringDescriptorProxy[list.size()];
		list.toArray(descriptors);
		final boolean last= isLastRefactoring();
		final IWizardContainer wizard= getContainer();
		if (!last) {
			final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
			if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_FINISH)) {
				final MessageDialogWithToggle dialog= MessageDialogWithToggle.openWarning(getShell(), wizard.getShell().getText(), RefactoringUIMessages.RefactoringHistoryWizard_warning_finish, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false, null, null);
				store.setValue(PREFERENCE_DO_NOT_WARN_FINISH, dialog.getToggleState());
			}
			final PerformMultipleRefactoringsOperation operation= new PerformMultipleRefactoringsOperation(new RefactoringHistoryImplementation(descriptors));
			try {
				wizard.run(false, true, new WorkbenchRunnableAdapter(operation, ResourcesPlugin.getWorkspace().getRoot()));
			} catch (InvocationTargetException exception) {
				final Throwable throwable= exception.getTargetException();
				if (throwable != null) {
					RefactoringUIPlugin.log(throwable);
					fErrorPage.setStatus(RefactoringStatus.createFatalErrorStatus(throwable.getLocalizedMessage()));
					fErrorPage.setNextPageDisabled(true);
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
				final RefactoringStatus status= performPreviewChange(change, refactoring);
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
		final UIPerformChangeOperation operation= new UIPerformChangeOperation(getShell().getDisplay(), change, getContainer());
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