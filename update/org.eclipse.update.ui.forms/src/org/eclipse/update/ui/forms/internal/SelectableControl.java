package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import java.text.*;

/**
 * FormText is a windowless control that
 * draws text in the provided context.
 */
public abstract class SelectableControl extends Canvas {
	private boolean hasFocus;

	public SelectableControl(Composite parent, int style) {
		super(parent, style);
		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				paint(e);
			}
		});
		addMouseListener(new MouseAdapter () {
			public void mouseDown(MouseEvent e) {
				notifyListeners(SWT.Selection);
			}
		});
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					// Activation
					notifyListeners(SWT.Selection);
				}
			}
		});
		addListener(SWT.Traverse, new Listener () {
			public void handleEvent(Event e) {
				if (e.detail != SWT.TRAVERSE_RETURN)
					e.doit = true;
			}
		});
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (!hasFocus) {
				   hasFocus=true;
				   redraw();
				}
			}
			public void focusLost(FocusEvent e) {
				if (hasFocus) {
					hasFocus=false;
					redraw();
				}
			}
		});
	}

	protected void paint(PaintEvent e) {
		GC gc = e.gc;
		Point size = getSize();
	   	gc.setFont(getFont());
	   	paint(gc);
		if (hasFocus) {
	   		gc.setForeground(getForeground());
	   		gc.drawFocus(0, 0, size.x, size.y);
		}
	}
	
	protected abstract void paint(GC gc);
	
	private void notifyListeners(int eventType) {
		Event event = new Event();
		event.type = eventType;
		event.widget = this;
		notifyListeners(eventType, event);
	}
	
	public void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection,typedListener);
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		removeListener (SWT.Selection, listener);
	}
}