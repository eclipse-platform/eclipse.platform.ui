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
package org.eclipse.ui.forms.parts;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

/**
 * This custom control is capable of accepting keyboard focus and be activated
 * from the keyboard (pressing 'Enter' while the focus is in the control).
 * Focus indication is rendered using platform-specific way. When control is in
 * a container with other controls capable of accepting focus, this control
 * will be part of the tab group.
 * <p>
 * The control is capable of firing selection events to the interested
 * listeners. When 'Entry' key is pressed while the control has focus, <code>SWT.DefaultSelection</code>
 * event will be fired. When the control gains or looses focus, <code>SWT.Selection</code>
 * event will be fired. Focus state is rendered using platform-specific way.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>WRAP</dd>
 * <dt><b>Events:</b></dt>
 * <dd>Selection, DefaultSelection</dd>
 * </dl>
 * @since 3.0
 */

public class TraversableLabel extends FormLabel {
	private boolean hasFocus;

	/**
	 * Constructor for SelectableFormLabel
	 */
	public TraversableLabel(Composite parent, int style) {
		super(parent, style);
		addListener(SWT.KeyDown, new Listener() {
			public void handleEvent(Event e) {
				if (e.character == '\r') {
					// Activation
					notifyListeners(SWT.DefaultSelection);
				}
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
		addListener(SWT.FocusIn, new Listener() {
			public void handleEvent(Event e) {
				if (!hasFocus) {
					hasFocus = true;
					notifyListeners(SWT.Selection);
					redraw();
				}
			}
		});
		addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(Event e) {
				if (hasFocus) {
					hasFocus = false;
					notifyListeners(SWT.Selection);
					redraw();
				}
			}
		});
		textMarginWidth = 1;
		textMarginHeight = 1;
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

	public void addSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			return;
		TypedListener typedListener = new TypedListener(listener);
		addListener(SWT.Selection, typedListener);
		addListener(SWT.DefaultSelection, typedListener);
	}
	/**
	 * Removes a listener that is notified about the event in this control.
	 * 
	 * @param listener
	 *            the selection listener
	 */
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget();
		if (listener == null)
			return;
		removeListener(SWT.Selection, listener);
		removeListener(SWT.DefaultSelection, listener);
	}

	/**
	 * Accessability support.
	 */
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
				e.detail =
					TraversableLabel.this.getSelection()
						? ACC.STATE_SELECTED
						: ACC.STATE_NORMAL;
			}
		});
	}

	private void notifyListeners(int eventType) {
		Event event = new Event();
		event.type = eventType;
		event.widget = this;
		notifyListeners(eventType, event);
	}

	/**
	 * Paints the control.
	 * 
	 * @param e
	 *            the paint event
	 */

	protected void paint(PaintEvent e) {
		super.paint(e);
		if (hasFocus) {
			GC gc = e.gc;
			Point size = getSize();
			gc.setForeground(getForeground());
			gc.drawFocus(0, 0, size.x, size.y);
		}
	}
}