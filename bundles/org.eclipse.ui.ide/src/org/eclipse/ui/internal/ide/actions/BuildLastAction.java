/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.ide.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * Repeats the build of the last working set to be built. Does nothing if there have
 * been no working set builds during this session.
 * 
 * @since 3.0
 */
public class BuildLastAction implements IWorkbenchWindowActionDelegate {
	public void dispose() {}
	public void init(IWorkbenchWindow window) {}
	public void run(IAction action) {
		if (BuildSetAction.lastBuilt != null)
			BuildSetAction.lastBuilt.run();
	}
	public void selectionChanged(IAction action, ISelection selection) {}
}
