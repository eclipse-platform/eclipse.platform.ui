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
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;

/**
 * This object is quite similar to an SWT Sash. However, the control is completely transparent
 * except when it is being dragged. This is useful for making the edge of some other control
 * appear draggable.
 * <p>
 * Note: this implementation currently uses a lot of dirty tricks to make the sash appear
 * transparent. Unfortunately, some of these tricks are expensive, CPU-wise... so this object
 * should be use sparingly. In particular, WidgetHider listens to every single event in the
 * SWT event loop. If there are only a couple of these in existance at any given time, this
 * shouldn't be noticable, but don't try to create a layout that contains 100s of these things.
 * FAILURE TO CALL HiddenSash.dispose() WILL RESULT IN SEVERE PERFORMANCE DEGRADATION.
 * </p>
 * 
 * @since 3.0
 */
public class HiddenSash {
	// Sash used for keyboard movement
	private Sash sash;
	private Canvas canvas;
	private WidgetHider hider;
	private Cursor dragCursor;
	private Point dragStart;
	private boolean dragging;
	private boolean vertical;
	private Rectangle initialBounds;
	private List selectionListeners = new ArrayList();
	
	private Listener mouseMoveListener = new Listener() {
		public void handleEvent(Event e) {
			e.detail |= SWT.DRAG;
			dragOccurred(e);
		}
	};
	
	/**
	 * Creates a sash with the given flags. Supports all flags that are supported by the constructor
	 * of the SWT Sash class.
	 * 
	 * @param parent parent widget
	 * @param flags Sash flags
	 */
	public HiddenSash(Composite parent, int flags) {
		sash = new Sash(parent, flags);
		canvas = new Canvas(parent, SWT.NO_BACKGROUND);
		hider = new WidgetHider(canvas);
		vertical = (flags & SWT.VERTICAL) != 0;
		
		if (vertical) {
			dragCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_SIZEWE);
		} else {
			dragCursor = new Cursor(parent.getDisplay(), SWT.CURSOR_SIZENS);
		}
		
		sash.setVisible(false);
		canvas.setVisible(false);
		canvas.setCursor(dragCursor);
		final KeyListener listener = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == SWT.ESC || e.character == '\r') {
					//currentPane.setFocus();
					sash.setVisible(false);
				}
			}
		};
		sash.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				hider.setEnabled(false);
				sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
				sash.addKeyListener(listener);
			}
			public void focusLost(FocusEvent e) {
				sash.setBackground(null);
				sash.removeKeyListener(listener);
				hider.setEnabled(true);
				sash.setVisible(false);
			}
		});
		canvas.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
				startMouseDrag(new Point(e.x, e.y));
			}

			public void mouseUp(MouseEvent e) {
				stopMouseDrag();
			}
		});
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (dragging) {
					GC gc = e.gc;
					
					Color color = canvas.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION);
					gc.setBackground(color);
					
					Rectangle rect = new Rectangle(e.x, e.y, e.width, e.height);
					
					gc.fillRectangle(rect);
				}
			}
		});
	}
	
	/**
	 * Disposes this HiddenSash. IT IS CRITICAL THAT YOU CALL THIS METHOD WHEN YOU ARE DONE
	 * WITH THE OBJECT, OR YOU WILL LEAK A GLOBAL EVENT LISTENER. This must be the last method
	 * that is invoked on the sash.
	 */
	public void dispose() {
		hider.dispose();
		canvas.dispose();
		sash.dispose();
		dragCursor.dispose();
	}
	
	/**
	 * Moves the sash above the given control
	 * 
	 * @param control control to move above, or null to move to the top
	 */
	public void moveAbove(Control control) {
		canvas.moveAbove(control);
		sash.moveAbove(canvas);
	}
	
	/**
	 * Makes the sash visible or invisible. Although the sash is always transparent,
	 * making it "invisible" will cause it to stop responding to mouse events.
	 * 
	 * @param isVisible true iff the sash should respond to mouse events
	 */
	public void setVisible(boolean isVisible) {
		hider.setEnabled(isVisible);
		if (!isVisible) {
			sash.setVisible(false);
			sash.setVisible(false);
		}
	}
	
	/**
	 * Allows the sash to be moved using the keyboard
	 */
	public void moveSash() {
		hider.setEnabled(false);
		sash.setVisible(true);
		sash.setFocus();
	}
	
	/**
	 * Adds a selection listener to this sash
	 * 
	 * @param listener listener to add
	 */
	public void addSelectionListener(SelectionAdapter listener) {
		sash.addSelectionListener(listener);
		selectionListeners.add(listener);
	}

	/**
	 * Removes a selection listener from this sash
	 * 
	 * @param listener listener to remove
	 */
	public void removeSelectionListener(SelectionAdapter listener) {
		sash.removeSelectionListener(listener);
		selectionListeners.add(listener);
	}
	
	/**
	 * Returns the current bounds of the sash
	 * 
	 * @return the current bounds of the sash
	 */
	public Rectangle getBounds() {
		return canvas.getBounds();
	}
	
	/**
	 * Sets the bounds of the sash
	 * 
	 * @param newBounds new bounds (in the parent's coordinate system)
	 */
	public void setBounds(Rectangle newBounds) {
		canvas.setBounds(newBounds);
		sash.setBounds(newBounds);
	}
	
	/**
	 * Initiates a mouse drag
	 * 
	 * @param startLoc
	 */
	private void startMouseDrag(Point startLoc) {
		hider.setEnabled(false);
		dragging = true;
		canvas.setCapture(true);
		canvas.setVisible(true);
		canvas.addListener(SWT.MouseMove, mouseMoveListener);
		dragStart = startLoc;
		initialBounds = getBounds();
		canvas.redraw();
	}
	
	/**
	 * Terminates a mouse drag
	 */
	private void stopMouseDrag() {
		dragging = false;
		hider.setEnabled(true);
		canvas.setCapture(false);
		canvas.removeListener(SWT.MouseMove, mouseMoveListener);
		canvas.setVisible(false);
	}

	/**
	 * Invoked during a mouse drag each time the mouse is moved, given the mouse move event
	 * 
	 * @param e the mouse move event
	 */
	private void dragOccurred(Event e) {	
		Rectangle currentBounds = getBounds();
		
		e.x += currentBounds.x;
		e.y += currentBounds.y;
		
		SelectionEvent selectionEvent = new SelectionEvent(e);
		
		Iterator iter = selectionListeners.iterator();
		while (iter.hasNext()) {
			SelectionAdapter next = (SelectionAdapter)iter.next();
			
			next.widgetSelected(selectionEvent);
		}
	}
}
