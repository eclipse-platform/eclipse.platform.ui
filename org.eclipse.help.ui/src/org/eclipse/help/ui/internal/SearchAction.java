/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

public class SearchAction implements IWorkbenchWindowActionDelegate {

    public SearchAction() {
    }

	@Override
	public void dispose() {
    }

	@Override
	public void init(IWorkbenchWindow window) {
    }

	@Override
	public void run(IAction action) {
        PlatformUI.getWorkbench().getHelpSystem().displaySearch();
    }

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
    }
}
