/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal.patch;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Button;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.*;
import org.eclipse.jface.action.IAction;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.actions.GlobalBuildAction;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.compare.internal.*;


public class CompareWithPatchAction implements IActionDelegate {

	static class PatchWizardDialog extends WizardDialog {
	
		PatchWizardDialog(Shell parent, IWizard wizard) {
			super(parent, wizard);
			
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setMinimumPageSize(700, 500);
		}
	}
	
	private ISelection fSelection;
	private boolean fSavedFiles;
	private boolean fAutobuildState;
	

	public void selectionChanged(IAction action, ISelection selection) {
		fSelection= selection;
		IResource[] resources= PatchWizard.getResource(fSelection);
		action.setEnabled(resources != null && resources.length == 1);
	}
		
	public void run(IAction action) {
		PatchWizard wizard= new PatchWizard(fSelection);
		
		if (areAllEditorsSaved()) {
			//RefactoringStatus activationStatus= refactoring.checkActivation(new NullProgressMonitor());
			//if (! activationStatus.hasFatalError()){
			//	wizard.setActivationStatus(activationStatus);
				PatchWizardDialog dialog= new PatchWizardDialog(CompareUIPlugin.getShell(), wizard);
				if (dialog.open() == Dialog.CANCEL)
					triggerBuild();
					
			//} else{
				//RefactoringErrorDialog.open(dialogTitle, activationStatus);
			//}
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
				fSavedFiles= true;
			} finally {
				description.setAutoBuilding(autoBuild);
				workspace.setDescription(description);
			}
			return true;
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, shell, PatchMessages.getString("PatchAction.ExceptionTitle"), PatchMessages.getString("Exception"));  //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		} catch (CoreException e) {
			ExceptionHandler.handle(e, shell, PatchMessages.getString("PatchAction.ExceptionTitle"), PatchMessages.getString("Exception"));  //$NON-NLS-1$ //$NON-NLS-2$
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
				pm.beginTask(PatchMessages.getString("PatchAction.SavingDirtyEditorsTask"), editorsToSave.length); //$NON-NLS-1$
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
				check.setText(PatchMessages.getString("PatchAction.AlwaysSaveQuestion")); //$NON-NLS-1$
				check.setSelection(ComparePreferencePage.getSaveAllEditors());
				check.addSelectionListener(
					new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							ComparePreferencePage.setSaveAllEditors(check.getSelection());
						}
					}
				);
				return result;
			}
		};
		dialog.setTitle(PatchMessages.getString("PatchAction.SaveAllQuestion")); //$NON-NLS-1$
		dialog.setAddCancelButton(true);
		dialog.setLabelProvider(createDialogLabelProvider());
		dialog.setMessage(PatchMessages.getString("PatchAction.SaveAllDescription")); //$NON-NLS-1$
		dialog.setContentProvider(new ListContentProvider());
		dialog.setInput(Arrays.asList(CompareUIPlugin.getDirtyEditors()));
		return dialog.open() == Dialog.OK;
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
	
	private void triggerBuild() {
		if (fSavedFiles && ResourcesPlugin.getWorkspace().getDescription().isAutoBuilding()) {
			new GlobalBuildAction(CompareUIPlugin.getActiveWorkbench(), CompareUIPlugin.getShell(), IncrementalProjectBuilder.INCREMENTAL_BUILD).run();
		}
	}
}
