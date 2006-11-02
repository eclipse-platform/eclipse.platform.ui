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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ExceptionHandler;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
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
	
	private WorkspacePatcher fPatcher;
	private PatchWizardDialog fDialog;
	
	private CompareConfiguration previewPatchPageConfiguration;
	private IStorage patch;

	private boolean patchReadIn;
	
	private HashSet modDiffs;
	private HashMap modFiles;
	
	/*
	 * Creates a wizard for applying a patch file to the workspace.
	 */
	public PatchWizard(ISelection selection) {

		setDefaultPageImageDescriptor(CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.png")); //$NON-NLS-1$
		setWindowTitle(PatchMessages.PatchWizard_title);

		fPatcher= new WorkspacePatcher();
		patchReadIn = false;
		setTarget(Utilities.getFirstResource(selection));

		IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
		if (section == null) {
			fHasNewDialogSettings= true;
		} else {
			fHasNewDialogSettings= false;
			setDialogSettings(section);
		}
	}


	public PatchWizard(IStorage patch, IResource target, CompareConfiguration previewPatchConfiguration, String patchWizardTitle, ImageDescriptor patchWizardImage) {
		
		if (patchWizardImage != null)
			setDefaultPageImageDescriptor(patchWizardImage);
		else
			setDefaultPageImageDescriptor(CompareUIPlugin.getImageDescriptor("wizban/applypatch_wizban.png")); //$NON-NLS-1$
		
			
		if (patchWizardTitle != null)
			setWindowTitle(patchWizardTitle);
		else
			setWindowTitle(PatchMessages.PatchWizard_title);

		this.patch = patch;
		this.previewPatchPageConfiguration = previewPatchConfiguration;
		
		fPatcher= new WorkspacePatcher();
		patchReadIn = false;
		setTarget(target);
		
		//make sure that the reader always get closed
		Reader reader = null;
		try{
			if (patch != null){
				reader = createPatchReader(patch);
				if (reader != null){
					readInPatch(reader);
				}
			}
		} finally {
			if (reader != null){
				try {
					reader.close();
				} catch (IOException e) { //ignored
				}
			}
			IDialogSettings workbenchSettings= CompareUIPlugin.getDefault().getDialogSettings();
			IDialogSettings section= workbenchSettings.getSection(DIALOG_SETTINGS_KEY);
			if (section == null) {
				fHasNewDialogSettings= true;
			} else {
				fHasNewDialogSettings= false;
				setDialogSettings(section);
			}
		}
	}

	protected void readInPatch(Reader reader) {
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

	void setTarget(IResource target) {
		fPatcher.setTarget(target);
	}
	
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public void addPages() {
		addPage(fPatchWizardPage = new InputPatchPage(this));
		addPage(fPatchTargetPage = new PatchTargetPage(this));
		
		IPreferenceStore store = CompareUIPlugin.getDefault().getPreferenceStore();
		boolean preference = store.getBoolean(ComparePreferencePage.USE_OLDAPPLYPATCH);
		if (System.getProperty("oldPatch") != null || preference) //$NON-NLS-1$
			addPage(new PreviewPatchPage(this));
		else {
			if (previewPatchPageConfiguration != null)
				fPreviewPage2 = new PreviewPatchPage2(this,
						previewPatchPageConfiguration);
			else
				fPreviewPage2 = new PreviewPatchPage2(this);
			addPage(fPreviewPage2);
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean needsProgressMonitor() {
		return true;
	}

	/* (non-Javadoc)
	 * Method declared on IWizard.
	 */
	public boolean performFinish() {
		
		//Before applying all of the enabled diffs you have to go through and disable the diffs
		//that have been merged by hand - this only applies if the current page is the hunk merge page
		IWizardPage currentPage = fDialog.getCurrentPage();
		ISchedulingRule[] retargetedFiles = new ISchedulingRule[0];
		
		
		if (currentPage.getName().equals(PreviewPatchPage2.PREVIEWPATCHPAGE_NAME)){
			Diff[] diffs = fPatcher.getDiffs();
			PreviewPatchPage2 previewPage = (PreviewPatchPage2) currentPage;
			
			previewPage.ensureContentsSaved();
			
			modDiffs = previewPage.getModifiedDiffs();
			modFiles = previewPage.getMergedFileContents();
			
			ArrayList retargetedFilesSchedulingRules = new ArrayList();
			for (int i = 0; i < diffs.length; i++) {
				//if this diff has been modified by hand, mark it as disabled to prevent
				//the patcher from modifying it later
				if (modDiffs.contains(diffs[i])){
					diffs[i].setEnabled(false);
				}
				
				if (diffs[i].isRetargeted())
					retargetedFilesSchedulingRules.add(diffs[i].getTargetFile());
			}
			retargetedFiles = (ISchedulingRule[]) retargetedFilesSchedulingRules.toArray(new ISchedulingRule[retargetedFilesSchedulingRules.size()]);
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
				ISchedulingRule[] allSchedulingRules = new ISchedulingRule[projectRules.length + retargetedFiles.length];
				System.arraycopy(projectRules, 0, allSchedulingRules, 0, projectRules.length);
				System.arraycopy(retargetedFiles, 0, allSchedulingRules, projectRules.length, retargetedFiles.length);
				scheduleRule = new MultiRule(allSchedulingRules);
			} else {
				// single patch
				IResource resource = getTarget();
				scheduleRule = ResourcesPlugin.getWorkspace().getRuleFactory().modifyRule(resource);
			}

			WorkspaceModifyOperation op = new WorkspaceModifyOperation(scheduleRule) {
				protected void execute(IProgressMonitor monitor) throws InvocationTargetException {
					try {
						fPatcher.applyAll(monitor, getShell(), PatchMessages.PatchWizard_title);
						writePatchedFiles(monitor);
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

	private void writePatchedFiles(IProgressMonitor monitor) throws CoreException{
		if (modDiffs != null){
			Iterator iter = modDiffs.iterator();
			while (iter.hasNext()){
				Diff diff = (Diff) iter.next();
				PatchedFileNode patchedFile = (PatchedFileNode) modFiles.get(diff);
				fPatcher.store(patchedFile.getBytes(), diff.getTargetFile(), monitor);
			}
		}
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
	
}
