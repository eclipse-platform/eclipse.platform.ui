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
import org.eclipse.swt.widgets.Composite;

public class ToggleControl extends SelectableControl {
	private boolean selection;
	private Color decorationColor;
	private Color activeColor;
	private Cursor activeCursor;
	private boolean hover=false;
	private static final int marginWidth = 2;
	private static final int marginHeight = 2;
	private static final int WIDTH = 9;
	private static final int HEIGHT = 9;
	private static final int [] offPoints = { 0,2, 8,2, 4,6 };
	private static final int [] onPoints = { 2, -1, 2,8, 6,4 };	

	public ToggleControl(Composite parent, int style) {
		super(parent, style);
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
		initAccessible();
	}
	void initAccessible() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});
		
		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getChildAtPoint(AccessibleControlEvent e) {
				Point testPoint = toControl(new Point(e.x, e.y));
				if (getBounds().contains(testPoint)) {
					e.childID = ACC.CHILDID_SELF;
				}
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
				e.detail = ACC.ROLE_TREE;
			}
		
			public void getState(AccessibleControlEvent e) {
				e.detail = ToggleControl.this.getSelection()?ACC.STATE_EXPANDED:ACC.STATE_COLLAPSED;
			}

			public void getValue(AccessibleControlEvent e) {
				e.result = ToggleControl.this.getSelection()?"1":"0";
			}
		});
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
		int y = (size.y - 9)/2;
		if (selection)
			data = translate(onPoints, x, y);
		
		else 
			data = translate(offPoints, x, y);
		gc.fillPolygon(data);
		gc.setBackground(getBackground());
	}
	
	private int [] translate(int [] data, int x, int y) {
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
}
