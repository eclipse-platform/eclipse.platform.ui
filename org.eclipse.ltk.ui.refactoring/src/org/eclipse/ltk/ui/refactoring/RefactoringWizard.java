/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ltk.ui.refactoring;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;

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
import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.ChangeExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.ErrorWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.ExceptionHandler;
import org.eclipse.ltk.internal.ui.refactoring.IPreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringPluginImages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIPlugin;
import org.eclipse.ltk.internal.ui.refactoring.WorkbenchRunnableAdapter;

public class RefactoringWizard extends Wizard {

	private String fDefaultPageTitle;
	private Refactoring fRefactoring;
	private Change fChange;
	private RefactoringStatus fActivationStatus= new RefactoringStatus();
	private RefactoringStatus fStatus;
	private boolean fHasUserInputPages;
	private boolean fExpandFirstNode;
	private boolean fIsChangeCreationCancelable;
	private boolean fPreviewReview;
	private boolean fPreviewShown;
	
	public RefactoringWizard(Refactoring refactoring) {
		this();
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
	} 
	
	public RefactoringWizard(Refactoring refactoring, String defaultPageTitle) {
		this(refactoring);
		Assert.isNotNull(defaultPageTitle);
		fDefaultPageTitle= defaultPageTitle;
	}
	
	
	/**
	 * Creates a new refactoring wizard without initializing its
	 * state. This constructor should only be used to create a
	 * refactoring spcified via a plugin manifest file. Clients
	 * that us this API must make sure that the <code>initialize</code>
	 * method gets called.
	 * 
	 * @see #initialize(Refactoring)
	 */
	public RefactoringWizard() {
		setNeedsProgressMonitor(true);
		setChangeCreationCancelable(true);
		setWindowTitle(RefactoringUIMessages.getString("RefactoringWizard.title")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(RefactoringPluginImages.DESC_WIZBAN_REFACTOR);
	}
	
	/**
	 * Initializes the refactoring with the given refactoring. This
	 * method should be called right after the wizard has been created
	 * using the default constructor.
	 * 
	 * @param refactoring the refactoring this wizard is working
	 *  on
	 * @see #RefactoringWizard()
	 */
	public void initialize(Refactoring refactoring) {
		Assert.isNotNull(refactoring);
		fRefactoring= refactoring;
	}
	
	/**
	 * Sets the default page title to the given value. This value is used
	 * as a page title for wizard page's which don't provide their own
	 * page title. Setting this value has only an effect as long as the
	 * user interface hasn't been created yet. 
	 * 
	 * @param defaultPageTitle the default page title.
	 * @see Wizard#setDefaultPageImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setDefaultPageTitle(String defaultPageTitle) {
		fDefaultPageTitle= defaultPageTitle;
	}
	
	public void setChangeCreationCancelable(boolean isChangeCreationCancelable){
		fIsChangeCreationCancelable= isChangeCreationCancelable;
	}
	
	//---- Hooks to overide ---------------------------------------------------------------

	/**
	 * Some refactorings do activation checking when the wizard is going to be opened. 
	 * They do this since activation checking is expensive and can't be performed on 
	 * opening a corresponding menu. Wizards that need activation checking on opening
	 * should reimplement this method and should return <code>true</code>. This default
	 * implementation returns <code>false</code>.
	 *
	 * @return <code>true<code> if activation checking should be performed on opening;
	 *  otherwise <code>false</code> is returned
	 */
	protected boolean checkActivationOnOpen() {
		return false;
	}
	 
	/**
	 * Hook to add user input pages to the wizard. This default implementation 
	 * adds nothing.
	 */
	protected void addUserInputPages(){
	}
	
	/**
	 * Hook to add the error page to the wizard. This default implementation 
	 * adds an <code>ErrorWizardPage</code> to the wizard.
	 */
	protected void addErrorPage(){
		addPage(new ErrorWizardPage());
	}
	
	/**
	 * Hook to add the page the gives a prefix of the changes to be performed. This default 
	 * implementation  adds a <code>PreviewWizardPage</code> to the wizard.
	 */
	protected void addPreviewPage(){
		addPage(new PreviewWizardPage());
	}
	
	/**
	 * Hook to determine if the wizard has more than one user input page without
	 * actually creating the pages.
	 * 
	 * @return boolean <code>true<code> if multi page user input exists.
	 * Otherwise <code>false</code> is returned
	 */
	public boolean hasMultiPageUserInput() {
		return false;
	}
	
	protected int getMessageLineWidthInChars() {
		return 80;
	}
	
	protected boolean hasUserInputPages() {
		return fHasUserInputPages;		
	}
	
	protected boolean hasPreviewPage() {
		return true;
	}
	
	protected boolean yesNoStyle() {
		return false;
	}

	//---- Setter and Getters ------------------------------------------------------------
	
	/**
	 * Returns the refactoring this wizard is using.
	 */	
	public Refactoring getRefactoring(){
		return fRefactoring;
	}
	
	/**
	 * Sets the change object.
	 */
	public void setChange(Change change){
		IPreviewWizardPage page= (IPreviewWizardPage)getPage(IPreviewWizardPage.PAGE_NAME);
		if (page != null)
			page.setChange(change);
		fChange= change;
	}

	/**
	 * Returns the current change object.
	 */
	public Change getChange() {
		return fChange;
	}
	
	/**
	 * Sets the refactoring status.
	 * 
	 * @param status the refactoring status to set.
	 */
	public void setStatus(RefactoringStatus status) {
		ErrorWizardPage page= (ErrorWizardPage)getPage(ErrorWizardPage.PAGE_NAME);
		if (page != null)
			page.setStatus(status);
		fStatus= status;
	}
	
	/**
	 * Returns the current refactoring status.
	 */
	public RefactoringStatus getStatus() {
		return fStatus;
	} 
	
	/**
	 * Sets the refactoring status returned from input checking. Any previously 
	 * computed activation status is merged into the given status before it is set 
	 * to the error page.
	 * 
	 * @param status the input status to set.
	 * @see #getActivationStatus()
	 */
	public void setInputStatus(RefactoringStatus status) {
		RefactoringStatus newStatus= new RefactoringStatus();
		if (fActivationStatus != null)
			newStatus.merge(fActivationStatus);
		newStatus.merge(status);	
		setStatus(newStatus);			
	}
	
	/**
	 * Sets the refactoring status returned from activation checking.
	 * 
	 * @param status the activation status to be set.
	 */
	public void setActivationStatus(RefactoringStatus status) {
		fActivationStatus= status;
		setStatus(status);
	}
		
	/**
	 * Returns the activation status computed during the start up off this
	 * wizard. This methdod returns <code>null</code> if no activation
	 * checking has been performed during startup.
	 * 
	 * @return the activation status computed during startup.
	 */
	public RefactoringStatus getActivationStatus() {
		return fActivationStatus;
	}
	
	/**
	 * Returns the default page title used for pages that don't
	 * provide their own page title.
	 * 
	 * @return the default page title.
	 */
	public String getDefaultPageTitle() {
		return fDefaultPageTitle;
	}
	
	/**
	 * Defines whether the frist node in the preview page is supposed to be expanded.
	 * 
	 * @param expand <code>true</code> if the first node is to be expanded. Otherwise
	 *  <code>false</code>
	 */
	public void setExpandFirstNode(boolean expand) {
		fExpandFirstNode= true;
	}
	
	/**
	 * Returns <code>true</code> if the first node in the preview page is supposed to be
	 * expanded. Otherwise <code>false</code> is returned.
	 * 
	 * @return <code>true</code> if the first node in the preview page is supposed to be
	 * 	expanded; otherwise <code>false</code>
	 */
	public boolean getExpandFirstNode() {
		return fExpandFirstNode;
	}
	
	/**
	 * Computes the wizard page that should follow the user input page. This is
	 * either the error page or the proposed changes page, depending on the
	 * result of the condition checking.
	 * 
	 * @return the wizard page that should be shown after the last user input
	 *  page
	 */
	public IWizardPage computeUserInputSuccessorPage(IWizardPage caller) {
		return computeUserInputSuccessorPage(caller, getContainer());
	}

	private IWizardPage computeUserInputSuccessorPage(IWizardPage caller, IRunnableContext context) {
		Change change= createChange(new CreateChangeOperation(
			new CheckConditionsOperation(fRefactoring, CheckConditionsOperation.FINAL_CONDITIONS),
			RefactoringStatus.OK), true, context);
		// Status has been updated since we have passed true
		RefactoringStatus status= getStatus();
		
		// Creating the change has been canceled
		if (change == null && status == null) {		
			setChange(change);
			return caller;
		}
				
		// Set change if we don't have fatal errors.
		if (!status.hasFatalError())
			setChange(change);
		
		if (status.isOK()) {
			return getPage(IPreviewWizardPage.PAGE_NAME);
		} else {
			return getPage(ErrorWizardPage.PAGE_NAME);
		}
	} 
	
	/**
	 * Initialize all pages with the managed page title.
	 */
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
	
	/**
	 * Forces the visiting of the preview page. The OK/Finish button will be
	 * disabled until the user has reached the preview page.
	 */
	public void setPreviewReview(boolean review) {
		fPreviewReview= review;
		getContainer().updateButtons();	
	}
	
	public void setPreviewShown(boolean shown) {
		fPreviewShown= shown;
		getContainer().updateButtons();
	}
	
	public boolean canFinish() {
		if (fPreviewReview && !fPreviewShown)
			return false;
		return super.canFinish();
	}


	//---- Change management -------------------------------------------------------------

	/**
	 * Creates a new change object for the refactoring. Method returns <code>
	 * null</code> if the change cannot be created.
	 * 
	 * <p>
	 * This method is for internal use only. It should not be called from outside
	 * the refactoring user interface framework
	 * </p>
	 * 
	 * @param style the conditions to check before creating the change.
	 * @param updateStatus if <code>true</code> the wizard's status is updated
	 *  with the status returned from the <code>CreateChangeOperation</code>.
	 *  if <code>false</code> no status updating is performed.
	 */
	public Change createChange(CreateChangeOperation operation, boolean updateStatus) {
		return createChange(operation, updateStatus, getContainer());
	}

	private Change createChange(CreateChangeOperation operation, boolean updateStatus, IRunnableContext context){
		InvocationTargetException exception= null;
		try {
			context.run(true, fIsChangeCreationCancelable, new WorkbenchRunnableAdapter(
				operation, ResourcesPlugin.getWorkspace().getRoot()));
		} catch (InterruptedException e) {
			setStatus(null);
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
					status.addFatalError(RefactoringUIMessages.getFormattedString("RefactoringWizard.see_log", msg)); //$NON-NLS-1$
				} else {
					status.addFatalError(RefactoringUIMessages.getString("RefactoringWizard.Internal_error")); //$NON-NLS-1$
				}
				RefactoringUIPlugin.log(exception);
			} else {
				status= operation.getConditionCheckingStatus();
			}
			setStatus(status, operation.getConditionCheckingStyle());
		} else {
			if (exception != null)
				ExceptionHandler.handle(exception, getContainer().getShell(), 
					RefactoringUIMessages.getString("RefactoringWizard.refactoring"),  //$NON-NLS-1$
					RefactoringUIMessages.getString("RefactoringWizard.unexpected_exception")); //$NON-NLS-1$
		}
		Change change= operation.getChange();	
		return change;
	}

	public boolean performFinish(PerformChangeOperation op) {
		return performRefactoring(op, fRefactoring, getContainer(), getContainer().getShell());
	}
	
	//---- Condition checking ------------------------------------------------------------

	public RefactoringStatus checkInput() {
		return internalCheckCondition(CheckConditionsOperation.FINAL_CONDITIONS);
	}
	
	/**
	 * Checks the condition for the given style.
	 * @param style the conditions to check.
	 * @return the result of the condition check.
	 * @see CheckConditionsOperation
	 */
	protected final RefactoringStatus internalCheckCondition(int style) {
		
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
			status.addFatalError(RefactoringUIMessages.getString("RefactoringWizard.internal_error_1")); //$NON-NLS-1$
		} else {
			status= op.getStatus();
		}
		setStatus(status, style);
		return status;	
	}
	
	/**
	 * Sets the status according to the given style flag.
	 * 
	 * @param status the refactoring status to set.
	 * @param style a flag indicating if the status is a activation, input checking, or
	 *  precondition checking status.
	 * @see CheckConditionsOperation
	 */
	protected void setStatus(RefactoringStatus status, int style) {
		if ((style & CheckConditionsOperation.ALL_CONDITIONS) == CheckConditionsOperation.ALL_CONDITIONS)
			setStatus(status);
		else if ((style & CheckConditionsOperation.INITIAL_CONDITONS) == CheckConditionsOperation.INITIAL_CONDITONS)
			setActivationStatus(status);
		else if ((style & CheckConditionsOperation.FINAL_CONDITIONS) == CheckConditionsOperation.FINAL_CONDITIONS)
			setInputStatus(status);
	}

	
	//---- Reimplementation of Wizard methods --------------------------------------------

	public boolean performFinish() {
		Assert.isNotNull(fRefactoring);
		
		RefactoringWizardPage page= (RefactoringWizardPage)getContainer().getCurrentPage();
		return page.performFinish();
	}
	
	public IWizardPage getPreviousPage(IWizardPage page) {
		if (fHasUserInputPages)
			return super.getPreviousPage(page);
		if (! page.getName().equals(ErrorWizardPage.PAGE_NAME)){
			if (fStatus.isOK())
				return null;
		}		
		return super.getPreviousPage(page);		
	}

	public IWizardPage getStartingPage() {
		if (fHasUserInputPages)
			return super.getStartingPage();
		return computeUserInputSuccessorPage(null, PlatformUI.getWorkbench().getActiveWorkbenchWindow());
	}
	
	public void addPages() {
		if (checkActivationOnOpen()) {
			internalCheckCondition(CheckConditionsOperation.INITIAL_CONDITONS);
		}
		if (fActivationStatus.hasFatalError()) {
			addErrorPage();
			// Set the status since we added the error page
			setStatus(getStatus());	
		} else { 
			Assert.isTrue(getPageCount() == 0);
			addUserInputPages();
			if (getPageCount() > 0)
				fHasUserInputPages= true;
			addErrorPage();
			addPreviewPage();	
		}
		initializeDefaultPageTitles();
	}
	
	public void addPage(IWizardPage page) {
		Assert.isTrue(page instanceof RefactoringWizardPage);
		super.addPage(page);
	}
	
	//---- private helper methods --------------------------------------------------------
	
	public static boolean performRefactoring(PerformChangeOperation op, Refactoring refactoring, IRunnableContext execContext, Shell parent) {
		op.setUndoManager(RefactoringCore.getUndoManager(), refactoring.getName());
		try{
			execContext.run(false, false, new WorkbenchRunnableAdapter(op, ResourcesPlugin.getWorkspace().getRoot()));
		} catch (InvocationTargetException e) {
			Throwable inner= e.getTargetException();
			if (op.changeExecutionFailed()) {
				ChangeExceptionHandler handler= new ChangeExceptionHandler(parent, refactoring);
				if (inner instanceof RuntimeException) {
					handler.handle(op.getChange(), (RuntimeException)inner);
					return false;
				} else if (inner instanceof CoreException) {
					handler.handle(op.getChange(), (CoreException)inner);
					return false;
				}
			}
			ExceptionHandler.handle(e, parent, 
				RefactoringUIMessages.getString("RefactoringWizard.refactoring"), //$NON-NLS-1$
				RefactoringUIMessages.getString("RefactoringWizard.unexpected_exception_1")); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			return false;
		} 
		return true;
	}	
	
}
