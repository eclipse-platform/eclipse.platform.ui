/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tools.search;

import java.util.Iterator;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.search.ui.*;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search2.internal.ui.text2.DefaultTextSearchQueryProvider;
import org.eclipse.ui.*;

public class FindUnreferencedFilesAction implements IObjectActionDelegate {

	private IStructuredSelection selection;

	public void setActivePart(IAction action, IWorkbenchPart part) {
		//not needed
	}

	public void run(IAction action) {
		try {
			for (Iterator it = selection.iterator(); it.hasNext();) {
				IResource resource = (IResource)it.next();
				findReferences(resource);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void findReferences(IResource resource) throws CoreException {
		if (resource instanceof IContainer) {
			IResource[] children = ((IContainer)resource).members();
			for (int i = 0; i < children.length; i++) {
				findReferences(children[i]);
			}
		} else if (resource instanceof IFile) {
			String name = resource.getName();
			IRunnableContext context = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			ISearchQuery query = DefaultTextSearchQueryProvider.getPreferred().createQuery(name);
			NewSearchUI.runQueryInForeground(context, query);
			ISearchResult result = query.getSearchResult();
			if (result instanceof AbstractTextSearchResult) {
				int matches = ((AbstractTextSearchResult)result).getMatchCount();
				if (matches == 0) {
					System.out.println("Orphan file: " + resource.getFullPath());
				}
			}
		}
		
	}

	public void selectionChanged(IAction action, ISelection aSelection) {
		if (aSelection instanceof IStructuredSelection)
			this.selection = (IStructuredSelection) aSelection;
	}
}