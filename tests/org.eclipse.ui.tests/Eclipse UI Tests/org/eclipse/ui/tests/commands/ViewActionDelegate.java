/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui.tests.commands;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.tests.api.workbenchpart.MenuContributionHarness;

/**
 * A view delegate for testing.
 * @since 3.3
 */
public class ViewActionDelegate implements IViewActionDelegate {

	private IViewPart viewPart;

	@Override
	public void init(IViewPart view) {
		viewPart = view;
	}

	@Override
	public void run(IAction action) {
		if (viewPart instanceof MenuContributionHarness) {
			((MenuContributionHarness)viewPart).updateCount();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// don't care
	}
}
