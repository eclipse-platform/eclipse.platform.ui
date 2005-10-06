/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.text.MessageFormat;

import org.eclipse.compare.internal.ICompareContextIds;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/***
 * This page only shows up if the user is trying to apply
 * a non-workspace rooted patch.
 */
public class PatchTargetPage extends WizardPage {

	private boolean fShowError = false;

	// SWT widgets
	private CheckboxTreeViewer fPatchTargets;

	private PatchWizard fPatchWizard;

	protected final static String PATCHTARGETPAGE_NAME = "PatchTargetPage"; //$NON-NLS-1$

	PatchTargetPage(PatchWizard pw) {
		super(PATCHTARGETPAGE_NAME, PatchMessages.PatchTargetPage_title, null);
		fPatchWizard = pw;
		setMessage(PatchMessages.PatchTargetPage_message);
	}

	/*
	 * Get a path from the supplied text widget.
	 * @return org.eclipse.core.runtime.IPath
	 */
	protected IPath getPathFromText(Text textField) {
		return (new Path(textField.getText())).makeAbsolute();
	}

	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		Label l = new Label(composite, SWT.NONE);
		l.setText(PatchMessages.InputPatchPage_SelectInput);

		buildInputGroup(composite);

		updateWidgetEnablements();

		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICompareContextIds.PATCH_INPUT_WIZARD_PAGE);
	}

	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public IWizardPage getNextPage() {

		WorkspacePatcher patcher = ((PatchWizard) getWizard()).getPatcher();

		// if selected target is file ensure that patch file
		// contains only a patch for a single file
		IResource target = fPatchWizard.getTarget();
		if (target instanceof IFile && patcher.getDiffs().length > 1) {
			InputPatchPage inputPage = (InputPatchPage) fPatchWizard.getPage(InputPatchPage.INPUTPATCHPAGE_NAME);
			String source = ""; //$NON-NLS-1$
			switch (inputPage.getInputMethod()) {
				case InputPatchPage.CLIPBOARD :
					source = PatchMessages.InputPatchPage_Clipboard_title;
					break;

				case InputPatchPage.FILE :
					source = PatchMessages.InputPatchPage_PatchFile_title;
					break;

				case InputPatchPage.WORKSPACE :
					source = PatchMessages.InputPatchPage_WorkspacePatch_title;
					break;
			}
			String format = PatchMessages.InputPatchPage_SingleFileError_format;
			String message = MessageFormat.format(format, new String[] {source});
			MessageDialog.openInformation(null, PatchMessages.InputPatchPage_PatchErrorDialog_title, message);
			return this;
		}

		return super.getNextPage();
	}

	/* (non-JavaDoc)
	 * Method declared in IWizardPage.
	 */
	public boolean canFlipToNextPage() {
		// we can't call getNextPage to determine if flipping is allowed since computing
		// the next page is quite expensive. So we say yes if the page is complete.
		return isPageComplete();
	}

	private void buildInputGroup(Composite parent) {

		Tree tree = new Tree(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		tree.setLayoutData(gd);

		fPatchTargets = new CheckboxTreeViewer(tree);
		fPatchTargets.setLabelProvider(new WorkbenchLabelProvider());
		fPatchTargets.setContentProvider(new WorkbenchContentProvider());
		fPatchTargets.setSorter(new WorkbenchViewerSorter());
		fPatchTargets.setInput(ResourcesPlugin.getWorkspace().getRoot());
		
		PatchWizard pw = (PatchWizard) getWizard();
		IResource target = pw.getTarget();
		if (target != null) {
			fPatchTargets.expandToLevel(target, 0);
			fPatchTargets.setSelection(new StructuredSelection(target));
		}

		// register listeners
		fPatchTargets.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				fPatchWizard.setTarget(Utilities.getResource(event.getSelection()));
				updateWidgetEnablements();
			}
		});
	}

	/**
	 * Updates the enable state of this page's controls.
	 */
	private void updateWidgetEnablements() {
		String error = null;

		ISelection selection = fPatchTargets.getSelection();
		boolean anySelected = selection != null && !selection.isEmpty();
		if (!anySelected)
			error = PatchMessages.InputPatchPage_NothingSelected_message;

		setPageComplete(anySelected);
		if (fShowError)
			setErrorMessage(error);
	}

	/**
	 *	The Finish button was pressed. Try to do the required work now and answer
	 *	a boolean indicating success. If false is returned then the wizard will
	 *	not close.
	 *
	 * @return boolean
	 */
	public boolean finish() {
		return true;
	}

}
