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
package org.eclipse.update.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.*;
import org.eclipse.update.core.ISite;
import org.eclipse.update.internal.ui.search.*;
import org.eclipse.update.internal.ui.wizards.*;
import org.eclipse.update.internal.ui.wizards.NewUpdatesWizard;

/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class NewUpdatesAction implements IWorkbenchWindowActionDelegate {
	private static final String KEY_TITLE = "NewUpdates.noUpdates.title";
	private static final String KEY_MESSAGE = "NewUpdates.noUpdates.message";
	IWorkbenchWindow window;
	SearchObject searchObject;
	ISearchCategory category;
	/**
	 * The constructor.
	 */
	public NewUpdatesAction() {
	}

	private void initialize() {
		searchObject = new DefaultUpdatesSearchObject();
		String categoryId = searchObject.getCategoryId();
		SearchCategoryDescriptor desc =
			SearchCategoryRegistryReader.getDefault().getDescriptor(categoryId);
		category = desc.createCategory();
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		BusyIndicator
			.showWhile(window.getShell().getDisplay(), new Runnable() {
			public void run() {
				doRun();
			}
		});
	}

	private void doRun() {
		initialize();
		ProgressMonitorDialog pmd =
			new ProgressMonitorDialog(window.getShell());
		try {
			pmd.run(true, true, getOperation());
			showResults();
		} catch (InterruptedException e) {
			UpdateUI.logException(e);
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				CoreException ce = (CoreException)t;
				IStatus status = ce.getStatus();
				if (status!=null &&
					status.getCode()==ISite.SITE_ACCESS_EXCEPTION) {
					// Just show this but do not throw exception
					// because there may be results anyway.
					showConnectionErrors(status);
					showResults();
					return;
				}
			}
			UpdateUI.logException(e);
		}
	}
	
	private void showResults() {
		if (searchObject.hasChildren())
			openNewUpdatesWizard();
		else
			showNoUpdatesMessage();
	}
	
	private void showConnectionErrors(IStatus status) {
		ErrorDialog.openError(window.getShell(),
			UpdateUI.getString(KEY_TITLE),
			null, 
			status);
	}			
			

	private void showNoUpdatesMessage() {
		MessageDialog.openInformation(
			window.getShell(),
			UpdateUI.getString(KEY_TITLE),
			UpdateUI.getString(KEY_MESSAGE));
	}

	private void openNewUpdatesWizard() {
		NewUpdatesWizard wizard = new NewUpdatesWizard(searchObject);
		WizardDialog dialog = new ResizableWizardDialog(window.getShell(), wizard);
		dialog.create();
		dialog.getShell().setText(UpdateUI.getString(KEY_TITLE));
		dialog.getShell().setSize(600, 500);
		dialog.open();
		if (wizard.isSuccessfulInstall())
			UpdateUI.informRestartNeeded();
	}

	private IRunnableWithProgress getOperation() {
		return searchObject.getSearchOperation(
			window.getShell().getDisplay(),
			category.getQueries());
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}