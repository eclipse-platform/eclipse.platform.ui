/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import java.io.File;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.EditorsUI;

/**
 * @since 3.1
 */
public class NewTextEditorAction extends Action implements IWorkbenchWindowActionDelegate {	
	
	private IWorkbenchWindow fWindow;

	public NewTextEditorAction() {
		setEnabled(true);
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow= null;
	}

	/*
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		fWindow= window;
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		run();
	}

	/*
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// ignore
	}

	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		File file= queryFile();
		IEditorInput input= createEditorInput(file);
		String editorId= getEditorId(file);
		IWorkbenchPage page= fWindow.getActivePage();
		try {
			page.openEditor(input, editorId);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private File queryFile() {
		IPath stateLocation= EditorsPlugin.getDefault().getStateLocation();
		IPath path= stateLocation.append("/_" + new Object().hashCode()); //$NON-NLS-1$ //$NON-NLS-2$
		return new File(path.toOSString());
	}

	private String getEditorId(File file) {
		IWorkbench workbench= fWindow.getWorkbench();
		IEditorRegistry editorRegistry= workbench.getEditorRegistry();
		IEditorDescriptor descriptor= editorRegistry.getDefaultEditor(file.getName());
		if (descriptor != null)
			return descriptor.getId();
		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
	}

	private IEditorInput createEditorInput(File file) {
		return new NonExistingFileEditorInput(file, TextEditorMessages.getString("NewTextEditorAction.namePrefix"));
	}
}
