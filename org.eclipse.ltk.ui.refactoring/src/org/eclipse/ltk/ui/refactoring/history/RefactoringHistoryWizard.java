/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import com.ibm.icu.text.MessageFormat;

import java.lang.reflect.InvocationTargetException;
import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringHistoryOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.history.IRefactoringHistoryService;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryImplementation;
import org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.IErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.IPreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.IRefactoringHelpContextIds;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringHistoryPreviewPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPreviewChangeFilter;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringStatusEntryFilter;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.UIPerformChangeOperation;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryErrorPage;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryOverviewPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.ui.PlatformUI;

/**
 * A default implementation of a refactoring history wizard. Refactoring history
 * wizards are used to execute the refactorings described by a refactoring
 * history. A refactoring history wizard differs from a normal wizard in the
 * following characteristics:
 * <ul>
 * <li>A refactoring history wizard consists of a sequence of one error page to
 * present the outcome of a refactoring's condition checking and one preview
 * page to present a preview of the workspace changes.</li>
 * <li> Refactorings are applied to the workspace as soon as a preview has been
 * accepted. Additionally, refactoring history wizards support the headless
 * execution of refactorings. The user guided execution of a refactoring history
 * triggers a series of error pages and preview pages. Within this sequence of
 * pages, going back is not supported anymore. However, canceling the
 * refactoring history wizard will undo the already performed refactorings.</li>
 * </ul>
 * <p>
 * A refactoring history wizard is usually opened using the {@link WizardDialog}.
 * Clients must ensure that the calling thread holds the workspace lock.
 * </p>
 * <p>
 * Note: this class is intended to be extended by clients.
 * </p>
 * 
 * @see org.eclipse.ltk.core.refactoring.Refactoring
 * @see org.eclipse.ltk.core.refactoring.history.RefactoringHistory
 * 
 * @since 3.2
 */
public class RefactoringHistoryWizard extends Wizard {

	/** The no overview wizard page */
	private final class NoOverviewWizardPage extends WizardPage {

		/** The no overview wizard page name */
		private static final String PAGE_NAME= "NoOverviewWizardPage"; //$NON-NLS-1$

		/**
		 * Creates a new no overview wizard page.
		 */
		private NoOverviewWizardPage() {
			super(PAGE_NAME);
			final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptors();
			if (proxies.length > 0)
				setTitle(proxies[0], 1, proxies.length);
			else
				setTitle(fOverviewTitle);
			setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_description);
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean canFlipToNextPage() {
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		public void createControl(final Composite parent) {
			final Composite composite= new Composite(parent, SWT.NULL);
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
			setControl(composite);
			Dialog.applyDialogFont(composite);
			PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IRefactoringHelpContextIds.REFACTORING_PREVIEW_WIZARD_PAGE);
		}

		/**
		 * {@inheritDoc}
		 */
		public IWizardPage getNextPage() {
			return getWizard().getNextPage(this);
		}

		/**
		 * {@inheritDoc}
		 */
		public IWizardPage getPreviousPage() {
			return getWizard().getPreviousPage(this);
		}

		/**
		 * {@inheritDoc}
		 */
		public void setPageComplete(final boolean complete) {
			super.setPageComplete(true);
		}

		/**
		 * Sets the title of the page according to the refactoring.
		 * 
		 * @param descriptor
		 *            the refactoring descriptor, or <code>null</code>
		 * @param current
		 *            the non-zero based index of the current refactoring
		 * @param total
		 *            the total number of refactorings
		 */
		public void setTitle(final RefactoringDescriptorProxy descriptor, final int current, final int total) {
			final String message;
			if (descriptor != null)
				message= descriptor.getDescription();
			else
				message= RefactoringUIMessages.RefactoringHistoryOverviewPage_title;
			if (total > 1)
				setTitle(Messages.format(RefactoringUIMessages.RefactoringHistoryPreviewPage_refactoring_pattern, new String[] { message, String.valueOf(current + 1), String.valueOf(total)}));
			else
				setTitle(message);
		}
	}

	/** Preference key for the show apply preference */
	private static final String PREFERENCE_DO_NOT_SHOW_APPLY_ERROR= RefactoringUIPlugin.getPluginId() + ".do.not.show.apply.refactoring"; //$NON-NLS-1$;

	/** Preference key for the show skip preference */
	private static final String PREFERENCE_DO_NOT_SHOW_SKIP= RefactoringUIPlugin.getPluginId() + ".do.not.show.skip.refactoring"; //$NON-NLS-1$

	/** Preference key for the warn finish preference */
	private static final String PREFERENCE_DO_NOT_WARN_FINISH= RefactoringUIPlugin.getPluginId() + ".do.not.warn.finish.wizard"; //$NON-NLS-1$;

	/** Preference key for the warn undo on cancel preference */
	private static final String PREFERENCE_DO_NOT_WARN_UNDO_ON_CANCEL= RefactoringUIPlugin.getPluginId() + ".do.not.warn.undo.on.cancel.refactoring"; //$NON-NLS-1$;

	/**
	 * The status code representing an interrupted operation.
	 * <p>
	 * Note: This API must not be used from outside the refactoring framework.
	 * </p>
	 */
	public static final int STATUS_CODE_INTERRUPTED= 10003;

	/**
	 * Converts a button label to pure text.
	 * 
	 * @param label
	 *            the button label
	 * @return the resulting text
	 */
	private static String getLabelAsText(final String label) {
		Assert.isNotNull(label);
		StringBuffer buffer= new StringBuffer(label.length());
		for (int index= 0; index < label.length(); index++) {
			char character= label.charAt(index);
			if (character != '&')
				buffer.append(character);
		}
		return buffer.toString();
	}

	/** Has the about to perform history event already been fired? */
	private boolean fAboutToPerformFired= false;

	/** Has an exception occurred while cancelling the wizard? */
	private boolean fCancelException= false;

	/** The refactoring history control configuration to use */
	private RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The index of the currently executed refactoring */
	private int fCurrentRefactoring= 0;

	/**
	 * The refactoring descriptor proxies, in ascending order of their time
	 * stamps, or <code>null</code>
	 */
	private RefactoringDescriptorProxy[] fDescriptorProxies= null;

	/** The error wizard page */
	private final RefactoringHistoryErrorPage fErrorPage;

	/** The number of successfully executed refactorings */
	private int fExecutedRefactorings= 0;

	/** Can the wizard be finished after fatal errors occurred in headless mode? */
	private boolean fHeadlessErrorStatus= false;

	/** Are we currently in method <code>addPages</code>? */
	private boolean fInAddPages= false;

	/**
	 * The no overview wizard page, or <code>null</code> if an overview is
	 * desired
	 */
	private NoOverviewWizardPage fNoOverviewPage;

	/** The description of the overview page */
	private final String fOverviewDescription;

	/**
	 * The overview wizard page, or <code>null</code> if no overview is
	 * desired
	 */
	private RefactoringHistoryOverviewPage fOverviewPage;

	/** The title of the overview page */
	private final String fOverviewTitle;

	/** The preview change filter */
	private RefactoringPreviewChangeFilter fPreviewChangeFilter= new RefactoringPreviewChangeFilter() {

		/**
		 * {@inheritDoc}
		 */
		public final boolean select(final Change change) {
			return selectPreviewChange(change);
		}
	};

	/** The preview wizard page */
	private final RefactoringHistoryPreviewPage fPreviewPage;

	/** The refactoring history to execute */
	private RefactoringHistory fRefactoringHistory;

	/** Does the wizard show an overview of the refactorings? */
	private final boolean fShowOverview;

	/** The status entry filter */
	private RefactoringStatusEntryFilter fStatusEntryFilter= new RefactoringStatusEntryFilter() {

		/**
		 * {@inheritDoc}
		 */
		public final boolean select(final RefactoringStatusEntry entry) {
			return selectStatusEntry(entry);
		}
	};

	/**
	 * Creates a new refactoring history wizard.
	 * <p>
	 * Clients must ensure that the refactoring history and the refactoring
	 * history control configuration are set before opening the wizard in a
	 * dialog.
	 * </p>
	 * 
	 * @param overview
	 *            <code>true</code> to show an overview of the refactorings,
	 *            <code>false</code> otherwise
	 * @param caption
	 *            the caption of the wizard window
	 * @param title
	 *            the title of the overview page
	 * @param description
	 *            the description of the overview page
	 * 
	 * @see #setConfiguration(RefactoringHistoryControlConfiguration)
	 * @see #setInput(RefactoringHistory)
	 */
	public RefactoringHistoryWizard(final boolean overview, final String caption, final String title, final String description) {
		Assert.isNotNull(caption);
		Assert.isNotNull(title);
		Assert.isNotNull(description);
		fShowOverview= overview;
		fOverviewTitle= title;
		fOverviewDescription= description;
		fErrorPage= new RefactoringHistoryErrorPage();
		fErrorPage.setFilter(fStatusEntryFilter);
		fPreviewPage= new RefactoringHistoryPreviewPage();
		fPreviewPage.setFilter(fPreviewChangeFilter);
		setNeedsProgressMonitor(true);
		setWindowTitle(caption);
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	}

	/**
	 * Creates a new refactoring history wizard.
	 * <p>
	 * Clients must ensure that the refactoring history and the refactoring
	 * history control configuration are set before opening the wizard in a
	 * dialog.
	 * </p>
	 * <p>
	 * Calling his constructor is equivalent to
	 * {@link #RefactoringHistoryWizard(boolean, String, String, String)} with
	 * the first argument equal to <code>true</code>.
	 * </p>
	 * 
	 * @param caption
	 *            the caption of the wizard window
	 * @param title
	 *            the title of the overview page
	 * @param description
	 *            the description of the overview page
	 * 
	 * @see #setConfiguration(RefactoringHistoryControlConfiguration)
	 * @see #setInput(RefactoringHistory)
	 */
	public RefactoringHistoryWizard(final String caption, final String title, final String description) {
		this(true, caption, title, description);
	}

	/**
	 * Hook method which is called before the first refactoring of the history
	 * is executed. This method may be called from non-UI threads.
	 * <p>
	 * This method is guaranteed to be called exactly once during the lifetime
	 * of a refactoring history wizard. The default implementation does nothing
	 * and returns a refactoring status of severity {@link RefactoringStatus#OK}.
	 * </p>
	 * <p>
	 * Subclasses may reimplement this method to perform any special processing.
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
		fExecutedRefactorings= 0;
		return new RefactoringStatus();
	}

	/**
	 * Hook method which is called before the a refactoring of the history is
	 * executed. The refactoring itself is in an initialized state at the time
	 * of the method call. The default implementation does nothing and returns a
	 * status of severity {@link RefactoringStatus#OK}. This method may be
	 * called from non-UI threads.
	 * <p>
	 * Subclasses may extend this method to perform any special processing.
	 * </p>
	 * <p>
	 * Returning a status of severity {@link RefactoringStatus#FATAL} will
	 * terminate the execution of the current refactoring.
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
		return new RefactoringStatus();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Clients must contribute their wizard pages by re-implementing
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
			if (fShowOverview) {
				fOverviewPage= new RefactoringHistoryOverviewPage(fRefactoringHistory, fOverviewTitle, fOverviewDescription, fControlConfiguration);
				addPage(fOverviewPage);
			} else {
				fNoOverviewPage= new NoOverviewWizardPage();
				addPage(fNoOverviewPage);
			}
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
		final IWizardPage page= getContainer().getCurrentPage();
		if (page == fErrorPage) {
			if (fHeadlessErrorStatus)
				return true;
			final RefactoringStatus status= fErrorPage.getStatus();
			final boolean fatal= status != null && status.hasFatalError();
			if (isLastRefactoring() && fDescriptorProxies.length > 1) {
				if (fatal)
					fHeadlessErrorStatus= true;
				return true;
			}
			return !fatal;
		}
		return true;
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
	 * Method which is called to create a refactoring instance from a
	 * refactoring descriptor. The refactoring must be in an initialized state
	 * after the return of the method call. The default implementation delegates
	 * the task to the refactoring descriptor. This method may be called from
	 * non-UI threads.
	 * <p>
	 * Subclasses may reimplement this method to customize the initialization of
	 * a refactoring.
	 * </p>
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param status
	 *            a refactoring status describing the outcome of the
	 *            initialization
	 * @return the refactoring, or <code>null</code> if this refactoring
	 *         descriptor represents the unknown refactoring, or if no
	 *         refactoring contribution is available for this refactoring
	 *         descriptor
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring instance
	 */
	protected Refactoring createRefactoring(final RefactoringDescriptor descriptor, final RefactoringStatus status) throws CoreException {
		Assert.isNotNull(descriptor);
		return descriptor.createRefactoring(status);
	}

	/**
	 * Creates a refactoring from the specified refactoring descriptor.
	 * 
	 * @param descriptor
	 *            the refactoring descriptor
	 * @param status
	 *            the refactoring status
	 * @param monitor
	 *            the progress monitor to use
	 * @return the refactoring, or <code>null</code> if this refactoring
	 *         descriptor represents the unknown refactoring, or if no
	 *         refactoring contribution is available for this refactoring
	 *         descriptor
	 * @throws CoreException
	 *             if an error occurs while creating the refactoring instance
	 */
	private Refactoring createRefactoring(final RefactoringDescriptor descriptor, final RefactoringStatus status, final IProgressMonitor monitor) throws CoreException {
		final Refactoring refactoring= createRefactoring(descriptor, status);
		if (refactoring != null) {
			status.merge(aboutToPerformRefactoring(refactoring, descriptor, monitor));
			if (!status.hasFatalError())
				return refactoring;
		} else
			status.addFatalError(Messages.format(RefactoringUIMessages.RefactoringHistoryWizard_error_instantiate_refactoring, descriptor.getDescription()));
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		SafeRunner.run(new ISafeRunnable() {

			public void handleException(final Throwable exception) {
				RefactoringUIPlugin.log(exception);
			}

			public final void run() throws Exception {
				if (fAboutToPerformFired) {
					final RefactoringStatusEntry entry= historyPerformed(new NullProgressMonitor()).getEntryWithHighestSeverity();
					if (entry != null)
						RefactoringUIPlugin.log(entry.toStatus());
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
		SafeRunner.run(new ISafeRunnable() {

			public void handleException(final Throwable exception) {
				RefactoringUIPlugin.log(exception);
				status.addFatalError(RefactoringUIMessages.RefactoringWizard_unexpected_exception_1);
			}

			public final void run() throws Exception {
				status.merge(aboutToPerformHistory(monitor));
			}
		});
		return status;
	}

	/**
	 * Returns the error wizard page.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
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
		if (page == fOverviewPage || page == fNoOverviewPage) {
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
					message= Messages.format(RefactoringUIMessages.RefactoringHistoryWizard_fatal_error_message, fErrorPage.getTitle());
					key= PREFERENCE_DO_NOT_SHOW_SKIP;
				} else {
					message= RefactoringUIMessages.RefactoringHistoryWizard_error_applying_changes;
					key= PREFERENCE_DO_NOT_SHOW_APPLY_ERROR;
				}
				if (!store.getBoolean(key)) {
					final MessageDialogWithToggle dialog= new MessageDialogWithToggle(getShell(), wizard.getShell().getText(), null, message, MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false);
					dialog.open();
					store.setValue(key, dialog.getToggleState());
					if (dialog.getReturnCode() == 1)
						return null;
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
						RefactoringUIPlugin.log(exception);
						fErrorPage.setStatus(RefactoringStatus.createFatalErrorStatus(RefactoringUIMessages.RefactoringWizard_unexpected_exception_1));
						return fErrorPage;
					}
				} catch (InterruptedException exception) {
					return fErrorPage;
				}
			} else {
				fPreviewPage.setRefactoring(null);
				fPreviewPage.setChange(null);
			}
			final RefactoringDescriptorProxy descriptor= getRefactoringDescriptor();
			if (descriptor != null)
				fPreviewPage.setTitle(descriptor, fCurrentRefactoring, fDescriptorProxies.length);
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
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
	 * </p>
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
	 * Returns the refactoring descriptor of the current refactoring.
	 * 
	 * @return the refactoring descriptor, or <code>null</code>
	 */
	private RefactoringDescriptorProxy getRefactoringDescriptor() {
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptors();
		if (fCurrentRefactoring >= 0 && fCurrentRefactoring < proxies.length)
			return proxies[fCurrentRefactoring];
		return null;
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
					final long delta= predecessor.getTimeStamp() - successor.getTimeStamp();
					if (delta > 0)
						return 1;
					else if (delta < 0)
						return -1;
					return 0;
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
					final RefactoringDescriptorProxy proxy= getRefactoringDescriptor();
					preparePreviewPage(status, proxy, last);
					prepareErrorPage(status, proxy, status.hasFatalError(), last || status.hasFatalError());
					fErrorPage.setRefactoring(null);
					if (!status.isOK()) {
						result[0]= fErrorPage;
					} else if (proxy != null) {
						final IRefactoringHistoryService service= RefactoringCore.getHistoryService();
						try {
							service.connect();
							final RefactoringDescriptor descriptor= proxy.requestDescriptor(new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
							if (descriptor != null) {
								final Refactoring refactoring= createRefactoring(descriptor, status, new SubProgressMonitor(monitor, 60, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
								if (refactoring != null && status.isOK()) {
									fPreviewPage.setRefactoring(refactoring);
									fErrorPage.setRefactoring(refactoring);
									status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), CheckConditionsOperation.INITIAL_CONDITONS));
									if (!status.isOK()) {
										prepareErrorPage(status, proxy, status.hasFatalError(), last);
										result[0]= fErrorPage;
									} else {
										status.merge(checkConditions(refactoring, new SubProgressMonitor(monitor, 65, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), CheckConditionsOperation.FINAL_CONDITIONS));
										if (!status.isOK()) {
											prepareErrorPage(status, proxy, status.hasFatalError(), last);
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
								} else {
									prepareErrorPage(status, proxy, status.hasFatalError(), last);
									result[0]= fErrorPage;
								}
							} else {
								status.merge(RefactoringStatus.createFatalErrorStatus(RefactoringUIMessages.RefactoringHistoryWizard_error_resolving_refactoring));
								prepareErrorPage(status, proxy, status.hasFatalError(), last);
								result[0]= fErrorPage;
							}
						} finally {
							service.disconnect();
						}
					} else {
						prepareErrorPage(status, proxy, status.hasFatalError(), last);
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
			RefactoringUIPlugin.log(exception);
			final Throwable throwable= exception.getTargetException();
			if (throwable != null) {
				fErrorPage.setNextPageDisabled(isLastRefactoring());
				fErrorPage.setStatus(RefactoringStatus.createFatalErrorStatus(RefactoringUIMessages.RefactoringWizard_unexpected_exception_1));
				result[0]= fErrorPage;
			}
		} catch (InterruptedException exception) {
			// Stay on same page
			result[0]= null;
		}
		getContainer().updateButtons();
		return result[0];
	}

	/**
	 * Hook method which is called when all refactorings of the history have
	 * been executed. This method may be called from non-UI threads.
	 * <p>
	 * This method is guaranteed to be called exactly once during the lifetime
	 * of a refactoring history wizard. It is not guaranteed that the user
	 * interface has not already been disposed of. The default implementation
	 * does nothing and returns a refactoring status of severity
	 * {@link RefactoringStatus#OK}.
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
	public boolean performCancel() {
		if (fExecutedRefactorings > 0 && !fCancelException) {
			final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
			if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_UNDO_ON_CANCEL)) {
				final MessageFormat format= new MessageFormat(RefactoringUIMessages.RefactoringHistoryWizard_undo_message_pattern);
				final String message= RefactoringUIMessages.RefactoringHistoryWizard_undo_message_explanation;
				final String[] messages= { RefactoringUIMessages.RefactoringHistoryWizard_one_refactoring_undone + message, RefactoringUIMessages.RefactoringHistoryWizard_several_refactorings_undone + message};
				final ChoiceFormat choice= new ChoiceFormat(new double[] { 1, Double.MAX_VALUE}, messages);
				format.setFormatByArgumentIndex(0, choice);
				final MessageDialogWithToggle dialog= new MessageDialogWithToggle(getShell(), getShell().getText(), null, format.format(new Object[] { new Integer(fExecutedRefactorings)}), MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false);
				dialog.open();
				store.setValue(PREFERENCE_DO_NOT_WARN_UNDO_ON_CANCEL, dialog.getToggleState());
				if (dialog.getReturnCode() == 1)
					return false;
			}
			final IRunnableWithProgress runnable= new IRunnableWithProgress() {

				public final void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					for (int index= 0; index < fExecutedRefactorings; index++) {
						try {
							RefactoringCore.getUndoManager().performUndo(null, new SubProgressMonitor(monitor, 100));
							if (fExecutedRefactorings > 0)
								fExecutedRefactorings--;
						} catch (CoreException exception) {
							throw new InvocationTargetException(exception);
						}
					}
				}
			};
			try {
				getContainer().run(false, false, runnable);
			} catch (InvocationTargetException exception) {
				RefactoringUIPlugin.log(exception);
				fCancelException= true;
				fErrorPage.setStatus(RefactoringStatus.createFatalErrorStatus(RefactoringUIMessages.RefactoringHistoryWizard_internal_error));
				fErrorPage.setNextPageDisabled(true);
				fErrorPage.setTitle(RefactoringUIMessages.RefactoringHistoryWizard_internal_error_title);
				fErrorPage.setDescription(RefactoringUIMessages.RefactoringHistoryWizard_internal_error_description);
				getContainer().showPage(fErrorPage);
				return false;
			} catch (InterruptedException exception) {
				// Does not happen
			}
		}
		return super.performCancel();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean performFinish() {
		if (fHeadlessErrorStatus)
			return true;
		if (fOverviewPage != null)
			fOverviewPage.performFinish();
		final IWizardContainer wizard= getContainer();
		final RefactoringStatus status= new RefactoringStatus();
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptors();
		final List list= new ArrayList(proxies.length);
		for (int index= fCurrentRefactoring; index < proxies.length; index++)
			list.add(proxies[index]);
		final RefactoringDescriptorProxy[] descriptors= new RefactoringDescriptorProxy[list.size()];
		list.toArray(descriptors);
		final boolean last= isLastRefactoring();
		if (wizard.getCurrentPage() == fPreviewPage && last) {
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
		} else {
			final IPreferenceStore store= RefactoringUIPlugin.getDefault().getPreferenceStore();
			if (!store.getBoolean(PREFERENCE_DO_NOT_WARN_FINISH) && proxies.length > 0) {
				final MessageDialogWithToggle dialog= new MessageDialogWithToggle(getShell(), wizard.getShell().getText(), null, Messages.format(RefactoringUIMessages.RefactoringHistoryWizard_warning_finish, getLabelAsText(IDialogConstants.FINISH_LABEL)), MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0, RefactoringUIMessages.RefactoringHistoryWizard_do_not_show_message, false);
				dialog.open();
				store.setValue(PREFERENCE_DO_NOT_WARN_FINISH, dialog.getToggleState());
				if (dialog.getReturnCode() == IDialogConstants.CANCEL_ID)
					return false;
			}
			final PerformRefactoringHistoryOperation operation= new PerformRefactoringHistoryOperation(new RefactoringHistoryImplementation(descriptors)) {

				protected RefactoringStatus aboutToPerformRefactoring(final Refactoring refactoring, final RefactoringDescriptor descriptor, final IProgressMonitor monitor) {
					final RefactoringStatus[] result= { new RefactoringStatus()};
					SafeRunner.run(new ISafeRunnable() {

						public void handleException(final Throwable exception) {
							RefactoringUIPlugin.log(exception);
						}

						public final void run() throws Exception {
							result[0]= RefactoringHistoryWizard.this.aboutToPerformRefactoring(refactoring, descriptor, monitor);
						}
					});
					return result[0];
				}

				protected Refactoring createRefactoring(final RefactoringDescriptor descriptor, final RefactoringStatus state) throws CoreException {
					return RefactoringHistoryWizard.this.createRefactoring(descriptor, state);
				}

				protected void refactoringPerformed(final Refactoring refactoring, final IProgressMonitor monitor) {
					SafeRunner.run(new ISafeRunnable() {

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
								status.merge(fireAboutToPerformHistory(new SubProgressMonitor(monitor, 20, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL)));
							} finally {
								fAboutToPerformFired= true;
							}
						}
						if (!status.isOK()) {
							final int severity= status.getSeverity();
							throw new CoreException(new Status(severity != RefactoringStatus.FATAL ? severity : IStatus.ERROR, RefactoringUIPlugin.getPluginId(), 0, null, null));
						}
						super.run(new SubProgressMonitor(monitor, 80, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
					} finally {
						monitor.done();
					}
				}
			};
			try {
				wizard.run(false, false, new WorkbenchRunnableAdapter(operation, ResourcesPlugin.getWorkspace().getRoot()));
			} catch (InvocationTargetException exception) {
				RefactoringUIPlugin.log(exception);
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
				// Does not happen
			}
			final RefactoringStatus result= operation.getExecutionStatus();
			if (!result.isOK()) {
				fHeadlessErrorStatus= true;
				fErrorPage.setStatus(result);
				fErrorPage.setNextPageDisabled(true);
				fErrorPage.setTitle(RefactoringUIMessages.RefactoringHistoryPreviewPage_finish_error_title);
				fErrorPage.setDescription(RefactoringUIMessages.RefactoringHistoryPreviewPage_finish_error_description);
				wizard.showPage(fErrorPage);
				return false;
			}
		}
		return true;
	}

	/**
	 * Performs the change previously displayed in the preview.
	 * <p>
	 * Note: This API must not be called from outside the refactoring framework.
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
					super.run(new SubProgressMonitor(monitor, 10, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
					refactoringPerformed(refactoring, new SubProgressMonitor(monitor, 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
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
			wizard.run(false, false, new WorkbenchRunnableAdapter(operation, ResourcesPlugin.getWorkspace().getRoot()));
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
				fErrorPage.setTitle(descriptor, fCurrentRefactoring, fDescriptorProxies.length);
				fErrorPage.setNextPageDisabled(disabled && fatal);
				fErrorPage.setPageComplete(!fatal);
				fErrorPage.setStatus(null);
				fErrorPage.setStatus(status);
				getContainer().updateButtons();
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
				fPreviewPage.setTitle(descriptor, fCurrentRefactoring, fDescriptorProxies.length);
				fPreviewPage.setNextPageDisabled(disabled);
				fPreviewPage.setPageComplete(!disabled);
				fPreviewPage.setStatus(status);
				getContainer().updateButtons();
			}
		});
	}

	/**
	 * Hook method which is called when the specified refactoring has been
	 * performed, e.g. its change object has been successfully applied to the
	 * workspace. The default implementation does nothing and returns a
	 * refactoring status of severity {@link RefactoringStatus#OK}. This method
	 * may be called from non-UI threads.
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
		fExecutedRefactorings++;
		return new RefactoringStatus();
	}

	/**
	 * Hook method which is called for each change before it is displayed in a
	 * preview page. The default implementation returns <code>true</code>.
	 * <p>
	 * Subclasses may reimplement this method to perform any special filtering
	 * of preview changes.
	 * </p>
	 * 
	 * @param change
	 *            the change to select
	 * @return <code>true</code> if the change passes the filter,
	 *         <code>false</code> otherwise
	 */
	protected boolean selectPreviewChange(final Change change) {
		return true;
	}

	/**
	 * Hook method which is called for each status entry before it is displayed
	 * in a wizard page. The default implementation returns <code>true</code>.
	 * <p>
	 * Subclasses may reimplement this method to perform any special filtering
	 * of status entries on error pages.
	 * </p>
	 * 
	 * @param entry
	 *            the status entry to select
	 * @return <code>true</code> if the status entry passes the filter,
	 *         <code>false</code> otherwise
	 */
	protected boolean selectStatusEntry(final RefactoringStatusEntry entry) {
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
