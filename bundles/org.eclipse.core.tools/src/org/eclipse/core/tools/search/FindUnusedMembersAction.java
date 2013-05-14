/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.search;

import java.io.File;
import java.io.FileWriter;
import java.util.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.*;

public class FindUnusedMembersAction implements IObjectActionDelegate {

	/**
	 * This flag controls how the search results are presented.  If <code>true</code>,
	 * the results are shown in the search view.  If <code>false</code>, the
	 * results are written to a file.
	 */
	private static final boolean SHOW_RESULTS_IN_SEARCH_VIEW = true;
	private IStructuredSelection selection;
	private IWorkbenchPart part;

	public void setActivePart(IAction action, IWorkbenchPart part) {
		this.part = part;
		//not needed
	}

	public void run(IAction action) {
		ArrayList allCus = new ArrayList();
		try {
			for (Iterator it = selection.iterator(); it.hasNext();) {
				Object element = it.next();
				if (element instanceof IJavaElement)
					collectCompilationUnits((IJavaElement) element, allCus);
			}
		} catch (JavaModelException e) {
			ErrorDialog.openError(part.getSite().getShell(), "Find Unused Members", "Problem collecting compilation units", e.getStatus()); //$NON-NLS-1$//$NON-NLS-2$
			return;
		}

		if (allCus.isEmpty()) {
			MessageDialog.openInformation(part.getSite().getShell(), "Find Unused Members", "No compilation units in 'internal' packages selected"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		ICompilationUnit[] cus = (ICompilationUnit[]) allCus.toArray(new ICompilationUnit[allCus.size()]);

		if (SHOW_RESULTS_IN_SEARCH_VIEW) {
			NewSearchUI.runQueryInBackground(new FindUnusedSearchQuery(cus));
			return;
		}

		//prompt the user for a file to write search results to
		FileDialog dialog = new FileDialog(part.getSite().getShell(), SWT.SAVE);
		String outFileName = dialog.open();
		if (outFileName == null)
			return;
		File outputFile = new File(outFileName);
		if (outputFile.exists())
			outputFile.delete();

		FileWriter writer = null;
		try {
			int unusedCount = 0;
			try {
				writer = new FileWriter(outputFile);
				FindUnusedMembers search = new FindUnusedMembers(cus, writer);
				PlatformUI.getWorkbench().getProgressService().run(true, true, search);
				unusedCount = search.getUnusedMethodCount();
			} finally {
				String summary = "Search complete.  Found " + unusedCount + " unreferenced methods."; //$NON-NLS-1$ //$NON-NLS-2$
				if (writer != null) {
					writer.write("\n\n"); //$NON-NLS-1$
					writer.write(summary);
					writer.close();
				}
				MessageDialog.openInformation(part.getSite().getShell(), "Search Complete", summary); //$NON-NLS-1$
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void collectCompilationUnits(IJavaElement current, Collection res) throws JavaModelException {
		if (current instanceof IJavaProject || current instanceof IPackageFragmentRoot) {
			IJavaElement[] children = ((IParent) current).getChildren();
			for (int i = 0; i < children.length; i++) {
				collectCompilationUnits(children[i], res);
			}
		} else if (current instanceof IPackageFragment) {
			//uncomment this condition to only search API packages
//			if (current.getElementName().indexOf("internal") > 0) { //$NON-NLS-1$
				IJavaElement[] children = ((IParent) current).getChildren();
				for (int i = 0; i < children.length; i++) {
					collectCompilationUnits(children[i], res);
				}
//			}
		} else if (current instanceof ICompilationUnit)
			res.add(current);
	}

	public void selectionChanged(IAction action, ISelection aSelection) {
		if (aSelection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) aSelection;
	}
}