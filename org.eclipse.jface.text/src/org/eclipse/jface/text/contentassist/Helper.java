package org.eclipse.jface.text.contentassist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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