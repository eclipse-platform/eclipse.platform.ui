/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.progress;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.viewers.LabelProvider;
/**
 * The ProgressViewerLabelProvider is the label provider for
 * progress viewers.
 */
public class ProgressViewerLabelProvider extends LabelProvider {
	private Control control;
	private String ellipsis = ProgressMessages.getString("ProgressFloatingWindow.EllipsisValue"); //$NON-NLS-1$
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object element) {
		JobTreeElement info = (JobTreeElement) element;
		return shortenText(info.getCondensedDisplayString());
	}
	/**
	 * Shorten the given text <code>t</code> so that its length
	 * doesn't exceed the given width. The default implementation
	 * replaces characters in the center of the original string with an
	 * ellipsis ("..."). Override if you need a different strategy.
	 */
	protected String shortenText(String textValue) {
		if (textValue == null)
			return null;
		Display display = control.getDisplay();
		GC gc = new GC(display);
		int maxWidth = control.getBounds().width;
		if (gc.textExtent(textValue).x < maxWidth) {
			gc.dispose();
			return textValue;
		}
		int length = textValue.length();
		int ellipsisWidth = gc.textExtent(ellipsis).x;
		int pivot = length / 2;
		int start = pivot;
		int end = pivot + 1;
		while (start >= 0 && end < length) {
			String s1 = textValue.substring(0, start);
			String s2 = textValue.substring(end, length);
			int l1 = gc.textExtent(s1).x;
			int l2 = gc.textExtent(s2).x;
			if (l1 + ellipsisWidth + l2 < maxWidth) {
				gc.dispose();
				return s1 + ellipsis + s2;
			}
			start--;
			end++;
		}
		gc.dispose();
		return textValue;
	}
	/**
	 * Create a new instance of the receiver within the 
	 * control.
	 * @param progressViewer
	 */
	public ProgressViewerLabelProvider(Control progressControl) {
		super();
		control = progressControl;
	}
}
