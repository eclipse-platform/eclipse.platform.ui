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
package org.eclipse.compare.internal.patch;

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ExceptionHandler;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class PatchWizard extends Wizard {

	// dialog store id constants
	private final static String DIALOG_SETTINGS_KEY= "PatchWizard"; //$NON-NLS-1$

	private boolean fHasNewDialogSettings;
	
	private InputPatchPage fPatchWizardPage;
	private PatchTargetPage fPatchTargetPage;
	private PreviewPatchPage2 fPreviewPage2;
	
	private final WorkspacePatcher fPatcher;
	private PatchWizardDialog fDialog;
	
	private CompareConfiguration fConfiguration;
	private IStorage patch;

	private boolean patchReadIn = false;

	public PatchWizard(IStorage patch, IResource target, CompareConfiguration configuration) {
		Assert.isNotNull(configuration);
		this.fConfiguration = configuration;
		setDefaultPageImageDescriptor(CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.png")); //$NON-NLS-1$
		setWindowTitle(PatchMessages.PatchWizard_title);
		initializeDialogSettings();
		fPatcher= new WorkspacePatcher(target);
		if (patch != null)
			readPatch(patch);
	}
	
	private void initializeDialogSettings() {
		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
		if (section == null) {
			fHasNewDialogSettings= true;
		} else {
			fHasNewDialogSettings= false;
			setDialogSettings(section);
		}
	}

	private void readPatch(IStorage thePatch) {
		//make sure that the reader always get closed
		this.patch = thePatch;
		Reader reader = null;
		try{
			reader = createPatchReader(patch);
			if (reader != null){
				readInPatch(reader);
			}
		} finally {
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e) { //ignored
				}
			}
		}
	}
	
	private void readInPatch(Reader reader) {
		if (reader != null) {
			try {
				fPatcher.parse(new BufferedReader(reader));
				patchReadIn=true;
			} catch (IOException ex) {
				MessageDialog.openError(null,
					PatchMessages.InputPatchPage_PatchErrorDialog_title, 
					PatchMessages.InputPatchPage_ParseError_message); 
			}
		}
	}

	private Reader createPatchReader(IStorage file) {
		String patchFilePath= file.getFullPath().toString();
		Reader reader = null;
		if (patchFilePath != null) {
			try {
				reader= new FileReader(patchFilePath);
			} catch (FileNotFoundException ex) {
				MessageDialog.openError(null,
					PatchMessages.InputPatchPage_PatchErrorDialog_title,	
					PatchMessages.InputPatchPage_PatchFileNotFound_message); 
			}
		}
		
		return reader;
	}

	WorkspacePatcher getPatcher() {
		return fPatcher;
	}
	
	IResource getTarget() {
		return fPatcher.getTarget();
	}
	
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		addPage(fPatchWizardPage = new InputPatchPage(this));
		addPage(fPatchTargetPage = new PatchTargetPage(this));
		fPreviewPage2 = new PreviewPatchPage2(fPatcher, fConfiguration);
		addPage(fPreviewPage2);
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		
		IWizardPage currentPage = fDialog.getCurrentPage();
		if (currentPage.getName().equals(PreviewPatchPage2.PREVIEWPATCHPAGE_NAME)){
			PreviewPatchPage2 previewPage = (PreviewPatchPage2) currentPage;
			previewPage.ensureContentsSaved();
		}
		
		if (fPatchWizardPage != null){
			fPatcher.setName(fPatchWizardPage.getPatchName());
			// make sure that the patch has been read
			if (!fPatchWizardPage.isPatchRead())
				fPatchWizardPage.readInPatch();
		} else {
			//either we have a patch from the patch input page or one has
			//been specified; double check this
			Assert.isNotNull(patch);
			fPatcher.setName(patch.getFullPath().toString());
			//make sure that the patch has been read in
			Assert.isTrue(patchReadIn);
		}
		
		try {
			// create scheduling rule based on the type of patch - single or workspace
			ISchedulingRule scheduleRule = null;
			if (fPatcher.isWorkspacePatch()) {
				// workspace patch 
				ISchedulingRule[] projectRules = fPatcher.getTargetProjects();
				scheduleRule = new MultiRule(projectRules);
			} else {
				// single patch
				IResource resource = getTarget();
				scheduleRule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(resource);
			}

			WorkspaceModifyOperation op = new WorkspaceModifyOperation(scheduleRule) {
				protected void execute(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						fPatcher.applyAll(monitor, getShell(), PatchMessages.PatchWizard_title);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			getContainer().run(true, false, op);

		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, PatchMessages.PatchWizard_title, PatchMessages.PatchWizard_unexpectedException_message);
		} catch (InterruptedException e) {
			// cannot happen
			// NeedWork: use assert!
		}

		// Save the dialog settings
		if (fHasNewDialogSettings) {
			IDialogSettings workbenchSettings = CompareUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
			section = workbenchSettings.addNewSection(DIALOG_SETTINGS_KEY);
			setDialogSettings(section);
		}

		if (fPatchWizardPage != null)
			fPatchWizardPage.saveWidgetValues();
			//fPreviewPatchPage.saveWidgetValues();
		return true;
	}

	public void setDialog(PatchWizardDialog dialog) {
		fDialog= dialog;
	}
	
	public void showPage(IWizardPage page) {
		fDialog.showPage(page);
	}
	
	public IWizardPage getNextPage(IWizardPage page) {
		//no patch has been read in yet, input patch page
		if (!patchReadIn)
			return fPatchWizardPage;

		//Check to see if we're already on the patch target page and if
		//a target has been set - if it has return the next page in sequence (the preview patch page)
		if (page instanceof PatchTargetPage && getTarget() != null) {
			return super.getNextPage(page);
		} else if (page instanceof InputPatchPage && !fPatcher.isWorkspacePatch()) {
			//Check to see if we need a target
			return fPatchTargetPage;
		}
		return super.getNextPage(page);
	}

	/**
	 * Used to report that the patch has
	 * 
	 */
	protected void patchReadIn() {
		patchReadIn = true;
	}

	public CompareConfiguration getCompareConfiguration() {
		return fConfiguration;
	}
	
	public boolean canFinish() {
		IWizardPage currentPage = fDialog.getCurrentPage();
		if (currentPage.getName().equals(PreviewPatchPage2.PREVIEWPATCHPAGE_NAME)){
			return currentPage.isPageComplete();
		}
		return super.canFinish();
	}
	
}
