/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
  *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.browser;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class WebBrowserSyncAction implements IViewActionDelegate {
	protected IViewPart view;

	public void init(IViewPart newView) {
		this.view = newView;
	}

	public void run(IAction action) {
		// ignore
	}

	public void selectionChanged(IAction action, ISelection selection) {
		System.out.println(selection);
	}
}