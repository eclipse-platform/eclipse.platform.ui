/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.eclipse.compare.internal.BaseCompareAction;
import org.eclipse.compare.internal.ComparePreferencePage;
import org.eclipse.compare.internal.CompareUIPlugin;
import org.eclipse.compare.internal.ExceptionHandler;
import org.eclipse.compare.internal.ListContentProvider;
import org.eclipse.compare.internal.ListDialog;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;


public class CompareWithPatchAction extends BaseCompareAction {

	static class PatchWizardDialog extends WizardDialog {
	
		private static final String PATCH_WIZARD_SETTINGS_SECTION = "PatchWizard"; //$NON-NLS-1$

		PatchWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
			
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setMinimumPageSize(700, 500);
		}
		
		protected IDialogSettings getDialogBoundsSettings() {
	        IDialogSettings settings = CompareUIPlugin.getDefault().getDialogSettings();
	        IDialogSettings section = settings.getSection(PATCH_WIZARD_SETTINGS_SECTION);
	        if (section == null) {
	            section = settings.addNewSection(PATCH_WIZARD_SETTINGS_SECTION);
	        } 
	        return section;
		}
	}
	
	protected boolean isEnabled(ISelection selection) {
		return Utilities.getResources(selection).length == 1;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.compare.internal.BaseCompareAction#run(org.eclipse.jface.viewers.ISelection)
	 */
	protected void run(ISelection selection) {
		PatchWizard wizard= new PatchWizard(selection);
		
		if (areAllEditorsSaved()) {
			PatchWizardDialog dialog= new PatchWizardDialog(CompareUIPlugin.getShell(), wizard);
			dialog.open();
		}
	}

	private boolean areAllEditorsSaved(){
		if (CompareUIPlugin.getDirtyEditors().length == 0)
			return true;
		if (! saveAllDirtyEditors())
			return false;
		Shell shell= CompareUIPlugin.getShell();
		try {
			// Save isn't cancelable.
			IWorkspace workspace= ResourcesPlugin.getWorkspace();
			IWorkspaceDescription description= workspace.getDescription();
			boolean autoBuild= description.isAutoBuilding();
			description.setAutoBuilding(false);
			workspace.setDescription(description);
			try {
				new ProgressMonitorDialog(shell).run(false, false, createRunnable());
			} finally {
				description.setAutoBuilding(autoBuild);
				workspace.setDescription(description);
			}
			return true;
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, shell, PatchMessages.PatchAction_ExceptionTitle, PatchMessages.PatchAction_Exception);  
			return false;
		} catch (CoreException e) {
			ExceptionHandler.handle(e, shell, PatchMessages.PatchAction_ExceptionTitle, PatchMessages.PatchAction_Exception);  
			return false;			
		} catch (InterruptedException e) {
			Assert.isTrue(false); // Can't happen. Operation isn't cancelable.
			return false;
		}
	}

	private IRunnableWithProgress createRunnable() {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				IEditorPart[] editorsToSave= CompareUIPlugin.getDirtyEditors();
				pm.beginTask(PatchMessages.PatchAction_SavingDirtyEditorsTask, editorsToSave.length); 
				for (int i= 0; i < editorsToSave.length; i++) {
					editorsToSave[i].doSave(new SubProgressMonitor(pm, 1));
					pm.worked(1);
				}
				pm.done();
			}
		};
	}

	private boolean saveAllDirtyEditors() {
		if (ComparePreferencePage.getSaveAllEditors()) //must save everything
			return true;
		ListDialog dialog= new ListDialog(CompareUIPlugin.getShell()) {
			protected Control createDialogArea(Composite parent) {
				Composite result= (Composite) super.createDialogArea(parent);
				final Button check= new Button(result, SWT.CHECK);
				check.setText(PatchMessages.PatchAction_AlwaysSaveQuestion); 
				check.setSelection(ComparePreferencePage.getSaveAllEditors());
				check.addSelectionListener(
					new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							ComparePreferencePage.setSaveAllEditors(check.getSelection());
						}
					}
				);
				applyDialogFont(result);
				return result;
			}
		};
		dialog.setTitle(PatchMessages.PatchAction_SaveAllQuestion); 
		dialog.setAddCancelButton(true);
		dialog.setLabelProvider(createDialogLabelProvider());
		dialog.setMessage(PatchMessages.PatchAction_SaveAllDescription); 
		dialog.setContentProvider(new ListContentProvider());
		dialog.setInput(Arrays.asList(CompareUIPlugin.getDirtyEditors()));
		return dialog.open() == Window.OK;
	}

	private ILabelProvider createDialogLabelProvider() {
		return new LabelProvider() {
			public Image getImage(Object element) {
				return ((IEditorPart) element).getTitleImage();
			}
			public String getText(Object element) {
				return ((IEditorPart) element).getTitle();
			}
		};
	}
}
