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
package org.eclipse.team.internal.ui.synchronize.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.ui.synchronize.views.SyncSetContentProvider;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.actions.OpenFileAction;

public class OpenFileInSystemEditorAction extends OpenFileAction {

	public OpenFileInSystemEditorAction(IWorkbenchPage page) {
		super(page);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionListenerAction#getSelectedResources()
	 */
	protected List getSelectedResources() {
		List resources = new ArrayList();
		IStructuredSelection selection = getStructuredSelection();
		for (Iterator e = selection.iterator(); e.hasNext();) {
			Object next = e.next();
			IResource resource = SyncSetContentProvider.getResource(next);
			if(resource != null) {
				resources.add(resource);
			}
		}
		return resources;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.SelectionListenerAction#getSelectedNonResources()
	 */
	protected List getSelectedNonResources() {		
		return Collections.EMPTY_LIST;
	}
}
