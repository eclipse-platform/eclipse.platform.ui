/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.compare.internal;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import org.eclipse.jface.viewers.Viewer;

import org.eclipse.compare.CompareEditorInput;


public class CompareNavigator {
	
	private boolean fLastDirection= true;
	private CompareViewerSwitchingPane[] fPanes;
	
	public CompareNavigator(CompareViewerSwitchingPane[] panes) {
		fPanes= panes;
	}

	public void selectChange(boolean next) {
		
		fLastDirection= next;
		
		// find most down stream Pane
		int n= 0;
		INavigatable[] navigators= new INavigatable[4];
		for (int i= 0; i < fPanes.length; i++) {
			navigators[n]= getNavigator(fPanes[i]);
			if (navigators[n] != null)
				n++;
		}
									
		while (n > 0) {
			n--;
			if (!navigators[n].gotoDifference(next))
				break;
		}
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
	
	public static void hookNavigation(final Control c) {
		c.addKeyListener(
			new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					handleNavigationKeys(e);
				}
			}
		);
	}
	
	public static void handleNavigationKeys(KeyEvent e) {
		if (e.character == 14 || e.character == 16) { // next 
			if (e.widget instanceof Control) {
				CompareNavigator nav= findNavigator((Control)e.widget);
				if (nav != null)
					nav.selectChange(e.character == 14);
			}
		}
	}
	
	private static CompareNavigator findNavigator(Control c) {
		while (c != null) {
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
	
	private boolean getLastDirection() {
		boolean last= fLastDirection;
		fLastDirection= true;
		return last;
	}
	
	public static boolean getDirection(Control c) {
		CompareNavigator nav= findNavigator(c);
		if (nav != null)
			return nav.getLastDirection();
		return true;
	}
}
