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
package org.eclipse.update.ui.forms.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class SelectableFormLabel extends FormLabel {
	private boolean hasFocus;

	public boolean getSelection() {
		return hasFocus;
	}
	
	/**
	 * Constructor for SelectableFormLabel
	 */
	public SelectableFormLabel(Composite parent, int style) {
		super(parent, style);
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.character == '\r') {
					// Activation
					notifyListeners(SWT.DefaultSelection);
				}
			}
		});
		addListener(SWT.Traverse, new Listener () {
			public void handleEvent(Event e) {
				switch (e.detail) {
					case SWT.TRAVERSE_PAGE_NEXT:
					case SWT.TRAVERSE_PAGE_PREVIOUS:
					case SWT.TRAVERSE_ARROW_NEXT:
					case SWT.TRAVERSE_ARROW_PREVIOUS:
					case SWT.TRAVERSE_RETURN:
					e.doit = false;
					return;
				}
				e.doit = true;
			}
		});
		addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				if (!hasFocus) {
				   hasFocus=true;
				   notifyListeners(SWT.Selection);
				   redraw();
				}
			}
			public void focusLost(FocusEvent e) {
				if (hasFocus) {
					hasFocus=false;
					notifyListeners(SWT.Selection);
					redraw();
				}
			}
		});
		textMarginWidth = 1;
		textMarginHeight = 1;
	}
	
	protected void initAccessible() {
		Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = getText();
			}

			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});

		accessible
			.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point pt = toControl(new Point(e.x, e.y));
				e.childID =
					(getBounds().contains(pt))
						? ACC.CHILDID_SELF
						: ACC.CHILDID_NONE;
			}

			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = getBounds();
				Point pt = toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 0;
			}

			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_PUSHBUTTON;
			}

			public void getState(AccessibleControlEvent e) {
				e.detail = SelectableFormLabel.this.getSelection()?ACC.STATE_SELECTED:ACC.STATE_NORMAL;
			}
		});
	}
	
	private void notifyListeners(int eventType) {
		Event event = new Event();
		event.type = eventType;
		event.widget = this;
		notifyListeners(eventType, event);
	}

	protected void paint(PaintEvent e) {
		super.paint(e);
	   	if (hasFocus) {
	   		GC gc = e.gc;
	   		Point size = getSize();
	   		gc.setForeground(getForeground());
	   		gc.drawFocus(0, 0, size.x, size.y);
		}
	}

	public void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection,typedListener);
		addListener (SWT.DefaultSelection,typedListener);
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		removeListener (SWT.Selection, listener);
		removeListener (SWT.DefaultSelection, listener);
	}
}