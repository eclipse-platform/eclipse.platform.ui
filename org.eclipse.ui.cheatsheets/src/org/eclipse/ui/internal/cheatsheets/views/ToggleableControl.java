/*******************************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.cheatsheets.views;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;

public class ToggleableControl extends Canvas {
	protected boolean selection;
	protected Color decorationColor;
	protected Color activeColor;
	protected Cursor activeCursor;
	protected boolean hasFocus;
	protected boolean hover=false;
	protected static final int marginWidth = 2;
	protected static final int marginHeight = 2;
	protected static final int WIDTH = 9;
	protected static final int HEIGHT = 9;
	protected static final int [] offPoints = { 0,2, 8,2, 4,6 };
	protected static final int [] onPoints = { 2, -1, 2,8, 6,4 };	

	/* accessibility */
	private String fName;
	private String fDescription;

	public ToggleableControl(Composite parent, int style) {
		super(parent, style);
		initAccessible();
		
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
				if (e.character == '\r' || e.character == ' ') {
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
		
		addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				selection = !selection;
				redraw();
			}
		});

		addMouseTrackListener(new MouseTrackAdapter() {
			public void mouseEnter(MouseEvent e) {
				hover = true;
				if (activeCursor!=null)
				   setCursor(activeCursor);
				redraw();
			}
			public void mouseExit(MouseEvent e) {
				hover = false;
				if (activeCursor!=null)
				   setCursor(null);
				redraw();
			}
		});
	}
	
	private void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection,typedListener);
	}
	
	public void setDecorationColor(Color decorationColor) {
		this.decorationColor = decorationColor;
	}
	
	public Color getDecorationColor() {
		return decorationColor;
	}
	
	public void setActiveDecorationColor(Color activeColor) {
		this.activeColor = activeColor;
	}
	
	public void removeSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		removeListener (SWT.Selection, listener);
	}
	
	public void setActiveCursor(Cursor activeCursor) {
		this.activeCursor = activeCursor;
	}
	
	public Color getActiveDecorationColor() {
		return activeColor;
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		int width, height;
		
		if (wHint!=SWT.DEFAULT) width = wHint; 
		else 
		   width = WIDTH + 2*marginWidth;
		if (hHint!=SWT.DEFAULT) height = hHint;
		else height = HEIGHT + 2*marginHeight;
		return new Point(width, height);
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

	/*
	 * @see SelectableControl#paint(GC)
	 */
	protected void paint(GC gc) {
		if (hover && activeColor!=null)
			gc.setBackground(activeColor);
		else if (decorationColor!=null)
	   	   gc.setBackground(decorationColor);
	   	else
	   			gc.setBackground(getForeground());
		int [] data;
		Point size = getSize();
		int x = (size.x - 9)/2;
		int y = (size.y - 5)/2;
		if (selection)
			//data = translate(downPoints, x, y);
			data = translate(onPoints, x, y);
		
		else 
			//data = translate(upPoints, x, y);
			data = translate(offPoints, x, y);
		gc.fillPolygon(data);
		gc.setBackground(getBackground());
	}
	
	private void notifyListeners(int eventType) {
		Event event = new Event();
		event.type = eventType;
		event.widget = this;
		notifyListeners(eventType, event);
	}
	
	protected int [] translate(int [] data, int x, int y) {
		int [] target = new int [data.length];
		for (int i=0; i<data.length; i+=2) {
			target[i] = data[i]+ x;
		}
		for (int i=1; i<data.length; i+=2) {
			target[i] = data[i]+y;
		}
		return target;
	}

	public boolean getSelection() {
		return selection;
	}
	
	public void setSelection(boolean selection) {
		this.selection = selection;
	}

	public void setName(String name) {
		fName = name;
	}

	public void setDescription(String description) {
		fDescription = description;
	}

	private void initAccessible() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {

			public void getName(AccessibleEvent e) {
				e.result = fName;
			}

			public void getDescription(AccessibleEvent e) {
				e.result = fDescription;
			}
		});

		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {

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
				e.detail = ACC.ROLE_CHECKBUTTON;
			}

			public void getState(AccessibleControlEvent e) {
				e.detail = selection ? ACC.STATE_SELECTED : ACC.STATE_SELECTABLE;
			}

		});
	}
}
