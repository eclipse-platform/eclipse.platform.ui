/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.doc.internal.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.validation.TocValidator;
import org.eclipse.help.internal.validation.TocValidator.BrokenLink;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ua.tests.doc.internal.dialogs.SelectTocDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.xml.sax.SAXException;

public class CheckTocAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
    public static List errors = new ArrayList();
    
    public static void showErrors() {
    	if (errors.size() == 0) {
    		reportStatus("No errors detected in load");
    	}
    	for (int i = 0; i < errors.size(); i++) {
			BrokenLink link = (BrokenLink)errors.get(i);
			reportStatus("Invalid link in \"" + link.getTocID() + "\": " + link.getHref());
		}
	}

	private static void reportStatus(String errorMessage) {
		HelpPlugin.logWarning(errorMessage);
	}
	
	
	/**
	 * The constructor.
	 */
	public CheckTocAction() {
	}

	/**
	 * The action has been activated. The argument of the
	 * method represents the 'real' action sitting
	 * in the workbench UI.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		SelectTocDialog dlg = new SelectTocDialog(window.getShell());
		dlg.open();
		if (dlg.getReturnCode() == SelectTocDialog.CANCEL) {
			return;
		}
		Toc[] tocsToCheck = dlg.getTocsToCheck();
	    checkTocFilesExist(tocsToCheck);

	}

	public  void checkTocFilesExist(Toc[] tocsToCheck) {
		for (int i = 0; i < tocsToCheck.length; i++) {
			Toc toc = tocsToCheck[i];
			String id = toc.getTocContribution().getId();
			reportStatus("Testing " + id);
			String[] href = { id };
			try {
				errors = TocValidator.validate(href);
			} catch (Exception e) {
				e.printStackTrace();
			}	
			showErrors();
		}
	}



	/**
	 * Selection in the workbench has been changed. We 
	 * can change the state of the 'real' action here
	 * if we want, but this can only happen after 
	 * the delegate has been created.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system
	 * resources we previously allocated.
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to
	 * be able to provide parent shell for the message dialog.
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}