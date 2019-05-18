/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.debug.internal.ui.views.console;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * ConsoleShowPreferencesAction Displays the Console's Preference page
 *
 * @since 3.2
 */
public class ConsoleShowPreferencesAction extends Action implements IViewActionDelegate {

	@Override
	public void init(IViewPart view) {}

	@Override
	public void run(IAction action) {
		SWTFactory.showPreferencePage("org.eclipse.debug.ui.ConsolePreferencePage"); //$NON-NLS-1$
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing
	}
}
