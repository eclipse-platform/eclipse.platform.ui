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
package org.eclipse.ui.forms.widgets;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.internal.widgets.FormsResources;

/**
 * This is the base class for custom hyperlink widget.
 * It is responsible for processing mouse and keyboard
 * events, and converting them into unified hyperlink
 * events. Subclasses are responsible for rendering
 * the hyperlink in the client area.
 * @since 3.0
 */

public abstract class AbstractHyperlink extends Canvas {
	private boolean hasFocus;
	private Vector listeners;
	protected int marginWidth = 1;
	protected int marginHeight = 1;

	/**
	 * Constructor for SelectableFormLabel
	 */
	public AbstractHyperlink(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (e.character == '\r') {
					handleActivate();
				}
			}
		});
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		addListener(SWT.Traverse, new Listener() {
			public void handleEvent(Event e) {
				switch (e.detail) {
					case SWT.TRAVERSE_PAGE_NEXT :
					case SWT.TRAVERSE_PAGE_PREVIOUS :
					case SWT.TRAVERSE_ARROW_NEXT :
					case SWT.TRAVERSE_ARROW_PREVIOUS :
					case SWT.TRAVERSE_RETURN :
						e.doit = false;
						return;
				}
				e.doit = true;
			}
		});
		Listener listener = new Listener() {
			public void handleEvent(Event e) {
				switch (e.type) {
					case SWT.FocusIn :
						hasFocus = true;
						handleEnter();
						break;
					case SWT.FocusOut :
						hasFocus = false;
						handleExit();
						break;
					case SWT.DefaultSelection :
						handleActivate();
						break;
					case SWT.MouseEnter :
						handleEnter();
						break;
					case SWT.MouseExit :
						handleExit();
						break;
					case SWT.MouseUp :
						handleMouseUp(e);
						break;
				}
			}
		};
		addListener(SWT.MouseEnter, listener);
		addListener(SWT.MouseExit, listener);
		addListener(SWT.MouseUp, listener);
		addListener(SWT.FocusIn, listener);
		addListener(SWT.FocusOut, listener);
		setCursor(FormsResources.getHandCursor());
	}
	/**
	 * @param listener
	 */
	public void addHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			listeners = new Vector();
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	/**
	 * @param listener
	 */
	public void removeHyperlinkListener(HyperlinkListener listener) {
		if (listeners == null)
			return;
		listeners.remove(listener);
	}
	/**
	 * Returns the selection state of the control. When focus is gained, the
	 * state will be <samp>true</samp>; it will switch to <samp>false
	 * </samp> when the control looses focus.
	 * 
	 * @return <code>true</code> if the widget has focus, <code>false</code>
	 *         otherwise.
	 */
	public boolean getSelection() {
		return hasFocus;
	}

	/**
	 * Add a listener that is notified about the event in this control.
	 * 
	 * @param listener
	 *            the selection listener
	 */

	protected void handleEnter() {
		redraw();
		if (listeners == null)
			return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkEntered(e);
		}
	}

	protected void handleExit() {
		redraw();
		if (listeners == null)
			return;
		int size = listeners.size();
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkExited(e);
		}
	}

	protected void handleActivate() {
		if (listeners == null)
			return;
		int size = listeners.size();
		setCursor(FormsResources.getBusyCursor());
		HyperlinkEvent e = new HyperlinkEvent(this, getHref(), getText());
		for (int i = 0; i < size; i++) {
			HyperlinkListener listener = (HyperlinkListener) listeners.get(i);
			listener.linkActivated(e);
		}
		if (!isDisposed())
			setCursor(FormsResources.getHandCursor());
	}

	private void handleMouseUp(Event e) {
		if (e.button != 1)
			return;
		Point size = getSize();
		// Filter out mouse up events outside
		// the link. This can happen when mouse is
		// clicked, dragged outside the link, then
		// released.
		if (e.x < 0)
			return;
		if (e.y < 0)
			return;
		if (e.x >= size.x)
			return;
		if (e.y >= size.y)
			return;
		handleActivate();
	}
	
	/**
	 * @param href
	 */
	public void setHref(Object href) {
		setData("href", href);
	}
	/**
	 * @return
	 */
	public Object getHref() {
		return getData("href");
	}

	public String getText() {
		return getToolTipText();
	}

	protected abstract void paintHyperlink(PaintEvent e);

	/**
	 * Paints the control.
	 * 
	 * @param e
	 *            the paint event
	 */

	protected void paint(PaintEvent e) {
		paintHyperlink(e);
		if (hasFocus) {
			GC gc = e.gc;
			Rectangle carea = getClientArea();
			gc.setForeground(getForeground());
			gc.drawFocus(0, 0, carea.width, carea.height);
		}
	}
}