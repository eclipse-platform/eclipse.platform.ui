/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.search;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.*;

public class FindUnusedMethodsAction implements IObjectActionDelegate {

	private IStructuredSelection selection;
	private int unusedCount;
	private IWorkbenchPart part;

	public void setActivePart(IAction action, IWorkbenchPart part) {
		this.part = part;
		//not needed
	}

	public void run(IAction action) {
		FileDialog dialog = new FileDialog(part.getSite().getShell(), SWT.SAVE);
		String outFileName = dialog.open();
		if (outFileName == null)
			return;
		File outputFile = new File(outFileName);
		if (outputFile.exists())
			outputFile.delete();
		try {
			FileWriter writer = new FileWriter(outputFile);
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object element = it.next();
				if (element instanceof IJavaElement)
					traverse((IJavaElement)element, writer);
			}
			String summary = "\n\nSearch complete.  Found " + unusedCount + " unreferenced methods."; //$NON-NLS-1$ //$NON-NLS-2$
			writer.write(summary);
			writer.close();
			MessageDialog.openInformation(part.getSite().getShell(), "Search Complete", summary);   //$NON-NLS-1$
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			//ignore
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void traverse(IJavaElement current, Writer output) throws JavaModelException, InvocationTargetException, InterruptedException {
		if (current instanceof IJavaProject || current instanceof IPackageFragmentRoot) {
			IJavaElement[] children = ((IParent) current).getChildren();
			for (int i = 0; i < children.length; i++) {
				traverse(children[i], output);
			}
		} else if (current instanceof IPackageFragment) {
			//don't search API packages
			if (current.getElementName().indexOf("internal") > 0) { //$NON-NLS-1$
				IJavaElement[] children = ((IParent) current).getChildren();
				for (int i = 0; i < children.length; i++) {
					traverse(children[i], output);
				}
			}
		} else if (current instanceof ICompilationUnit)
			traverseCU((ICompilationUnit)current, output);
	}

	protected void traverseCU(ICompilationUnit unit, Writer output) throws InvocationTargetException, InterruptedException {
		FindUnusedMethods search = new FindUnusedMethods(unit, output);
		PlatformUI.getWorkbench().getProgressService().run(true, true, search);
		unusedCount += search.getUnusedMethodCount();
	}

	public void selectionChanged(IAction action, ISelection aSelection) {
		if (aSelection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) aSelection;
	}
}