/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Copied from org.eclipse.ui.internal.dialogs;
 * TODO: Use the API class for preferences linking that is going to be available soon.
 */
public class LinkArea extends Canvas implements Listener {

	private IRunnableContext runnable;

	private String text;

	final static int MARGINWIDTH = 1;

	final static int MARGINHEIGHT = 1;

	boolean hasFocus;

	boolean mouseOver;

	// to be disposed
	private Cursor handCursor;

	private Cursor normalCursor;

	boolean linkEnabled;

	boolean foundImage;

	/**
	 * Create a new instance of the receiver.
	 * @param selectionRunnable the Runnable to run when the link is 
	 * selected
	 * @param labelText The label for the link
	 */

	public LinkArea(Composite parent, int style) {

		super(parent, SWT.NONE);

		handCursor = new Cursor(getDisplay(), SWT.CURSOR_HAND);
		normalCursor = new Cursor(getDisplay(), SWT.CURSOR_ARROW);

		addListener(SWT.Paint, this);
		addListener(SWT.MouseEnter, this);
		addListener(SWT.MouseExit, this);
		addListener(SWT.MouseDown, this);
		addListener(SWT.MouseUp, this);
		addListener(SWT.FocusIn, this);
		addListener(SWT.FocusOut, this);

		redraw();
	}

	/**
	 * Handle the event.
	 */
	public void handleEvent(Event e) {
		switch (e.type) {

		case SWT.Paint:
			paint(e.gc);
			break;
		case SWT.FocusIn:
			hasFocus = true;
		case SWT.MouseEnter:
			mouseOver = true;
			redraw();
			break;
		case SWT.FocusOut:
			hasFocus = false;
		case SWT.MouseExit:
			mouseOver = false;
			redraw();
			break;

		case SWT.MouseDown:
			runRunnable();
			break;
		case SWT.MouseUp:
			Point size = getSize();
			if (e.button != 1 || e.x < 0 || e.y < 0 || e.x >= size.x || e.y >= size.y)
				return;
			redraw();
			break;
		}
	}

	private void setLinkEnable(boolean enable) {
		if (enable != linkEnabled) {
			linkEnabled = enable;
			setCursor(handCursor);
			redraw();
		}
	}

	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		int innerWidth = wHint;
		if (innerWidth != SWT.DEFAULT)
			innerWidth -= MARGINWIDTH * 2;
		GC gc = new GC(this);
		gc.setFont(getFont());
		Point extent = gc.textExtent(text);
		gc.dispose();
		return new Point(extent.x + 2 * MARGINWIDTH, extent.y + 2 * MARGINHEIGHT);
	}

	private Color getFGColor() {
		if (mouseOver)
			return JFaceColors.getActiveHyperlinkText(getDisplay());
		return JFaceColors.getHyperlinkText(getDisplay());
	}

	protected void paint(GC gc) {
		Rectangle clientArea = getClientArea();
		if (clientArea.isEmpty())
			return;
		Color fg = getFGColor();

		Image buffer = null;
		GC bufferGC = gc;

		bufferGC.setForeground(fg);
		bufferGC.fillRectangle(0, 0, clientArea.width, clientArea.height);
		bufferGC.setFont(getFont());

		bufferGC.drawText(text, MARGINWIDTH, MARGINHEIGHT, true);
		int sw = bufferGC.stringExtent(text).x;
		FontMetrics fm = bufferGC.getFontMetrics();
		int lineY = clientArea.height - MARGINHEIGHT - fm.getDescent() + 1;
		bufferGC.drawLine(MARGINWIDTH, lineY, MARGINWIDTH + sw, lineY);
		if (hasFocus)
			bufferGC.drawFocus(0, 0, sw, clientArea.height);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		super.dispose();
		handCursor.dispose();
		normalCursor.dispose();
	}

	/**
	 * Run the runnable for the receiver.
	 */
	private void runRunnable() {
		try {
			runnable.run(false, false, null);
		} catch (InvocationTargetException exception) {
			WorkbenchPlugin.log("Error in hyperlink", exception); //$NON-NLS-1$
		} catch (InterruptedException exception) {
			//Do not worry about interruptions
		}
	}
	/**
	 * Set the runnable to run when the link is selected.
	 * @param runnable
	 */
	public void setRunnable(IRunnableContext runnable) {
		this.runnable = runnable;
	}
	/**
	 * Set the text for the link.
	 * @param text The text to set.
	 */
	public void setText(String text) {
		this.text = text;
	}
}
