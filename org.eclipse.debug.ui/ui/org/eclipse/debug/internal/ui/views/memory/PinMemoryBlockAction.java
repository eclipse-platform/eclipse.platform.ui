/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action for pinning the display of the memory view to
 * the current memory block.  The view would not change
 * selection when a new memory block is added if the diplay is pinned.
 *
 */
public class PinMemoryBlockAction implements IViewActionDelegate {

	private MemoryView fView;
	public void init(IViewPart view) {
		if (view instanceof MemoryView)
			fView = (MemoryView)view;

	}

	public void run(IAction action) {
		if (fView == null)
			return;
		
		boolean pin = !fView.isPinMBDisplay();
		fView.setPinMBDisplay(pin);
		
		action.setChecked(pin);
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
