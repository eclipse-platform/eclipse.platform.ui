/**********************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ant.internal.ui;
 
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Superclass of run & debug pulldown actions.
 */
public class AntLaunchDropDownAction implements IWorkbenchWindowPulldownDelegate {
	

/**
 * @see IWorkbenchWindowActionDelegate
 */
public void dispose() {
}

private void createMenuForAction(Menu parent, Action action) {
	ActionContributionItem item= new ActionContributionItem(action);
	item.fill(parent, -1);
}

/**
 * @see IWorkbenchWindowPulldownDelegate
 */
public Menu getMenu(Control parent) {
	Menu menu= new Menu(parent);
	IFile[] historyList= AntUIPlugin.getPlugin().getHistory();
	int count= 0;
	for (int i = 0; i < historyList.length; i++) {
		IFile file = historyList[i];
		if (file == null) 
			break;
		AntAction newAction= new AntAction(file);
		createMenuForAction(menu, newAction);
		count++;
	}
	if (count == 0)
		createMenuForAction(menu, new AntAction(null));

	return menu;
}

/**
 * @see IWorkbenchWindowActionDelegate
 */
public void init(IWorkbenchWindow window){
}

/**
 * @see IActionDelegate
 */
public void run(IAction action) {
	IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	IStructuredSelection selection= resolveSelection(window);
	
	if (selection != null) {
		Object selectedObject = selection.getFirstElement();
		if (selectedObject instanceof IFile) {
			if (((IFile) selectedObject).getFileExtension().equals("xml"))
				new AntAction((IFile) selectedObject).run();
		}
		else if (selectedObject instanceof FileEditorInput) {
			if ((((FileEditorInput) selectedObject).getFile()).getFileExtension().equals("xml"))
				new AntAction(((FileEditorInput) selectedObject).getFile()).run();
		}
	}
}

/**
 * Determines and returns the selection that provides context for the launch,
 * or <code>null</code> if there is no selection.
 */
protected IStructuredSelection resolveSelection(IWorkbenchWindow window) {
	if (window == null) {
		return null;
	}
	ISelection selection= window.getSelectionService().getSelection();
	if (selection == null || selection.isEmpty() || !(selection instanceof IStructuredSelection)) {
		// there is no obvious selection - go fishing
		selection= null;
		IWorkbenchPage page= window.getActivePage();
		if (page == null) {
			//workspace is closed
			return null;
		}

		// first, see if there is an active editor, and try its input element
		IEditorPart editor= page.getActiveEditor();
		Object element= null;
		if (editor != null) {
			element= editor.getEditorInput();
		}

		if (selection == null && element != null) {
			selection= new StructuredSelection(element);
		}
	}
	return (IStructuredSelection)selection;
}

/**
 * @see IActionDelegate
 */
public void selectionChanged(IAction action, ISelection selection){
}

}
