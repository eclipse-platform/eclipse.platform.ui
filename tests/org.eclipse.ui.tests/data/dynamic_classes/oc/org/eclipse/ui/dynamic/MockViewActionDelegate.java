/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dynamic;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @since 3.1
 */
public class MockViewActionDelegate implements IViewActionDelegate {

	/**
	 *
	 */
	public MockViewActionDelegate() {
		super();
	}

	@Override
	public void init(IViewPart view) {
	}

	@Override
	public void run(IAction action) {

	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
	}

}
