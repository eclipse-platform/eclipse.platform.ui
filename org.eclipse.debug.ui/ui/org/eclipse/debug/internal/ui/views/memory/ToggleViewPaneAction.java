/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @since 3.1
 */
abstract public class ToggleViewPaneAction  extends Action implements IViewActionDelegate, IActionDelegate2, IPropertyChangeListener {

	MemoryView fView;
	IAction fAction;
	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init(IViewPart view) {
		if (view instanceof MemoryView)
		{
			fView = (MemoryView)view;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		
		if (fView == null)
			return;
		
		fView.showViewPane(!fView.isViewPaneVisible(getPaneId()), getPaneId());
		
		if (fView.isViewPaneVisible(getPaneId()))
			action.setChecked(true);
		else
			action.setChecked(false);
		
	}
	
	public void run() {
		if (fView == null)
			return;
		
		fView.showViewPane(!fView.isViewPaneVisible(getPaneId()), getPaneId());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (fView.isViewPaneVisible(getPaneId()))
			action.setChecked(true);
		else
			action.setChecked(false);
	}

	public void dispose() {
		DebugUITools.getPreferenceStore().removePropertyChangeListener(this);
	}

	public void init(IAction action) {
		fAction = action;
		DebugUITools.getPreferenceStore().addPropertyChangeListener(this);
	}

	public void runWithEvent(IAction action, Event event) {
		run(action);
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (fView != null && fAction != null)
		{
			if (fView.isViewPaneVisible(getPaneId()))
				fAction.setChecked(true);
			else
				fAction.setChecked(false);
		}
	}
	
	abstract public String getPaneId();	
}
