package org.eclipse.ui.internal.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Utility class that wraps a given control with a black 'border'. Moving the
 * border control will cause the given control to move to stay within its bounds.
 *  
 * @since 3.2
 *
 */
public class DragBorder {
	private Composite clientControl = null;
	private Control dragControl = null;
	private Canvas border = null;

	/**
	 * Construct a new DragBorder.
	 * 
	 * @param client The client window that the border must stay within
	 * @param toDrag The control to be placed 'inside' the border
	 */
	public DragBorder(Composite client, Control toDrag) {
		clientControl = client;
		dragControl = toDrag;
		Point dragSize = toDrag.getSize();
		
		// Create a control large enough to 'contain' the dragged control
		border = new Canvas(dragControl.getParent(), SWT.NONE);
		border.setSize(dragSize.x+2, dragSize.y+2);
		
		// Ensure the border is visible and the control is 'above' it...
		border.moveAbove(null);
		dragControl.moveAbove(null);
		
		border.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				Color black = border.getDisplay().getSystemColor(SWT.COLOR_BLACK);
				e.gc.setBackground(black);
				e.gc.setBackground(black);
				Rectangle bb = border.getBounds();
				e.gc.drawRectangle(0,0,bb.width-1, bb.height-1);
			}
		});
	}
	
    
    /**
     * Move the border (and its 'contained' control to a new position. The new
     * position will be adjusted to lie entirely within the client area of the
     * <code>clientControl</code>.
     * 
     * @param newPos The new position for the border
     * @param centered <code>true</code> iff the border should be centered on the point
     */
    public void setLocation(Point newPos, boolean centered) {
		// Move the border but ensure that it is still inside the Client area
    	if (centered) {
    		Point size = border.getSize();
    		border.setLocation(newPos.x - (size.x/2), newPos.y - (size.y/2));
    	}
    	else
    		border.setLocation(newPos.x, newPos.y);
    	
		Rectangle bb = border.getBounds();
		Rectangle cr = clientControl.getClientArea();
		Geometry.moveInside(bb,cr);
		
		// OK, now move the drag control and the border to their new locations
		dragControl.setLocation(bb.x+1, bb.y+1);
		border.setBounds(bb);
    }


	/**
	 * Dispose the controls owned by the border.
	 */
	public void dispose() {
		border.dispose();
	}


	/**
	 * @return The bounds of the border's control.
	 */
	public Rectangle getBounds() {
		return border.getBounds();
	}
}
