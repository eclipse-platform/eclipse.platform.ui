/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.*;

/**
 * Supports cross-pane navigation through differences.
 * XXX: Design is as it is because the feature had to be added without touching API.
 */
public class CompareNavigator implements ICompareNavigator {
	
	private boolean fLastDirection= true;
	private CompareViewerSwitchingPane[] fPanes;
	// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	private boolean fNextFirstTime= true;
	
	public CompareNavigator(CompareViewerSwitchingPane[] panes) {
		fPanes= panes;
	}

	public CompareViewerSwitchingPane[] getPanes() {
		return fPanes;
	}
	
	public boolean selectChange(boolean next) {
		
		fLastDirection= next;

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
			}
			// not at end
			return false;
		}
		
		return true;
	}
	
	private static INavigatable getNavigator(CompareViewerSwitchingPane pane) {
		if (pane == null)
			return null;
		if (pane.isEmpty())
			return null;
		Viewer viewer= pane.getViewer();
		if (viewer == null)
			return null;
		Control control= viewer.getControl();
		if (control == null)
			return null;
		Object data= control.getData(INavigatable.NAVIGATOR_PROPERTY);
		if (data instanceof INavigatable)
			return (INavigatable) data;
		return null;
	}
	
	private static CompareNavigator findNavigator(Control c) {
		while (c != null && !c.isDisposed()) {	// PR 1GEUVV2
			Object data= c.getData();
			if (data instanceof CompareEditorInput) {
				CompareEditorInput cei= (CompareEditorInput) data;
				Object adapter= cei.getAdapter(CompareNavigator.class);
				if (adapter instanceof CompareNavigator)
					return (CompareNavigator)adapter;
			}
			c= c.getParent();
		}
		return null;
	}
	
	private boolean resetDirection() {
		boolean last= fLastDirection;
		fLastDirection= true;
		return last;
	}
	
	public static boolean getDirection(Control c) {
		CompareNavigator nav= findNavigator(c);
		if (nav != null)
			return nav.resetDirection();
		return true;
	}
	
	/*
	 * Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=20106
	 */
	private boolean mustOpen() {
		if (fPanes == null || fPanes.length == 0)
			return false;
		for (int i= 1; i < fPanes.length; i++) {
			CompareViewerSwitchingPane pane= fPanes[i];
			if (pane != null && pane.getInput() != null)
				return false;
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
	private static IOpenable getOpenable(CompareViewerSwitchingPane pane) {
		if (pane == null)
			return null;
		if (pane.isEmpty())
			return null;
		Viewer viewer= pane.getViewer();
		if (viewer == null)
			return null;
		Control control= viewer.getControl();
		if (control == null)
			return null;
		Object data= control.getData(IOpenable.OPENABLE_PROPERTY);
		if (data instanceof IOpenable)
			return (IOpenable) data;
		return null;
	}	
}
