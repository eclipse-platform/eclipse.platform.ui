package org.eclipse.jface.text.contentassist;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */


import org.eclipse.swt.widgets.Widget;


/**
 * Helper class for testing widget state.
 */
class Helper {
	
	/**
	 * Returns whether the widget is <code>null</code> or disposed.
	 *
	 * @param widget the widget to check
	 * @return <code>true</code> if the widget is neither <code>null</code> nor disposed
	 */
	public static boolean okToUse(Widget widget) {
		return (widget != null && !widget.isDisposed());
	}
}
