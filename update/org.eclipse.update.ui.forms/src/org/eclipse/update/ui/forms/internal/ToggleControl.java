package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.events.*;
import org.eclipse.swt.SWT;

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
	private static final int [] upPoints = { 0,5, 5,0, 9,5 };
	private static final int [] downPoints = { 1,1, 5,5, 9,1 };	

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
		int y = (size.y - 5)/2;
		if (selection)
			data = translate(downPoints, x, y);
		
		else 
			data = translate(upPoints, x, y);
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
