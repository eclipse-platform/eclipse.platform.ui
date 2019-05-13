/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.ui.IWorkbenchWindow;

public class ExistingWindowProvider implements IWorkbenchWindowProvider {

	private IWorkbenchWindow window;

	public ExistingWindowProvider(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return window;
	}

}
