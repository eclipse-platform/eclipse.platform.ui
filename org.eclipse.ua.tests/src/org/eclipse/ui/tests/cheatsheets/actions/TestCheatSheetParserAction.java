/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.cheatsheets.actions;

import java.net.*;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.cheatsheets.data.*;

public class TestCheatSheetParserAction implements IObjectActionDelegate {
	private ISelection selection;

	/**
	 * Constructor for Action1.
	 */
	public TestCheatSheetParserAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if(selection != null && selection instanceof IStructuredSelection) {
			for (Iterator iter = ((IStructuredSelection)selection).iterator(); iter.hasNext();) {
				Object obj = iter.next();
				
				System.out.println();
				System.out.println(obj.toString());

				if(obj instanceof IFile) {
					IFile selectedFile = (IFile)obj;
					
					System.out.print("About to parse file, ");
					System.out.println(selectedFile.getFullPath().toString());

					try {
						CheatSheetParser parser = new CheatSheetParser();
						CheatSheet cheatSheet = parser.parse(new URL("file:/"+selectedFile.getLocation().toOSString()));
						if(cheatSheet == null) {
							System.out.println("FAIL: Parsing of the cheat sheet failed.");
						} else {
							System.out.println("PASS: Parsing of the cheat sheet was successful.");
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		this.selection = selection;
	}

}
