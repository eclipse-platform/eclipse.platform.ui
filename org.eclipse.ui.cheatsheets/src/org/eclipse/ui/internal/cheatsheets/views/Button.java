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

public class Button extends Canvas {
	protected static final int MARGIN_HEIGHT = 2;
	protected static final int MARGIN_WIDTH = 2;
	protected String fAccessibleDescription;
	protected String fAccessibleName;
	protected boolean fHasFocus;
	protected Image fImage;
	protected Rectangle fImageSize;
	protected boolean fSelection;
	protected int fToggleImageHeight;
	protected int fToggleImageWidth;

	public Button(Composite parent, int style, Image newImage) {
		super(parent, style);
		initAccessible();

		fImage = newImage;
		
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
				if (!fHasFocus) {
				   fHasFocus=true;
				   redraw();
				}
			}
			public void focusLost(FocusEvent e) {
				if (fHasFocus) {
					fHasFocus=false;
					redraw();
				}
			}
		});
		
		addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fSelection = !fSelection;
				redraw();
			}
		});

//		addMouseTrackListener(new MouseTrackAdapter() {
//			public void mouseEnter(MouseEvent e) {
//				hover = true;
//				if (activeCursor!=null)
//				   setCursor(activeCursor);
//				redraw();
//			}
//			public void mouseExit(MouseEvent e) {
//				hover = false;
//				if (activeCursor!=null)
//				   setCursor(null);
//				redraw();
//			}
//		});

		calculateDimensions();
	}
	
	/*package*/ void addSelectionListener(SelectionListener listener) {
		checkWidget ();
		if (listener == null) return;
		TypedListener typedListener = new TypedListener (listener);
		addListener (SWT.Selection,typedListener);
	}

	private void calculateDimensions() {
		fImageSize = fImage.getBounds();
		fToggleImageWidth = fImageSize.width + MARGIN_WIDTH*2;
		fToggleImageHeight = fImageSize.height + MARGIN_HEIGHT*2;
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(fToggleImageWidth, fToggleImageWidth);
	}
	/**
	 * Returns the fAccessibleDescription.
	 * @return String
	 */
	public String getFAccessibleDescription() {
		return fAccessibleDescription;
	}

	/**
	 * Returns the fAccessibleName.
	 * @return String
	 */
	public String getFAccessibleName() {
		return fAccessibleName;
	}
	
	public Image getImage(){
		return fImage;	
	}

	public boolean getSelection() {
		return fSelection;
	}

	private void initAccessible() {
		getAccessible().addAccessibleListener(new AccessibleAdapter() {

			public void getDescription(AccessibleEvent e) {
				if(fAccessibleDescription!=null)
					e.result = fAccessibleDescription;
			}

			public void getName(AccessibleEvent e) {
				if(fAccessibleName != null)
					e.result = fAccessibleName;
			}
		});

		getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {

			public void getChildCount(AccessibleControlEvent e) {
				e.detail = 0;
			}

			public void getLocation(AccessibleControlEvent e) {
				Rectangle location = getBounds();
				Point pt = toDisplay(new Point(location.x, location.y));
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_PUSHBUTTON;
			}

			public void getState(AccessibleControlEvent e) {
//				e.detail = fSelection ? ACC.STATE_SELECTED : ACC.STATE_SELECTABLE;
			}

		});
	}
	
	protected void notifyListeners(int eventType) {
		Event event = new Event();
		event.type = eventType;
		event.widget = this;
		notifyListeners(eventType, event);
	}

	/*
	 * @see SelectableControl#paint(GC)
	 */
	protected void paint(GC gc) {
				
//		if (hover && activeColor!=null)
//			gc.setBackground(activeColor);
//		else if (decorationColor!=null)
//			gc.setBackground(decorationColor);
//	   	else
//			gc.setBackground(getForeground());

		// Find point to center image		
		Point size = getSize();
		int x = (size.x - fImageSize.width)/2;
		int y = (size.y - fImageSize.height)/2;

		gc.drawImage(fImage, 0, 0, fImageSize.width, fImageSize.height, x, y, fImageSize.width, fImageSize.height);
//		gc.setBackground(getBackground());
	}
	
//	public void setDecorationColor(Color decorationColor) {
//		this.decorationColor = decorationColor;
//	}
//	
//	public Color getDecorationColor() {
//		return decorationColor;
//	}
//	
//	public void setActiveDecorationColor(Color activeColor) {
//		this.activeColor = activeColor;
//	}
//	
//	public void removeSelectionListener(SelectionListener listener) {
//		checkWidget ();
//		if (listener == null) return;
//		removeListener (SWT.Selection, listener);
//	}
//	
//	public void setActiveCursor(Cursor activeCursor) {
//		this.activeCursor = activeCursor;
//	}
//	
//	public Color getActiveDecorationColor() {
//		return activeColor;
//	}
	
//	public Point computeSize(int wHint, int hHint, boolean changed) {
//		int width, height;
//		
//		if (wHint!=SWT.DEFAULT) width = wHint; 
//		else 
//		   width = WIDTH + 2*marginWidth;
//		if (hHint!=SWT.DEFAULT) height = hHint;
//		else height = HEIGHT + 2*marginHeight;
//		return new Point(width, height);
//	}
	
	protected void paint(PaintEvent e) {
		GC gc = e.gc;
		Point size = getSize();
	   	gc.setFont(getFont());
	   	paint(gc);
		if (fHasFocus) {
	   		gc.setForeground(getForeground());
	   		gc.drawFocus(0, 0, size.x, size.y);
		}
	}

	/**
	 * Sets the fAccessibleDescription.
	 * @param fAccessibleDescription The fAccessibleDescription to set
	 */
	public void setFAccessibleDescription(String fAccessibleDescription) {
		this.fAccessibleDescription = fAccessibleDescription;
	}

	/**
	 * Sets the fAccessibleName.
	 * @param fAccessibleName The fAccessibleName to set
	 */
	public void setFAccessibleName(String fAccessibleName) {
		this.fAccessibleName = fAccessibleName;
	}

	public void setImage(Image myimage){
		if( fImage != myimage ) {
			fImage = myimage;
			calculateDimensions();
		}
	}	
	
	public void setSelection(boolean selection) {
		this.fSelection = selection;
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

}
