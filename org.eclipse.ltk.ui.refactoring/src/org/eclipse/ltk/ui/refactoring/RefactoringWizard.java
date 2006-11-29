/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ltk.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import org.eclipse.ui.PlatformUI;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.FinishResult;
import org.eclipse.ltk.internal.ui.refactoring.IErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.IPreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.InternalAPI;
import org.eclipse.ltk.internal.ui.refactoring.Messages;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;

/**
 * An abstract base implementation of a refactoring wizard. A refactoring
 * wizard differs from a normal wizard in the following characteristics:
 * <ul>
 *   <li>only pages of type {@link org.eclipse.ltk.ui.refactoring.RefactoringWizardPage
 *       RefactoringWizardPage} can be added to a refactoring wizard. Trying to
 *       add a different kind of page results in an exception.</li>
 *   <li>a refactoring wizard consists of 0 .. n user input pages, one error page
 *       to present the outcome of the refactoring's condition checking and one
 *       preview page to present a preview of the workspace changes.</li> 
 * </ul> 
 * <p>
 * A refactoring wizard is best opened using the {@link RefactoringWizardOpenOperation}.
 * </p>
 * <p>
 * Clients may extend this class.
 * </p>
 * 
 * @see org.eclipse.ltk.core.refactoring.Refactoring
 * 
 * @since 3.0
 */
public abstract class RefactoringWizard extends Wizard {

	/** 
	 * Flag (value 0) indicating that no special flags are provided.
	 */
	public static final int NONE= 0;
	
	/**
	 * Flag (value 1) indicating that the initial condition checking of the refactoring is done when 
	 * the wizard opens. If not specified it is assumed that the initial condition checking
	 * has been done by the client before opening the wizard dialog. 
	 */
	public static final int CHECK_INITIAL_CONDITIONS_ON_OPEN= 1 << 0;
	
	/**
	 * Flag (value 2) indicating that a normal wizard based user interface consisting
	 * of a back, next, finish and cancel button should be used to present
	 * this refactoring wizard. This flag can't be specified together with
	 * the flag {@link #DIALOG_BASED_USER_INTERFACE}.
	 */
	public static final int WIZARD_BASED_USER_INTERFACE= 1 << 1;
	
	/**
	 * Flag (value 4) indicating that a lightweight dialog based user interface should
	 * be used to present this refactoring wizard. This user interface consists
	 * of a preview, finish and cancel button and the initial size of dialog
	 * is based on the first user input page. This flag is only valid if only
	 * one user input page is present. Specifying this flag together with more
	 * than one input page will result in an exception when adding the user input
	 * pages. This flag can't be specified together with the flag 
	 * {@link #WIZARD_BASED_USER_INTERFACE}.
	 * 
	 * @since 3.1
	 */
	public static final int DIALOG_BASED_USER_INTERFACE= 1 << 2;
	
	/**
	 * @deprecated Use {@link #DIALOG_BASED_USER_INTERFACE} instead.
	 */
	public static final int DIALOG_BASED_UESR_INTERFACE= DIALOG_BASED_USER_INTERFACE;
	
	/**
	 * Flag (value 8) indicating that the finish and cancel button should be named
	 * yes and no. The flag is ignored if the flag {@link #WIZARD_BASED_USER_INTERFACE}
	 * is specified.
	 */
	public static final int YES_NO_BUTTON_STYLE= 1 << 3;
	
	/**
	 * Flag (value 16) indicating that the wizard should not show a preview page.
	 * The flag is ignored if the flag {@link #WIZARD_BASED_USER_INTERFACE}
	 * is specified.
	 * */
	public static final int NO_PREVIEW_PAGE= 1 << 4;
	
	/**
	 * Flag (value 32) indicating that the first change node presented in the
	 * preview page should be fully expanded.
	 */
	public static final int PREVIEW_EXPAND_FIRST_NODE= 1 << 5;

	/**
	 * Flag (value 64) indicating that the dialog representing the refactoring
	 * status to the user will not contain a back button. The flag
	 * is ignored if the flag (@link #WIZARD_BASED_USER_INTERFACE}
	 * is specified.
	 */
	public static final int NO_BACK_BUTTON_ON_STATUS_DIALOG= 1 << 6;
	
	private static final int LAST= 1 << 7;
	
	private int fFlags;
	private Refactoring fRefactoring;
	private String fDefaultPageTitle;
	
	private Change fChange;
	private RefactoringStatus fInitialConditionCheckingStatus= new RefactoringStatus();
	private RefactoringStatus fConditionCheckingStatus;
	
	private int fUserInputPages;
	private boolean fInAddPages;
	
	private boolean fIsChangeCreationCancelable;
	private boolean fForcePreviewReview;
	private boolean fPreviewShown;
	
	/**
	 * Creates a new refactoring wizard for the given refactoring. 
	 * 
	 * @param refactoring the refactoring the wizard is presenting
	 * @param flags flags specifying the behavior of the wizard. If neither 
	 *  <code>WIZARD_BASED_USER_INTERFACE</code> nor <code>DIALOG_BASED_UESR_INTERFACE</code> 
	 *  is specified then <code>WIZARD_BASED_USER_INTERFACE</code> will be
	 *  taken as a default.
	 */
	public RefactoringWizard(Refactoring refactoring, int flags) {
		Assert.isNotNull(refactoring);
		Assert.isTrue(flags < LAST);
		if ((flags & DIALOG_BASED_USER_INTERFACE) == 0) 
			flags |= WIZARD_BASED_USER_INTERFACE;
		Assert.isTrue((flags & DIALOG_BASED_USER_INTERFACE) != 0 || (flags & WIZARD_BASED_USER_INTERFACE) != 0);
		fRefactoring= refactoring;
		fFlags= flags;
		setNeedsProgressMonitor(true);
		setChangeCreationCancelable(true);
		setWindowTitle(RefactoringUIMessages.RefactoringWizard_title); 
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	} 
	
	//---- Setter and Getters ------------------------------------------------------------
	
	/**
	 * Returns the refactoring this wizard is associated with.
	 * 
	 * @return the wizard's refactoring
	 */	
	public final Refactoring getRefactoring(){
		return fRefactoring;
	}
	
	/**
	 * Sets the default page title to the given value. This value is used
	 * as a page title for wizard pages which don't provide their own
	 * page title. Setting this value has only an effect as long as the
	 * user interface hasn't been created yet. 
	 * 
	 * @param defaultPageTitle the default page title.
	 * @see Wizard#setDefaultPageImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public final void setDefaultPageTitle(String defaultPageTitle) {
		Assert.isNotNull(defaultPageTitle);
		fDefaultPageTitle= defaultPageTitle;
	}
	
	/**
	 * Returns the default page title used for pages that don't provide their 
	 * own page title.
	 * 
	 * @return the default page title or <code>null</code> if non has been set
	 * 
	 * @see #setDefaultPageTitle(String)
	 */
	public final String getDefaultPageTitle() {
		return fDefaultPageTitle;
	}
	
	/**
	 * If set to <code>true</code> the Finish or OK button, respectively will
	 * be disabled until the user has visited the preview page. If set to
	 * <code>false</code> the refactoring can be performed before the preview
	 * page has been visited.
	 * 
	 * @param forcePreviewReview if <code>true</code> to user must confirm the
	 *  preview
	 */
	public final void setForcePreviewReview(boolean forcePreviewReview) {
		fForcePreviewReview= forcePreviewReview;
		getContainer().updateButtons();	
	}
	
	/**
	 * Returns the width in characters to be used for the message line embedded into
	 * the refactoring wizard dialog.
	 * <p>
	 * Subclasses may override this method and return a different value.
	 * </p>
	 * 
	 * @return the message lines width in characters
	 */
	public int getMessageLineWidthInChars() {
		return 80;
	}
	
	/**
	 * If set to <code>true</code> the change creation is cancelable by the user.
	 * <p>
	 * By default, change creation is cancelable.
	 * </p>
	 * @param isChangeCreationCancelable determines whether the change creation
	 *  is cancelable by the user or not.
	 * 
	 * @see Refactoring#createChange(IProgressMonitor)
	 */
	public final void setChangeCreationCancelable(boolean isChangeCreationCancelable){
		fIsChangeCreationCancelable= isChangeCreationCancelable;
	}
	
	/**
	 * Sets the initial condition checking status computed by the refactoring.
	 * Clients should uses this method if the initial condition checking
	 * status has been computed outside of this refactoring wizard.
	 * 
	 * @param status the initial condition checking status.
	 * 
	 * @see Refactoring#checkInitialConditions(IProgressMonitor)
	 * @see #CHECK_INITIAL_CONDITIONS_ON_OPEN
	 */
	public final void setInitialConditionCheckingStatus(RefactoringStatus status) {
		Assert.isNotNull(status);
		fInitialConditionCheckingStatus= status;
		setConditionCheckingStatus(status);
	}
		
	/**
	 * Returns the refactoring's change object or <code>null</code> if no change
	 * object has been created yet.
	 * 
	 * @return the refactoring's change object or <code>null</code>
	 * 
	 * @see Refactoring#createChange(IProgressMonitor)
	 */
	public final Change getChange() {
		return fChange;
	}
	
	/**
	 * Returns the status of the initial condition checking or <code>null</code>
	 * if the initial condition checking hasn't been performed yet.
	 * 
	 * @return the status of the initial condition checking or <code>null</code>
	 * 
	 * @see Refactoring#checkInitialConditions(IProgressMonitor)
	 */
	/* package */ final RefactoringStatus getInitialConditionCheckingStatus() {
		return fInitialConditionCheckingStatus;
	}
	
	/**
	 * Returns <code>true</code> if the wizard needs a wizard based user interface.
	 * Otherwise <code>false</code> is returned.
	 * 
	 * @return whether the wizard needs a wizard based user interface or not
	 */
	/* package */ boolean needsWizardBasedUserInterface() {
		return (fFlags & WIZARD_BASED_USER_INTERFACE) != 0;
	}
	
	//---- Page management ------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 * 
	 * This method calls the hook method {@link #addUserInputPages()} to allow
	 * subclasses to add specific user input pages.
	 */
	public final void addPages() {
		Assert.isNotNull(fRefactoring);
		try {
			fInAddPages= true;
			if (checkActivationOnOpen()) {
				internalCheckCondition(CheckConditionsOperation.INITIAL_CONDITONS);
			}
			if (fInitialConditionCheckingStatus.hasFatalError()) {
				addErrorPage();
				// Set the status since we added the error page
				setConditionCheckingStatus(getConditionCheckingStatus());	
			} else { 
				Assert.isTrue(getPageCount() == 0);
				addUserInputPages();
				fUserInputPages= getPageCount();
				if (fUserInputPages > 0) {
					IWizardPage[] pages= getPages();
					((UserInputWizardPage)pages[fUserInputPages - 1]).markAsLastUserInputPage();
				}
				addErrorPage();
				addPreviewPage();	
			}
			initializeDefaultPageTitles();
		} finally {
			fInAddPages= false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * This method asserts that the pages added to the refactoring wizard
	 * are instances of type {@link RefactoringWizardPage}.
	 */
	public final void addPage(IWizardPage page) {
		Assert.isTrue(page instanceof RefactoringWizardPage && fInAddPages);
		super.addPage(page);
	}
	
	/**
	 * Hook method to add user input pages to this refactoring wizard. Pages
	 * added via this call have to be instances of the type {@link UserInputWizardPage}.
	 * Adding pages of a different kind is not permitted and will result
	 * in unexpected behavior.
	 */
	protected abstract void addUserInputPages();
	
	private void addErrorPage(){
		addPage(new ErrorWizardPage());
	}
	
	private void addPreviewPage(){
		addPage(new PreviewWizardPage());
	}
	
	private boolean hasUserInput() {
		return fUserInputPages > 0;		
	}
	
	private void initializeDefaultPageTitles() {
		if (fDefaultPageTitle == null)
			return;
			
		IWizardPage[] pages= getPages();
		for (int i= 0; i < pages.length; i++) {
			IWizardPage page= pages[i];
			if (page.getTitle() == null)
				page.setTitle(fDefaultPageTitle);
		}
	}
	
	//---- Page computation -----------------------------------------------------------
	
	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getStartingPage() {
		if (hasUserInput())
			return super.getStartingPage();
		
		/*
		 * XXX: Can return null if there's no user input page and change creation has been cancelled.
		 * The only way to avoid this would be setChangeCreationCancelable(true).
		 */
		return computeUserInputSuccessorPage(null, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (hasUserInput())
			return super.getPreviousPage(page);
		if (! page.getName().equals(IErrorWizardPage.PAGE_NAME)){
			if (fConditionCheckingStatus.isOK())
				return null;
		}		
		return super.getPreviousPage(page);		
	}

	/* package */ IWizardPage computeUserInputSuccessorPage(IWizardPage caller, IRunnableContext context) {
		Change change= createChange(new CreateChangeOperation(
			new CheckConditionsOperation(fRefactoring, CheckConditionsOperation.FINAL_CONDITIONS),
			RefactoringStatus.FATAL), true, context);
		// Status has been updated since we have passed true
		RefactoringStatus status= getConditionCheckingStatus();
		
		// Creating the change has been canceled
		if (change == null && status == null) {		
			internalSetChange(InternalAPI.INSTANCE, change);
			return caller;
		}
				
		// Set change if we don't have fatal errors.
		if (!status.hasFatalError())
			internalSetChange(InternalAPI.INSTANCE, change);
		
		if (status.isOK()) {
			return getPage(IPreviewWizardPage.PAGE_NAME);
		} else {
			return getPage(IErrorWizardPage.PAGE_NAME);
		}
	} 
	
	/**
	 * {@inheritDoc}
	 */
	public boolean canFinish() {
		if (fForcePreviewReview && !fPreviewShown)
			return false;
		return super.canFinish();
	}

	//---- Condition checking ------------------------------------------------------------

	/* package */ final RefactoringStatus checkFinalConditions() {
		return internalCheckCondition(CheckConditionsOperation.FINAL_CONDITIONS);
	}
	
	private RefactoringStatus internalCheckCondition(int style) {
		
		CheckConditionsOperation op= new CheckConditionsOperation(fRefactoring, style); 

		Exception exception= null;
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(
				new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot()));
		} catch (InterruptedException e) {
			exception= e;
		} catch (InvocationTargetException e) {
			exception= e;
		}
		RefactoringStatus status= null;
		if (exception != null) {
			RefactoringUIPlugin.log(exception);
			status= new RefactoringStatus();
			status.addFatalError(RefactoringUIMessages.RefactoringWizard_internal_error_1); 
		} else {
			status= op.getStatus();
		}
		setConditionCheckingStatus(status, style);
		return status;	
	}
	
	private void setConditionCheckingStatus(RefactoringStatus status, int style) {
		if ((style & CheckConditionsOperation.ALL_CONDITIONS) == CheckConditionsOperation.ALL_CONDITIONS)
			setConditionCheckingStatus(status);
		else if ((style & CheckConditionsOperation.INITIAL_CONDITONS) == CheckConditionsOperation.INITIAL_CONDITONS)
			setInitialConditionCheckingStatus(status);
		else if ((style & CheckConditionsOperation.FINAL_CONDITIONS) == CheckConditionsOperation.FINAL_CONDITIONS)
			setFinalConditionCheckingStatus(status);
	}

	private RefactoringStatus getConditionCheckingStatus() {
		return fConditionCheckingStatus;
	} 
		
	/**
	 * Sets the refactoring status.
	 * 
	 * @param status the refactoring status to set.
	 */
	/* package */ final void setConditionCheckingStatus(RefactoringStatus status) {
		ErrorWizardPage page= (ErrorWizardPage)getPage(IErrorWizardPage.PAGE_NAME);
		if (page != null)
			page.setStatus(status);
		fConditionCheckingStatus= status;
	}
	
	/**
	 * Sets the refactoring status returned from final condition checking. Any previously 
	 * computed initial status is merged into the given status before it is set to the 
	 * error page.
	 * 
	 * @param status the final condition checking status to set
	 */
	private void setFinalConditionCheckingStatus(RefactoringStatus status) {
		RefactoringStatus newStatus= new RefactoringStatus();
		if (fInitialConditionCheckingStatus != null)
			newStatus.merge(fInitialConditionCheckingStatus);
		newStatus.merge(status);	
		setConditionCheckingStatus(newStatus);			
	}
	
	//---- Change management -------------------------------------------------------------

	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * @param operation the create change operation
	 * @param updateStatus flag indicating if status updating is requested
	 * 
	 * @return the created change
	 */
	public final Change internalCreateChange(InternalAPI api, CreateChangeOperation operation, boolean updateStatus) {
		Assert.isNotNull(api);
		return createChange(operation, updateStatus, getContainer());
	}

	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * @param op the perform change operation
	 * 
	 * @return whether the finish ended OK or not
	 */
	public final FinishResult internalPerformFinish(InternalAPI api, PerformChangeOperation op) {
		op.setUndoManager(RefactoringCore.getUndoManager(), fRefactoring.getName());
		Shell parent= getContainer().getShell();
		try{
			getContainer().run(true, true, new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot()));
		} catch (InvocationTargetException e) {
			Throwable inner= e.getTargetException();
			if (op.changeExecutionFailed()) {
				ChangeExceptionHandler handler= new ChangeExceptionHandler(parent, fRefactoring);
				if (inner instanceof RuntimeException) {
					handler.handle(op.getChange(), (RuntimeException)inner);
					return FinishResult.createException();
				} else if (inner instanceof CoreException) {
					handler.handle(op.getChange(), (CoreException)inner);
					return FinishResult.createException();
				}
			}
			ExceptionHandler.handle(e, parent, 
				RefactoringUIMessages.RefactoringWizard_refactoring, 
				RefactoringUIMessages.RefactoringWizard_unexpected_exception_1); 
			return FinishResult.createException();
		} catch (InterruptedException e) {
			return FinishResult.createInterrupted();
		}
		return FinishResult.createOK();
	}
	
	private Change createChange(CreateChangeOperation operation, boolean updateStatus, IRunnableContext context){
		InvocationTargetException exception= null;
		try {
			context.run(true, fIsChangeCreationCancelable, new WorkbenchRunnableAdapter(
				operation, ResourcesPlugin.getWorkspace().getRoot()));
		} catch (InterruptedException e) {
			setConditionCheckingStatus(null);
			return null;
		} catch (InvocationTargetException e) {
			exception= e;
		}
		
		if (updateStatus) {
			RefactoringStatus status= null;
			if (exception != null) {
				status= new RefactoringStatus();
				String msg= exception.getMessage();
				if (msg != null) {
					status.addFatalError(Messages.format(RefactoringUIMessages.RefactoringWizard_see_log, msg)); 
				} else {
					status.addFatalError(RefactoringUIMessages.RefactoringWizard_Internal_error); 
				}
				RefactoringUIPlugin.log(exception);
			} else {
				status= operation.getConditionCheckingStatus();
			}
			setConditionCheckingStatus(status, operation.getConditionCheckingStyle());
		} else {
			if (exception != null)
				ExceptionHandler.handle(exception, getContainer().getShell(), 
					RefactoringUIMessages.RefactoringWizard_refactoring,  
					RefactoringUIMessages.RefactoringWizard_unexpected_exception); 
		}
		Change change= operation.getChange();	
		return change;
	}

	//---- Re-implementation of Wizard methods --------------------------------------------

	public boolean performFinish() {
		Assert.isNotNull(fRefactoring);
		
		RefactoringWizardPage page= (RefactoringWizardPage)getContainer().getCurrentPage();
		return page.performFinish();
	}
	
	public boolean performCancel() {
		if (fChange != null)
			fChange.dispose();
		return super.performCancel();
	}
	
	//---- Internal API, but public due to Java constraints ------------------------------
	
	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * 
	 * @return whether the wizard has a preview page or not.
	 */
	public final boolean internalHasPreviewPage(InternalAPI api) {
		Assert.isNotNull(api);
		return (fFlags & NO_PREVIEW_PAGE) == 0;
	}
	
	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * 
	 * @return whether yes no button style is requested
	 */
	public final boolean internalIsYesNoStyle(InternalAPI api) {
		Assert.isNotNull(api);
		return (fFlags & YES_NO_BUTTON_STYLE) != 0;
	}
	
	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * 
	 * @return whether the first node of the preview is supposed to be expanded
	 */
	public final boolean internalGetExpandFirstNode(InternalAPI api) {
		Assert.isNotNull(api);
		return (fFlags & PREVIEW_EXPAND_FIRST_NODE) != 0;
	}
	
	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * @param change the change to set
	 */
	public final void internalSetChange(InternalAPI api, Change change){
		Assert.isNotNull(api);
		IPreviewWizardPage page= (IPreviewWizardPage)getPage(IPreviewWizardPage.PAGE_NAME);
		if (page != null)
			page.setChange(change);
		fChange= change;
	}

	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * @param shown a boolean indicating if the preview page has been shown or not
	 */
	public final void internalSetPreviewShown(InternalAPI api, boolean shown) {
		Assert.isNotNull(api);
		fPreviewShown= shown;
		getContainer().updateButtons();
	}
	
	/**
	 * Note: This method is for internal use only. Clients are not allowed to call this method.
	 * 
	 * @param api internal instance to avoid access from external clients
	 * @return whether to show a back button or not
	 */
	public final boolean internalShowBackButtonOnStatusDialog(InternalAPI api) {
		Assert.isNotNull(api);
		return (fFlags & NO_BACK_BUTTON_ON_STATUS_DIALOG) == 0;
	}
	
	//---- Helper methods to check style bits --------------------------------------------
	
	private boolean checkActivationOnOpen() {
		return (fFlags & CHECK_INITIAL_CONDITIONS_ON_OPEN) != 0;
	}
}
