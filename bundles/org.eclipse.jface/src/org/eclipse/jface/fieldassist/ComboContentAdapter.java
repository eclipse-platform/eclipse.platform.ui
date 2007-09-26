/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.fieldassist;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

/**
 * An {@link IControlContentAdapter} for SWT Combo controls. This is a
 * convenience class for easily creating a {@link ContentProposalAdapter} for
 * combo fields.
 * 
 * @since 3.2
 */
public class ComboContentAdapter implements IControlContentAdapter,
		IControlContentAdapter2 {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.taskassistance.IControlContentAdapter#getControlContents(org.eclipse.swt.widgets.Control)
	 */
	public String getControlContents(Control control) {
		return ((Combo) control).getText();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setControlContents(org.eclipse.swt.widgets.Control,
	 *      java.lang.String, int)
	 */
	public void setControlContents(Control control, String text,
			int cursorPosition) {
		((Combo) control).setText(text);
		((Combo) control)
				.setSelection(new Point(cursorPosition, cursorPosition));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#insertControlContents(org.eclipse.swt.widgets.Control,
	 *      java.lang.String, int)
	 */
	public void insertControlContents(Control control, String text,
			int cursorPosition) {
		Combo combo = (Combo) control;
		String contents = combo.getText();
		Point selection = combo.getSelection();
		StringBuffer sb = new StringBuffer();
		sb.append(contents.substring(0, selection.x));
		sb.append(text);
		if (selection.y < contents.length()) {
			sb.append(contents.substring(selection.y, contents.length()));
		}
		combo.setText(sb.toString());
		selection.x = selection.x + cursorPosition;
		selection.y = selection.x;
		combo.setSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getCursorPosition(org.eclipse.swt.widgets.Control)
	 */
	public int getCursorPosition(Control control) {
		return ((Combo) control).getSelection().x;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#getInsertionBounds(org.eclipse.swt.widgets.Control)
	 */
	public Rectangle getInsertionBounds(Control control) {
		Combo combo = (Combo) control;
		int position = combo.getSelection().y;
		String contents = combo.getText();
		GC gc = new GC(combo);
		gc.setFont(combo.getFont());
		Point extent = gc.textExtent(contents.substring(0, Math.min(position,
				contents.length())));
		gc.dispose();
		return new Rectangle(combo.getClientArea().x + extent.x, combo
				.getClientArea().y, 1, combo.getClientArea().height);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter#setCursorPosition(org.eclipse.swt.widgets.Control,
	 *      int)
	 */
	public void setCursorPosition(Control control, int index) {
		((Combo) control).setSelection(new Point(index, index));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#getSelection(org.eclipse.swt.widgets.Control)
	 * 
	 * @since 3.4
	 */
	public Point getSelection(Control control) {
		return ((Combo) control).getSelection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.fieldassist.IControlContentAdapter2#setSelection(org.eclipse.swt.widgets.Control,
	 *      org.eclipse.swt.graphics.Point)
	 * 
	 * @since 3.4
	 */
	public void setSelection(Control control, Point range) {
		((Combo) control).setSelection(range);
	}

}
