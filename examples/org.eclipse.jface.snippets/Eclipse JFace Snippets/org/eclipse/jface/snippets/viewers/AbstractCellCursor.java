/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 414565
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 414565
 ******************************************************************************/

package org.eclipse.jface.snippets.viewers;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @since 3.3
 *
 */
public abstract class AbstractCellCursor extends Canvas {

	private ViewerCell[] cells = new ViewerCell[0];
	private ColumnViewer viewer;
	private int activationTime = 0;
	private boolean inFocusRequest = false;

	/**
	 * @param viewer
	 * @param style
	 */
	public AbstractCellCursor(ColumnViewer viewer, int style) {
		super((Composite) viewer.getControl(), style);
		this.viewer = viewer;

		Listener listener = createListener();
		int[] eventsToListen = { SWT.Paint, SWT.KeyDown, SWT.MouseDown,
				SWT.MouseDoubleClick };

		for (int event : eventsToListen) {
			addListener(event, listener);
		}
		getParent().addListener(SWT.FocusIn, listener);
	}

	private Listener createListener() {
		return new Listener() {

			@Override
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Paint:
					paint(event);
					break;

				case SWT.KeyDown:
					getParent().notifyListeners(SWT.KeyDown, event);
					ArrayList<Object> list = new ArrayList<Object>();
					for (ViewerCell cell : cells) {
						list.add(cell.getElement());
					}
					AbstractCellCursor.this.viewer
							.setSelection(new StructuredSelection(list));
					break;

				case SWT.MouseDown:
					if (event.time < activationTime) {
						Event cEvent = copyEvent(event);
						cEvent.type = SWT.MouseDoubleClick;
						getParent().notifyListeners(SWT.MouseDoubleClick,
								cEvent);
					} else {
						getParent().notifyListeners(SWT.MouseDown,
								copyEvent(event));
					}
					break;

				case SWT.MouseDoubleClick:
					getParent().notifyListeners(SWT.MouseDoubleClick,
							copyEvent(event));
					break;

				case SWT.FocusIn:
					if (isVisible()) {
						inFocusRequest = true;
						if (!inFocusRequest) {
							forceFocus();
						}
						inFocusRequest = false;
					}

				default:
					break;
				}
			}
		};
	}

	/**
	 * @param cell
	 * @param eventTime
	 */
	public void setSelection(ViewerCell cell, int eventTime) {
		this.cells = new ViewerCell[] { cell };
		setBounds(cell.getBounds());
		forceFocus();
		redraw();
		activationTime = eventTime + getDisplay().getDoubleClickTime();
	}

	/**
	 * @return the cells who should be highlighted
	 */
	protected ViewerCell[] getSelectedCells() {
		return cells;
	}

	private Event copyEvent(Event event) {
		Event cEvent = new Event();
		cEvent.button = event.button;
		cEvent.character = event.character;
		cEvent.count = event.count;
		cEvent.data = event.data;
		cEvent.detail = event.detail;
		cEvent.display = event.display;
		cEvent.doit = event.doit;
		cEvent.end = event.end;
		cEvent.gc = event.gc;
		cEvent.height = event.height;
		cEvent.index = event.index;
		cEvent.item = getSelectedCells()[0].getControl();
		cEvent.keyCode = event.keyCode;
		cEvent.start = event.start;
		cEvent.stateMask = event.stateMask;
		cEvent.text = event.text;
		cEvent.time = event.time;
		cEvent.type = event.type;
		cEvent.widget = event.widget;
		cEvent.width = event.width;
		Point p = viewer.getControl().toControl(toDisplay(event.x, event.y));
		cEvent.x = p.x;
		cEvent.y = p.y;

		return cEvent;
	}

	/**
	 * @param event
	 */
	protected abstract void paint(Event event);
}
