/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.compare.CompareViewerSwitchingPane;
import org.eclipse.compare.internal.INavigatable;
import org.eclipse.compare.internal.IOpenable;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.team.internal.ui.synchronize.actions.NavigateAction;

/**
 * A navigator that coordinates navigation between several navigable
 * objects. This is copied from the compare plugin and enhanced to
 * support navigating adaptables.
 * <p>
 * This navigator can be used as input to the {@link NavigateAction}
 * actions and should be passed to the actions via the 
 * {@link SynchronizePageConfiguration.P_NAVIGATOR}.
 * </p>
 * @since 3.0
 */
public class PartNavigator implements INavigatable {
	
	private Object[] fPanes;
	// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	private boolean fNextFirstTime= true;
	
	public PartNavigator(Object[] panes) {
		fPanes= panes;
	}

	public Object[] getPanes() {
		return fPanes;
	}
	
	public boolean gotoDifference(boolean next) {

		// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
		if (next && fNextFirstTime && mustOpen()) {
			fNextFirstTime= false;
			openElement();
		}
		
		// find most down stream CompareViewerPane
		int n= 0;
		INavigatable[] navigators= new INavigatable[4];
		for (int i= 0; i < fPanes.length; i++) {
			navigators[n]= getNavigator(fPanes[i]);
			if (navigators[n] != null)
				n++;
		}
									
		while (n > 0) {
			n--;
			if (navigators[n].gotoDifference(next)) {
				// at end of this navigator
				continue;
			} else // not at end
				return false;
		}		
		return true;
	}
	
	private static INavigatable getNavigator(Object p) {
		if (p == null)
			return null;
		Control control = null;
		if (p instanceof CompareViewerSwitchingPane) {
			CompareViewerSwitchingPane pane = (CompareViewerSwitchingPane) p;
			if (pane.isEmpty())
				return null;
			Viewer viewer = pane.getViewer();
			if (viewer == null)
				return null;
			control = viewer.getControl();
			if (control == null)
				return null;
			Object data = control.getData(INavigatable.NAVIGATOR_PROPERTY);
			if (data instanceof INavigatable)
				return (INavigatable) data;
		} else if(p instanceof IAdaptable) {
			return (INavigatable)((IAdaptable)p).getAdapter(INavigatable.class);
		}	
		return null;
	}
	
	/*
	 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private boolean mustOpen() {
		if (fPanes == null || fPanes.length == 0)
			return false;
		for (int i= 1; i < fPanes.length; i++) {
			Object p= fPanes[i];
			if (p instanceof CompareViewerSwitchingPane) {
				CompareViewerSwitchingPane pane = (CompareViewerSwitchingPane) p;
				if (pane != null && pane.getInput() != null)
					return false;
			}
		}
		return true;
	}
	
	/*
	 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private void openElement() {
		if (fPanes == null || fPanes.length == 0)
			return;
		IOpenable openable= getOpenable(fPanes[0]);
		if (openable != null) {
			openable.openSelected();
		}
	}
	
	/*
	 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private static IOpenable getOpenable(Object p) {
		if (p instanceof CompareViewerSwitchingPane) {
			CompareViewerSwitchingPane pane = (CompareViewerSwitchingPane) p;
			if (pane == null)
				return null;
			if (pane.isEmpty())
				return null;
			Viewer viewer = pane.getViewer();
			if (viewer == null)
				return null;
			Control control = viewer.getControl();
			if (control == null)
				return null;
			Object data = control.getData(IOpenable.OPENABLE_PROPERTY);
			if (data instanceof IOpenable)
				return (IOpenable) data;
		}
		return null;
	}	
}
